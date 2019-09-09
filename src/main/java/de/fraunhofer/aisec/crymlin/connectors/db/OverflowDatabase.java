package de.fraunhofer.aisec.crymlin.connectors.db;

import com.google.common.collect.Sets;
import de.fraunhofer.aisec.cpg.graph.Node;
import de.fraunhofer.aisec.cpg.helpers.Benchmark;
import de.fraunhofer.aisec.cpg.helpers.SubgraphWalker;
import io.shiftleft.overflowdb.*;
import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.javatuples.Pair;
import org.neo4j.ogm.annotation.Relationship;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.*;

/**
 * <code></code>Database</code> implementation for OVerflowDB.
 *
 * <p>OverflowDB is Shiftleft's fork of Tinkergraph, which is a more efficient in-memory graph DB
 * which overflows to disk when memory is full.
 */
public class OverflowDatabase implements Database {
  /** Package containing all CPG classes * */
  private static final String CPG_PACKAGE = "de.fraunhofer.aisec.cpg.graph";

  private static OverflowDatabase INSTANCE;
  private final OdbGraph graph;

  // Scan all classes in package
  Reflections reflections;

  private OverflowDatabase() {
    List<ClassLoader> classLoadersList = new LinkedList<>();
    classLoadersList.add(ClasspathHelper.contextClassLoader());
    classLoadersList.add(ClasspathHelper.staticClassLoader());
    reflections =
        new Reflections(
            new ConfigurationBuilder()
                .setScanners(
                    new SubTypesScanner(false /* don't exclude Object.class */),
                    new ResourcesScanner())
                .setUrls(
                    ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
                .addUrls(ClasspathHelper.forJavaClassPath())
                .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(CPG_PACKAGE))));

    Pair<List<NodeFactory<OdbNode>>, List<EdgeFactory<OdbEdge>>> factories = getFactories();
    List<NodeFactory<OdbNode>> nodeFactories = factories.getValue0();
    List<EdgeFactory<OdbEdge>> edgeFactories = factories.getValue1();

