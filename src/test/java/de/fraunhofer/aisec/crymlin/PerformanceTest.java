package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.cpg.graph.Node;
import de.fraunhofer.aisec.cpg.helpers.Benchmark;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PerformanceTest {

  // These are minimum values. Actual values will be ~5 times higher due to associated nodes
  public static final int NODE_NUMBER = 1900;
  public static final int EDGE_NUMBER = 10000;

  public static void main(String... args) throws Exception {

    List<Node> nodes = new ArrayList<>();

    Random rand = new Random();
    System.out.println("Initializing nodes and edges ...");
    for (int i = 0; i < NODE_NUMBER; i++) {
      Node node = new Node();
      node.setName("some node");
      node.setCode("no code");
      if (i > 0) {
        for (int a = 0; a < EDGE_NUMBER / NODE_NUMBER; a++) {
          node.addNextDFG(nodes.get(rand.nextInt(i)));
        }
      }
      nodes.add(node);
    }

    System.out.println("Creating DB instance ...");
    OverflowDatabase db = OverflowDatabase.getInstance();
    Graph g = db.getGraph();

    System.out.println("Saving nodes and edges to DB");
    long start = System.currentTimeMillis();

    // We do not use saveAll, because it would traverse the whole AST hierarchy and save many more
    // nodes
//     OverflowDatabase.getInstance().saveAll(nodes);

    for (Node n : nodes) {
      Vertex v = db.createVertex(n);
      db.createEdges(v, n);
    }
    long end = System.currentTimeMillis();
    System.out.println((end - start));
    Benchmark bench = new Benchmark(PerformanceTest.class, "Counting all nodes");
    long numNodes = g.traversal().V().count().next();
    bench.stop();
    long numEdges = g.traversal().V().outE().count().next();

    System.out.println(
        "Creation of "
            + numNodes
            + " nodes and "
            + numEdges
            + " edges took "
            + (end - start)
            + " MilliSeconds");
  }
}
