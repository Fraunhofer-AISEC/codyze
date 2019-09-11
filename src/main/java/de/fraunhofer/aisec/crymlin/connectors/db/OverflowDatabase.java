package de.fraunhofer.aisec.crymlin.connectors.db;

import com.google.common.collect.Sets;
import de.fraunhofer.aisec.cpg.graph.Node;
import de.fraunhofer.aisec.cpg.helpers.Benchmark;
import de.fraunhofer.aisec.cpg.helpers.SubgraphWalker;
import io.shiftleft.overflowdb.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.*;
import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.javatuples.Pair;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Transient;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.typeconversion.AttributeConverter;
import org.neo4j.ogm.typeconversion.CompositeAttributeConverter;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

/**
 * <code></code>Database</code> implementation for OVerflowDB.
 *
 * <p>OverflowDB is Shiftleft's fork of Tinkergraph, which is a more efficient in-memory graph DB
 * which overflows to disk when memory is full.
 */
public class OverflowDatabase implements Database {
  // persistable property types, taken from Neo4j
  private static final String PRIMITIVES =
      "char,byte,short,int,long,float,double,boolean,char[],byte[],short[],int[],long[],float[],double[],boolean[]";
  private static final String AUTOBOXERS =
      "java.lang.Object"
          + "java.lang.Character"
          + "java.lang.Byte"
          + "java.lang.Short"
          + "java.lang.Integer"
          + "java.lang.Long"
          + "java.lang.Float"
          + "java.lang.Double"
          + "java.lang.Boolean"
          + "java.lang.String"
          + "java.lang.Object[]"
          + "java.lang.Character[]"
          + "java.lang.Byte[]"
          + "java.lang.Short[]"
          + "java.lang.Integer[]"
          + "java.lang.Long[]"
          + "java.lang.Float[]"
          + "java.lang.Double[]"
          + "java.lang.Boolean[]"
          + "java.lang.String[]";

  /** Package containing all CPG classes * */
  private static final String CPG_PACKAGE = "de.fraunhofer.aisec.cpg.graph";

  private static OverflowDatabase INSTANCE;
  private final OdbGraph graph;

  Map<Integer, Long> hashCodeToVertexId = new HashMap<>();

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
    Benchmark bench = new Benchmark(OverflowDatabase.class, "save all");
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

  public Vertex createNode(Node n) {
    List<String> labels = getNodeLabel(n.getClass());
    HashMap<Object, Object> properties = new HashMap<>();

    // Set node label (from its class)
    properties.put(T.label, String.join("::", labels));

    // Set node properties (from field values which are not relationships)
    List<Field> fields = getFieldsIncludingSuperclasses(n.getClass());
    for (Field f : fields) {
      if (!mapsToRelationship(f) && mapsToProperty(f)) {
        try {
          f.setAccessible(true);
          Object x = f.get(n);
          if (x == null) {
            continue;
          }
          if (hasAnnotation(f, Convert.class)) {
            properties.putAll(convert(f, x));
          } else if (mapsToProperty(f)) {
            properties.putIfAbsent(f.getName(), x.toString());
          } else {
            System.out.println("Not a property");
          }
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }
      }
    }

    System.out.println("Node " + n.getName());
    for (Map.Entry p : properties.entrySet()) {
      System.out.println(p.getKey() + " -> " + p.getValue());
    }
    System.out.println("---------");

    List<Object> props = new ArrayList<>(properties.size() * 2);
    for (Map.Entry p : properties.entrySet()) {
      props.add(p.getKey());
      props.add(p.getValue());
    }

    // Add types of nodes (names of superclasses) to properties
    List<String> superclasses = getNodeLabel(n.getClass());
    props.add("labels");
    props.add(superclasses);

    // Add hashCode of object so we can easily retrieve a vertex from graph given the node object
    props.add("hashCode");
    props.add(n.hashCode());

    return graph.addVertex(props.toArray());
  }

