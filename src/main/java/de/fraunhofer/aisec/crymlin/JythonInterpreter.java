package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.crymlin.connectors.db.Neo4jDatabase;
import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection;
import org.apache.tinkerpop.gremlin.jsr223.DefaultGremlinScriptEngineManager;
import org.python.util.InteractiveConsole;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.Map;

/**
 * Demonstrates how to run Crymlin queries dynamically from Java.
 *
 * @author julian
 */
public class JythonInterpreter implements AutoCloseable {

  /** connection to the database via a traversal */
  private TraversalConnection traversalConnection = null;

  /**
   * gremlin-jython engine. We use an interpreted language like Python (or Groovy) so we can easily
   * evaluate Crymlin queries that are entered by the user at runtime and handled as mere strings.
   */
  final ScriptEngine engine =
      new DefaultGremlinScriptEngineManager().getEngineByName("gremlin-jython");

  // store last result
  private TranslationResult lastTranslationResult = null;

  /** Connect to the graph database and initialize the internal Jython engine. */
  public void connect() {

    traversalConnection = new TraversalConnection(TraversalConnection.Type.NEO4J);

    // Make Java objects available in python
    this.engine
        .getBindings(ScriptContext.ENGINE_SCOPE)
        .put("graph", traversalConnection.getGraph()); // Generic graph
    this.engine
        .getBindings(ScriptContext.ENGINE_SCOPE)
        .put("crymlin", traversalConnection.getCrymlinTraversal()); // Trav. source of crymlin
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
    Neo4jDatabase.getInstance().connect();
    Neo4jDatabase.getInstance().purgeDatabase();

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

    try (InteractiveConsole c = new InteractiveConsole()) {
      for (Map.Entry<String, Object> kv :
          this.engine.getBindings(ScriptContext.ENGINE_SCOPE).entrySet()) {
        c.set(kv.getKey(), kv.getValue());
      }
      c.set("server", commands);
      c.interact();
    }
  }

  @Override
  public void close() {

    if (traversalConnection != null) {
      traversalConnection.close();
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
