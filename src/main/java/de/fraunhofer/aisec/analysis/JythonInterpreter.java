
package de.fraunhofer.aisec.analysis;

import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.analysis.utils.Utils;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import org.apache.tinkerpop.gremlin.jsr223.DefaultGremlinScriptEngineManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.python.core.Console;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.jline.Terminal;
import org.python.jline.TerminalFactory;
import org.python.jline.console.completer.Completer;
import org.python.util.InteractiveConsole;
import org.python.util.JLineConsole;
import org.python.util.PythonInterpreter;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Main class for the interactive Codyze console.
 * <p>
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

	private InteractiveConsole c;

	/** Connect to the graph database and initialize the internal Jython engine. */
	public void connect() {

		traversalConnection = new TraversalConnection(TraversalConnection.Type.OVERFLOWDB);
		System.out.println("Graph connection: " + traversalConnection.getCrymlinTraversal());

		// Make Java objects available in python
		this.engine.getBindings(ScriptContext.ENGINE_SCOPE)
				.put("graph", traversalConnection.getGraph()); // Generic graph
		this.engine.getBindings(ScriptContext.ENGINE_SCOPE)
				.put("crymlin", traversalConnection.getCrymlinTraversal()); // Trav. source of crymlin

		// If we aready have a running console, update bound objects
		if (this.c != null) {
			for (Map.Entry<String, Object> kv : this.engine.getBindings(ScriptContext.ENGINE_SCOPE)
					.entrySet()) {
				c.set(kv.getKey(), kv.getValue());
			}
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
	@SuppressWarnings("java:S106")
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

		// Set up Jline
		try {
			JLineConsole jCon = new JLineConsole(null);
			Py.installConsole(jCon);
			Py.getSystemState()
					.__setattr__("_jy_console", Py.java2py(Py.getConsole()));
			jCon.getReader().addCompleter(new CrymlinCompleter());
		}
		catch (Exception e) {
			// If JLine is not available, InteractiveConsole will fall back to PlainConsole.
		}

		try (InteractiveConsole c = new InteractiveConsole()) {
			this.c = c;
			c.setIn(System.in);
			c.setOut(System.out);
			c.setErr(System.err);
			for (Map.Entry<String, Object> kv : this.engine.getBindings(ScriptContext.ENGINE_SCOPE)
					.entrySet()) {
				c.set(kv.getKey(), kv.getValue());
			}
			c.set("server", commands);
			// Overwrite Jython help() builtin with our help
			c.push("import " + Commands.class.getName());
			c.push("import readline");
			c.push("import rlcompleter");
			c.push("help = " + Commands.class.getName() + ".help");

			// Create all @ShellCommand-annotated methods in Command as builtins
			for (Method m : Utils.getMethodsAnnotatedWith(Commands.class, ShellCommand.class)) {
				String cmd = m.getName();
				// Register as a builtin function in Jython console
				c.push(cmd + " = " + Commands.class.getName() + "." + cmd);
			}

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

	private class CrymlinCompleter implements Completer {

		@Override
		public int complete(String s, int i, List<CharSequence> list) {

			return 0;
		}
	}
}
