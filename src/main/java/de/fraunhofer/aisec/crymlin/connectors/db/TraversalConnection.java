package de.fraunhofer.aisec.crymlin.connectors.db;

import com.steelbridgelabs.oss.neo4j.structure.Neo4JElementIdProvider;
import com.steelbridgelabs.oss.neo4j.structure.Neo4JGraph;
import com.steelbridgelabs.oss.neo4j.structure.providers.Neo4JNativeElementIdProvider;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TraversalConnection implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(TraversalConnection.class);

  private final Graph tg;
  private final CrymlinTraversalSource crymlinSource;

  public TraversalConnection() {
    String uri = System.getenv().getOrDefault("NEO4J_URI", "bolt://localhost");
    String username = System.getenv().getOrDefault("NEO4J_USERNAME", "neo4j");
    String password = System.getenv().getOrDefault("NEO4J_PASSWORD", "password");

    Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));

    // Connect to to Neo4J as usual and return generic Tinkerpop "Graph" object
    Neo4JElementIdProvider<?> vertexIdProvider = new Neo4JNativeElementIdProvider();
    Neo4JElementIdProvider<?> edgeIdProvider = new Neo4JNativeElementIdProvider();
    this.tg = new Neo4JGraph(driver, vertexIdProvider, edgeIdProvider);
    this.crymlinSource = this.tg.traversal(CrymlinTraversalSource.class);
  }

  public CrymlinTraversalSource getCrymlinTraversal() {
    return this.crymlinSource;
  }

  public GraphTraversalSource getGremlinTraversal() {
    return this.tg.traversal();
  }

  public Graph getGraph() {
    return tg;
  }

  public void close() {

    try {
      if (tg != null) {
        tg.close();
      }
    } catch (Exception e) {
      log.debug("Closing TG: ", e);
    }

    try {
      if (crymlinSource != null) {
        crymlinSource.close();
      }
    } catch (Exception e) {
      log.debug("Closing crymlin: ", e);
    }
  }
}
