package de.fraunhofer.aisec.crymlin.connectors.db;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

public class DriverSingleton {

  private static Driver driver;
  private static final Object sync = new Object();

  private DriverSingleton() {
    /*hide*/
  }

  public static Driver getInstance() {
    synchronized (sync) {
      if (driver == null) {
        String uri = System.getenv().getOrDefault("NEO4J_URI", "bolt://localhost");
        String username = System.getenv().getOrDefault("NEO4J_USERNAME", "neo4j");
        String password = System.getenv().getOrDefault("NEO4J_PASSWORD", "password");

        driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));
      }
      return driver;
    }
  }

  public static synchronized void destroy() {
    synchronized (sync) {
      if (driver != null) {
        driver.close();
        driver = null;
      }
    }
  }
}
