package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.Commands
import de.fraunhofer.aisec.codyze.JythonInterpreter
import de.fraunhofer.aisec.codyze.analysis.AnalysisServer
import de.fraunhofer.aisec.codyze.analysis.TypestateMode
import de.fraunhofer.aisec.codyze.config.Configuration
import java.io.*
import kotlin.test.assertTrue
import org.junit.jupiter.api.*

/** Testing console commands. */
internal class CommandsTest {
    @Test
    fun commandConsoleTest() {
        val config = Configuration()
        config.codyze.executionMode.isLsp = false
        config.codyze.executionMode.isCli = true
        config.codyze.analysis.tsMode = TypestateMode.DFA
        config.codyze.mark = arrayOf(File("src/test/resources/mark_java"))

        val server = AnalysisServer.builder().config(config).build()
        server.start()
        val oldOut = System.out
        val oldErr = System.err
        val bosOut = ByteArrayOutputStream()
        val o = PrintStream(bosOut)
        val bosErr = ByteArrayOutputStream()
        val e = PrintStream(bosErr)
        System.setOut(o)
        System.setErr(e)

        // Init console
        val interp = JythonInterpreter()
        val com = Commands(interp)
        Commands.help()
        assertTrue(bosOut.toString().contains("help"))

        com.load_rules("src/test/resources/mark_java")
        com.list_rules()
        assertTrue(bosOut.toString().contains("Foo"))

        com.analyze("src/test/resources/good/Bouncycastle.java")
        com.show_findings()
        System.setOut(oldOut)
        System.setErr(oldErr)
    }
}
