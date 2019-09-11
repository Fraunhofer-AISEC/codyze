package de.fraunhofer.aisec.crymlin.dsl;

import de.fraunhofer.aisec.cpg.graph.*;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.hasLabel;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.out;

/**
 * This class adds new functions to the traversal to START from
 *
 * <p>The DSL definition must be a class that extends {@code GraphTraversalSource} and should be
 * referenced in the {@code GremlinDsl} annotation on the {@code GraphTraversal} extension - in this
 * example {@link CrymlinTraversalDsl}. The methods on this class will be exposed with the other
 * traversal start steps on {@code GraphTraversalSource}.
 */
public class CrymlinTraversalSourceDsl extends GraphTraversalSource {

  public CrymlinTraversalSourceDsl(
      final Graph graph, final TraversalStrategies traversalStrategies) {
    super(graph, traversalStrategies);
  }

  public CrymlinTraversalSourceDsl(final Graph graph) {
    super(graph);
  }

  public GraphTraversal<Vertex, Vertex> variableDeclarations() {
    GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

    return traversal.hasLabel(LabelP.of(VariableDeclaration.class.getSimpleName()));
  }

  // FIXME names of functions are '<namespace>::<function name>'
  // Thus, we would need to search for labels with the fully qualified name.
  // I think C++ discourages the use of namespace names as class/struct names. We could "interpret"
  // namespaces as "types".

  /**
   * Returns function and method calls.
   *
   * @return traversal of matched {@code CallExpression} vertices
   */
  public GraphTraversal<Vertex, Vertex> calls() {
    GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

    return traversal.has(T.label, LabelP.of(CallExpression.class.getSimpleName()));
  }

  /**
   * Returns function and method calls with the given name.
   *
   * @param callee_name name of the called function/method
   * @return traversal of matched {@code CallExpression} vertices
   */
  public GraphTraversal<Vertex, Vertex> calls(String callee_name) {
    GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

    return traversal
        .has(T.label, LabelP.of(CallExpression.class.getSimpleName()))
        .has("name", callee_name);
  }

  /**
   * Returns method calls on an instance (object) with the given name and where the instance has the
   * specified type.
   *
   * @param callee_name name of the called method
   * @param base_type type of the instance (object)
   * @return traversal of matched {@code CallExpression} vertices
   */
  public GraphTraversal<Vertex, Vertex> calls(String callee_name, String base_type) {
    GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

    return traversal
        .has(T.label, LabelP.of(CallExpression.class.getSimpleName()))
        .has("name", callee_name)
        .where(out("BASE").has("type", base_type));
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
    return this.clone().V().has(T.label, LabelP.of(FunctionDeclaration.class.getSimpleName()));
//    return this.clone().V().hasLabel(FunctionDeclaration.class.getSimpleName());
//    return traversal.has(T.label, LabelP.of(FunctionDeclaration.class.getSimpleName()));
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

  /**
   * Returns the node by ID
   *
   * @return
   */
  public GraphTraversal<Vertex, Vertex> byID(long id) {
    GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

    return traversal.has(T.id, id);
  }

  /**
   * Returns the next nodes connected via EOG
   *
   * @return
   */
  public GraphTraversal<Vertex, Vertex> nextEOGFromID(long id) {

    //    this.clone().V().has(T.id, id)
    //            .repeat(out("EOG").simplePath().store("x"))
    //            .cap("x")
    //            .unfold()
    //            .dedup()

    GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

    return traversal
        .has(T.id, id)
        .repeat(out("EOG"))
        .until(hasLabel(LabelP.of(MemberCallExpression.class.getSimpleName())))
        .emit(hasLabel(LabelP.of(MemberCallExpression.class.getSimpleName())));
  }
}
