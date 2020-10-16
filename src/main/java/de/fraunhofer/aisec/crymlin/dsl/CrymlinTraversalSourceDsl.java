
package de.fraunhofer.aisec.crymlin.dsl;

import de.fraunhofer.aisec.analysis.ShellCommand;
import de.fraunhofer.aisec.cpg.graph.declarations.EnumConstantDeclaration;
import de.fraunhofer.aisec.cpg.graph.declarations.FieldDeclaration;
import de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration;
import de.fraunhofer.aisec.cpg.graph.declarations.MethodDeclaration;
import de.fraunhofer.aisec.cpg.graph.declarations.NamespaceDeclaration;
import de.fraunhofer.aisec.cpg.graph.declarations.RecordDeclaration;
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration;
import de.fraunhofer.aisec.cpg.graph.declarations.TypedefDeclaration;
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration;
import de.fraunhofer.aisec.cpg.graph.statements.IfStatement;
import de.fraunhofer.aisec.cpg.graph.statements.ReturnStatement;
import de.fraunhofer.aisec.cpg.graph.statements.Statement;
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression;
import de.fraunhofer.aisec.cpg.graph.statements.expressions.ConstructExpression;
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberCallExpression;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import org.apache.tinkerpop.gremlin.process.remote.RemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.TextP;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.eclipse.xtend.lib.macro.declaration.ParameterDeclaration;

import static de.fraunhofer.aisec.crymlin.dsl.CrymlinConstants.EOG;
import static de.fraunhofer.aisec.crymlin.dsl.CrymlinConstants.NAME;
import static de.fraunhofer.aisec.crymlin.dsl.__.hasLabel;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.in;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.out;

/**
 * This class adds new functions to the traversal to START from
 *
 * <p>
 * The DSL definition must be a class that extends {@code GraphTraversalSource} and should be referenced in the {@code GremlinDsl} annotation on the
 * {@code GraphTraversal} extension - in this example {@link CrymlinTraversalDsl}. The methods on this class will be exposed with the other traversal start steps on
 * {@code GraphTraversalSource}.</p>
 * <p>
 * Note: Overloading methods is of course possible at Java level, but might lead to undesired effects at Jython level.
 */
public class CrymlinTraversalSourceDsl extends GraphTraversalSource {

	public CrymlinTraversalSourceDsl(
			final Graph graph, final TraversalStrategies traversalStrategies) {
		super(graph, traversalStrategies);
	}

	public CrymlinTraversalSourceDsl(final Graph graph) {
		super(graph);
	}

	public CrymlinTraversalSourceDsl(final RemoteConnection connection) {
		super(connection);
	}

	/**
	 * Returns function and method calls.
	 * <p>
	 * This traversal step will return vertices of type CallExpression (or its subclasses).
	 *
	 * @return traversal of matched {@code CallExpression} vertices
	 */
	@ShellCommand("All function/method calls")
	public GraphTraversal<Vertex, Vertex> calls() {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal.hasLabel(
			CallExpression.class.getSimpleName(), OverflowDatabase.getSubclasses(CallExpression.class));
	}

	/**
	 * Returns the vertices representing the call site of a function with the given fully qualified name.
	 * <p>
	 * This traversal step will return vertices of type CallExpression (or its subclasses).
	 *
	 * @param calleeName name of the called function/method
	 * @return traversal of matched {@code CallExpression} vertices
	 */
	public GraphTraversal<Vertex, Vertex> callsFqn(String calleeName) {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal
				.hasLabel(
					CallExpression.class.getSimpleName(),
					OverflowDatabase.getSubclasses(CallExpression.class))
				.has("fqn", TextP.eq(calleeName));
	}

	/**
	 * Returns the vertices representing the call site of a function whose given fully qualified name contains the argument.
	 * <p>
	 * This traversal step will return vertices of type CallExpression (or its subclasses).
	 *
	 * @param calleeName name of the called function/method
	 * @return traversal of matched {@code CallExpression} vertices
	 */
	@ShellCommand("Calls to functions/methods whose (fully qualified) name contains the argument.")
	public GraphTraversal<Vertex, Vertex> calls(String calleeName) {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal
				.hasLabel(
					CallExpression.class.getSimpleName(),
					OverflowDatabase.getSubclasses(CallExpression.class))
				.has("fqn", TextP.containing(calleeName));
	}

