
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.JythonInterpreter;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.jupiter.api.*;
import org.python.core.Py;
import org.python.util.JLineConsole;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static de.fraunhofer.aisec.analysis.JythonInterpreter.*;
import static org.junit.jupiter.api.Assertions.*;

/** Testing the Crymlin console. */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JythonInterpreterTest {

	private static JLineConsole jlineConsole;
	private static PrintStream originalErr;
	private static PrintStream originalOut;
	private static ByteArrayOutputStream outContent;
	private static ByteArrayOutputStream errContent;
	private static InputStream oldIn;
	private static JythonInterpreter interp;

	/**
	 * The console reads and prints from/to stdin/stdout.
	 * We redirect these streams to access them from these tests.
	 *
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@BeforeAll
	public static void beforeAll() throws IOException, InterruptedException {
		// Connect a POS to stdin
		PipedOutputStream pos = new PipedOutputStream();
		PipedInputStream pis = new PipedInputStream(pos);
		oldIn = System.in;
		System.setIn(pis);

		// Redirect stdout, stderr to BOS
		outContent = new ByteArrayOutputStream();
		errContent = new ByteArrayOutputStream();
		originalOut = System.out;
		originalErr = System.err;
		System.setOut(new PrintStream(outContent));
		System.setErr(new PrintStream(errContent));

		// Trick to make InputStreamReader for stdin blocking to avoid exception when reading from pis.
		System.setProperty("org.python.jline.esc.timeout", "0");

		// Start console (in thread, because it will block)
		interp = new JythonInterpreter();
		interp.connect();
		new Thread(() -> {
			interp.spawnInteractiveConsole();
		}).start();

		// Wait until console is up
		ReentrantLock lock = new ReentrantLock();
		Condition consoleAvailable = lock.newCondition();
		new Thread(() -> {
			lock.lock();
			while (interp.getConsole() == null) {
				try {
					Thread.sleep(50);
				}
				catch (InterruptedException e) {
					Thread.currentThread()
							.interrupt();
				}
			}
			consoleAvailable.signalAll();
			lock.unlock();
		}).start();
		lock.lock();
		consoleAvailable.await(100, TimeUnit.SECONDS);
		lock.unlock();

		jlineConsole = (JLineConsole) Py.getConsole();

		assertNotNull(jlineConsole);
		assertNotNull(outContent);
		assertNotNull(errContent);
	}

	/**
	 * Test behavior of tab completion if no input exists.
	 *
	 * @throws Exception
	 */
	@Test
	@Order(1)
	public void completionSimpleTest() throws Exception {
		List<CharSequence> completions = new ArrayList<>();
		jlineConsole.getReader()
				.getCompleters()
				.iterator()
				.next()
				.complete("\t", 0, completions);
		outContent.flush();
		errContent.flush();

		assertTrue(completions.contains(PY_QUERY));
		assertTrue(completions.contains(PY_Q));
		assertTrue(completions.contains(PY_SERVER));
		assertTrue(completions.contains(PY_S));
		assertTrue(completions.contains(PY_GRAPH));
		assertTrue(completions.contains(PY_G));
	}

	/**
	 * Test behavior of tab completion for "server.<TAB>"
	 *
	 * @throws Exception
	 */
	@Test
	@Order(2)
	public void completionServerObjectTest() throws Exception {
		List<CharSequence> completions = new ArrayList<>();
		jlineConsole.getReader()
				.getCompleters()
				.iterator()
				.next()
				.complete("server.\t", 0, completions);
		outContent.flush();
		errContent.flush();

		assertTrue(completions.contains("help()"));
		assertTrue(completions.contains("show_findings()"));
		assertTrue(completions.contains("load_rules()"));
		assertTrue(completions.contains("list_rules()"));
		assertTrue(completions.contains("analyze()"));
	}

	@Test
	@Order(3)
	public void simpleJythonTest() throws Exception {
		// Just for testing: We can run normal Gremlin queries:
		Object result = interp.query("q.V().toSet()"); // Get all (!) nodes
		assertEquals(HashSet.class, result.getClass());
	}

	@Test
	@Order(4)
	public void crymlinOverJythonTest() throws Exception {
		// Run crymlin queries as strings and get back the results as Java objects:
		List<Vertex> classes = (List<Vertex>) interp.query("crymlin.recorddeclarations().toList()");
		assertNotNull(classes);

		List<Vertex> literals = (List<Vertex>) interp.query("crymlin.translationunits().literals().toList()");
		assertNotNull(literals);
	}

	@AfterAll
	public static void afterAll() {
		// Restore system streams
		System.setOut(originalOut);
		System.setErr(originalErr);
		System.setIn(oldIn);

		interp.close();
		jlineConsole.getReader().close();
	}

}
