package de.fraunhofer.aisec.crymlin.dsl;

import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.process.computer.Computer;
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer;
import org.apache.tinkerpop.gremlin.process.remote.RemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.AddEdgeStartStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.AddVertexStartStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.GraphStep;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class CrymlinTraversalSource extends CrymlinTraversalSourceDsl {
  public CrymlinTraversalSource(Graph graph) {
    super(graph);
  }

  public CrymlinTraversalSource(Graph graph, TraversalStrategies strategies) {
    super(graph, strategies);
  }

  @Override
  public CrymlinTraversalSource clone() {
    return (CrymlinTraversalSource) super.clone();
  }

  @Override
  public CrymlinTraversalSource withStrategies(TraversalStrategy... traversalStrategies) {
    return (CrymlinTraversalSource) super.withStrategies(traversalStrategies);
  }

  @Override
  public CrymlinTraversalSource withoutStrategies(Class<? extends TraversalStrategy>... traversalStrategyClasses) {
    return (CrymlinTraversalSource) super.withoutStrategies(traversalStrategyClasses);
  }

  @Override
  public CrymlinTraversalSource withComputer(Computer computer) {
    return (CrymlinTraversalSource) super.withComputer(computer);
  }

  @Override
  public CrymlinTraversalSource withComputer(Class<? extends GraphComputer> graphComputerClass) {
    return (CrymlinTraversalSource) super.withComputer(graphComputerClass);
  }

  @Override
  public CrymlinTraversalSource withComputer() {
    return (CrymlinTraversalSource) super.withComputer();
  }

  @Override
  public <A> CrymlinTraversalSource withSideEffect(String key, Supplier<A> initialValue,
      BinaryOperator<A> reducer) {
    return (CrymlinTraversalSource) super.withSideEffect(key,initialValue,reducer);
  }

  @Override
  public <A> CrymlinTraversalSource withSideEffect(String key, A initialValue,
      BinaryOperator<A> reducer) {
    return (CrymlinTraversalSource) super.withSideEffect(key,initialValue,reducer);
  }

  @Override
  public <A> CrymlinTraversalSource withSideEffect(String key, A initialValue) {
    return (CrymlinTraversalSource) super.withSideEffect(key,initialValue);
  }

  @Override
  public <A> CrymlinTraversalSource withSideEffect(String key, Supplier<A> initialValue) {
    return (CrymlinTraversalSource) super.withSideEffect(key,initialValue);
  }

  @Override
  public <A> CrymlinTraversalSource withSack(Supplier<A> initialValue,
      UnaryOperator<A> splitOperator, BinaryOperator<A> mergeOperator) {
    return (CrymlinTraversalSource) super.withSack(initialValue,splitOperator,mergeOperator);
  }

  @Override
  public <A> CrymlinTraversalSource withSack(A initialValue, UnaryOperator<A> splitOperator,
      BinaryOperator<A> mergeOperator) {
    return (CrymlinTraversalSource) super.withSack(initialValue,splitOperator,mergeOperator);
  }

  @Override
  public <A> CrymlinTraversalSource withSack(A initialValue) {
    return (CrymlinTraversalSource) super.withSack(initialValue);
  }

  @Override
  public <A> CrymlinTraversalSource withSack(Supplier<A> initialValue) {
    return (CrymlinTraversalSource) super.withSack(initialValue);
  }

  @Override
  public <A> CrymlinTraversalSource withSack(Supplier<A> initialValue,
      UnaryOperator<A> splitOperator) {
    return (CrymlinTraversalSource) super.withSack(initialValue,splitOperator);
  }

  @Override
  public <A> CrymlinTraversalSource withSack(A initialValue, UnaryOperator<A> splitOperator) {
    return (CrymlinTraversalSource) super.withSack(initialValue,splitOperator);
  }

  @Override
  public <A> CrymlinTraversalSource withSack(Supplier<A> initialValue,
      BinaryOperator<A> mergeOperator) {
    return (CrymlinTraversalSource) super.withSack(initialValue,mergeOperator);
  }

  @Override
  public <A> CrymlinTraversalSource withSack(A initialValue, BinaryOperator<A> mergeOperator) {
    return (CrymlinTraversalSource) super.withSack(initialValue,mergeOperator);
  }

  @Override
  public CrymlinTraversalSource withBulk(boolean useBulk) {
    return (CrymlinTraversalSource) super.withBulk(useBulk);
  }

  @Override
  public CrymlinTraversalSource withPath() {
    return (CrymlinTraversalSource) super.withPath();
  }

  @Override
  public CrymlinTraversalSource withRemote(Configuration conf) {
    return (CrymlinTraversalSource) super.withRemote(conf);
  }

  @Override
  public CrymlinTraversalSource withRemote(String configFile) throws Exception {
    return (CrymlinTraversalSource) super.withRemote(configFile);
  }

  @Override
  public CrymlinTraversalSource withRemote(RemoteConnection connection) {
    return (CrymlinTraversalSource) super.withRemote(connection);
  }

  @Override
  public CrymlinTraversal<Vertex, Vertex> variableDeclarations() {
    CrymlinTraversalSource clone = this.clone();
    return new DefaultCrymlinTraversal (clone, super.variableDeclarations().asAdmin());
  }

  @Override
  public CrymlinTraversal<Vertex, Vertex> calls() {
    CrymlinTraversalSource clone = this.clone();
    return new DefaultCrymlinTraversal (clone, super.calls().asAdmin());
  }

  @Override
  public CrymlinTraversal<Vertex, Vertex> calls(String callee_name) {
    CrymlinTraversalSource clone = this.clone();
    return new DefaultCrymlinTraversal (clone, super.calls(callee_name).asAdmin());
  }

  @Override
  public CrymlinTraversal<Vertex, Vertex> methods() {
    CrymlinTraversalSource clone = this.clone();
    return new DefaultCrymlinTraversal (clone, super.methods().asAdmin());
  }

  @Override
  public CrymlinTraversal<Vertex, Vertex> cipherListSetterCalls() {
    CrymlinTraversalSource clone = this.clone();
    return new DefaultCrymlinTraversal (clone, super.cipherListSetterCalls().asAdmin());
  }

  @Override
  public CrymlinTraversal<Vertex, Vertex> translationunits() {
    CrymlinTraversalSource clone = this.clone();
    return new DefaultCrymlinTraversal (clone, super.translationunits().asAdmin());
  }

  @Override
  public CrymlinTraversal<Vertex, Vertex> recorddeclarations() {
    CrymlinTraversalSource clone = this.clone();
    return new DefaultCrymlinTraversal (clone, super.recorddeclarations().asAdmin());
  }

  @Override
  public CrymlinTraversal<Vertex, Vertex> recorddeclaration(String recordname) {
    CrymlinTraversalSource clone = this.clone();
    return new DefaultCrymlinTraversal (clone, super.recorddeclaration(recordname).asAdmin());
  }

  @Override
  public CrymlinTraversal<Vertex, Vertex> functiondeclarations() {
    CrymlinTraversalSource clone = this.clone();
    return new DefaultCrymlinTraversal (clone, super.functiondeclarations().asAdmin());
  }

  @Override
  public CrymlinTraversal<Vertex, Vertex> functiondeclaration(String functionname) {
    CrymlinTraversalSource clone = this.clone();
    return new DefaultCrymlinTraversal (clone, super.functiondeclaration(functionname).asAdmin());
  }

  @Override
  public CrymlinTraversal<Vertex, Vertex> declarations() {
    CrymlinTraversalSource clone = this.clone();
    return new DefaultCrymlinTraversal (clone, super.declarations().asAdmin());
  }

  @Override
  public CrymlinTraversal<Vertex, Vertex> addV() {
    CrymlinTraversalSource clone = this.clone();
    clone.getBytecode().addStep(GraphTraversal.Symbols.addV);
    DefaultCrymlinTraversal traversal = new DefaultCrymlinTraversal(clone);
    return (CrymlinTraversal) traversal.asAdmin().addStep(new AddVertexStartStep(traversal, (String) null));
  }

  @Override
  public CrymlinTraversal<Vertex, Vertex> addV(String label) {
    CrymlinTraversalSource clone = this.clone();
    clone.getBytecode().addStep(GraphTraversal.Symbols.addV, label);
    DefaultCrymlinTraversal traversal = new DefaultCrymlinTraversal(clone);
    return (CrymlinTraversal) traversal.asAdmin().addStep(new AddVertexStartStep(traversal, label));
  }

  @Override
  public CrymlinTraversal<Vertex, Vertex> addV(Traversal vertexLabelTraversal) {
    CrymlinTraversalSource clone = this.clone();
    clone.getBytecode().addStep(GraphTraversal.Symbols.addV, vertexLabelTraversal);
    DefaultCrymlinTraversal traversal = new DefaultCrymlinTraversal(clone);
    return (CrymlinTraversal) traversal.asAdmin().addStep(new AddVertexStartStep(traversal, vertexLabelTraversal));
  }

  @Override
  public CrymlinTraversal<Edge, Edge> addE(String label) {
    CrymlinTraversalSource clone = this.clone();
    clone.getBytecode().addStep(GraphTraversal.Symbols.addV, label);
    DefaultCrymlinTraversal traversal = new DefaultCrymlinTraversal(clone);
    return (CrymlinTraversal) traversal.asAdmin().addStep(new AddEdgeStartStep(traversal, label));
  }

  @Override
  public CrymlinTraversal<Edge, Edge> addE(Traversal edgeLabelTraversal) {
    CrymlinTraversalSource clone = this.clone();
    clone.getBytecode().addStep(GraphTraversal.Symbols.addV, edgeLabelTraversal);
    DefaultCrymlinTraversal traversal = new DefaultCrymlinTraversal(clone);
    return (CrymlinTraversal) traversal.asAdmin().addStep(new AddEdgeStartStep(traversal, edgeLabelTraversal));
  }

  @Override
  public CrymlinTraversal<Vertex, Vertex> V(Object... vertexIds) {
    CrymlinTraversalSource clone = this.clone();
    clone.getBytecode().addStep(GraphTraversal.Symbols.V, vertexIds);
    DefaultCrymlinTraversal traversal = new DefaultCrymlinTraversal(clone);
    return (CrymlinTraversal) traversal.asAdmin().addStep(new GraphStep(traversal, Vertex.class, true, vertexIds));
  }

  @Override
  public CrymlinTraversal<Edge, Edge> E(Object... edgeIds) {
    CrymlinTraversalSource clone = this.clone();
    clone.getBytecode().addStep(GraphTraversal.Symbols.E, edgeIds);
    DefaultCrymlinTraversal traversal = new DefaultCrymlinTraversal(clone);
    return (CrymlinTraversal) traversal.asAdmin().addStep(new GraphStep(traversal, Edge.class, true, edgeIds));
  }

  @Override
  public Optional<Class> getAnonymousTraversalClass() {
    return Optional.of(__.class);
  }
}
