
package de.fraunhofer.aisec.analysis;

import org.python.core.*;
import org.python.util.InteractiveInterpreter;

import static de.fraunhofer.aisec.analysis.JythonInterpreter.*;

/**
 * This class provides the read, execute, print loop needed by a Python console; it is not actually
 * a console itself. The primary capability is the {@link #interact()} method, which repeatedly
 * calls {@link #raw_input(PyObject)}, and hence {@link __builtin__#raw_input(PyObject)}, in order
 * to get lines, and {@link #push(String)} them into the interpreter. The built-in
 * <code>raw_input()</code> method prompts on <code>sys.stdout</code> and reads from
 * <code>sys.stdin</code>, the standard console. These may be redirected using
 * {@link #setOut(java.io.OutputStream)} and {@link #setIn(java.io.InputStream)}, as may also
 * <code>sys.stderr</code>.
 */
// Based on CPython-1.5.2's code module
public class CrymlinConsole extends InteractiveInterpreter {

	public static final String BANNER = " ██████╗ ██████╗ ██████╗ ██╗   ██╗███████╗███████╗\n"
			+ "██╔════╝██╔═══██╗██╔══██╗╚██╗ ██╔╝╚══███╔╝██╔════╝\n"
			+ "██║     ██║   ██║██║  ██║ ╚████╔╝   ███╔╝ █████╗  \n"
			+ "██║     ██║   ██║██║  ██║  ╚██╔╝   ███╔╝  ██╔══╝  \n"
			+ "╚██████╗╚██████╔╝██████╔╝   ██║   ███████╗███████╗\n"
			+ " ╚═════╝ ╚═════╝ ╚═════╝    ╚═╝   ╚══════╝╚══════╝\n"
			+ ""
			+ "              Welcome to Codyze!                  \n"
			+ "         To get help, enter 'help()'.             \n";

	public static final String CONSOLE_FILENAME = "<stdin>";
	private String mood = Mood.HAPPY;
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
						line = raw_input(prompt);
					} else {
						line = raw_input(prompt, file);
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
				this.mood = Mood.UNHAPPY;
			} else {
				this.mood = Mood.HAPPY;
			}
			this.systemState.last_type = Py.None;
		}
		return more;
	}

	public String raw_input(PyObject prompt) {
		return __builtin__.raw_input(prompt);
	}

	public String raw_input(PyObject prompt, PyObject file) {
		return __builtin__.raw_input(prompt, file);
	}

	interface Mood {
		String UNHAPPY = "\uD83D\uDE1F";
		String HAPPY = "\uD83D\uDE00";
	}
}