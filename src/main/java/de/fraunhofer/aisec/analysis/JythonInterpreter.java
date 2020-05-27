
package de.fraunhofer.aisec.analysis;

import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.analysis.utils.Utils;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection;
import org.apache.tinkerpop.gremlin.jsr223.DefaultGremlinScriptEngineManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.python.core.Py;
import org.python.core.PyMethod;
import org.python.jline.console.completer.Completer;
import org.python.util.JLineConsole;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.python.jline.internal.Ansi.stripAnsi;

/**
 * Main class for the interactive Codyze console.
 * <p>
 * The console allows interacting with the server and running crymlin queries dynamically from Java.
 *
 * @author julian
 */
public class JythonInterpreter implements AutoCloseable {
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	public static final String ANSI_BLACK_BG = "\u001B[40m";
	public static final String ANSI_RED_BG = "\u001B[41m";
	public static final String ANSI_GREEN_BG = "\u001B[42m";
	public static final String ANSI_YELLOW_BG = "\u001B[43m";
	public static final String ANSI_BLUE_BG = "\u001B[44m";
	public static final String ANSI_PURPLE_BG = "\u001B[45m";
	public static final String ANSI_CYAN_BG = "\u001B[46m";
	public static final String ANSI_WHITE_BG = "\u001B[47m";
	private static final String PY_GRAPH = "graph";
	private static final String PY_G = "g";
	private static final String PY_CRYMLIN = "crymlin";
	private static final String PY_SERVER = "server";
	private static final String PY_S = "s";
	private static final String PY_QUERY = "query";
	private static final String PY_Q = "q";

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

	private CrymlinConsole c;

	/** Connect to the graph database and initialize the internal Jython engine. */
	public void connect() {

		traversalConnection = new TraversalConnection(TraversalConnection.Type.OVERFLOWDB);

		// Make Java objects available with a few shorthand aliases in python
		Bindings bindings = this.engine.getBindings(ScriptContext.ENGINE_SCOPE);
		bindings.put(PY_GRAPH, traversalConnection.getGraph()); // Generic graph
		bindings.put(PY_G, traversalConnection.getGraph()); // Generic graph
		bindings.put(PY_CRYMLIN, traversalConnection.getCrymlinTraversal()); // Trav. source of crymlin
		bindings.put(PY_QUERY, traversalConnection.getCrymlinTraversal()); // Trav. source of crymlin
		bindings.put(PY_Q, traversalConnection.getCrymlinTraversal()); // Trav. source of crymlin

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

		// Print help
		Commands commands = new Commands(this);

		// Set up Jline
		try {
			JLineConsole jCon = new JLineConsole("utf-8");
			Py.installConsole(jCon);
			jCon.getReader()
					.addCompleter(new CrymlinCompleter());
		}
		catch (Exception e) {
			// If JLine is not available, we will fall back to PlainConsole.
		}

		try (CrymlinConsole cons = new CrymlinConsole()) {
			this.c = cons;
			cons.setIn(System.in);
			cons.setOut(System.out);
			cons.setErr(System.out);

			// Define variables from bindings
			for (Map.Entry<String, Object> kv : this.engine.getBindings(ScriptContext.ENGINE_SCOPE)
					.entrySet()) {
				cons.set(kv.getKey(), kv.getValue());
			}
			cons.set(PY_SERVER, commands);
			cons.set(PY_S, commands);
			// Overwrite Jython help() builtin with our help
			cons.push("import " + Commands.class.getName());
			cons.push("help = " + Commands.class.getName() + ".help");

			// Create all @ShellCommand-annotated methods in Command as builtins
			for (Method m : Utils.getMethodsAnnotatedWith(Commands.class, ShellCommand.class)) {
				String cmd = m.getName();
				// Register as a builtin function in Jython console
				cons.push(cmd + " = " + Commands.class.getName() + "." + cmd);
			}
			cons.interact(null);
		}
	}

	@Override
	public void close() {
		if (traversalConnection != null) {
			traversalConnection.close();
		}
		Bindings bindings = this.engine.getBindings(ScriptContext.ENGINE_SCOPE);
		bindings.remove(PY_GRAPH);
		bindings.remove(PY_G);
		bindings.remove(PY_CRYMLIN);
		bindings.remove(PY_QUERY);
		bindings.remove(PY_Q);
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

	/**
	 * Command line completion when user presses TAB.
	 * <p>
	 * The suggestions are derived from properties of the current python object. Internal properties are stripped off.
	 */
	private class CrymlinCompleter implements Completer {

		@Override
		public int complete(String s, int i, List<CharSequence> list) {
			int dotPos = s.indexOf('.');
			s = stripAnsi(s);
			Object items = null;
			String prefix = "";
			try {
				if (dotPos > -1) {
					prefix = s.length() > 1 ? s.substring(dotPos + 1) : "";
					items = engine.eval("dir(" + s.substring(0, dotPos) + ")");
				} else {
					items = List.of(PY_QUERY, PY_Q, PY_SERVER, PY_S, PY_GRAPH, PY_G);
				}
			}
			catch (Exception e) {
				// Nothing to do here.
				c.write(e.getMessage() + "\n");
			}

			// Collect and filter out irrelevant entries
			if (items instanceof List) {
				for (Object entry : ((List) items)) {
					if (entry instanceof String) {
						String str = (String) entry;
						if (str.startsWith(prefix)) {
							Class<?> type = Class.class;
							try {
								String typeEval = "type(" + s.substring(0, dotPos) + "." + str + ")";
								type = ((Class) engine.eval(typeEval));
							}
							catch (ScriptException e) {
								c.write(e.getMessage() + "\n");
							}

							if (str.startsWith("__")
									|| List.of("addE", "addV", "hashCode", "toString", "class", "clone", "wait", "equals", "notify", "notifyAll")
											.contains(str)) {
								list.add(ANSI_BLUE + str + (type.equals(PyMethod.class) ? "(" : "") + ANSI_RESET);
							} else {
								list.add(str + (type.equals(PyMethod.class) ? "(" : ""));
							}
						}
					}
				}
			}

			// Sort
			list.stream()
					.sorted()
					.collect(Collectors.toList());

			// This is necessary to avoid echoing going blank.
			c.setIn(System.in);
			c.setOut(System.out);
			c.setErr(System.out);

			return dotPos == -1 ? 0 : dotPos + 1;
		}
	}
}
