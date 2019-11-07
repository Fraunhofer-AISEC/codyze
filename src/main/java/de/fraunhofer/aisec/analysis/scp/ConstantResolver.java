
package de.fraunhofer.aisec.analysis.scp;

import com.steelbridgelabs.oss.neo4j.structure.Neo4JVertex;
import de.fraunhofer.aisec.cpg.graph.Declaration;
import de.fraunhofer.aisec.cpg.graph.VariableDeclaration;
import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversal;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

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
	 * 8. {no intializer with value e.g. function argument} continue traversing the graph
	 *
	 * @param variableDeclaration
	 * @param attributeType
	 */ // TODO Should be replaced by a more generic function that takes a single DeclaredReferenceExpression as an argument
	public Optional<Object> resolveConstantValueOfFunctionArgument(@Nullable Declaration variableDeclaration, @NonNull Vertex callExpressionVertex,
			@Nullable String attributeType) {
		if (variableDeclaration == null) {
			return Optional.empty();
		}
		Vertex v = callExpressionVertex;

		try (TraversalConnection conn = new TraversalConnection(this.dbType)) {
			CrymlinTraversalSource crymlin = conn.getCrymlinTraversal();
			Optional<Vertex> vdVertexOpt = crymlin
					.byID(variableDeclaration.getId())
					.tryNext();
			assert vdVertexOpt.isPresent() : "Unexpected: VariableDeclaration not available in graph. ID=" + variableDeclaration.getId();

			Vertex variableDeclarationVertex = vdVertexOpt.get();
			log.debug("Vertex for function call: {}", v);
			log.debug("Vertex of variable declaration: {}", variableDeclarationVertex);

			// traverse in reverse along EOG edges from v until variableDeclarationVertex -->
			// one of them must have more information on the value of the operand
			CrymlinTraversal<Vertex, Vertex> traversal = crymlin.byID((long) v.id())
					.repeat(in("EOG"))
					.until(
						is(variableDeclarationVertex))
					.emit();
			dumpVertices(traversal.clone()
					.toList());

			while (traversal.hasNext()) {
				Vertex tVertex = traversal.next();

				boolean isBinaryOperatorVertex = Arrays.asList(tVertex.label()
						.split(Neo4JVertex.LabelDelimiter))
						.contains("BinaryOperator");

				if (isBinaryOperatorVertex && "=".equals(tVertex.property("operatorCode")
						.value())) {
					// this is an assignment that may set the value of our operand
					Vertex lhs = tVertex.vertices(Direction.OUT, "LHS")
							.next();

					if (lhs.vertices(Direction.OUT, "REFERS_TO")
							.next()
							.equals(variableDeclarationVertex)) {
						Vertex rhs = tVertex.vertices(Direction.OUT, "RHS")
								.next();

						boolean isRhsLiteral = Arrays.asList(rhs.label()
								.split(Neo4JVertex.LabelDelimiter))
								.contains("Literal");

						if (isRhsLiteral) {
							Object literalValue = rhs.property("value")
									.value();
							Class literalValueClass = literalValue.getClass();

							if (literalValueClass.equals(Long.class) || literalValueClass.equals(Integer.class)) {
								return Optional.of(((Number) literalValue).intValue());
							}

							if (literalValueClass.equals(Double.class) || literalValueClass.equals(Float.class)) {
								return Optional.of(((Number) literalValue).floatValue());
							}

							if (literalValueClass.equals(Boolean.class)) {
								return Optional.of((Boolean) literalValue);
							}

							if (literalValueClass.equals(String.class)) {
								// character and string literals both have value of type String
								String valueString = (String) literalValue;

								// FIXME incomplete hack; only works for primitive char type; is that
								// enough?
								if ("char".equals(attributeType) || "char".equals(variableDeclarationVertex.property("type")
										.value())) {
									// FIXME this will likely break on an empty string
									return Optional.of(valueString.charAt(0));
								}
								return Optional.of(valueString);
							}
							log.error("Unknown literal type encountered: {} (value: {})", literalValue.getClass(), literalValue);
						}

						// TODO properly resolve rhs expression

						log.warn("Value of operand set in assignment expression");
						break;
					}
				}
			}

			// we arrived at the declaration of the variable used as an argument
			log.warn("Checking declaration for a literal initializer");

			// check if we have an initializer with a literal
			Iterator<Vertex> itInitializerVertex = variableDeclarationVertex.vertices(Direction.OUT, "INITIALIZER");
			while (itInitializerVertex.hasNext()) {
				// there should be at most one
				Vertex initializerVertex = itInitializerVertex.next();

				if (Arrays.asList(initializerVertex.label()
						.split(Neo4JVertex.LabelDelimiter))
						.contains("Literal")) {
					Object literalValue = initializerVertex.property("value")
							.value();
					Class literalValueClass = literalValue.getClass();

					if (literalValueClass.equals(Long.class) || literalValueClass.equals(Integer.class)) {
						return Optional.of(((Number) literalValue).intValue());
					}

					if (literalValueClass.equals(Double.class) || literalValueClass.equals(Float.class)) {
						return Optional.of(((Number) literalValue).floatValue());
					}

					if (literalValueClass.equals(Boolean.class)) {
						return Optional.of((Boolean) literalValue);
					}

					if (literalValueClass.equals(String.class)) {
						// character and string literals both have value of type String
						String valueString = (String) literalValue;

						// FIXME incomplete hack; only works for primitive char type; is that
						// enough?
						if ("char".equals(attributeType) || "char".equals(variableDeclarationVertex.property("type")
								.value())) {
							// FIXME this will likely break on an empty string
							return Optional.of(valueString.charAt(0));
						}
						return Optional.of(valueString);
					}
					log.error("Unknown literal type encountered: {} (value: {})", literalValue.getClass(), literalValue);
				}
			}
		}

		return Optional.empty();
	}

	private void dumpVertices(Collection<Vertex> vertices) {
		log.debug("Dumping vertices: {}", vertices.size());

		int i = 0;
		for (Vertex v : vertices) {
			log.debug("Vertex {}: {}", i++, v);
		}
	}

	private void dumpPaths(Collection<Path> paths) {
		log.debug("Number of paths: {}", paths.size());

		for (Path p : paths) {
			log.debug("Path of length: {}", p.size());
			for (Object o : p) {
				log.debug("Path step: {}", o);
			}
		}
	}

}
