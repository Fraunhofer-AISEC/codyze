
package de.fraunhofer.aisec.crymlin.dsl;

import de.fraunhofer.aisec.analysis.ShellCommand;
import de.fraunhofer.aisec.cpg.graph.*;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import static de.fraunhofer.aisec.crymlin.dsl.CrymlinConstants.*;
import static de.fraunhofer.aisec.crymlin.dsl.__.hasLabel;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.out;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.outE;

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

	/**
	 * Returns function and method calls.
	 *
	 * This traversal step will return vertices of type CallExpression (or its subclasses).
	 * 
	 * @return traversal of matched {@code CallExpression} vertices
	 */
	@ShellCommand("All callees (i.e., functions/methods called from the current traversal's nodes)")
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
	@ShellCommand("Calls to functions/methods with given (fully qualified) name")
	public GraphTraversal<Vertex, Vertex> calls(String calleeName) {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal.hasLabel(
			CallExpression.class.getSimpleName(),
			OverflowDatabase.getSubclasses(CallExpression.class)).has("fqn", calleeName);
	}

	/**
	 * Returns the vertices representing the construct site of a object with the given fully qualified type.
	 *
	 * This traversal step will return vertices of type ConstructExpression (or its subclasses) which
	 * match the given type, i.e. which have a TYPE edge to any Type node with a name that equals
	 * the given {@code type}.
	 *
	 * @param type Fully qualified name of the constructed type.
	 * @return traversal of matched {@code ConstructExpression} vertices
	 */
	@ShellCommand("Constructors of given type")
	public GraphTraversal<Vertex, Vertex> ctor(String type) {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal.hasLabel(ConstructExpression.class.getSimpleName(), OverflowDatabase.getSubclasses(ConstructExpression.class))
				.where(out(CrymlinConstants.TYPE)
						.has(NAME, type));
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
			OverflowDatabase.getSubclasses(CallExpression.class))
				.has("fqn", calleeName)
				.where(out(BASE).out(CrymlinConstants.TYPE).has(NAME, baseType));
	}

	/**
	 * Returns nodes with a label {@code MethodDeclaration}.
	 *
	 * @return
	 */
	@ShellCommand("Class methods")
	public GraphTraversal<Vertex, Vertex> methods() {
		return this.clone()
				.V()
				.hasLabel(
					MethodDeclaration.class.getSimpleName(),
					OverflowDatabase.getSubclasses(MethodDeclaration.class));
	}

	/**
	 * Returns all TranslationUnitDeclaration nodes.
	 *
	 * @return
	 */
	@ShellCommand("Translation Units (=Source code files)")
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
	@ShellCommand("Functions")
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
	}

	/**
	 * Returns the FunctionDeclarations with a given name.
	 *
	 * @return
	 */
	@ShellCommand("Functions matching the given name")
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
	@ShellCommand("All declarations (field, variables, records, ...)")
	public GraphTraversal<Vertex, Vertex> declarations() {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal.hasLabel(
			Declaration.class.getSimpleName(), OverflowDatabase.getSubclasses(Declaration.class));
	}

	/**
	 * Returns all Variable declarations.
	 *
	 * @return
	 */
	@ShellCommand("Variable declarations")
	public GraphTraversal<Vertex, Vertex> vars() {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal.hasLabel(
			VariableDeclaration.class.getSimpleName(), OverflowDatabase.getSubclasses(VariableDeclaration.class));
	}

	/**
	 * Returns all Field declarations.
	 *
	 * @return
	 */
	@ShellCommand("Field declarations")
	public GraphTraversal<Vertex, Vertex> fields() {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal.hasLabel(
			FieldDeclaration.class.getSimpleName(), OverflowDatabase.getSubclasses(FieldDeclaration.class));
	}

	public GraphTraversal<Vertex, Vertex> field(String fieldName) {
		return this.clone()
				.V()
				.hasLabel(
					FieldDeclaration.class.getSimpleName(),
					OverflowDatabase.getSubclasses(FieldDeclaration.class))
				.has("name", fieldName);
	}

	/**
	 * Returns the node by ID
	 *
	 * @return
	 */
	@ShellCommand("Node by its ID")
	public GraphTraversal<Vertex, Vertex> byID(long id) {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal.has(T.id, id);
	}

	/**
	 * Returns the next nodes connected via EOG
	 *
	 * @return
	 */
	@ShellCommand("Next node in evaluation order (from node by given id)")
	public GraphTraversal<Vertex, Vertex> nextEOGFromID(long id) {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal.has(T.id, id)
				.repeat(out(EOG))
				.until(
					hasLabel(
						MemberCallExpression.class.getSimpleName(),
						OverflowDatabase.getSubclasses(MemberCallExpression.class)))
				.emit(
					hasLabel(
						MemberCallExpression.class.getSimpleName(),
						OverflowDatabase.getSubclasses(MemberCallExpression.class)));
	}

	/**
	 * Returns nodes connected via outgoing DFG edges.
	 *
	 * @return
	 */
	@ShellCommand("Data flow into a node")
	public GraphTraversal<Vertex, Vertex> flowTo() {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal.out(EOG).inV();
	}

	/**
	 * Returns nodes connected via incoming DFG edges.
	 *
	 * @return
	 */
	@ShellCommand("Data flow from a node")
	public GraphTraversal<Vertex, Vertex> flowFrom() {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal.in(EOG).outV();
	}

}
