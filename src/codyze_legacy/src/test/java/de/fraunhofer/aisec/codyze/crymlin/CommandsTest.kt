package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.Commands
import de.fraunhofer.aisec.codyze.JythonInterpreter
import de.fraunhofer.aisec.codyze.analysis.AnalysisServer
import de.fraunhofer.aisec.codyze.analysis.TypestateMode
import de.fraunhofer.aisec.codyze.config.CodyzeConfiguration
import de.fraunhofer.aisec.codyze.config.Configuration
import de.fraunhofer.aisec.codyze.config.CpgConfiguration
import java.io.*
import kotlin.test.assertTrue
import org.junit.jupiter.api.*

/** Testing console commands. */
internal class CommandsTest {
    @Test
    fun commandConsoleTest() {
        val codyze = CodyzeConfiguration()
        codyze.analysis.tsMode = TypestateMode.DFA
        codyze.mark = arrayOf(File("src/test/resources/legacy/mark_java"))

        val config = Configuration(codyze, CpgConfiguration())
        config.executionMode.isCli = true
        config.executionMode.isLsp = false

        val server = AnalysisServer(config)
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

        com.load_rules("src/test/resources/legacy/mark_java")
        com.list_rules()
        assertTrue(bosOut.toString().contains("Foo"))

        com.analyze("src/test/resources/legacy/good/Bouncycastle.java")
        com.show_findings()
        System.setOut(oldOut)
        System.setErr(oldErr)
    }
}
