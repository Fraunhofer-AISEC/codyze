package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.TypestateMode
import de.fraunhofer.aisec.codyze.config.Configuration
import de.fraunhofer.aisec.codyze.config.Language
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
        assertFalse(codyze.sarifOutput)

        assertFalse(cpg.translation.analyzeIncludes)
        assertEquals(
            0,
            cpg.translation.includes.size,
            "Expected to be empty but size was ${cpg.translation.includes.size}"
        )
        assertEquals(0, cpg.additionalLanguages.size, "Set of additional languages is not empty")
        assertFalse(cpg.useUnityBuild)
    }

    @Test
    @Throws(Exception::class)
    fun languageOptionTest() {
        val config = Configuration.initConfig(null, "-c", "--additional-languages=PYTHON")
        val cpg = config.cpg
        assertEquals(1, cpg.additionalLanguages.size, "Expected size 1 but was ${cpg.passes.size}")
        assertTrue(cpg.additionalLanguages.contains(Language.PYTHON))
    }

    @Test
    @Throws(Exception::class)
    fun passesOptionTest() {
        val config =
            Configuration.initConfig(
                null,
                "-c",
                "--passes=de.fraunhofer.aisec.cpg.passes.EdgeCachePass," +
                    "de.fraunhofer.aisec.cpg.passes.FilenameMapper," +
                    "de.fraunhofer.aisec.cpg.passes.CallResolver"
            )
        val cpg = config.cpg

        val expectedPassesNames =
            arrayOf(
                "de.fraunhofer.aisec.cpg.passes.EdgeCachePass",
                "de.fraunhofer.aisec.cpg.passes.FilenameMapper",
                "de.fraunhofer.aisec.cpg.passes.CallResolver"
            )
        assertEquals(3, cpg.passes.size, "Expected size 3 but was ${cpg.passes.size}")
        val passesNames = cpg.passes.map { s -> s.javaClass.name }
        assertContentEquals(expectedPassesNames, passesNames.toTypedArray())
    }

    @Test
    @Throws(Exception::class)
    fun invalidPassesOptionTest() {
        val config =
            Configuration.initConfig(
                null,
                "-c",
                "--passes=de.fraunhofer.aisec.cpg.passes.MyPass," +
                    "de.fraunhofer.aisec.cpg.passes.Pass," +
                    "de.fraunhofer.aisec.cpg.passes.scopes.BlockScope," +
                    "de.fraunhofer.aisec.cpg.passes.EdgeCachePass," +
                    "MyPass2"
            )
        val cpg = config.cpg
        assertEquals(1, cpg.passes.size, "Expected to have size 1 but was ${cpg.passes.size}")
        assertEquals("de.fraunhofer.aisec.cpg.passes.EdgeCachePass", cpg.passes[0].javaClass.name)
    }

    @Test
    @Throws(Exception::class)
    fun symbolsOptionTest() {
        val config = Configuration.initConfig(null, "-c", "--symbols=#=hash,*=star")
        val cpg = config.cpg
        assertEquals(2, cpg.symbols.size, "Expected size 2 but was ${cpg.passes.size}")
        assertTrue(cpg.symbols.containsKey("#"), "Did not contain \'#\' as a key")
        assertEquals("hash", cpg.symbols["#"])
        assertTrue(cpg.symbols.containsKey("*"), "Did not contain \'*\' as a key")
        assertEquals("star", cpg.symbols["*"])
    }
}
