
package de.fraunhofer.aisec.crymlin.dsl;

import static de.fraunhofer.aisec.crymlin.dsl.__.hasLabel;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.out;

import de.fraunhofer.aisec.cpg.graph.*;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * This class adds new functions to the traversal to START from
 *
 * <p>
 * The DSL definition must be a class that extends {@code GraphTraversalSource} and should be referenced in the {@code GremlinDsl} annotation on the
 * {@code GraphTraversal} extension - in this example {@link CrymlinTraversalDsl}. The methods on this class will be exposed with the other traversal start steps on
 * {@code GraphTraversalSource}.
 */
public class CrymlinTraversalSourceDsl extends GraphTraversalSource {

	public CrymlinTraversalSourceDsl(final Graph graph, final TraversalStrategies traversalStrategies) {
		super(graph, traversalStrategies);
	}

	public CrymlinTraversalSourceDsl(final Graph graph) {
		super(graph);
	}

	public GraphTraversal<Vertex, Vertex> variableDeclarations() {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal.has("type", VariableDeclaration.class.getSimpleName());
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

		return traversal.hasLabel(CallExpression.class.getSimpleName(), OverflowDatabase.getSubclasses(CallExpression.class));
	}

	/**
	 * Returns function and method calls with the given name.
	 *
	 * @param callee_name name of the called function/method
	 * @return traversal of matched {@code CallExpression} vertices
	 */
	public GraphTraversal<Vertex, Vertex> calls(String callee_name) {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal.hasLabel(CallExpression.class.getSimpleName(), OverflowDatabase.getSubclasses(CallExpression.class)).has("fqn", callee_name);
	}

	/**
	 * Returns method calls on an instance (object) with the given name and where the instance has the specified type.
	 *
	 * @param callee_name name of the called method
	 * @param base_type type of the instance (object)
	 * @return traversal of matched {@code CallExpression} vertices
	 */
	public GraphTraversal<Vertex, Vertex> calls(String callee_name, String base_type) {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal.hasLabel(CallExpression.class.getSimpleName(), OverflowDatabase.getSubclasses(CallExpression.class)).has("fqn", callee_name).where(
			out("BASE").has("type", base_type));
	}

	/**
	 * Returns nodes with a label {@code MethodDeclaration}.
	 *
	 * @return
	 */
	public GraphTraversal<Vertex, Vertex> methods() {
		return this.clone().V().hasLabel(MethodDeclaration.class.getSimpleName(), OverflowDatabase.getSubclasses(MethodDeclaration.class));
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
		return this.clone().V().hasLabel(TranslationUnitDeclaration.class.getSimpleName(), OverflowDatabase.getSubclasses(TranslationUnitDeclaration.class));
	}

	/**
	 * Returns all RecordDeclarations (e.g., Java classes).
	 *
	 * @return
	 */
	public GraphTraversal<Vertex, Vertex> recorddeclarations() {
		return this.clone().V().hasLabel(RecordDeclaration.class.getSimpleName(), OverflowDatabase.getSubclasses(RecordDeclaration.class));
	}

	/**
	 * Returns the RecordDeclarations with a given name.
	 *
	 * @return
	 */
	public GraphTraversal<Vertex, Vertex> recorddeclaration(String recordname) {
		return this.clone().V().hasLabel(RecordDeclaration.class.getSimpleName(), OverflowDatabase.getSubclasses(RecordDeclaration.class)).has("name", recordname);
	}

	/**
	 * Returns all RecordDeclarations (e.g., Java classes).
	 *
	 * @return
	 */
	public GraphTraversal<Vertex, Vertex> functiondeclarations() {

		// NOTE We can query for multiple labels like this:
		// graph.traversal().V().where(hasLabel("VariableDeclaration").or().hasLabel("DeclaredReferenceExpression")).label().toList()
		//
		// or, much simpler:
		//
		// graph.traversal().V().hasLabel("VariableDeclaration", "DeclaredReferenceExpression").toList()

		return this.clone().V().hasLabel(FunctionDeclaration.class.getSimpleName(), OverflowDatabase.getSubclasses(FunctionDeclaration.class));
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

		return traversal.hasLabel(FunctionDeclaration.class.getSimpleName(), OverflowDatabase.getSubclasses(FunctionDeclaration.class)).has("name", functionname);
	}

	/**
	 * Returns all Declarations (e.g., variables).
	 *
	 * @return
	 */
	public GraphTraversal<Vertex, Vertex> declarations() {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal.hasLabel(Declaration.class.getSimpleName(), OverflowDatabase.getSubclasses(Declaration.class));
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

		return traversal.has(T.id, id).repeat(out("EOG")).until(
			hasLabel(MemberCallExpression.class.getSimpleName(), OverflowDatabase.getSubclasses(MemberCallExpression.class))).emit(
				hasLabel(MemberCallExpression.class.getSimpleName(), OverflowDatabase.getSubclasses(MemberCallExpression.class)));
	}
}
