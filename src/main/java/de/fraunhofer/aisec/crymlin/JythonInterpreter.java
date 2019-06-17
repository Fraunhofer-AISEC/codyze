package de.fraunhofer.aisec.crymlin;

import com.steelbridgelabs.oss.neo4j.structure.Neo4JElementIdProvider;
import com.steelbridgelabs.oss.neo4j.structure.Neo4JGraph;
import com.steelbridgelabs.oss.neo4j.structure.providers.Neo4JNativeElementIdProvider;
import de.fraunhofer.aisec.cpg.Database;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSourceDsl;
import java.util.Map;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.apache.tinkerpop.gremlin.jsr223.DefaultGremlinScriptEngineManager;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.python.util.InteractiveConsole;

/**
 * Demonstrates how to run Crymlin queries dynamically from Java.
 *
 * @author julian
 */
public class JythonInterpreter implements AutoCloseable {

  /**
   * gremlin-jython engine. We use an interpreted language like Python (or Groovy) so we can easily
   * evaluate Crymlin queries that are entered by the user at runtime and handled as mere strings.
   */
  final ScriptEngine engine =
      new DefaultGremlinScriptEngineManager().getEngineByName("gremlin-jython");

  private Graph tg;

  private CrymlinTraversalSource crymlinSource;

  // store last result
  private TranslationResult lastTranslationResult = null;

  /** Connect to the graph database and initialize the internal Jython engine. */
  public void connect() {
    String uri = System.getenv().getOrDefault("NEO4J_URI", "bolt://localhost");
    String username = System.getenv().getOrDefault("NEO4J_USERNAME", "neo4j");
    String password = System.getenv().getOrDefault("NEO4J_PASSWORD", "password");

    Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));

    // Connect to to Neo4J as usual and return generic Tinkerpop "Graph" object
    Neo4JElementIdProvider<?> vertexIdProvider = new Neo4JNativeElementIdProvider();
    Neo4JElementIdProvider<?> edgeIdProvider = new Neo4JNativeElementIdProvider();
    this.tg = new Neo4JGraph(driver, vertexIdProvider, edgeIdProvider);
    this.crymlinSource = this.tg.traversal(CrymlinTraversalSource.class);

    // Make Java objects available in python
    this.engine.getBindings(ScriptContext.ENGINE_SCOPE).put("graph", tg); // Generic graph
    this.engine
        .getBindings(ScriptContext.ENGINE_SCOPE)
        .put("crymlin", crymlinSource); // Trav. source of crymlin
  }

  /**
   * Run a gremlin/crymlin query given as a string.
   *
   * @param s
   * @return
   * @throws ScriptException
   */
  public Object query(String s) throws ScriptException {
    return this.engine.eval(s);
  }

  public CrymlinTraversalSource getCrymlinTraversal() {
    return this.crymlinSource;
  }

  public GraphTraversalSource getGremlinTraversal() {
    return this.tg.traversal();
  }

  /**
   * Spawns an interactive Jython console that binds to stdin and stdout.
   *
   * <p>Note that this console will be initialized with all python objects from the current script
   * engine, but it will in fact be a second independent engine. It will not see any intermediate
   * results from the query ScriptEngine.
   *
   * <p>This interactive console serves as a rapid experimental interface for testing
   * gremlin/crymlin queries.
   */
  public void spawnInteractiveConsole() {

    // Clear database
    Database.getInstance().connect();
    Database.getInstance().purgeDatabase();

    System.out.println(
        "                           _ _       \n"
            + "                          | (_)      \n"
            + "  ___ _ __ _   _ _ __ ___ | |_ _ __  \n"
            + " / __| '__| | | | '_ ` _ \\| | | '_ \\ \n"
            + "| (__| |  | |_| | | | | | | | | | | |\n"
            + " \\___|_|   \\__, |_| |_| |_|_|_|_| |_|\n"
            + "            __/ |                    \n"
            + "           |___/                     \n"
            + "\n");
    // Print help
    Commands commands = new Commands(this);
    commands.help();

    InteractiveConsole c = new InteractiveConsole();
    for (Map.Entry<String, Object> kv :
        this.engine.getBindings(ScriptContext.ENGINE_SCOPE).entrySet()) {
      c.set(kv.getKey(), kv.getValue());
    }
    c.set("server", commands);
    c.interact();
  }

  @Override
  public void close() throws Exception {
    Graph tg = this.tg;
    if (tg != null) {
      tg.close();
    }

    CrymlinTraversalSourceDsl code = this.crymlinSource;
    if (code != null) {
      code.close();
    }

    this.engine.getBindings(ScriptContext.ENGINE_SCOPE).remove("graph"); // Generic graph
    this.engine
        .getBindings(ScriptContext.ENGINE_SCOPE)
        .remove("crymlin"); // Trav. source of crymlin
  }

  public void setResult(TranslationResult translationResult) {
    this.lastTranslationResult = translationResult;
  }

  public TranslationResult getLastResult() {
    return lastTranslationResult;
  }
}