	/**
	 * Returns the vertices representing the construct site of a object with the given fully qualified type.
	 * <p>
	 * This traversal step will return vertices of type ConstructExpression (or its subclasses) which
	 * match the given type, i.e. which have a TYPE edge to any Type node with a name that equals
	 * the given {@code type}.
	 *
	 * @param type Fully qualified name of the constructed type.
	 * @return traversal of matched {@code ConstructExpression} vertices
	 */
	@ShellCommand("Constructors containing a given type")
	public GraphTraversal<Vertex, Vertex> ctors(String type) {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal.hasLabel(ConstructExpression.class.getSimpleName(), OverflowDatabase.getSubclasses(ConstructExpression.class))
				.where(out(CrymlinConstants.TYPE)
						.has(NAME, TextP.containing(type)));
	}

	/**
	 * Returns nodes with a label {@code NamepaceDeclaration}.
	 *
	 * @return
	 */
	@ShellCommand("Namespaces")
	public GraphTraversal<Vertex, Vertex> namespaces() {
		return this.clone()
				.V()
				.hasLabel(
					NamespaceDeclaration.class.getSimpleName(),
					OverflowDatabase.getSubclasses(NamespaceDeclaration.class));
	}

	/**
	 * Returns nodes with a label {@code NamepaceDeclaration}.
	 *
	 * @return
	 */
	@ShellCommand("Namespaces containing the given substring")
	public GraphTraversal<Vertex, Vertex> namespaces(String substring) {
		return this.clone()
				.V()
				.hasLabel(
					NamespaceDeclaration.class.getSimpleName(),
					OverflowDatabase.getSubclasses(NamespaceDeclaration.class))
				.has("name", TextP.containing(substring));
	}

	/**
	 * Returns nodes with a label {@code Statement}.
	 *
	 * @return
	 */
	@ShellCommand("Statements")
	public GraphTraversal<Vertex, Vertex> statements() {
		return this.clone()
				.V()
				.hasLabel(
					Statement.class.getSimpleName(),
					OverflowDatabase.getSubclasses(Statement.class));
	}

	/**
	 * Returns nodes with a label {@code MethodDeclaration}.
	 *
	 * @return
	 */
	@ShellCommand("All class methods (Note: rather use 'functions()' to include C/C++ functions)")
	public GraphTraversal<Vertex, Vertex> methods() {
		return this.clone()
				.V()
				.hasLabel(
					MethodDeclaration.class.getSimpleName(),
					OverflowDatabase.getSubclasses(MethodDeclaration.class));
	}

	/**
	 * Returns nodes with a label {@code MethodDeclaration} whose name contains the given substring.
	 *
	 * @return
	 */
	@ShellCommand("Class methods containing the given name (Note: rather use 'functions()' to include C/C++ functions)")
	public GraphTraversal<Vertex, Vertex> methods(String substring) {
		return this.clone()
				.V()
				.hasLabel(
					MethodDeclaration.class.getSimpleName(),
					OverflowDatabase.getSubclasses(MethodDeclaration.class))
				.has("name", TextP.containing(substring));
	}

	/**
	 * Returns all TranslationUnitDeclaration nodes.
	 *
	 * @return
	 */
	@ShellCommand("All TranslationUnits (=Source code files)")
	public GraphTraversal<Vertex, Vertex> sourcefiles() {
		return this.clone()
				.V()
				.hasLabel(
					TranslationUnitDeclaration.class.getSimpleName(),
					OverflowDatabase.getSubclasses(TranslationUnitDeclaration.class));
	}

	/**
	 * Returns all TranslationUnitDeclaration nodes.
	 *
	 * @return
	 */
	@ShellCommand("TranslationUnits (=Source code files) containing the given name")
	public GraphTraversal<Vertex, Vertex> sourcefiles(String substring) {
		return this.clone()
				.V()
				.hasLabel(
					TranslationUnitDeclaration.class.getSimpleName(),
					OverflowDatabase.getSubclasses(TranslationUnitDeclaration.class))
				.has("name", TextP.containing(substring));
	}

	/**
	 * Returns all IfStatements (e.g., Java classes).
	 *
	 * @return
	 */
	@ShellCommand("All IfStatements")
	public GraphTraversal<Vertex, Vertex> ifstmts() {
		return this.clone()
				.V()
				.hasLabel(
					IfStatement.class.getSimpleName(),
					OverflowDatabase.getSubclasses(IfStatement.class));
	}

	/**
	 * Returns all IfStatements (e.g., Java classes).
	 *
	 * @return
	 */
	@ShellCommand("IfStatements whose code contains the given substring")
	public GraphTraversal<Vertex, Vertex> ifstmts(String subcode) {
		return this.clone()
				.V()
				.hasLabel(
					IfStatement.class.getSimpleName(),
					OverflowDatabase.getSubclasses(IfStatement.class))
				.has("code", TextP.containing(subcode));
	}

