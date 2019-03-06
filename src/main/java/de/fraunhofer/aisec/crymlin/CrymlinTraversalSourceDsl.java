package de.fraunhofer.aisec.crymlin;

import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * The DSL definition must be a class that extends {@code GraphTraversalSource} and should be
 * referenced in the {@code GremlinDsl} annotation on the {@code GraphTraversal} extension - in this
 * example {@link CrymlinTraversalDsl}. The methods on this class will be exposed with the other
 * traversal start steps on {@code GraphTraversalSource}.
 */
public class CrymlinTraversalSourceDsl extends GraphTraversalSource {

  private static final String CALL_EXPRESSION = "CallExpression::Expression::Statement";
  public static final String LITERAL = "Expression::Literal::Statement";
  public static final String VARIABLE_DECLARATION = "Declaration::VariableDeclaration";

  public static final String ARGUMENTS = "ARGUMENTS";

  public static final String ARGUMENT_INDEX = "argumentIndex";
  public static final String NAME = "name";
  public static final String VALUE = "value";

  public CrymlinTraversalSourceDsl(
      final Graph graph, final TraversalStrategies traversalStrategies) {
    super(graph, traversalStrategies);
  }

  public CrymlinTraversalSourceDsl(final Graph graph) {
    super(graph);
  }

  public GraphTraversal<Vertex, Vertex> variableDeclarations() {
    GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

    return traversal.hasLabel(VARIABLE_DECLARATION);
  }

  public GraphTraversal<Vertex, Vertex> calls() {
    GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

    return traversal.hasLabel(CALL_EXPRESSION);
  }

  public GraphTraversal<Vertex, Vertex> cipherListSetterCalls() {
    return this.calls().has(NAME, "SSL_CTX_set_cipher_list");
  }
}
