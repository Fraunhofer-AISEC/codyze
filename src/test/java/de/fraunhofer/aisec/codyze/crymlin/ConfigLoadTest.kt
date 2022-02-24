package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.TypestateMode
import de.fraunhofer.aisec.codyze.config.Configuration
import de.fraunhofer.aisec.codyze.config.Language
import java.io.File
import kotlin.Exception
import kotlin.Throws
import kotlin.test.*
import org.junit.jupiter.api.Test

internal class ConfigLoadTest {

    @Test
    @Throws(Exception::class)
    fun correctConfigFileTest() {
        val config = Configuration.initConfig(correctFile, "-c")
        val codyze = config.codyze
        val cpg = config.cpg

        // assert that the data in the config file was parsed and set correctly
        assertEquals(File("source.java"), codyze.source)
        assertContentEquals(
            arrayOf("mark1", "mark4", "mark3", "mark2").map { s -> File(s) }.toTypedArray(),
            codyze.mark
        )
        assertEquals("result.out", codyze.output)
        assertEquals(140L, codyze.timeout)
        assertTrue(codyze.sarifOutput)
        assertEquals(TypestateMode.WPDS, codyze.analysis.tsMode)

        assertFalse(cpg.translation.analyzeIncludes)
        assertContentEquals(
            arrayOf("include1", "include2").map { s -> File(s) }.toTypedArray(),
            cpg.translation.includes
        )
        assertEquals(
            1,
            cpg.additionalLanguages.size,
            "Size of set of additional languages is not 1"
        )
        assertContains(cpg.additionalLanguages, Language.PYTHON)

        // assert that nothing else was changed from the default values
        assertFalse(codyze.noGoodFindings)
        assertFalse(cpg.useUnityBuild)
    }

    @Test
    @Throws(Exception::class)
    fun incorrectConfigFileTest() {
        val config = Configuration.initConfig(incorrectFile, "-c")
        val codyze = config.codyze
        val cpg = config.cpg

        // assert that nothing was changed from the default values
        assertNull(codyze.source)
        assertContentEquals(arrayOf("./").map { s -> File(s) }.toTypedArray(), codyze.mark)
        assertEquals("findings.sarif", codyze.output)
        assertEquals(TypestateMode.DFA, codyze.analysis.tsMode)
        assertEquals(120L, codyze.timeout)
        assertFalse(codyze.noGoodFindings)
        assertFalse(codyze.sarifOutput)

        assertFalse(cpg.translation.analyzeIncludes)
        assertEquals(0, cpg.translation.includes.size, "Array of includes was not empty")
        assertEquals(0, cpg.additionalLanguages.size, "Set of additional languages is not empty")
        assertFalse(cpg.useUnityBuild)
    }

    companion object {
        private val correctFile =
            File(
                ConfigLoadTest::class
                    .java
                    .classLoader
                    .getResource("config-files/correct_structure.yml")
                    .toURI()
            )
        private val incorrectFile =
            File(
                ConfigLoadTest::class
                    .java
                    .classLoader
                    .getResource("config-files/incorrect_structure.yml")
                    .toURI()
            )
    }
}
