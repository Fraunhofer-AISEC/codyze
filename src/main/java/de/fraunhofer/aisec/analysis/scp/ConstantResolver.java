
package de.fraunhofer.aisec.analysis.scp;

import de.fraunhofer.aisec.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.analysis.structures.ConstantValue;
import de.fraunhofer.aisec.cpg.graph.*;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversal;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.cdt.internal.core.model.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.*;

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
	 * @param variableDeclaration
	 */ // TODO Should be replaced by a more generic function that takes a single DeclaredReferenceExpression as an argument
	public Optional<ConstantValue> resolveConstantValueOfFunctionArgument(@Nullable Declaration variableDeclaration, @NonNull Vertex callExpressionVertex) {
		if (variableDeclaration == null) {
			return Optional.empty();
		}

		Optional<ConstantValue> retVal = Optional.empty();

		try (TraversalConnection conn = new TraversalConnection(this.dbType)) {
			CrymlinTraversalSource crymlin = conn.getCrymlinTraversal();
			Optional<Vertex> vdVertexOpt = crymlin
					.byID(variableDeclaration.getId())
					.tryNext();

			if (vdVertexOpt.isEmpty()) {
				log.warn("Unexpected: VariableDeclaration not available in graph. ID={}", variableDeclaration.getId());
				return Optional.empty();
			}

			Vertex variableDeclarationVertex = vdVertexOpt.get();
			log.debug("Vertex for function call: {}", callExpressionVertex.property("code").value());
			log.debug("Vertex of variable declaration: {}", variableDeclarationVertex.property("code").value());

			// traverse in reverse along EOG edges from v until variableDeclarationVertex -->
			// one of them must have more information on the value of the operand
			CrymlinTraversal<Vertex, Vertex> traversal = crymlin.byID((long) callExpressionVertex.id())
					.repeat(in("EOG"))
					.until(
						is(variableDeclarationVertex))
					.emit();

			while (traversal.hasNext()) {
				Vertex tVertex = traversal.next();
				log.debug("Check if is assignment to {}: {}", variableDeclarationVertex.property("code").value(), tVertex.property("code").value());

				boolean isBinaryOperatorVertex = tVertex.label().contains(BinaryOperator.class.getSimpleName());

				if (isBinaryOperatorVertex && "=".equals(tVertex.property("operatorCode")
						.value())) {
					// this is an assignment that may set the value of our operand
					Vertex lhs = tVertex.vertices(Direction.OUT, "LHS")
							.next();

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
						} else if (isRhsExpressionList) {

							if (rhs.edges(Direction.IN, "EOG").hasNext()) {
								Vertex lastExpressionInList = rhs.edges(Direction.IN, "EOG")
										.next()
										.outVertex();
								// TODO Do not assume that last in ExpressionList is a constant but rather apply ConstantResolution again.
								// See https://***REMOVED***/***REMOVED***/issues/17
								if (lastExpressionInList.label().equals(Literal.class.getSimpleName())) {
									Object literalValue = lastExpressionInList.property("value").value();
									Optional<ConstantValue> constantValue = ConstantValue.tryOf(literalValue);
									if (constantValue.isPresent()) {
										return constantValue;
									}
									log.warn("Unknown literal type encountered: {} (value: {})", literalValue.getClass(), literalValue);
								} else if (lastExpressionInList.label().equals(DeclaredReferenceExpression.class.getSimpleName())) {
									System.out.println("Trying to resolve " + lastExpressionInList);

									// Get declaration of the variable used as last item in expression list
									Iterator<Edge> refersTo = lastExpressionInList.edges(Direction.IN, "DFG");
									if (refersTo.hasNext()) {
										Vertex v = refersTo.next().outVertex();
										if (v.label().equals(VariableDeclaration.class.getSimpleName())) {
											VariableDeclaration varDecl = (VariableDeclaration) OverflowDatabase.getInstance().vertexToNode(v);
											ConstantResolver resolver = new ConstantResolver(this.dbType);
											Optional<ConstantValue> constantValue = resolver.resolveConstantValueOfFunctionArgument(varDecl, lastExpressionInList);
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
						}
						log.error("Value of operand set in assignment expression");
						return Optional.empty();
					}
				}
			}

			// we arrived at the declaration of the variable used as an argument
			//log.info("Checking declaration for a literal initializer");

			// check if we have an initializer with a literal
			Iterator<Vertex> itInitializerVertex = variableDeclarationVertex.vertices(Direction.OUT, "INITIALIZER");

			if (itInitializerVertex.hasNext()) {
				// there should be at most one
				Vertex initializerVertex = itInitializerVertex.next();

				List<String> labels = Arrays.asList(initializerVertex.label().split(OverflowDatabase.LabelDelimiter));

				if (labels.contains(Literal.class.getSimpleName())) {
					Object literalValue = initializerVertex.property("value").value();
					retVal = ConstantValue.tryOf(literalValue);

				} else if (labels.contains(ConstructExpression.class.getSimpleName())) {
					Iterator<Vertex> initializers = initializerVertex.vertices(Direction.OUT, "ARGUMENTS");
					if (initializers.hasNext()) {
						Vertex init = initializers.next();
						if (Arrays.asList(init.label()
								.split(OverflowDatabase.LabelDelimiter))
								.contains("Literal")) {
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
				} else if (labels.contains(InitializerListExpression.class.getSimpleName())) {
					Iterator<Vertex> initializers = initializerVertex.vertices(Direction.OUT, "INITIALIZERS");
					if (initializers.hasNext()) {
						Vertex init = initializers.next();
						if (Arrays.asList(init.label()
								.split(OverflowDatabase.LabelDelimiter))
								.contains("Literal")) {
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
				} else if (labels.contains(ExpressionList.class.getSimpleName())) {
					Iterator<Vertex> initializers = initializerVertex.vertices(Direction.OUT, "SUBEXPR");
					Vertex init = null;
					while (initializers.hasNext()) { // get the last initializer according to C++17 standard
						init = initializers.next();
					}
					if (init != null) {
						if (Arrays.asList(init.label()
								.split(OverflowDatabase.LabelDelimiter))
								.contains("Literal")) {
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
