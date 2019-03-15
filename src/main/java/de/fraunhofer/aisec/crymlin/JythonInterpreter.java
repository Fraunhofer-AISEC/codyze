package de.fraunhofer.aisec.crymlin;

import static de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSourceDsl.NAME;

import com.steelbridgelabs.oss.neo4j.structure.Neo4JElementIdProvider;
import com.steelbridgelabs.oss.neo4j.structure.Neo4JGraph;
import com.steelbridgelabs.oss.neo4j.structure.providers.Neo4JNativeElementIdProvider;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSourceDsl;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.apache.tinkerpop.gremlin.jsr223.DefaultGremlinScriptEngineManager;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
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

  /**
   * Connect to the graph database and initialize the internal Jython engine.
   *
   * @throws IOException
   */
  public void connect() throws IOException {
    var uri = System.getenv().getOrDefault("NEO4J_URI", "bolt://localhost");
    var username = System.getenv().getOrDefault("NEO4J_USERNAME", "neo4j");
    var password = System.getenv().getOrDefault("NEO4J_PASSWORD", "password");

    // TODO parameterize
    Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));

    // Connect to to Neo4J as usual and return generic Tinkerpop "Graph" object
    Neo4JElementIdProvider<?> vertexIdProvider = new Neo4JNativeElementIdProvider();
    Neo4JElementIdProvider<?> edgeIdProvider = new Neo4JNativeElementIdProvider();
    this.tg = new Neo4JGraph(driver, vertexIdProvider, edgeIdProvider);
    this.crymlinSource = tg.traversal(CrymlinTraversalSource.class);

    // Make Java objects available in python
    this.engine.getBindings(ScriptContext.ENGINE_SCOPE).put("graph", tg); // Generic graph
    this.engine
        .getBindings(ScriptContext.ENGINE_SCOPE)
        .put("crymlin", crymlinSource); // Trav. source of crymlin
  }

  @Deprecated() // Just for testing. Use Main class instead
  public static void main(String[] args) {
    try (Driver driver =
        GraphDatabase.driver("bolt://localhost", AuthTokens.basic("neo4j", "password")); ) {
      // Connect to to Neo4J as usual and return generic Tinkerpop "Graph" object
      Neo4JElementIdProvider<?> vertexIdProvider = new Neo4JNativeElementIdProvider();
      Neo4JElementIdProvider<?> edgeIdProvider = new Neo4JNativeElementIdProvider();
      Graph tg = new Neo4JGraph(driver, vertexIdProvider, edgeIdProvider);
      CrymlinTraversalSourceDsl code = tg.traversal(CrymlinTraversalSourceDsl.class);

      // all variable declarations
      List<Vertex> declarations = code.variableDeclarations().toList();
      System.out.println(
          declarations.stream().map(d -> d.value(NAME)).collect(Collectors.toList()));

      // all calls declarations
      List<Vertex> calls = code.calls().toList();
      System.out.println(calls.stream().map(c -> c.value(NAME)).collect(Collectors.toList()));

      // calls that have SSL_ in the name
      calls = code.calls().filter(c -> c.get().<String>value(NAME).startsWith("SSL")).toList();
      System.out.println(calls.stream().map(c -> c.value(NAME)).collect(Collectors.toList()));

      // all ciphers (only literals at the moment)
      // List<Vertex> ciphers = code.cipherListSetterCalls().ciphers().toList();
      // System.out.println(ciphers.stream().map(c -> c.value(VALUE)).collect(Collectors.toList()));
    } catch (Exception ex) {
      ex.printStackTrace();
    }
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
    System.out.println(
        "                           _ _       \n"
            + "                          | (_)      \n"
            + "  ___ _ __ _   _ _ __ ___ | |_ _ __  \n"
            + " / __| '__| | | | '_ ` _ \\| | | '_ \\ \n"
            + "| (__| |  | |_| | | | | | | | | | | |\n"
            + " \\___|_|   \\__, |_| |_| |_|_|_|_| |_|\n"
            + "            __/ |                    \n"
            + "           |___/                     \n"
            + "\n"
            + "You may now start writing crymlin queries.\n"
            + "\n"
            + "Examples: \n"
            + "   crymlin.recorddeclarations().toList()\n"
            + "          Returns array of vertices representing RecordDeclarations.\n"
            + "\n"
            + "   crymlin.recorddeclaration(\"good.Bouncycastle\").next()\n"
            + "          Returns vertex representing the RecordDeclarations of \"good.Bouncycastle\".\n"
            + "\n"
            + "   crymlin.recorddeclaration(\"good.Bouncycastle\").sourcecode().next()\n"
            + "          Returns source code of \"good.Bouncycastle\".\n"
            + "\n"
            + "   crymlin.translationunits().name().toList()\n"
            + "          Returns array of strings representing the names of TranslationUnits.\n"
            + "\n"
            + "   crymlin.translationunits().next()\n"
            + "          Returns the first TranslationUnit vertex (or null if none exists).\n"
            + "\n"
            + "   dir(crymlin.translationunits())\n"
            + "          Good ol' Python dir() to find out what properties/methods are available.\n");
    InteractiveConsole c = new InteractiveConsole();
    for (Map.Entry<String, Object> kv :
        this.engine.getBindings(ScriptContext.ENGINE_SCOPE).entrySet()) {
      c.set(kv.getKey(), kv.getValue());
    }
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
}
