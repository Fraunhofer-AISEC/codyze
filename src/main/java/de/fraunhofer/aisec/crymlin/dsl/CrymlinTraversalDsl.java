
package de.fraunhofer.aisec.crymlin.dsl;

import de.fraunhofer.aisec.analysis.ShellCommand;
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration;
import de.fraunhofer.aisec.cpg.graph.statements.expressions.BinaryOperator;
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Literal;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.GremlinDsl;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * this class adds new Traversal-Steps
 *
 * <p>
 * This is the implementation of a custom DSL for graph traversals. A DSL definition must be an interface and extend {@code GraphTraversal.Admin} and should be annotated
 * with the {@code
 * GremlinDsl} annotation. Methods that are added to this interface become steps that are "appended" to the common steps of the Gremlin language. These methods must:
 *
 * <ul>
 * <li>Return a {@code GraphTraversal}
 * <li>Use common Gremlin steps or other DSL steps to compose the returned {@code GraphTraversal}
 * </ul>
 *
 * These methods are only applied to a {@code GraphTraversal}, but recall that a {@code
 * GraphTraversal} is spawned from a {@code GraphTraversalSource}. To be clear, the "g" in {@code
 * g.V()} is a {@code GraphTraversalSource} and the {@code V()} is a start step. To include DSL-based start steps on a custom {@code GraphTraversalSource} the
 * "traversalSource" parameter is supplied to the {@code GremlinDsl} annotation which specifies the fully qualified name of the class that contains those DSL-based start
 * steps.
 *
 * @author julian
 */
@GremlinDsl(traversalSource = "de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSourceDsl")
public interface CrymlinTraversalDsl<S, E> extends GraphTraversal.Admin<S, E> {

	default CrymlinTraversalDsl<S, Vertex> argument(int i) {
		return (CrymlinTraversalDsl<S, Vertex>) out(CrymlinConstants.ARGUMENTS).has(CrymlinConstants.ARGUMENT_INDEX, i);
	}

	@GremlinDsl.AnonymousMethod(returnTypeParameters = { "A", "A" }, // c/p from example, unclear.
			methodTypeParameters = { "A" })
	default GraphTraversal<S, E> literals() {
		return hasLabel(LabelP.of(Literal.class.getSimpleName()));
	}

	/**
	 * Returns nodes with a label {@code VariableDeclaration}.
	 *
	 * @return
	 */
	@GremlinDsl.AnonymousMethod(returnTypeParameters = { "A", "A" }, // c/p from example, unclear.
			methodTypeParameters = { "A", "B" })
	default CrymlinTraversal<S, E> variables() {
		return (CrymlinTraversal<S, E>) hasLabel(LabelP.of(VariableDeclaration.class.getSimpleName()));
	}

	/**
	 * Shortcut for {@code .values("name")}.
	 *
	 * @return
	 */
	@GremlinDsl.AnonymousMethod(returnTypeParameters = { "A", "Object" }, // c/p from example, unclear.
			methodTypeParameters = { "A" })
	@ShellCommand("Name of the selected node(s)")
	default CrymlinTraversal<S, Object> name() {
		return (CrymlinTraversal<S, Object>) values("name");
	}

	/**
	 * Shortcut for {@code .values("file")}.
	 *
	 * @return
	 */
	@GremlinDsl.AnonymousMethod(returnTypeParameters = { "A", "Object" }, // c/p from example, unclear.
			methodTypeParameters = { "A" })
	default CrymlinTraversal<S, Object> file() {
		return (CrymlinTraversal<S, Object>) values("file");
	}

	/**
	 * Shortcut for {@code .values("startLine")}.
	 *
	 * @return
	 */
	@GremlinDsl.AnonymousMethod(returnTypeParameters = { "A", "Object" }, // c/p from example, unclear.
			methodTypeParameters = { "A" })
	@ShellCommand("Source code line of the selected node(s)")
	default CrymlinTraversal<S, Object> line() {
		return (CrymlinTraversal<S, Object>) this.values("startLine");
	}

	/**
	 * Shortcut for {@code .values("comment")}.
	 *
	 * @return
	 */
	@GremlinDsl.AnonymousMethod(returnTypeParameters = { "A", "Object" }, // c/p from example, unclear.
			methodTypeParameters = { "A" })
	@ShellCommand("Comments attached to the selected node(s)")
	default CrymlinTraversal<S, Object> comment() {
		return (CrymlinTraversal<S, Object>) values("comment");
	}

	/**
	 * Shortcut for {@code .values("code")}.
	 *
	 * @return
	 */
	@GremlinDsl.AnonymousMethod(returnTypeParameters = { "A", "Object" }, // c/p from example, unclear.
			methodTypeParameters = { "A" })
	@ShellCommand("Original source code of the selected node(s)")
	default CrymlinTraversal<S, Object> code() {
		return (CrymlinTraversal<S, Object>) values("code");
	}

	/**
	 * Shortcut for {@code .out("CONDITION")}.
	 *
	 * @return
	 */
	@GremlinDsl.AnonymousMethod(returnTypeParameters = { "A", "Vertex" }, methodTypeParameters = { "A" })
	@ShellCommand("Condition (of an IfStatement)")
	default CrymlinTraversal<S, Vertex> condition() {
		return (CrymlinTraversal<S, Vertex>) out("CONDITION");
	}

