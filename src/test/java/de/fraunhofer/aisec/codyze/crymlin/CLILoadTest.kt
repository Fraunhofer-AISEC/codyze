package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.TypestateMode
import de.fraunhofer.aisec.codyze.config.Configuration
import java.io.File
import java.lang.Exception
import kotlin.Throws
import kotlin.test.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import picocli.CommandLine

internal class CLILoadTest {

    @Test
    @Throws(Exception::class)
    fun noArgsTest() {
        Assertions.assertThrows(CommandLine.MissingParameterException::class.java) {
            Configuration.initConfig(null, *emptyArray())
        }
    }

    @Test
    @Throws(Exception::class)
    fun exclusiveOptionTest() {
        Assertions.assertThrows(CommandLine.MutuallyExclusiveArgsException::class.java) {
            Configuration.initConfig(null, "-t", "-c")
        }
    }

    @Test
    @Throws(Exception::class)
    fun unknownOptionTest() {
        val config = Configuration.initConfig(null, "-c", "-z")
        val codyze = config.codyze
        val cpg = config.cpg

        // assert that nothing was changed from the default values
        assertTrue(codyze.executionMode.isCli)
        assertFalse(codyze.executionMode.isLsp)
        assertFalse(codyze.executionMode.isTui)
        assertNull(codyze.source)
        assertContentEquals(arrayOf("./").map { s -> File(s) }.toTypedArray(), codyze.mark)
        assertEquals("findings.sarif", codyze.output)
        assertEquals(TypestateMode.DFA, codyze.analysis.tsMode)
        assertEquals(120L, codyze.timeout)
        assertFalse(codyze.noGoodFindings)
        assertFalse(codyze.pedantic)
        assertFalse(codyze.sarifOutput)

        assertFalse(cpg.translation.analyzeIncludes)
        assertEquals(0, cpg.translation.includes.size, "Array of includes was not empty")
        assertEquals(0, cpg.additionalLanguages.size, "Set of additional languages was not empty")
        assertFalse(cpg.useUnityBuild)
    }
}
