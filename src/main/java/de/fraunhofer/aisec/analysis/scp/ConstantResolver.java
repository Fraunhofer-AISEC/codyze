
package de.fraunhofer.aisec.analysis.scp;

import de.fraunhofer.aisec.analysis.structures.ConstantValue;
import de.fraunhofer.aisec.analysis.utils.Utils;
import de.fraunhofer.aisec.cpg.graph.*;
import de.fraunhofer.aisec.crymlin.CrymlinQueryWrapper;
import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversal;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.in;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.is;

/**
 * Resolves constant values of variables, if possible.
 */
public class ConstantResolver {
	private static final Logger log = LoggerFactory.getLogger(ConstantResolver.class);
	private final TraversalConnection.Type dbType;

	public ConstantResolver(TraversalConnection.Type dbType) {
		this.dbType = dbType;
	}

	/**
	 * Given a VariableDeclaration, this method attempts to resolve its constant value.
	 * <p>
	 * Approach:
	 * <p>
	 * 1. from CPG vertex representing the function argument ('crymlin.byID((long) v.id()).out("ARGUMENTS").has("argumentIndex", argumentIndex)') create all paths to
	 * vertex with variable declaration ('variableDeclarationVertex') in theory 'crymlin.byID((long) v.id()).repeat(in("EOG").simplePath())
	 * .until(hasId(variableDeclarationVertex.id())).path()'
	 * <p>
	 * 2. traverse this path from 'v' ---> 'variableDeclarationVertex'
	 * <p>
	 * 3. for each assignment, i.e. BinaryOperator{operatorCode: "="}
	 * <p>
	 * 4. check if -{"LHS"}-> v -{"REFERS_TO"}-> variableDeclarationVertex
	 * <p>
	 * 5. then determine value RHS
	 * <p>
	 * 6. done
	 * <p>
	 * 7. {no interjacent assignment} determine value of variableDeclarationVertex (e.g. from its initializer)
	 * <p>
	 * 8. {no initializer with value e.g. function argument} continue traversing the graph
	 *
	 * @param declRefExpr The DeclaredReferenceExpression that will be resolved.
	 */
	public Set<ConstantValue> resolveConstantValues(@NonNull DeclaredReferenceExpression declRefExpr) {
		Set<ConstantValue> result = new HashSet<>();
		Set<ValueDeclaration> declarations = CrymlinQueryWrapper.getDeclarationSites(declRefExpr);

		try (TraversalConnection conn = new TraversalConnection(this.dbType)) {
			CrymlinTraversalSource t = conn.getCrymlinTraversal();
			for (ValueDeclaration decl : declarations) {
				Vertex vDecl = t.byID(decl.getId()).tryNext().orElseThrow();
				Vertex vExpr = t.byID(declRefExpr.getId())
						.tryNext()
						.orElseThrow();
				Optional<ConstantValue> val = resolveConstantValueOfFunctionArgument(vDecl, vExpr);
				if (val.isPresent()) {
					result.add(val.get());
				}
			}
		}

		return result;
	}

