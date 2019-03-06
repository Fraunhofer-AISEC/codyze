package de.fraunhofer.aisec.crymlin;

import java.lang.Double;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Number;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.tinkerpop.gremlin.process.computer.VertexProgram;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Pop;
import org.apache.tinkerpop.gremlin.process.traversal.Scope;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.Tree;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.util.TraverserSet;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalMetrics;
import org.apache.tinkerpop.gremlin.structure.Column;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

public interface CrymlinTraversal<S, E> extends CrymlinTraversalDsl<S, E> {
  @Override
  default CrymlinTraversal<S, Vertex> argument(int i) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.argument(i);
  }

  @Override
  default CrymlinTraversal<S, E> literals() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.literals();
  }

  @Override
  default <E2> CrymlinTraversal<S, E2> map(Function<Traverser<E>, E2> function) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.map(function);
  }

  @Override
  default <E2> CrymlinTraversal<S, E2> map(Traversal<?, E2> mapTraversal) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.map(mapTraversal);
  }

  @Override
  default <E2> CrymlinTraversal<S, E2> flatMap(Function<Traverser<E>, Iterator<E2>> function) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.flatMap(function);
  }

  @Override
  default <E2> CrymlinTraversal<S, E2> flatMap(Traversal<?, E2> flatMapTraversal) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.flatMap(flatMapTraversal);
  }

  @Override
  default CrymlinTraversal<S, Object> id() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.id();
  }

  @Override
  default CrymlinTraversal<S, String> label() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.label();
  }

  @Override
  default CrymlinTraversal<S, E> identity() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.identity();
  }

  @Override
  default <E2> CrymlinTraversal<S, E2> constant(E2 e) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.constant(e);
  }

  @Override
  default CrymlinTraversal<S, Vertex> V(Object... vertexIdsOrElements) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.V(vertexIdsOrElements);
  }

  @Override
  default CrymlinTraversal<S, Vertex> to(Direction direction, String... edgeLabels) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.to(direction,edgeLabels);
  }

  @Override
  default CrymlinTraversal<S, Vertex> out(String... edgeLabels) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.out(edgeLabels);
  }

  @Override
  default CrymlinTraversal<S, Vertex> in(String... edgeLabels) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.in(edgeLabels);
  }

  @Override
  default CrymlinTraversal<S, Vertex> both(String... edgeLabels) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.both(edgeLabels);
  }

  @Override
  default CrymlinTraversal<S, Edge> toE(Direction direction, String... edgeLabels) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.toE(direction,edgeLabels);
  }

  @Override
  default CrymlinTraversal<S, Edge> outE(String... edgeLabels) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.outE(edgeLabels);
  }

  @Override
  default CrymlinTraversal<S, Edge> inE(String... edgeLabels) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.inE(edgeLabels);
  }

  @Override
  default CrymlinTraversal<S, Edge> bothE(String... edgeLabels) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.bothE(edgeLabels);
  }

  @Override
  default CrymlinTraversal<S, Vertex> toV(Direction direction) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.toV(direction);
  }

  @Override
  default CrymlinTraversal<S, Vertex> inV() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.inV();
  }

  @Override
  default CrymlinTraversal<S, Vertex> outV() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.outV();
  }

  @Override
  default CrymlinTraversal<S, Vertex> bothV() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.bothV();
  }

  @Override
  default CrymlinTraversal<S, Vertex> otherV() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.otherV();
  }

  @Override
  default CrymlinTraversal<S, E> order() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.order();
  }

  @Override
  default CrymlinTraversal<S, E> order(Scope scope) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.order(scope);
  }

  @Override
  default <E2> CrymlinTraversal<S, ? extends Property<E2>> properties(String... propertyKeys) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.properties(propertyKeys);
  }

  @Override
  default <E2> CrymlinTraversal<S, E2> values(String... propertyKeys) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.values(propertyKeys);
  }

  @Override
  default <E2> CrymlinTraversal<S, Map<String, E2>> propertyMap(String... propertyKeys) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.propertyMap(propertyKeys);
  }

  @Override
  default <E2> CrymlinTraversal<S, Map<String, E2>> valueMap(String... propertyKeys) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.valueMap(propertyKeys);
  }

  @Override
  default <E2> CrymlinTraversal<S, Map<Object, E2>> valueMap(boolean includeTokens,
      String... propertyKeys) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.valueMap(includeTokens,propertyKeys);
  }

  @Override
  default CrymlinTraversal<S, String> key() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.key();
  }

  @Override
  default <E2> CrymlinTraversal<S, E2> value() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.value();
  }

  @Override
  default CrymlinTraversal<S, Path> path() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.path();
  }

  @Override
  default <E2> CrymlinTraversal<S, Map<String, E2>> match(Traversal<?, ?>... matchTraversals) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.match(matchTraversals);
  }

  @Override
  default <E2> CrymlinTraversal<S, E2> sack() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.sack();
  }

  @Override
  default CrymlinTraversal<S, Integer> loops() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.loops();
  }

  @Override
  default <E2> CrymlinTraversal<S, Map<String, E2>> project(String projectKey,
      String... otherProjectKeys) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.project(projectKey,otherProjectKeys);
  }

  @Override
  default <E2> CrymlinTraversal<S, Map<String, E2>> select(Pop pop, String selectKey1,
      String selectKey2, String... otherSelectKeys) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.select(pop,selectKey1,selectKey2,otherSelectKeys);
  }

  @Override
  default <E2> CrymlinTraversal<S, Map<String, E2>> select(String selectKey1, String selectKey2,
      String... otherSelectKeys) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.select(selectKey1,selectKey2,otherSelectKeys);
  }

  @Override
  default <E2> CrymlinTraversal<S, E2> select(Pop pop, String selectKey) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.select(pop,selectKey);
  }

  @Override
  default <E2> CrymlinTraversal<S, E2> select(String selectKey) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.select(selectKey);
  }

  @Override
  default <E2> CrymlinTraversal<S, E2> select(Pop pop, Traversal<S, E2> keyTraversal) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.select(pop,keyTraversal);
  }

  @Override
  default <E2> CrymlinTraversal<S, E2> select(Traversal<S, E2> keyTraversal) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.select(keyTraversal);
  }

  @Override
  default <E2> CrymlinTraversal<S, Collection<E2>> select(Column column) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.select(column);
  }

  @Override
  default <E2> CrymlinTraversal<S, E2> unfold() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.unfold();
  }

  @Override
  default CrymlinTraversal<S, List<E>> fold() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.fold();
  }

  @Override
  default <E2> CrymlinTraversal<S, E2> fold(E2 seed, BiFunction<E2, E, E2> foldFunction) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.fold(seed,foldFunction);
  }

  @Override
  default CrymlinTraversal<S, Long> count() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.count();
  }

  @Override
  default CrymlinTraversal<S, Long> count(Scope scope) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.count(scope);
  }

  @Override
  default <E2 extends Number> CrymlinTraversal<S, E2> sum() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.sum();
  }

  @Override
  default <E2 extends Number> CrymlinTraversal<S, E2> sum(Scope scope) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.sum(scope);
  }

  @Override
  default <E2 extends Number> CrymlinTraversal<S, E2> max() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.max();
  }

  @Override
  default <E2 extends Number> CrymlinTraversal<S, E2> max(Scope scope) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.max(scope);
  }

  @Override
  default <E2 extends Number> CrymlinTraversal<S, E2> min() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.min();
  }

  @Override
  default <E2 extends Number> CrymlinTraversal<S, E2> min(Scope scope) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.min(scope);
  }

  @Override
  default <E2 extends Number> CrymlinTraversal<S, E2> mean() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.mean();
  }

  @Override
  default <E2 extends Number> CrymlinTraversal<S, E2> mean(Scope scope) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.mean(scope);
  }

  @Override
  default <K, V> CrymlinTraversal<S, Map<K, V>> group() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.group();
  }

  @Override
  default <K> CrymlinTraversal<S, Map<K, Long>> groupCount() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.groupCount();
  }

  @Override
  default CrymlinTraversal<S, Tree> tree() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.tree();
  }

  @Override
  default CrymlinTraversal<S, Vertex> addV(String vertexLabel) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.addV(vertexLabel);
  }

  @Override
  default CrymlinTraversal<S, Vertex> addV(Traversal<?, String> vertexLabelTraversal) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.addV(vertexLabelTraversal);
  }

  @Override
  default CrymlinTraversal<S, Vertex> addV() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.addV();
  }

  @Override
  default CrymlinTraversal<S, Edge> addE(String edgeLabel) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.addE(edgeLabel);
  }

  @Override
  default CrymlinTraversal<S, Edge> addE(Traversal<?, String> edgeLabelTraversal) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.addE(edgeLabelTraversal);
  }

  @Override
  default CrymlinTraversal<S, E> to(String toStepLabel) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.to(toStepLabel);
  }

  @Override
  default CrymlinTraversal<S, E> from(String fromStepLabel) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.from(fromStepLabel);
  }

  @Override
  default CrymlinTraversal<S, E> to(Traversal<?, Vertex> toVertex) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.to(toVertex);
  }

  @Override
  default CrymlinTraversal<S, E> from(Traversal<?, Vertex> fromVertex) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.from(fromVertex);
  }

  @Override
  default CrymlinTraversal<S, E> to(Vertex toVertex) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.to(toVertex);
  }

  @Override
  default CrymlinTraversal<S, E> from(Vertex fromVertex) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.from(fromVertex);
  }

  @Override
  default CrymlinTraversal<S, Double> math(String expression) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.math(expression);
  }

  @Override
  default CrymlinTraversal<S, E> filter(Predicate<Traverser<E>> predicate) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.filter(predicate);
  }

  @Override
  default CrymlinTraversal<S, E> filter(Traversal<?, ?> filterTraversal) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.filter(filterTraversal);
  }

  @Override
  default CrymlinTraversal<S, E> or(Traversal<?, ?>... orTraversals) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.or(orTraversals);
  }

  @Override
  default CrymlinTraversal<S, E> and(Traversal<?, ?>... andTraversals) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.and(andTraversals);
  }

  @Override
  default CrymlinTraversal<S, E> inject(E... injections) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.inject(injections);
  }

  @Override
  default CrymlinTraversal<S, E> dedup(Scope scope, String... dedupLabels) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.dedup(scope,dedupLabels);
  }

  @Override
  default CrymlinTraversal<S, E> dedup(String... dedupLabels) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.dedup(dedupLabels);
  }

  @Override
  default CrymlinTraversal<S, E> where(String startKey, P<String> predicate) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.where(startKey,predicate);
  }

  @Override
  default CrymlinTraversal<S, E> where(P<String> predicate) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.where(predicate);
  }

  @Override
  default CrymlinTraversal<S, E> where(Traversal<?, ?> whereTraversal) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.where(whereTraversal);
  }

  @Override
  default CrymlinTraversal<S, E> has(String propertyKey, P<?> predicate) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.has(propertyKey,predicate);
  }

  @Override
  default CrymlinTraversal<S, E> has(T accessor, P<?> predicate) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.has(accessor,predicate);
  }

  @Override
  default CrymlinTraversal<S, E> has(String propertyKey, Object value) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.has(propertyKey,value);
  }

  @Override
  default CrymlinTraversal<S, E> has(T accessor, Object value) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.has(accessor,value);
  }

  @Override
  default CrymlinTraversal<S, E> has(String label, String propertyKey, P<?> predicate) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.has(label,propertyKey,predicate);
  }

  @Override
  default CrymlinTraversal<S, E> has(String label, String propertyKey, Object value) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.has(label,propertyKey,value);
  }

  @Override
  default CrymlinTraversal<S, E> has(T accessor, Traversal<?, ?> propertyTraversal) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.has(accessor,propertyTraversal);
  }

  @Override
  default CrymlinTraversal<S, E> has(String propertyKey, Traversal<?, ?> propertyTraversal) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.has(propertyKey,propertyTraversal);
  }

  @Override
  default CrymlinTraversal<S, E> has(String propertyKey) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.has(propertyKey);
  }

  @Override
  default CrymlinTraversal<S, E> hasNot(String propertyKey) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.hasNot(propertyKey);
  }

  @Override
  default CrymlinTraversal<S, E> hasLabel(String label, String... otherLabels) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.hasLabel(label,otherLabels);
  }

  @Override
  default CrymlinTraversal<S, E> hasLabel(P<String> predicate) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.hasLabel(predicate);
  }

  @Override
  default CrymlinTraversal<S, E> hasId(Object id, Object... otherIds) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.hasId(id,otherIds);
  }

  @Override
  default CrymlinTraversal<S, E> hasId(P<Object> predicate) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.hasId(predicate);
  }

  @Override
  default CrymlinTraversal<S, E> hasKey(String label, String... otherLabels) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.hasKey(label,otherLabels);
  }

  @Override
  default CrymlinTraversal<S, E> hasKey(P<String> predicate) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.hasKey(predicate);
  }

  @Override
  default CrymlinTraversal<S, E> hasValue(Object value, Object... otherValues) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.hasValue(value,otherValues);
  }

  @Override
  default CrymlinTraversal<S, E> hasValue(P<Object> predicate) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.hasValue(predicate);
  }

  @Override
  default CrymlinTraversal<S, E> is(P<E> predicate) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.is(predicate);
  }

  @Override
  default CrymlinTraversal<S, E> is(Object value) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.is(value);
  }

  @Override
  default CrymlinTraversal<S, E> not(Traversal<?, ?> notTraversal) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.not(notTraversal);
  }

  @Override
  default CrymlinTraversal<S, E> coin(double probability) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.coin(probability);
  }

  @Override
  default CrymlinTraversal<S, E> range(long low, long high) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.range(low,high);
  }

  @Override
  default <E2> CrymlinTraversal<S, E2> range(Scope scope, long low, long high) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.range(scope,low,high);
  }

  @Override
  default CrymlinTraversal<S, E> limit(long limit) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.limit(limit);
  }

  @Override
  default <E2> CrymlinTraversal<S, E2> limit(Scope scope, long limit) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.limit(scope,limit);
  }

  @Override
  default CrymlinTraversal<S, E> tail() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.tail();
  }

  @Override
  default CrymlinTraversal<S, E> tail(long limit) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.tail(limit);
  }

  @Override
  default <E2> CrymlinTraversal<S, E2> tail(Scope scope) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.tail(scope);
  }

  @Override
  default <E2> CrymlinTraversal<S, E2> tail(Scope scope, long limit) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.tail(scope,limit);
  }

  @Override
  default CrymlinTraversal<S, E> skip(long skip) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.skip(skip);
  }

  @Override
  default <E2> CrymlinTraversal<S, E2> skip(Scope scope, long skip) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.skip(scope,skip);
  }

  @Override
  default CrymlinTraversal<S, E> timeLimit(long timeLimit) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.timeLimit(timeLimit);
  }

  @Override
  default CrymlinTraversal<S, E> simplePath() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.simplePath();
  }

  @Override
  default CrymlinTraversal<S, E> cyclicPath() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.cyclicPath();
  }

  @Override
  default CrymlinTraversal<S, E> sample(int amountToSample) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.sample(amountToSample);
  }

  @Override
  default CrymlinTraversal<S, E> sample(Scope scope, int amountToSample) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.sample(scope,amountToSample);
  }

  @Override
  default CrymlinTraversal<S, E> drop() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.drop();
  }

  @Override
  default CrymlinTraversal<S, E> sideEffect(Consumer<Traverser<E>> consumer) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.sideEffect(consumer);
  }

  @Override
  default CrymlinTraversal<S, E> sideEffect(Traversal<?, ?> sideEffectTraversal) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.sideEffect(sideEffectTraversal);
  }

  @Override
  default <E2> CrymlinTraversal<S, E2> cap(String sideEffectKey, String... sideEffectKeys) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.cap(sideEffectKey,sideEffectKeys);
  }

  @Override
  default CrymlinTraversal<S, Edge> subgraph(String sideEffectKey) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.subgraph(sideEffectKey);
  }

  @Override
  default CrymlinTraversal<S, E> aggregate(String sideEffectKey) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.aggregate(sideEffectKey);
  }

  @Override
  default CrymlinTraversal<S, E> group(String sideEffectKey) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.group(sideEffectKey);
  }

  @Override
  default CrymlinTraversal<S, E> groupCount(String sideEffectKey) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.groupCount(sideEffectKey);
  }

  @Override
  default CrymlinTraversal<S, E> tree(String sideEffectKey) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.tree(sideEffectKey);
  }

  @Override
  default <V, U> CrymlinTraversal<S, E> sack(BiFunction<V, U, V> sackOperator) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.sack(sackOperator);
  }

  @Override
  default CrymlinTraversal<S, E> store(String sideEffectKey) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.store(sideEffectKey);
  }

  @Override
  default CrymlinTraversal<S, E> profile(String sideEffectKey) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.profile(sideEffectKey);
  }

  @Override
  default CrymlinTraversal<S, TraversalMetrics> profile() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.profile();
  }

  @Override
  default CrymlinTraversal<S, E> property(VertexProperty.Cardinality cardinality, Object key,
      Object value, Object... keyValues) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.property(cardinality,key,value,keyValues);
  }

  @Override
  default CrymlinTraversal<S, E> property(Object key, Object value, Object... keyValues) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.property(key,value,keyValues);
  }

  @Override
  default <M, E2> CrymlinTraversal<S, E2> branch(Traversal<?, M> branchTraversal) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.branch(branchTraversal);
  }

  @Override
  default <M, E2> CrymlinTraversal<S, E2> branch(Function<Traverser<E>, M> function) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.branch(function);
  }

  @Override
  default <M, E2> CrymlinTraversal<S, E2> choose(Traversal<?, M> choiceTraversal) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.choose(choiceTraversal);
  }

  @Override
  default <E2> CrymlinTraversal<S, E2> choose(Traversal<?, ?> traversalPredicate,
      Traversal<?, E2> trueChoice, Traversal<?, E2> falseChoice) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.choose(traversalPredicate,trueChoice,falseChoice);
  }

  @Override
  default <E2> CrymlinTraversal<S, E2> choose(Traversal<?, ?> traversalPredicate,
      Traversal<?, E2> trueChoice) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.choose(traversalPredicate,trueChoice);
  }

  @Override
  default <M, E2> CrymlinTraversal<S, E2> choose(Function<E, M> choiceFunction) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.choose(choiceFunction);
  }

  @Override
  default <E2> CrymlinTraversal<S, E2> choose(Predicate<E> choosePredicate,
      Traversal<?, E2> trueChoice, Traversal<?, E2> falseChoice) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.choose(choosePredicate,trueChoice,falseChoice);
  }

  @Override
  default <E2> CrymlinTraversal<S, E2> choose(Predicate<E> choosePredicate,
      Traversal<?, E2> trueChoice) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.choose(choosePredicate,trueChoice);
  }

  @Override
  default <E2> CrymlinTraversal<S, E2> optional(Traversal<?, E2> optionalTraversal) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.optional(optionalTraversal);
  }

  @Override
  default <E2> CrymlinTraversal<S, E2> union(Traversal<?, E2>... unionTraversals) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.union(unionTraversals);
  }

  @Override
  default <E2> CrymlinTraversal<S, E2> coalesce(Traversal<?, E2>... coalesceTraversals) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.coalesce(coalesceTraversals);
  }

  @Override
  default CrymlinTraversal<S, E> repeat(Traversal<?, E> repeatTraversal) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.repeat(repeatTraversal);
  }

  @Override
  default CrymlinTraversal<S, E> emit(Traversal<?, ?> emitTraversal) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.emit(emitTraversal);
  }

  @Override
  default CrymlinTraversal<S, E> emit(Predicate<Traverser<E>> emitPredicate) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.emit(emitPredicate);
  }

  @Override
  default CrymlinTraversal<S, E> emit() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.emit();
  }

  @Override
  default CrymlinTraversal<S, E> until(Traversal<?, ?> untilTraversal) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.until(untilTraversal);
  }

  @Override
  default CrymlinTraversal<S, E> until(Predicate<Traverser<E>> untilPredicate) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.until(untilPredicate);
  }

  @Override
  default CrymlinTraversal<S, E> times(int maxLoops) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.times(maxLoops);
  }

  @Override
  default <E2> CrymlinTraversal<S, E2> local(Traversal<?, E2> localTraversal) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.local(localTraversal);
  }

  @Override
  default CrymlinTraversal<S, E> pageRank() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.pageRank();
  }

  @Override
  default CrymlinTraversal<S, E> pageRank(double alpha) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.pageRank(alpha);
  }

  @Override
  default CrymlinTraversal<S, E> peerPressure() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.peerPressure();
  }

  @Override
  default CrymlinTraversal<S, E> program(VertexProgram<?> vertexProgram) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.program(vertexProgram);
  }

  @Override
  default CrymlinTraversal<S, E> as(String stepLabel, String... stepLabels) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.as(stepLabel,stepLabels);
  }

  @Override
  default CrymlinTraversal<S, E> barrier() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.barrier();
  }

  @Override
  default CrymlinTraversal<S, E> barrier(int maxBarrierSize) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.barrier(maxBarrierSize);
  }

  @Override
  default CrymlinTraversal<S, E> barrier(Consumer<TraverserSet<Object>> barrierConsumer) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.barrier(barrierConsumer);
  }

  @Override
  default CrymlinTraversal<S, E> by() {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.by();
  }

  @Override
  default CrymlinTraversal<S, E> by(Traversal<?, ?> traversal) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.by(traversal);
  }

  @Override
  default CrymlinTraversal<S, E> by(T token) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.by(token);
  }

  @Override
  default CrymlinTraversal<S, E> by(String key) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.by(key);
  }

  @Override
  default <V> CrymlinTraversal<S, E> by(Function<V, Object> function) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.by(function);
  }

  @Override
  default <V> CrymlinTraversal<S, E> by(Traversal<?, ?> traversal, Comparator<V> comparator) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.by(traversal,comparator);
  }

  @Override
  default CrymlinTraversal<S, E> by(Comparator<E> comparator) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.by(comparator);
  }

  @Override
  default CrymlinTraversal<S, E> by(Order order) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.by(order);
  }

  @Override
  default <V> CrymlinTraversal<S, E> by(String key, Comparator<V> comparator) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.by(key,comparator);
  }

  @Override
  default <U> CrymlinTraversal<S, E> by(Function<U, Object> function, Comparator comparator) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.by(function,comparator);
  }

  @Override
  default <M, E2> CrymlinTraversal<S, E> option(M pickToken, Traversal<?, E2> traversalOption) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.option(pickToken,traversalOption);
  }

  @Override
  default <E2> CrymlinTraversal<S, E> option(Traversal<?, E2> traversalOption) {
    return (CrymlinTraversal) CrymlinTraversalDsl.super.option(traversalOption);
  }

  @Override
  default CrymlinTraversal<S, E> iterate() {
    CrymlinTraversalDsl.super.iterate();
    return this;
  }
}