	/**
	 * Shortcut for {@code .out("CONDITION")}.
	 *
	 * @return
	 */
	@GremlinDsl.AnonymousMethod(returnTypeParameters = { "A", "Vertex" }, methodTypeParameters = { "A" })
	@ShellCommand("Condition (of an IfStatement) containing the given substring")
	default CrymlinTraversal<S, Vertex> condition(String subcode) {

		return (CrymlinTraversal<S, Vertex>) out("CONDITION").has("code");
	}

	/**
	 * Shortcut for {@code .out("THEN_STATEMENT")}.
	 *
	 * @return
	 */
	@GremlinDsl.AnonymousMethod(returnTypeParameters = { "A", "Vertex" }, methodTypeParameters = { "A" })
	@ShellCommand("'then' block (of an IfStatement)")
	default CrymlinTraversal<S, Vertex> thenStmt() {
		return (CrymlinTraversal<S, Vertex>) out("THEN_STATEMENT");
	}

	/**
	 * Shortcut for {@code .out("ELSE_STATEMENT")}.
	 *
	 * @return
	 */
	@GremlinDsl.AnonymousMethod(returnTypeParameters = { "A", "Vertex" }, methodTypeParameters = { "A" })
	@ShellCommand("'else' block (of an IfStatement)")
	default CrymlinTraversal<S, Vertex> elseStmt() {
		return (CrymlinTraversal<S, Vertex>) out("ELSE_STATEMENT");
	}

	/**
	 * Shortcut for {@code .out("TYPE")}.
	 *
	 * @return
	 */
	@GremlinDsl.AnonymousMethod(returnTypeParameters = { "A", "Vertex" }, methodTypeParameters = { "A" })
	@ShellCommand("Type of the node(s)")
	default CrymlinTraversal<S, Vertex> type() {
		return (CrymlinTraversal<S, Vertex>) out(CrymlinConstants.TYPE);
	}

	/**
	 * Shortcut for {@code .out("CFG")}.
	 *
	 * @return
	 */
	@GremlinDsl.AnonymousMethod(returnTypeParameters = { "A", "Vertex" }, methodTypeParameters = { "A" })
	@ShellCommand("Next node(s) in Control Flow Graph")
	default CrymlinTraversal<S, Vertex> nextCfg() {
		return (CrymlinTraversal<S, Vertex>) out("CFG");
	}

	/**
	 * Shortcut for {@code .in("CFG")}.
	 *
	 * @return
	 */
	@GremlinDsl.AnonymousMethod(returnTypeParameters = { "A", "Vertex" }, methodTypeParameters = { "A" })
	@ShellCommand("Previous node(s) in Control Flow Graph")
	default CrymlinTraversal<S, Vertex> prevCfg() {
		return (CrymlinTraversal<S, Vertex>) in("CFG");
	}

	/**
	 * Shortcut for {@code .out("EOG")}.
	 *
	 * @return
	 */
	@GremlinDsl.AnonymousMethod(returnTypeParameters = { "A", "Vertex" }, methodTypeParameters = { "A" })
	@ShellCommand("Next node(s) in Evaluation Order Graph")
	default CrymlinTraversal<S, Vertex> nextEog() {
		return (CrymlinTraversal<S, Vertex>) out(CrymlinConstants.EOG);
	}

	/**
	 * Shortcut for {@code .in("EOG")}.
	 *
	 * @return
	 */
	@GremlinDsl.AnonymousMethod(returnTypeParameters = { "A", "Vertex" }, methodTypeParameters = { "A" })
	@ShellCommand("Previous node(s) in Evaluation Order Graph")
	default CrymlinTraversal<S, Vertex> prevEog() {
		return (CrymlinTraversal<S, Vertex>) in(CrymlinConstants.EOG);
	}

	/**
	 * Shortcut for {@code .out("BODY")}.
	 *
	 * @return
	 */
	@GremlinDsl.AnonymousMethod(returnTypeParameters = { "A", "Vertex" }, methodTypeParameters = { "A" })
	default CrymlinTraversal<S, Vertex> body() {
		return (CrymlinTraversal<S, Vertex>) out("BODY");
	}

	@GremlinDsl.AnonymousMethod(returnTypeParameters = { "A", "Vertex" }, methodTypeParameters = { "A" })
	default CrymlinTraversal<S, Vertex> initializerVariable() {
		return (CrymlinTraversal<S, Vertex>) in("INITIALIZER").has(T.label, LabelP.of(VariableDeclaration.class.getSimpleName()));
	}

	@GremlinDsl.AnonymousMethod(returnTypeParameters = { "A", "Vertex" }, methodTypeParameters = { "A" })
	default CrymlinTraversal<S, Vertex> lhsVariableOfAssignment() {
		return (CrymlinTraversal<S, Vertex>) in("RHS").where(
			has(T.label, LabelP.of(BinaryOperator.class.getSimpleName())).and().has("operatorCode", "="))
				.out("LHS")
				.has(T.label,
					LabelP.of(VariableDeclaration.class.getSimpleName()));
	}

}
