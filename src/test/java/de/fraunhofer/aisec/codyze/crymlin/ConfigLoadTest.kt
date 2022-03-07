package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.TypestateMode
import de.fraunhofer.aisec.codyze.config.Configuration
import de.fraunhofer.aisec.codyze.config.Language
import java.io.File
import kotlin.Exception
import kotlin.Throws
import kotlin.test.*
import org.junit.jupiter.api.BeforeAll
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
        assertEquals(0, cpg.translation.includes.size, "List of includes is not empty")
        assertEquals(0, cpg.additionalLanguages.size, "Set of additional languages is not empty")
        assertFalse(cpg.useUnityBuild)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun additionalOptionsConfigFileTest() {
        val config = Configuration.initConfig(additionalOptionFile, "-c")
        val cpg = config.cpg

        assertFalse(cpg.typeSystemInFrontend)
        assertNull(cpg.defaultPasses)

        val expectedPassesNames =
            arrayOf(
                "de.fraunhofer.aisec.cpg.passes.EdgeCachePass",
                "de.fraunhofer.aisec.cpg.passes.UnreachableEOGPass"
            )
        assertEquals(2, cpg.passes.size, "Expected size 2 but was ${cpg.passes.size}")
        val passesNames = cpg.passes.map { s -> s.javaClass.name }
        assertContentEquals(expectedPassesNames, passesNames.toTypedArray())

        assertEquals(3, cpg.symbols.size, "Expected size 3 but was ${cpg.passes.size}")
        assertTrue(cpg.symbols.containsKey("#"), "Did not contain \'#\' as a key")
        assertEquals("hash", cpg.symbols["#"])
        assertTrue(cpg.symbols.containsKey("$"), "Did not contain \'$\' as a key")
        assertEquals("dollar", cpg.symbols["$"])
        assertTrue(cpg.symbols.containsKey("*"), "Did not contain \'*\' as a key")
        assertEquals("star", cpg.symbols["*"])

        val expectedIncludes =
            arrayOf("include1", "include7", "include3", "include5")
                .map { s -> File(s) }
                .toTypedArray()
        assertContentEquals(expectedIncludes, cpg.translation.includes)

        val expectedEnabledIncludes =
            arrayOf("include3", "include5", "include1").map { s -> File(s) }.toTypedArray()
        assertContentEquals(expectedEnabledIncludes, cpg.translation.enabledIncludes)

        val expectedDisabledIncludes =
            arrayOf("include7", "include3").map { s -> File(s) }.toTypedArray()
        assertContentEquals(expectedDisabledIncludes, cpg.translation.disabledIncludes)
    }

    @Test
    @Throws(Exception::class)
    fun unknownLanguageTest() {
        val config = Configuration.initConfig(unknownLanguageFile, "-c")

        // able to handle unknown languages
        assert(true)
    }

    companion object {
        private lateinit var correctFile: File
        private lateinit var incorrectFile: File
        private lateinit var additionalOptionFile: File
        private lateinit var unknownLanguageFile: File

        @BeforeAll
        @JvmStatic
        fun startup() {
            val correctStructureResource =
                ConfigLoadTest::class.java.classLoader.getResource(
                    "config-files/correct_structure.yml"
                )
            assertNotNull(correctStructureResource)
            correctFile = File(correctStructureResource.file)
            assertNotNull(correctFile)

            val incorrectStructureResource =
                ConfigLoadTest::class.java.classLoader.getResource(
                    "config-files/incorrect_structure.yml"
                )
            assertNotNull(incorrectStructureResource)
            incorrectFile = File(incorrectStructureResource.file)
            assertNotNull(incorrectFile)

            val additionalOptionResource =
                ConfigLoadTest::class.java.classLoader.getResource(
                    "config-files/additional_options.yml"
                )
            assertNotNull(additionalOptionResource)
            additionalOptionFile = File(additionalOptionResource.file)
            assertNotNull(additionalOptionFile)

            val unknownLanguageResource =
                ConfigLoadTest::class.java.classLoader.getResource(
                    "config-files/unknown_language.yml"
                )
            assertNotNull(unknownLanguageResource)
            unknownLanguageFile = File(unknownLanguageResource.file)
            assertNotNull(unknownLanguageFile)
        }
    }
}