    // Delete overflow cache file. Otherwise, OverflowDB will try to initialize the DB from it.
    try {
      Files.deleteIfExists(new File("graph-cache-overflow.bin").toPath());
    } catch (IOException e) {
      e.printStackTrace();
    }
    graph =
        OdbGraph.open(
            OdbConfig.withDefaults()
                .withStorageLocation("graph-cache-overflow.bin") // Overflow file
                .withHeapPercentageThreshold(90), // Threshold for mem-to-disk overflow
            Collections.unmodifiableList(nodeFactories),
            Collections.unmodifiableList(edgeFactories));
  }

  public static OverflowDatabase getInstance() {
    if (INSTANCE == null || INSTANCE.graph.isClosed()) {
      INSTANCE = new OverflowDatabase();
    }
    return INSTANCE;
  }

  @Override
  public boolean connect() {
    // Nothing to do. No remote connection
    return true;
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  @Override
  public <T extends Node> T find(Class<T> clazz, Long id) {
    return (T) graph.traversal().V(id).next(); // TODO cast unchecked. Will get Vertex here
  }

  @Override
  public void saveAll(Collection<? extends Node> list) {
    Benchmark bench = new Benchmark(Neo4jDatabase.class, "save all");
    List<Node> workList = new ArrayList<>(list);

    for (int i = 0; i < workList.size(); i++) {
      final Node n = workList.get(i);
      // Store node
      Vertex v = createNode(n);

      // Store edges of this node
      createEdges(v, n);

      Set<Node> children = SubgraphWalker.getAstChildren(n);
      workList.addAll(children);
    }
    bench.stop();
  }

  private Vertex createNode(Node n) {
    List<String> labels = getLabels(n.getClass());
    return graph.addVertex(T.label, String.join("::", labels));
  }

  private void createEdges(Vertex v, Node n) {
    for (Class<?> c = n.getClass(); c != Object.class; c = c.getSuperclass()) {
      for (Field f : getFieldsIncludingSuperclasses(c)) {
        if (mapsToRelationship(f)) {

          Direction direction = getRelationshipDirection(f);
          String relName = getRelationshipLabel(f);

          if (direction.equals(Direction.OUT) || direction.equals(Direction.BOTH)) {
            try {
              // Create an edge from a field value
              f.setAccessible(true);
              Object x = f.get(n);
              if (x == null) {
                continue;
              }

              if (isCollection(x.getClass())) {
                // Add multiple edges for collections
                Collection coll = ((Collection) x);
                for (Object entry : coll) {
                  if (Node.class.isAssignableFrom(entry.getClass())) {
                    Vertex target = createNode((Node) entry);
                    v.addEdge(relName, target);
                  } else {
                    System.out.println("Found non-Node class in collection: " + f.getName());
                  }
                }

              } else {
                // Add single edge for non-collections
                Vertex target = createNode((Node) x);
                try {
                  v.addEdge(relName, target);
                } catch (RuntimeException e) {
                  System.out.println(
                      "Was adding edge "
                          + relName
                          + " from "
                          + n.getClass()
                          + " to "
                          + x.getClass());
                  e.printStackTrace();
                  throw e;
                }
              }
            } catch (IllegalAccessException e) {
              e.printStackTrace();
            }
          } else {
            // TODO Handle INCOMING relations
          }
        }
      }
    }
  }

  /**
   * Reproduced Neo4J-OGM behavior for mapping fields to relationships (or properties otherwise).
   */
  private boolean mapsToRelationship(Field f) {
    return hasAnnotation(f, Relationship.class) || Node.class.isAssignableFrom(f.getType());
  }

  private boolean isCollection(Class<?> aClass) {
    return Collection.class.isAssignableFrom(aClass);
  }

  private List<String> getLabels(Class c) {
    List<String> labels = new ArrayList<>();
    while (!c.equals(Object.class)) {
      labels.add(c.getSimpleName());
      c = c.getSuperclass();
    }
    return labels;
  }

  @Override
  public void purgeDatabase() {
    graph.traversal().V().remove();
  }

  @Override
  public void close() {
    try {
      graph.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public <U> Iterable<U> search(Class<U> clazz, String query, Map<String, String> parameters) {
    return null;
  }

  @Override
  public long getNumNodes() {
    return graph.traversal().V().count().next();
  }

  @Override
  public void setCancelled() {
    // Nothing to do here.
  }

  /** Generate the Node and Edge factories that are required by OverflowDB. */
  private Pair<List<NodeFactory<OdbNode>>, List<EdgeFactory<OdbEdge>>> getFactories() {
    Set<Class<? extends Node>> allClasses = reflections.getSubTypesOf(Node.class);

    // Make sure to first call createEdgeFactories, which will collect some IN fields needed for
    // createNodeFactories
    Pair<List<EdgeFactory<OdbEdge>>, Map<Class, Set<String>>> edgeFactories =
        createEdgeFactories(allClasses);
    List<NodeFactory<OdbNode>> nodeFactories =
        createNodeFactories(allClasses, edgeFactories.getValue1());

    return new Pair<>(nodeFactories, edgeFactories.getValue0());
  }

  /**
   * Edge factories for all fields with @Relationship annotation
   *
   * @param allClasses Set of classes to create edge factories for.
   * @return a Pair of edge factories and a pre-computed map of classes/IN-Fields.
   */
  private Pair<List<EdgeFactory<OdbEdge>>, Map<Class, Set<String>>> createEdgeFactories(
      @NonNull Set<Class<? extends Node>> allClasses) {
    Map<Class, Set<String>> inFields = new HashMap<>();
    List<EdgeFactory<OdbEdge>> edgeFactories = new ArrayList<>();
    for (Class c : allClasses) {
      for (Field field : getFieldsIncludingSuperclasses(c)) {
        if (!mapsToRelationship(field)) {
          continue;
        }

        /**
         * Handle situation where class A has a field f to class B:
         *
         * B and all of its subclasses need to accept INCOMING edges labeled "f".
         */
        // TODO still contains collections.
        final Set<String> EMPTY_SET = new HashSet<>();
        if (getRelationshipDirection(field).equals(Direction.OUT)) {
          List<Class> classesWithIncomingEdge = new ArrayList<>();
          classesWithIncomingEdge.add(field.getType());
          for (int i = 0; i < classesWithIncomingEdge.size(); i++) {
            Class subclass = classesWithIncomingEdge.get(i);
            String relName = getRelationshipLabel(field);
            if (inFields.getOrDefault(subclass, EMPTY_SET).contains(relName)) {
              continue;
            }
//            System.out.println(
//                "Remembering IN edge "
//                    + relName
//                    + " for "
//                    + subclass.getSimpleName() + " (seen in "+c.getSimpleName()+")");
            if (!inFields.containsKey(subclass)) {
              inFields.put(subclass, new HashSet<>());
            }
            inFields.get(subclass).add(relName);
            classesWithIncomingEdge.addAll(reflections.getSubTypesOf(subclass));
          }
        }

        EdgeFactory<OdbEdge> edgeFactory =
            new EdgeFactory<OdbEdge>() {
              @Override
              public String forLabel() {
                return getRelationshipLabel(field);
              }

              @Override
              public OdbEdge createEdge(OdbGraph graph, NodeRef outNode, NodeRef inNode) {
                return new OdbEdge(
                    graph, getRelationshipLabel(field), outNode, inNode, Sets.newHashSet()) {};
              }
            };
        edgeFactories.add(edgeFactory);
      }
    }
    return new Pair<>(edgeFactories, inFields);
  }

  private List<Field> getFieldsIncludingSuperclasses(Class c) {
    List<Field> fields = new ArrayList<>();
    for (; !c.equals(Object.class); c = c.getSuperclass()) {
      fields.addAll(Arrays.asList(c.getDeclaredFields()));
    }
    return fields;
  }

  private String getRelationshipLabel(Field f) {
    String relName = f.getName();
    if (hasAnnotation(f, Relationship.class)) {
      Relationship rel =
          (Relationship)
              Arrays.stream(f.getAnnotations())
                  .filter(a -> a.annotationType().equals(Relationship.class))
                  .findFirst()
                  .get();
      relName = rel.value().trim().isEmpty() ? f.getName() : rel.value();
    }
    return relName;
  }

  /**
   * For each class that should become a node in the graph, we must register a NodeFactory and for
   * each edge we must register an EdgeFactory. The factories provide labels and properties,
   * according to the field names and/or their annotations.
   *
   * @param allClasses classes to create node factories for.
   * @param inFields Map from class names to IN fields which must be supported by that class. Will
   *     be collected by <code>createEdgeFactories</code>
   */
  private List<NodeFactory<OdbNode>> createNodeFactories(
      @NonNull Set<Class<? extends Node>> allClasses, @NonNull Map<Class, Set<String>> inFields) {
    List<NodeFactory<OdbNode>> nodeFactories = new ArrayList<>();
    for (Class<? extends Node> c : allClasses) {
      nodeFactories.add(createNodeFactory(c, inFields));
    }
    return nodeFactories;
  }

  private NodeFactory<OdbNode> createNodeFactory(
      @NonNull Class<? extends Node> c, @NonNull Map<Class, Set<String>> inFields) {
    return new NodeFactory<>() {
      @Override
      public String forLabel() {
        return String.join("::", getLabels(c));
      }

      @Override
      public OdbNode createNode(NodeRef<OdbNode> ref) {
        return new OdbNode(ref) {
          private Map<String, Object> propertyValues;

          {
            // All fields which are no relationships will become properties.
            propertyValues = new HashMap<>();
            for (Field f : getFieldsIncludingSuperclasses(c)) {
              if (!mapsToRelationship(f)) {
                propertyValues.put(f.getName(), propertyValues.get(f.getName()));
              }
            }
          }

          /** All fields annotated with <code></code>@Relationship</code> will become edges. */
          @Override
          protected NodeLayoutInformation layoutInformation() {
            Pair<List<EdgeLayoutInformation>, List<EdgeLayoutInformation>> inAndOut =
                getInAndOutFields(c);

            List<EdgeLayoutInformation> out = inAndOut.getValue1();
            List<EdgeLayoutInformation> in = inAndOut.getValue0();

            for (String relName : inFields.getOrDefault(c, new HashSet<>())) {
              if (relName != null) {
                in.add(new EdgeLayoutInformation(relName, new HashSet<>()));
              }
            }

            return new NodeLayoutInformation(
                Sets.newHashSet(), // TODO edge properties currently not supported
                out,
                in);
          }

          @Override
          protected <V> Iterator<VertexProperty<V>> specificProperties(String key) {
            Iterator<VertexProperty<V>> it =
                IteratorUtils.of(new OdbNodeProperty(this, key, this.propertyValues.get(key)));
            return it;
          }

          @Override
          public Map<String, Object> valueMap() {
            return propertyValues;
          }

          @Override
          protected <V> VertexProperty<V> updateSpecificProperty(
              VertexProperty.Cardinality cardinality, String key, V value) {
            this.propertyValues.put(key, value);
            // TODO create VertexProperty from propertyValues
            return null;
          }

          @Override
          protected void removeSpecificProperty(String key) {
            propertyValues.remove(key);
          }
        };
      }

      @Override
      public NodeRef createNodeRef(OdbGraph graph, long id) {
        return new NodeRef(graph, id) {
          @Override
          public String label() {
            return c.getSimpleName();
          }
        };
      }
    };
  }

  private Pair<List<EdgeLayoutInformation>, List<EdgeLayoutInformation>> getInAndOutFields(
      Class c) {
    List<EdgeLayoutInformation> inFields = new ArrayList<>();
    List<EdgeLayoutInformation> outFields = new ArrayList<>();

    for (Field f : getFieldsIncludingSuperclasses(c)) {
      if (mapsToRelationship(f)) {
        String relName = getRelationshipLabel(f);
        Direction dir = getRelationshipDirection(f);
        if (dir.equals(Direction.IN)) {
          inFields.add(new EdgeLayoutInformation(relName, new HashSet<>()));
        } else if (dir.equals(Direction.OUT) || dir.equals(Direction.BOTH)) {
          outFields.add(new EdgeLayoutInformation(relName, new HashSet<>()));

          // Note that each target of an OUT field must also be registered as an IN field
        }
      }
    }

    return new Pair<>(inFields, outFields);
  }

  private boolean hasAnnotation(Field f, Class annotationClass) {
    return Arrays.stream(f.getAnnotations())
        .anyMatch(a -> a.annotationType().equals(annotationClass));
  }

  public Graph getGraph() {
    return this.graph;
  }

  private Direction getRelationshipDirection(Field f) {
    Direction direction = Direction.OUT;
    if (hasAnnotation(f, Relationship.class)) {
      Relationship rel =
          (Relationship)
              Arrays.stream(f.getAnnotations())
                  .filter(a -> a.annotationType().equals(Relationship.class))
                  .findFirst()
                  .get();
      switch (rel.direction()) {
        case Relationship.INCOMING:
          direction = Direction.IN;
          break;
        case Relationship.UNDIRECTED:
          direction = Direction.BOTH;
          break;
        default:
          direction = Direction.OUT;
      }
    }
    return direction;
  }
}
