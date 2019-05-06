package de.fraunhofer.aisec.crymlin;

import static org.junit.jupiter.api.Assertions.*;

import com.steelbridgelabs.oss.neo4j.structure.Neo4JElementIdProvider;
import com.steelbridgelabs.oss.neo4j.structure.Neo4JGraph;
import com.steelbridgelabs.oss.neo4j.structure.providers.Neo4JNativeElementIdProvider;
import de.fhg.aisec.markmodel.fsm.Node;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import de.fraunhofer.aisec.crymlin.dsl.DefaultCrymlinTraversal;
import java.util.Collection;
import java.util.UUID;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;

public class Neo4jSimpleDBTest {

  @Test
  @Disabled
  public void test1() throws Exception {
    try (Driver driver =
        GraphDatabase.driver("bolt://localhost", AuthTokens.basic("neo4j", "password"))) {
      // Connect to to Neo4J as usual and return generic Tinkerpop "Graph" object
      Neo4JElementIdProvider<?> vertexIdProvider = new Neo4JNativeElementIdProvider();
      Neo4JElementIdProvider<?> edgeIdProvider = new Neo4JNativeElementIdProvider();
      Graph tg = new Neo4JGraph(driver, vertexIdProvider, edgeIdProvider);
      CrymlinTraversalSource code = tg.traversal(CrymlinTraversalSource.class);

      DefaultCrymlinTraversal traversal = new DefaultCrymlinTraversal(code);

    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  @Test
  public void saveLoadNode() throws Exception {
    String uri = System.getenv().getOrDefault("NEO4J_URI", "bolt://localhost");
    String username = System.getenv().getOrDefault("NEO4J_USERNAME", "neo4j");
    String password = System.getenv().getOrDefault("NEO4J_PASSWORD", "password");
    Configuration configuration =
        (new Configuration.Builder())
            .uri(uri)
            .autoIndex("none")
            .credentials(username, password)
            .verifyConnection(true)
            .build();
    SessionFactory sessionFactory = new SessionFactory(configuration, "de.fhg.aisec.markmodel.fsm");
    Session session = sessionFactory.openSession();
    session.purgeDatabase();
    Transaction tx;
    final String test = UUID.randomUUID().toString();
    {
      tx = session.beginTransaction();
      Node n = new Node(test);
      Node n2 = new Node(test);
      n.addSuccessor(n2);
      session.save(n);
      tx.commit();
    }
    {
      tx = session.beginTransaction();
      Collection<Node> nodes = session.loadAll(Node.class);
      assertEquals(2, nodes.size());
      for (Node n : nodes) {
        assertEquals(test, n.getName());
      }
      tx.commit();
    }
    tx.close();
    session.clear();
    sessionFactory.close();
  }
}
