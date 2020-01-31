
package de.fraunhofer.aisec.crymlin.dsl;

import de.fraunhofer.aisec.cpg.graph.*;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import static de.fraunhofer.aisec.crymlin.dsl.__.hasLabel;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.out;

/**
 * This class adds new functions to the traversal to START from
 *
 * <p>
 * The DSL definition must be a class that extends {@code GraphTraversalSource} and should be referenced in the {@code GremlinDsl} annotation on the
 * {@code GraphTraversal} extension - in this example {@link CrymlinTraversalDsl}. The methods on this class will be exposed with the other traversal start steps on
 * {@code GraphTraversalSource}.
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

		return traversal.has("type", VariableDeclaration.class.getSimpleName());
	}

	/**
	 * Returns function and method calls.
	 *
	 * This traversal step will return vertices of type CallExpression (or its subclasses).
	 * 
	 * @return traversal of matched {@code CallExpression} vertices
	 */
	public GraphTraversal<Vertex, Vertex> calls() {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal.hasLabel(
			CallExpression.class.getSimpleName(), OverflowDatabase.getSubclasses(CallExpression.class));
	}

	/**
	 * Returns the vertices representing the call site of a function with the given fully qualified name.
	 *
	 * This traversal step will return vertices of type CallExpression (or its subclasses).
	 *
	 * @param calleeName name of the called function/method
	 * @return traversal of matched {@code CallExpression} vertices
	 */
	public GraphTraversal<Vertex, Vertex> calls(String calleeName) {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal.hasLabel(
			CallExpression.class.getSimpleName(),
			OverflowDatabase.getSubclasses(CallExpression.class)).has("fqn", calleeName);
	}

	/**
	 * Returns the vertices representing the construct site of a object with the given fully qualified type.
	 *
	 * This traversal step will return vertices of type ConstructExpression (or its subclasses).
	 *
	 * @param type of the ctor
	 * @return traversal of matched {@code ConstructExpression} vertices
	 */
	public GraphTraversal<Vertex, Vertex> ctor(String type) {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal.hasLabel(
			ConstructExpression.class.getSimpleName(),
			OverflowDatabase.getSubclasses(ConstructExpression.class)).has("possibleSubTypes", type);
	}

	/**
	 * Returns method calls on an instance (object) with the given name and where the instance has the specified type.
	 *
	 * @param calleeName name of the called method
	 * @param baseType type of the instance (object)
	 * @return traversal of matched {@code CallExpression} vertices
	 */
	public GraphTraversal<Vertex, Vertex> calls(String calleeName, String baseType) {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal.hasLabel(
			CallExpression.class.getSimpleName(),
			OverflowDatabase.getSubclasses(CallExpression.class)).has("fqn", calleeName).where(out("BASE").has("type", baseType));
	}

	/**
	 * Returns nodes with a label {@code MethodDeclaration}.
	 *
	 * @return
	 */
	public GraphTraversal<Vertex, Vertex> methods() {
		return this.clone()
				.V()
				.hasLabel(
					MethodDeclaration.class.getSimpleName(),
					OverflowDatabase.getSubclasses(MethodDeclaration.class));
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
		return this.clone()
				.V()
				.hasLabel(
					TranslationUnitDeclaration.class.getSimpleName(),
					OverflowDatabase.getSubclasses(TranslationUnitDeclaration.class));
	}

	/**
	 * Returns all RecordDeclarations (e.g., Java classes).
	 *
	 * @return
	 */
	public GraphTraversal<Vertex, Vertex> recorddeclarations() {
		return this.clone()
				.V()
				.hasLabel(
					RecordDeclaration.class.getSimpleName(),
					OverflowDatabase.getSubclasses(RecordDeclaration.class));
	}

	/**
	 * Returns the RecordDeclarations with a given name.
	 *
	 * @return
	 */
	public GraphTraversal<Vertex, Vertex> recorddeclaration(String recordname) {
		return this.clone()
				.V()
				.hasLabel(
					RecordDeclaration.class.getSimpleName(),
					OverflowDatabase.getSubclasses(RecordDeclaration.class))
				.has("name", recordname);
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

		return this.clone()
				.V()
				.hasLabel(
					FunctionDeclaration.class.getSimpleName(),
					OverflowDatabase.getSubclasses(FunctionDeclaration.class));
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

		return traversal.hasLabel(
			FunctionDeclaration.class.getSimpleName(),
			OverflowDatabase.getSubclasses(FunctionDeclaration.class)).has("name", functionname);
	}

	/**
	 * Returns all Declarations (e.g., variables).
	 *
	 * @return
	 */
	public GraphTraversal<Vertex, Vertex> declarations() {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal.hasLabel(
			Declaration.class.getSimpleName(), OverflowDatabase.getSubclasses(Declaration.class));
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

		return traversal.has(T.id, id)
				.repeat(out("EOG"))
				.until(
					hasLabel(
						MemberCallExpression.class.getSimpleName(),
						OverflowDatabase.getSubclasses(MemberCallExpression.class)))
				.emit(
					hasLabel(
						MemberCallExpression.class.getSimpleName(),
						OverflowDatabase.getSubclasses(MemberCallExpression.class)));
	}
}
