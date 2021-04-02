
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.JythonInterpreter;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.python.core.Py;
import org.python.util.JLineConsole;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static de.fraunhofer.aisec.analysis.JythonInterpreter.PY_G;
import static de.fraunhofer.aisec.analysis.JythonInterpreter.PY_GRAPH;
import static de.fraunhofer.aisec.analysis.JythonInterpreter.PY_Q;
import static de.fraunhofer.aisec.analysis.JythonInterpreter.PY_QUERY;
import static de.fraunhofer.aisec.analysis.JythonInterpreter.PY_S;
import static de.fraunhofer.aisec.analysis.JythonInterpreter.PY_SERVER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testing the Crymlin console.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled
class JythonInterpreterTest {

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
		new Thread(() -> interp.spawnInteractiveConsole()).start();

		// Wait until console is up
		ReentrantLock lock = new ReentrantLock();
		Condition consoleAvailable = lock.newCondition();
		new Thread(() -> {
			lock.lock();
			while (interp.getConsole() == null) {
				try {
					//noinspection BusyWait
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

		// instead of running a full analysis, just provide access to an empty database
		interp.connect(new OverflowDatabase(ServerConfiguration.builder().disableOverflow(true).build()));

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
	void completionSimpleTest() throws Exception {
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
	void completionServerObjectTest() throws Exception {
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

	/**
	 * Test behavior of tab completion for "server.sho<TAB>"
	 *
	 * @throws Exception
	 */
	@Test
	@Order(3)
	void completionServerObjectTest2() throws Exception {
		List<CharSequence> completions = new ArrayList<>();
		jlineConsole.getReader()
				.getCompleters()
				.iterator()
				.next()
				.complete("server.sho\t", 0, completions);
		outContent.flush();
		errContent.flush();

		assertTrue(completions.contains("show_findings()"));
		assertEquals(1, completions.size());
	}

	/**
	 * Test behavior of tab completion for "server.sho<TAB>"
	 *
	 * @throws Exception
	 */
	@Test
	@Order(4)
	void completionQueryObjectTest() throws Exception {
		List<CharSequence> completions = new ArrayList<>();
		jlineConsole.getReader()
				.getCompleters()
				.iterator()
				.next()
				.complete("query.cal\t", 0, completions);
		outContent.flush();
		errContent.flush();

		assertTrue(completions.contains("calls()"));
		assertEquals(2, completions.size());
	}

	/**
	 * Test behavior of tab completion for "query.allCalls().<TAB>"
	 *
	 * @throws Exception
	 */
	@Test
	@Order(5)
	void completionQueryObjectTest2() throws Exception {
		List<CharSequence> completions = new ArrayList<>();
		jlineConsole.getReader()
				.getCompleters()
				.iterator()
				.next()
				.complete("q.calls().\t", 0, completions);
		outContent.flush();
		errContent.flush();

		assertTrue(completions.contains("next()"));
	}

	@Test
	@Order(6)
	void simpleJythonTest() throws Exception {
		// Just for testing: We can run normal Gremlin queries:
		Object result = interp.query("q.V([]).toSet()"); // Get all (!) nodes
		assertEquals(HashSet.class, result.getClass());
	}

	@Test
	@Order(7)
	void crymlinOverJythonTest() throws Exception {
		// Run crymlin queries as strings and get back the results as Java objects:
		List<Vertex> classes = (List<Vertex>) interp.query("crymlin.records().toList()");
		assertNotNull(classes);

		List<Vertex> literals = (List<Vertex>) interp.query("crymlin.sourcefiles().literals().toList()");
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
