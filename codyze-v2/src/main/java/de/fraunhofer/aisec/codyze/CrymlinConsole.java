
package de.fraunhofer.aisec.codyze;

import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyUnicode;
import org.python.core.__builtin__;
import org.python.util.InteractiveInterpreter;

import static de.fraunhofer.aisec.codyze.JythonInterpreter.ANSI_CYAN;
import static de.fraunhofer.aisec.codyze.JythonInterpreter.ANSI_CYAN_BG;
import static de.fraunhofer.aisec.codyze.JythonInterpreter.ANSI_RESET;

/**
 * This class provides the read, execute, print loop needed by a Python console; it is not actually
 * a console itself. The primary capability is the {@link #interact()} method, which repeatedly
 * calls {@link #rawInput(PyObject)}, and hence {@link __builtin__#raw_input(PyObject)}, in order
 * to get lines, and {@link #push(String)} them into the interpreter. The built-in
 * <code>raw_input()</code> method prompts on <code>sys.stdout</code> and reads from
 * <code>sys.stdin</code>, the standard console. These may be redirected using
 * {@link #setOut(java.io.OutputStream)} and {@link #setIn(java.io.InputStream)}, as may also
 * <code>sys.stderr</code>.
 */
// Based on CPython-1.5.2's code module
public class CrymlinConsole extends InteractiveInterpreter {

	public static final String BANNER = "\n\n\n\n"
			+ " ██████╗ ██████╗ ██████╗ ██╗   ██╗███████╗███████╗\n"
			+ "██╔════╝██╔═══██╗██╔══██╗╚██╗ ██╔╝╚══███╔╝██╔════╝\n"
			+ "██║     ██║   ██║██║  ██║ ╚████╔╝   ███╔╝ █████╗  \n"
			+ "██║     ██║   ██║██║  ██║  ╚██╔╝   ███╔╝  ██╔══╝  \n"
			+ "╚██████╗╚██████╔╝██████╔╝   ██║   ███████╗███████╗\n"
			+ " ╚═════╝ ╚═════╝ ╚═════╝    ╚═╝   ╚══════╝╚══════╝\n"
			+ ""
			+ "              Welcome to Codyze!                  \n"
			+ "         To get help, enter 'help()'.             \n"
			+ "\n\n";

	public static final String CONSOLE_FILENAME = "<stdin>";

	private static final String MOOD_UNHAPPY = "\uD83D\uDE1F";
	private static final String MOOD_HAPPY = "\uD83D\uDE00";

	private String mood = MOOD_HAPPY;
	private boolean stop = false;

	public CrymlinConsole() {
		this(null, CONSOLE_FILENAME);
	}

	public CrymlinConsole(PyObject locals, String filename) {
		super(locals);
		this.filename = filename;
	}

	public void interact(PyObject file) {
		stop = false;
		PyObject oldPs1 = systemState.ps1;
		PyObject oldPs2 = systemState.ps2;
		systemState.ps2 = new PyString("... ");
		try {
			System.out.println(BANNER);

			// Dummy exec in order to speed up response on first command
			exec("2");

			boolean more = false;
			while (!stop) {
				systemState.ps1 = new PyString(new PyUnicode(ANSI_CYAN_BG + " " + mood + "  " + ANSI_RESET + ANSI_CYAN + "\uE0B0" + ANSI_RESET).encode("utf-8"));
				PyObject prompt = more ? systemState.ps2 : systemState.ps1;
				String line;
				try {
					if (file == null) {
						line = rawInput(prompt);
					} else {
						line = rawInput(prompt, file);
					}
				}
				catch (PyException exc) {
					if (!exc.match(Py.EOFError)) {
						throw exc;
					}
					break;
				}
				catch (Exception t) {
					// catch jline.console.UserInterruptException, rethrow as a KeyboardInterrupt
					throw Py.JavaError(t);
				}
				more = push(line);
			}
		}
		finally {
			systemState.ps1 = oldPs1;
			systemState.ps2 = oldPs2;
		}

	}

	public void stop() {
		this.stop = true;
	}

	/**
	 * Push a line to the interpreter.
	 */
	public boolean push(String line) {
		if (buffer.length() > 0) {
			buffer.append("\n");
		}
		buffer.append(line);
		boolean more = runsource(buffer.toString(), filename);
		if (!more) {
			resetbuffer();
			if (this.systemState.last_type != null && this.systemState.last_type != Py.None) {
				this.mood = MOOD_HAPPY;
			} else {
				this.mood = MOOD_UNHAPPY;
			}
			this.systemState.last_type = Py.None;
		}
		return more;
	}

	private String rawInput(PyObject prompt) {
		return __builtin__.raw_input(prompt);
	}

	private String rawInput(PyObject prompt, PyObject file) {
		return __builtin__.raw_input(prompt, file);
	}

}