package de.fraunhofer.aisec.crymlin.dsl;

import de.fraunhofer.aisec.cpg.graph.CallExpression;
import de.fraunhofer.aisec.cpg.graph.Declaration;
import de.fraunhofer.aisec.cpg.graph.FunctionDeclaration;
import de.fraunhofer.aisec.cpg.graph.MethodDeclaration;
import de.fraunhofer.aisec.cpg.graph.RecordDeclaration;
import de.fraunhofer.aisec.cpg.graph.TranslationUnitDeclaration;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * The DSL definition must be a class that extends {@code GraphTraversalSource} and should be
 * referenced in the {@code GremlinDsl} annotation on the {@code GraphTraversal} extension - in this
 * example {@link CrymlinTraversalDsl}. The methods on this class will be exposed with the other
 * traversal start steps on {@code GraphTraversalSource}.
 */
public class CrymlinTraversalSourceDsl extends GraphTraversalSource {

  private static final String CALL_EXPRESSION = "CallExpressicon::Expression::Statement";
  public static final String TRANSLATION_UNIT_DECLARATION = "Expression::Literal::Statement";
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

  /**
   * Returns nodes with a label {@code CallExpression}.
   *
   * @return
   */
  public GraphTraversal<Vertex, Vertex> calls() {
    GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

    return traversal.has(T.label, LabelP.of(CallExpression.class.getSimpleName()));
  }

  /**
   * Returns the calls with a given name.
   *
   * @return
   */
  public GraphTraversal<Vertex, Vertex> calls(String callee_name) {
    GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

    return traversal
        .has(T.label, LabelP.of(CallExpression.class.getSimpleName()))
        .has("name", callee_name);
  }

  public GraphTraversal<Vertex, Vertex> calls(String callee_name, String base_type) {
    GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

    return traversal
        .has(T.label, LabelP.of(CallExpression.class.getSimpleName()))
        .has("name", callee_name)
        .has("type", base_type);
  }

  /**
   * Returns nodes with a label {@code MethodDeclaration}.
   *
   * @return
   */
  public GraphTraversal<Vertex, Vertex> methods() {
    GraphTraversalSource traversal = this.clone();

    return traversal.V().has(T.label, LabelP.of(MethodDeclaration.class.getSimpleName()));
  }

  //  @Deprecated
  //  public GraphTraversal<Vertex, Vertex> cipherListSetterCalls() {
  //    GraphTraversalSource traversal = this.clone();
  //
  //    return this.calls().has(NAME, "SSL_CTX_set_cipher_list");
  //  }

  /**
   * Returns all TranslationUnitDeclaration nodes.
   *
   * @return
   */
  public GraphTraversal<Vertex, Vertex> translationunits() {
    GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

    return traversal.has(T.label, LabelP.of(TranslationUnitDeclaration.class.getSimpleName()));
  }

  /**
   * Returns all RecordDeclarations (e.g., Java classes).
   *
   * @return
   */
  public GraphTraversal<Vertex, Vertex> recorddeclarations() {
    GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

    return traversal.has(T.label, LabelP.of(RecordDeclaration.class.getSimpleName()));
  }

  /**
   * Returns the RecordDeclarations with a given name.
   *
   * @return
   */
  public GraphTraversal<Vertex, Vertex> recorddeclaration(String recordname) {
    GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

    return traversal
        .has(T.label, LabelP.of(RecordDeclaration.class.getSimpleName()))
        .has("name", recordname);
  }

  /**
   * Returns all RecordDeclarations (e.g., Java classes).
   *
   * @return
   */
  public GraphTraversal<Vertex, Vertex> functiondeclarations() {
    GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

    return traversal.has(T.label, LabelP.of(FunctionDeclaration.class.getSimpleName()));
  }

  /**
   * Returns the FunctionDeclarations with a given name.
   *
   * @return
   */
  public GraphTraversal<Vertex, Vertex> functiondeclaration(String functionname) {
    GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

    return traversal
        .has(T.label, LabelP.of(FunctionDeclaration.class.getSimpleName()))
        .has("name", functionname);
  }

  /**
   * Returns all Declarations (e.g., variables).
   *
   * @return
   */
  public GraphTraversal<Vertex, Vertex> declarations() {
    GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

    return traversal.has(T.label, LabelP.of(Declaration.class.getSimpleName()));
  }
}
