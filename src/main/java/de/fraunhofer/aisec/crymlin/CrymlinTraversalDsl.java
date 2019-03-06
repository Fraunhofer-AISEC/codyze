package de.fraunhofer.aisec.crymlin;

import static de.fraunhofer.aisec.crymlin.CrymlinTraversalSourceDsl.*;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.GremlinDsl;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
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
@GremlinDsl(traversalSource = "de.fraunhofer.aisec.crymlin.CrymlinTraversalSourceDsl")
public interface CrymlinTraversalDsl<S, E> extends GraphTraversal.Admin<S, E> {

  default GraphTraversal<S, Vertex> ciphers() {
    GraphTraversal<S, Vertex> firstArgument = ((GraphTraversal<S, Vertex>) argument(1));

    // for now just return literals
    return firstArgument;
  }

  default GraphTraversal<S, Vertex> argument(int i) {
    return out(ARGUMENTS).has(ARGUMENT_INDEX, i);
  }

  default GraphTraversal<S, E> literals() {
    return hasLabel(LITERAL);
  }
}
