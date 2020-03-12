
package de.fraunhofer.aisec.analysis;

import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection;
import org.apache.tinkerpop.gremlin.jsr223.DefaultGremlinScriptEngineManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.python.util.InteractiveConsole;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Main class for the interactive Codyze console.
 *
 * The console allows interacting with the server and running crymlin queries dynamically from Java.
 *
 * @author julian
 */
public class JythonInterpreter implements AutoCloseable {

	/** connection to the database via a traversal */
	private TraversalConnection traversalConnection = null;

	/**
	 * gremlin-jython engine. We use an interpreted language like Python (or Groovy) so we can easily evaluate Crymlin queries that are entered by the user at runtime and
	 * handled as mere strings.
	 */
	final ScriptEngine engine = new DefaultGremlinScriptEngineManager().getEngineByName("gremlin-jython");

	// store last result
	private TranslationResult lastTranslationResult = null;

	@NonNull
	private Set<Finding> findings = new HashSet<>();

	/** Connect to the graph database and initialize the internal Jython engine. */
	public void connect() {

		traversalConnection = new TraversalConnection(TraversalConnection.Type.OVERFLOWDB);

		// Make Java objects available in python
		this.engine.getBindings(ScriptContext.ENGINE_SCOPE).put("graph", traversalConnection.getGraph()); // Generic graph
		this.engine.getBindings(ScriptContext.ENGINE_SCOPE).put("crymlin", traversalConnection.getCrymlinTraversal()); // Trav. source of crymlin
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
	 * <p>
	 * Note that this console will be initialized with all python objects from the current script engine, but it will in fact be a second independent engine. It will not
	 * see any intermediate results from the query ScriptEngine.
	 *
	 * <p>
	 * This interactive console serves as a rapid experimental interface for testing gremlin/crymlin queries.
	 */
	public void spawnInteractiveConsole() {

		System.out.println(
			" ██████╗ ██████╗ ██████╗ ██╗   ██╗███████╗███████╗\n"
					+ "██╔════╝██╔═══██╗██╔══██╗╚██╗ ██╔╝╚══███╔╝██╔════╝\n"
					+ "██║     ██║   ██║██║  ██║ ╚████╔╝   ███╔╝ █████╗  \n"
					+ "██║     ██║   ██║██║  ██║  ╚██╔╝   ███╔╝  ██╔══╝  \n"
					+ "╚██████╗╚██████╔╝██████╔╝   ██║   ███████╗███████╗\n"
					+ " ╚═════╝ ╚═════╝ ╚═════╝    ╚═╝   ╚══════╝╚══════╝");
		// Print help
		Commands commands = new Commands(this);

		try (InteractiveConsole c = new InteractiveConsole()) {
			for (Map.Entry<String, Object> kv : this.engine.getBindings(ScriptContext.ENGINE_SCOPE).entrySet()) {
				c.set(kv.getKey(), kv.getValue());
			}
			c.set("server", commands);
			// Overwrite Jython help() builtin with our help
			c.push("import " + Commands.class.getName());
			c.push("help = " + Commands.class.getName() + ".help");
			c.interact();
		}
	}

	@Override
	public void close() {

		if (traversalConnection != null) {
			traversalConnection.close();
		}

		this.engine.getBindings(ScriptContext.ENGINE_SCOPE).remove("graph"); // Generic graph
		this.engine.getBindings(ScriptContext.ENGINE_SCOPE).remove("crymlin"); // Trav. source of crymlin
	}

	public TranslationResult getLastResult() {
		return lastTranslationResult;
	}

	public ScriptEngine getEngine() {
		return engine;
	}

	public void setFindings(@NonNull Set<Finding> findings) {
		this.findings = findings;
	}

	@NonNull
	public Set<Finding> getFindings() {
		return this.findings;
	}
}
