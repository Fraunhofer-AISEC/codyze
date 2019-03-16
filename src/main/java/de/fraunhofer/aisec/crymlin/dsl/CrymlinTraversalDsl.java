package de.fraunhofer.aisec.crymlin.dsl;

import static de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSourceDsl.ARGUMENTS;
import static de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSourceDsl.ARGUMENT_INDEX;
import static de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSourceDsl.LITERAL;

import de.fraunhofer.aisec.cpg.graph.Statement;
import de.fraunhofer.aisec.cpg.graph.VariableDeclaration;
import de.fraunhofer.aisec.crymlin.server.AnalysisContext;
import de.fraunhofer.aisec.crymlin.server.AnalysisServer;
import java.util.List;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.GremlinDsl;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * Instead of starting a traversal with "g.V().", we start Crymlin with "crymlin.".
 *
 * <p>This is the implementation of a custom DSL for graph traversals. A DSL definition must be an
 * interface and extend {@code GraphTraversal.Admin} and should be annotated with the {@code
 * GremlinDsl} annotation. Methods that are added to this interface become steps that are "appended"
 * to the common steps of the Gremlin language. These methods must:
 *
 * <ul>
 *   <li>Return a {@code GraphTraversal}
 *   <li>Use common Gremlin steps or other DSL steps to compose the returned {@code GraphTraversal}
 * </ul>
 *
 * These methods are only applied to a {@code GraphTraversal}, but recall that a {@code
 * GraphTraversal} is spawned from a {@code GraphTraversalSource}. To be clear, the "g" in {@code
 * g.V()} is a {@code GraphTraversalSource} and the {@code V()} is a start step. To include
 * DSL-based start steps on a custom {@code GraphTraversalSource} the "traversalSource" parameter is
 * supplied to the {@code GremlinDsl} annotation which specifies the fully qualified name of the
 * class that contains those DSL-based start steps.
 *
 * @author julian
 */
@GremlinDsl(traversalSource = "de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSourceDsl")
public interface CrymlinTraversalDsl<S, E> extends GraphTraversal.Admin<S, E> {

  public default CrymlinTraversalDsl<S, Vertex> argument(int i) {
    return (CrymlinTraversalDsl<S, Vertex>) out(ARGUMENTS).has(ARGUMENT_INDEX, i);
  }

  @GremlinDsl.AnonymousMethod(
      returnTypeParameters = {"A", "A"}, // c/p from example, unclear.
      methodTypeParameters = {"A"})
  public default GraphTraversal<S, E> literals() {
    return hasLabel(LITERAL);
  }

  /**
   * Returns nodes with a label {@code VariableDeclaration}.
   *
   * @return
   */
  @GremlinDsl.AnonymousMethod(
      returnTypeParameters = {"A", "B"}, // c/p from example, unclear.
      methodTypeParameters = {"A", "B"})
  public default CrymlinTraversal<S, E> variables() {
    return (CrymlinTraversal<S, E>) hasLabel(VariableDeclaration.class.getSimpleName());
  }

  /**
   * Shortcut for {@code .values("name")}.
   *
   * @return
   */
  @GremlinDsl.AnonymousMethod(
      returnTypeParameters = {"A", "Object"}, // c/p from example, unclear.
      methodTypeParameters = {"A"})
  public default CrymlinTraversal<S, Object> name() {
    return (CrymlinTraversal<S, Object>) values("name");
  }

  /**
   * Shortcut for {@code .values("code")}.
   *
   * @return
   */
  @GremlinDsl.AnonymousMethod(
      returnTypeParameters = {"A", "Object"}, // c/p from example, unclear.
      methodTypeParameters = {"A"})
  public default CrymlinTraversal<S, Object> sourcecode() {
    return (CrymlinTraversal<S, Object>) values("code");
  }

  /**
   * Example of a Crymlin step that operates on the in-memory AnalysisContext and returns its
   * results in form of a GraphTraversal step.
   *
   * @return
   */
  @GremlinDsl.AnonymousMethod(
      returnTypeParameters = {"A", "Vertex"}, // c/p from example, unclear.
      methodTypeParameters = {"A"})
  public default CrymlinTraversal<S, Vertex> statements() {
    AnalysisContext ctx = AnalysisServer.getInstance().retrieveContext();
    if (ctx == null) {
      return (CrymlinTraversal<S, Vertex>) this;
    }
    List<Statement> stmts =
        ctx.methods.get("good.Bouncycastle.main(java.lang.String[])void").getStatements();
    CrymlinTraversal<S, Vertex> t = (CrymlinTraversal<S, Vertex>) this;
    for (Statement stmt : stmts) {
      System.out.println("Adding " + stmt.toString());
      t =
          t.addV()
              .property(T.label, Statement.class.getSimpleName() + stmt.toString())
              .property("code", stmt.getCode())
              .property("region", stmt.getRegion().toString())
              .as(stmt.toString());
    }
    return (CrymlinTraversal<S, Vertex>) t;
  }
}
