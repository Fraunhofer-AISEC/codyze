package de.fraunhofer.aisec.crymlin;

import java.lang.Override;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.util.DefaultTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;

public class DefaultCrymlinTraversal<S, E> extends DefaultTraversal<S, E> implements CrymlinTraversal<S, E> {
  public DefaultCrymlinTraversal() {
    super();
  }

  public DefaultCrymlinTraversal(Graph graph) {
    super(graph);
  }

  public DefaultCrymlinTraversal(CrymlinTraversalSource traversalSource) {
    super(traversalSource);
  }

  public DefaultCrymlinTraversal(CrymlinTraversalSource traversalSource,
      GraphTraversal.Admin traversal) {
    super(traversalSource, traversal.asAdmin());
  }

  @Override
  public CrymlinTraversal<S, E> iterate() {
    return (CrymlinTraversal) super.iterate();
  }

  @Override
  public GraphTraversal.Admin<S, E> asAdmin() {
    return (GraphTraversal.Admin) super.asAdmin();
  }

  @Override
  public DefaultCrymlinTraversal<S, E> clone() {
    return (DefaultCrymlinTraversal) super.clone();
  }
}
