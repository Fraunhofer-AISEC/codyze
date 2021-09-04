package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.JythonInterpreter
import de.fraunhofer.aisec.cpg.ExperimentalGraph
import de.fraunhofer.aisec.cpg.TranslationManager
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.graph
import java.io.*
import java.lang.Exception
import java.lang.InterruptedException
import java.util.ArrayList
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.Throws
import org.junit.jupiter.api.*
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.python.core.Py
import org.python.util.JLineConsole

/** Testing the Crymlin console. */
@TestMethodOrder(OrderAnnotation::class)
@Disabled
internal class JythonInterpreterTest {
    /**
     * Test behavior of tab completion if no input exists.
     *
     * @throws Exception
     */
    @Test
    @Order(1)
    @Throws(Exception::class)
    fun completionSimpleTest() {
        val completions: List<CharSequence> = ArrayList()
        jlineConsole!!.getReader().completers.iterator().next().complete("\t", 0, completions)
        outContent!!.flush()
        errContent!!.flush()
        Assertions.assertTrue(completions.contains(JythonInterpreter.PY_SERVER))
        Assertions.assertTrue(completions.contains(JythonInterpreter.PY_S))
        Assertions.assertTrue(completions.contains(JythonInterpreter.PY_GRAPH))
        Assertions.assertTrue(completions.contains(JythonInterpreter.PY_G))
    }

    /**
     * Test behavior of tab completion for "server.<TAB>"
     *
     * @throws Exception </TAB>
     */
    @Test
    @Order(2)
    @Throws(Exception::class)
    fun completionServerObjectTest() {
        val completions: List<CharSequence> = ArrayList()
        jlineConsole!!
            .getReader()
            .completers
            .iterator()
            .next()
            .complete("server.\t", 0, completions)
        outContent!!.flush()
        errContent!!.flush()
        Assertions.assertTrue(completions.contains("help()"))
        Assertions.assertTrue(completions.contains("show_findings()"))
        Assertions.assertTrue(completions.contains("load_rules()"))
        Assertions.assertTrue(completions.contains("list_rules()"))
        Assertions.assertTrue(completions.contains("analyze()"))
    }

    /**
     * Test behavior of tab completion for "server.sho<TAB>"
     *
     * @throws Exception </TAB>
     */
    @Test
    @Order(3)
    @Throws(Exception::class)
    fun completionServerObjectTest2() {
        val completions: List<CharSequence> = ArrayList()
        jlineConsole!!
            .getReader()
            .completers
            .iterator()
            .next()
            .complete("server.sho\t", 0, completions)
        outContent!!.flush()
        errContent!!.flush()
        Assertions.assertTrue(completions.contains("show_findings()"))
        Assertions.assertEquals(1, completions.size)
    }

    /**
     * Test behavior of tab completion for "server.sho<TAB>"
     *
     * @throws Exception </TAB>
     */
    @Test
    @Order(4)
    @Throws(Exception::class)
    fun completionQueryObjectTest() {
        val completions: List<CharSequence> = ArrayList()
        jlineConsole!!
            .getReader()
            .completers
            .iterator()
            .next()
            .complete("query.cal\t", 0, completions)
        outContent!!.flush()
        errContent!!.flush()
        Assertions.assertTrue(completions.contains("calls()"))
        Assertions.assertEquals(2, completions.size)
    }

    /**
     * Test behavior of tab completion for "query.allCalls().<TAB>"
     *
     * @throws Exception </TAB>
     */
    @Test
    @Order(5)
    @Throws(Exception::class)
    fun completionQueryObjectTest2() {
        val completions: List<CharSequence> = ArrayList()
        jlineConsole!!
            .getReader()
            .completers
            .iterator()
            .next()
            .complete("q.calls().\t", 0, completions)
        outContent!!.flush()
        errContent!!.flush()
        Assertions.assertTrue(completions.contains("next()"))
    }

    companion object {
        private var jlineConsole: JLineConsole? = null
        private var originalErr: PrintStream? = null
        private var originalOut: PrintStream? = null
        private var outContent: ByteArrayOutputStream? = null
        private var errContent: ByteArrayOutputStream? = null
        private var oldIn: InputStream? = null
        private var interp: JythonInterpreter? = null

        /**
         * The console reads and prints from/to stdin/stdout. We redirect these streams to access
         * them from these tests.
         *
         * @throws IOException
         * @throws InterruptedException
         */
        @OptIn(ExperimentalGraph::class)
        @BeforeAll
        @Throws(IOException::class, InterruptedException::class)
        fun beforeAll() {
            // Connect a POS to stdin
            val pos = PipedOutputStream()
            val pis = PipedInputStream(pos)
            oldIn = System.`in`
            System.setIn(pis)

            // Redirect stdout, stderr to BOS
            outContent = ByteArrayOutputStream()
            errContent = ByteArrayOutputStream()
            originalOut = System.out
            originalErr = System.err
            System.setOut(PrintStream(outContent))
            System.setErr(PrintStream(errContent))

            // Trick to make InputStreamReader for stdin blocking to avoid exception when reading
            // from pis.
            System.setProperty("org.python.jline.esc.timeout", "0")

            // Start console (in thread, because it will block)
            interp = JythonInterpreter()
            Thread { interp!!.spawnInteractiveConsole() }.start()

            // Wait until console is up
            val lock = ReentrantLock()
            val consoleAvailable = lock.newCondition()
            Thread {
                    lock.lock()
                    while (interp!!.console == null) {
                        try {
                            Thread.sleep(50)
                        } catch (e: InterruptedException) {
                            Thread.currentThread().interrupt()
                        }
                    }
                    consoleAvailable.signalAll()
                    lock.unlock()
                }
                .start()
            lock.lock()
            consoleAvailable.await(100, TimeUnit.SECONDS)
            lock.unlock()
            val result = TranslationResult(TranslationManager.builder().build())

            // instead of running a full analysis, just provide access to an empty database
            interp!!.connect(result.graph)
            jlineConsole = Py.getConsole() as JLineConsole
            Assertions.assertNotNull(jlineConsole)
            Assertions.assertNotNull(outContent)
            Assertions.assertNotNull(errContent)
        }

        /*@Test
        @Order(6)
        void simpleJythonTest() throws Exception {
        	// Just for testing: We can run normal Gremlin queries:
        	Object result = interp.query("q.V([]).toSet()"); // Get all (!) nodes
        	assertEquals(HashSet.class, result.getClass());
        }*/
        /*@Test
        @Order(7)
        void crymlinOverJythonTest() throws Exception {
        	// Run crymlin queries as strings and get back the results as Java objects:
        	List<Vertex> classes = (List<Vertex>) interp.query("crymlin.records().toList()");
        	assertNotNull(classes);

        	List<Vertex> literals = (List<Vertex>) interp.query("crymlin.sourcefiles().literals().toList()");
        	assertNotNull(literals);
        }*/
        @AfterAll
        fun afterAll() {
            // Restore system streams
            System.setOut(originalOut)
            System.setErr(originalErr)
            System.setIn(oldIn)
            interp!!.close()
            jlineConsole!!.getReader().close()
        }
    }
}