	/**
	 * Returns all RecordDeclarations (e.g., Java classes).
	 *
	 * @return
	 */
	@ShellCommand("All RecordDeclarations (Java classes, enums, C/C++ structs)")
	public GraphTraversal<Vertex, Vertex> records() {
		return this.clone()
				.V()
				.hasLabel(
					RecordDeclaration.class.getSimpleName(),
					OverflowDatabase.getSubclasses(RecordDeclaration.class));
	}

	/**
	 * Returns all RecordDeclarations (e.g., Java classes) whose name contains the given substring.
	 *
	 * @return
	 */
	@ShellCommand("RecordDeclarations (Java classes, enums, C/C++ structs) containing the given name")
	public GraphTraversal<Vertex, Vertex> records(String substring) {
		return this.clone()
				.V()
				.hasLabel(
					RecordDeclaration.class.getSimpleName(),
					OverflowDatabase.getSubclasses(RecordDeclaration.class))
				.has("name", TextP.containing(substring));
	}

	/**
	 * Returns the FunctionDeclarations with a given name.
	 *
	 * @return
	 */
	@ShellCommand("All functions/methods")
	public GraphTraversal<Vertex, Vertex> functions() {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal.hasLabel(
			FunctionDeclaration.class.getSimpleName(),
			OverflowDatabase.getSubclasses(FunctionDeclaration.class));
	}

	/**
	 * Returns the FunctionDeclarations with a given name.
	 *
	 * @return
	 */
	@ShellCommand("Functions/methods containing the given name")
	public GraphTraversal<Vertex, Vertex> functions(String functionname) {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal.hasLabel(
			FunctionDeclaration.class.getSimpleName(),
			OverflowDatabase.getSubclasses(FunctionDeclaration.class)).has("name", TextP.containing(functionname));
	}

	/**
	 * Returns immediate childs of ValueDeclaration.
	 *
	 * @return
	 */
	@ShellCommand("All declarations of values (parameters, variables, fields, enums constants)")
	public GraphTraversal<Vertex, Vertex> valdecl() {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal.hasLabel(
			ParameterDeclaration.class.getSimpleName(),
			VariableDeclaration.class.getSimpleName(),
			FieldDeclaration.class.getSimpleName(),
			EnumConstantDeclaration.class.getSimpleName());
	}

	/**
	 * Returns all Declarations (e.g., variables) containing the given substring.
	 *
	 * @return
	 */
	@ShellCommand("All declarations of values (parameters, variables, fields, enums constants) containing the given name")
	public GraphTraversal<Vertex, Vertex> valdecl(String substring) {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal.hasLabel(
			ParameterDeclaration.class.getSimpleName(),
			VariableDeclaration.class.getSimpleName(),
			FieldDeclaration.class.getSimpleName(),
			EnumConstantDeclaration.class.getSimpleName())
				.has("name", TextP.containing(substring));
	}

	/**
	 * Returns all Variable declarations.
	 *
	 * @return
	 */
	@ShellCommand("Variable declarations. Use valdecl() instead to include parameters, fields, and enums")
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

	public GraphTraversal<Vertex, Vertex> fields(String fieldName) {
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
	@ShellCommand("Next call statement following node by ID")
	public GraphTraversal<Vertex, Vertex> nextCallByID(long id) {
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
	 * Returns the next nodes connected via EOG
	 *
	 * @return
	 */
	@ShellCommand("Next call statement following node by ID")
	public GraphTraversal<Vertex, Vertex> prevCallByID(long id) {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal.has(T.id, id)
				.repeat(in(EOG))
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
	 * Returns the FunctionDeclarations with a given name.
	 *
	 * @return
	 */
	@ShellCommand("All return statements")
	public GraphTraversal<Vertex, Vertex> returns() {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal.hasLabel(
			FunctionDeclaration.class.getSimpleName(),
			OverflowDatabase.getSubclasses(ReturnStatement.class));
	}

	/**
	 * Returns all TypedefDeclarations.
	 *
	 * @return
	 */
	@ShellCommand("All typedefs")
	public GraphTraversal<Vertex, Vertex> typedefs() {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal.hasLabel(
			FunctionDeclaration.class.getSimpleName(),
			OverflowDatabase.getSubclasses(TypedefDeclaration.class));
	}

	/**
	 * Returns all TypedefDeclarations containing the given name.
	 *
	 * @return
	 */
	@ShellCommand("All typedefs containing the given name")
	public GraphTraversal<Vertex, Vertex> typedefs(String substring) {
		GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

		return traversal.hasLabel(
			FunctionDeclaration.class.getSimpleName(),
			OverflowDatabase.getSubclasses(TypedefDeclaration.class)).has("name", TextP.containing(substring));
	}

}