  private Map<Object, Object> convert(Field f, Object content) {
    try {
      Object converter =
          f.getAnnotation(Convert.class)
              .value()
              .getDeclaredConstructor(new Class[] {})
              .newInstance();
      if (converter instanceof AttributeConverter) {
        // Single attribute will be provided
        return Map.of(f.getName(), ((AttributeConverter) converter).toGraphProperty(content));
      } else if (converter instanceof CompositeAttributeConverter) {
        // Yields a map of properties
        return ((CompositeAttributeConverter) converter).toGraphProperties(content);
      }
    } catch (NoSuchMethodException e) {
      System.err.println("A converter needs to have an empty constructor");
      e.printStackTrace();
    } catch (Exception e) {
      System.err.println("Error creating new converter instance");
      e.printStackTrace();
    }

    return Collections.emptyMap();
  }

  public void createEdges(Vertex v, Node n) {
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
                //                System.out.println(n);
                //                System.out.println(f.getName());
                //                System.out.println(x);
                for (Object entry : coll) {
                  //                  System.out.println(entry + " " + entry.hashCode());
                  if (Node.class.isAssignableFrom(entry.getClass())) {
                    Vertex target = null;
                    if (hashCodeToVertexId.containsKey(entry.hashCode())) {
                      Iterator<Vertex> vIt =
                          graph.vertices(hashCodeToVertexId.get(entry.hashCode()));
                      if (vIt.hasNext()) {
                        target = vIt.next();
                      }
                    }
                    if (target == null) {
                      target = createNode((Node) entry);
                      hashCodeToVertexId.put(entry.hashCode(), (long) target.id());
                    }
                    v.addEdge(relName, target);
                  } else {
                    System.out.println("Found non-Node class in collection: " + f.getName());
                  }
                }

              } else {
                // Add single edge for non-collections
                Vertex target = null;
                if (hashCodeToVertexId.containsKey(x.hashCode())) {
                  Iterator<Vertex> vIt = graph.vertices(hashCodeToVertexId.get(x.hashCode()));
                  if (vIt.hasNext()) {
                    target = vIt.next();
                  }
                }
                if (target == null) {
                  target = createNode((Node) x);
                  hashCodeToVertexId.put(x.hashCode(), (long) target.id());
                }

                assert target.property("hashCode").value().equals(x.hashCode());

                v.addEdge(relName, target);
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

  private boolean mapsToProperty(Field f) {
    // Transient fields are not supposed to be persisted
    if (Modifier.isTransient(f.getModifiers()) || hasAnnotation(f, Transient.class)) {
      return false;
    }

    // constant values are not considered properties
    if (Modifier.isFinal(f.getModifiers())) {
      return false;
    }

    // check if we have a converter for this
    if (f.getAnnotation(Convert.class) != null) {
      return true;
    }

    // check whether this is some kind of primitive datatype that seems likely to be a property
    String type = f.getType().getTypeName();
    return PRIMITIVES.contains(type) || AUTOBOXERS.contains(type);
  }

  private boolean isCollection(Class<?> aClass) {
    return Collection.class.isAssignableFrom(aClass);
  }

  public static List<String> getNodeLabel(Class c) {
    List<String> labels = new ArrayList<>();
    while (!c.equals(Object.class)) {
      labels.add(c.getSimpleName());
      c = c.getSuperclass();
    }
    return labels;
  }

  @Override
  public void purgeDatabase() {
    System.out.println(graph.traversal().V().count().next());
    graph.traversal().V().drop();
    System.out.println(graph.traversal().V().count().next());
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
    allClasses.add(Node.class);

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
         * <p>B and all of its subclasses need to accept INCOMING edges labeled "f".
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
        return String.join("::", getNodeLabel(c));
      }

      @Override
      public OdbNode createNode(NodeRef<OdbNode> ref) {
        return new OdbNode(ref) {
          private Map<String, Object> propertyValues = new HashMap<>();

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
                Sets.newHashSet(), // TODO node properties currently not supported
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