	private Optional<ConstantValue> resolveConstantValueOfFunctionArgument(@Nullable Vertex variableDeclarationVertex, @NonNull Vertex vDeclaredReferenceExpr) {
		if (variableDeclarationVertex == null) {
			return Optional.empty();
		}

		Optional<ConstantValue> retVal = Optional.empty();

		try (TraversalConnection conn = new TraversalConnection(this.dbType)) {
			CrymlinTraversalSource crymlin = conn.getCrymlinTraversal();
			log.debug("Vertex for function call: {}", vDeclaredReferenceExpr.property("code").value());
			log.debug("Vertex of variable declaration: {}", variableDeclarationVertex.property("code").value());

			// traverse in reverse along EOG edges from v until variableDeclarationVertex -->
			// one of them must have more information on the value of the operand
			// this does not work, as a loop in the eog will result in an endless traversal here.
			//			CrymlinTraversal<Vertex, Vertex> traversal = crymlin.byID((long) vDeclaredReferenceExpr.id())
			//					.repeat(in("EOG"))
			//					.until(
			//						is(variableDeclarationVertex))
			//					.dedup();

			HashSet<Vertex> workList = new HashSet<>();
			HashSet<Vertex> seen = new HashSet<>();

			workList.add(vDeclaredReferenceExpr);

			while (!workList.isEmpty()) {
				HashSet<Vertex> nextWorklist = new HashSet<>();

				for (Vertex tVertex : workList) {
					if (seen.contains(tVertex)) {
						continue;
					}
					seen.add(tVertex);

					log.debug("Check if is assignment to {}: {}", variableDeclarationVertex.property("code").value(), tVertex.property("code").value());

					boolean isBinaryOperatorVertex = tVertex.label().contains(BinaryOperator.class.getSimpleName());

					Iterator<Vertex> lhsVertices = tVertex.vertices(Direction.OUT, "LHS");

					if (isBinaryOperatorVertex
							&& "=".equals(tVertex.property("operatorCode").value())
							&& lhsVertices.hasNext()) {
						// this is an assignment that may set the value of our operand
						Vertex lhs = lhsVertices.next();

						Iterator<Vertex> assignees = lhs.vertices(Direction.OUT, "REFERS_TO");
						if (assignees.hasNext() && assignees
								.next()
								.id()
								.equals(variableDeclarationVertex.id())) {
							log.debug("   LHS of this node is interesting. Will evaluate RHS: {}", tVertex.property("code").value());
							Vertex rhs = tVertex.vertices(Direction.OUT, "RHS")
									.next();

							boolean isRhsLiteral = rhs.label().equals(Literal.class.getSimpleName());
							boolean isRhsExpressionList = rhs.label().equals(ExpressionList.class.getSimpleName());

							if (isRhsLiteral) {
								Object literalValue = rhs.property("value").value();

								Optional<ConstantValue> constantValue = ConstantValue.tryOf(literalValue);
								if (constantValue.isPresent()) {
									return constantValue;
								}

								log.warn("Unknown literal type encountered: {} (value: {})", literalValue.getClass(), literalValue);
							} else if (isRhsExpressionList
									&& rhs.edges(Direction.IN, "EOG").hasNext()) {
								// C/C++ assigns last expression in list.
								Vertex lastExpressionInList = rhs.edges(Direction.IN, "EOG")
										.next()
										.outVertex();

								if (Utils.hasLabel(lastExpressionInList, Literal.class)) {
									// If last expression is Literal --> assign its value immediately.
									Object literalValue = lastExpressionInList.property("value").value();
									Optional<ConstantValue> constantValue = ConstantValue.tryOf(literalValue);
									if (constantValue.isPresent()) {
										return constantValue;
									}
									log.warn("Unknown literal type encountered: {} (value: {})", literalValue.getClass(), literalValue);
								} else if (lastExpressionInList.label().equals(DeclaredReferenceExpression.class.getSimpleName())) {
									// Get declaration of the variable used as last item in expression list
									Iterator<Edge> refersTo = lastExpressionInList.edges(Direction.IN, "DFG");
									if (refersTo.hasNext()) {
										Vertex v = refersTo.next().outVertex();
										if (v.label().equals(VariableDeclaration.class.getSimpleName())) {
											ConstantResolver resolver = new ConstantResolver(this.dbType);
											Optional<ConstantValue> constantValue = resolver.resolveConstantValueOfFunctionArgument(v, lastExpressionInList);
											if (constantValue.isPresent()) {
												return constantValue;
											}
										} else {
											log.warn("Last expression in ExpressionList does not have a VariableDeclaration. Cannot resolve its value: {}",
												lastExpressionInList.property("code").value());
										}
									} else {
										log.warn("Last expression in ExpressionList has no incoming DFG. Cannot resolve its value: {}",
											lastExpressionInList.property("code").value());
									}
								}
							}
							log.error("Value of operand set in assignment expression");
							return Optional.empty();
						}
					}
					if (tVertex != variableDeclarationVertex) { // stop once we are at the declaration
						Iterator<Edge> eog = tVertex.edges(Direction.IN, "EOG");
						while (eog.hasNext()) {
							Vertex vertex = eog.next().outVertex();
							if (!seen.contains(vertex)) {
								nextWorklist.add(vertex);
							}
						}
					}
				}
				workList = nextWorklist;
			}

			// we arrived at the declaration of the variable
			//log.info("Checking declaration for a literal initializer");

			// See if we have an initializer
			Iterator<Vertex> itInitializerVertex = variableDeclarationVertex.vertices(Direction.OUT, "INITIALIZER");

			if (itInitializerVertex.hasNext()) {
				Vertex initializerVertex = itInitializerVertex.next();

				if (Utils.hasLabel(initializerVertex, Literal.class)) {
					Object literalValue = initializerVertex.property("value").value();
					retVal = ConstantValue.tryOf(literalValue);

				} else if (Utils.hasLabel(initializerVertex, ConstructExpression.class)) {
					Iterator<Vertex> initializers = initializerVertex.vertices(Direction.OUT, "ARGUMENTS");
					if (initializers.hasNext()) {
						Vertex init = initializers.next();
						if (Utils.hasLabel(init, Literal.class)) {
							Object initValue = init.property("value").value();
							retVal = ConstantValue.tryOf(initValue);
						} else {
							log.warn("Cannot evaluate ConstructExpression, it is a {}", init.label());
						}
					} else {
						log.warn("No Argument to ConstructExpression");
					}
					if (initializers.hasNext()) {
						log.warn("More than one Arguments to ConstructExpression found, not using one of them.");
						retVal = Optional.empty();
					}

				} else if (Utils.hasLabel(initializerVertex, InitializerListExpression.class)) {
					Iterator<Vertex> initializers = initializerVertex.vertices(Direction.OUT, "INITIALIZERS");
					if (initializers.hasNext()) {
						Vertex init = initializers.next();
						if (Utils.hasLabel(init, Literal.class)) {
							Object initValue = init.property("value").value();
							retVal = ConstantValue.tryOf(initValue);

						} else {
							log.warn("Cannot evaluate initializer, it is a {}", init.label());
						}
					} else {
						log.warn("No initializer found");
					}
					if (initializers.hasNext()) {
						log.warn("More than one initializer found, using none of them");
						retVal = Optional.empty();
					}

				} else if (Utils.hasLabel(initializerVertex, ExpressionList.class)) {
					Iterator<Vertex> initializers = initializerVertex.vertices(Direction.OUT, "SUBEXPR");
					Vertex init = null;
					while (initializers.hasNext()) { // get the last initializer according to C++17 standard
						init = initializers.next();
					}
					if (init != null) {
						if (Utils.hasLabel(init, Literal.class)) {
							Object initValue = init.property("value").value();
							retVal = ConstantValue.tryOf(initValue);

						} else {
							log.warn("Cannot evaluate initializer, it is a {}", init.label());
						}
					} else {
						log.warn("No initializer found");
					}
					if (initializers.hasNext()) {
						log.warn("More than one initializer found, using none of them");
						retVal = Optional.empty();
					}
				} else {
					log.warn("Unknown Initializer: {}", initializerVertex.label());
				}

			}

			if (itInitializerVertex.hasNext()) {
				log.warn("More than one initializer found, using none of them");
				retVal = Optional.empty();
			}
		}

		return retVal;
	}

}
