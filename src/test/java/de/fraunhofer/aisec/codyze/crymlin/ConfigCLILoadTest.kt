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

class ConfigCLILoadTest {

    @Test
    @Throws(Exception::class)
    fun correctConfigFileAndOptionsTest() {
        val options =
            arrayOf(
                "-c",
                "-s=new_source.java",
                "--timeout=160",
                "--sarif", // test if true stays true
                "--unity", // test if false is set to true
                "--analyze-includes=false", // test if false is set to false
                "-m=mark5,mark7,mark6"
            )
        val config = Configuration.initConfig(correctFile, *options)
        val codyze = config.codyze
        val cpg = config.cpg

        // assert that CLI configurations have a higher priority than config file configurations
        assertNotEquals(
            File("source.java"),
            codyze.source,
            "Option specified in CLI should be prioritized"
        )
        assertEquals(File("new_source.java"), codyze.source)
        assertContentEquals(
            arrayOf("mark5", "mark7", "mark6").map { s -> File(s) }.toTypedArray(),
            codyze.mark,
            "Option specified in CLI should be prioritized"
        )
        assertNotEquals(140L, codyze.timeout, "Option specified in CLI should be prioritized")
        assertEquals(160L, codyze.timeout)
        assertTrue(codyze.sarifOutput)
        assertTrue(cpg.useUnityBuild)
        assertFalse(cpg.translation.analyzeIncludes)

        // assert that rest is either default value or data from config file
        assertEquals("result.out", codyze.output)
        assertEquals(TypestateMode.WPDS, codyze.analysis.tsMode)
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
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun additionalOptionsConfigFileTest() {
        val config =
            Configuration.initConfig(
                additionalOptionFile,
                "-c",
                "--passes=de.fraunhofer.aisec.cpg.passes.FilenameMapper," +
                    "de.fraunhofer.aisec.cpg.passes.CallResolver",
                "--symbols=&=and,+=plus",
                "--no-type-system-in-frontend",
                "--default-passes"
            )
        val cpg = config.cpg

        assertFalse(cpg.typeSystemInFrontend)
        assertNotNull(cpg.defaultPasses)
        assertTrue(cpg.defaultPasses!!)

        val expectedPassesNames =
            arrayOf(
                "de.fraunhofer.aisec.cpg.passes.FilenameMapper",
                "de.fraunhofer.aisec.cpg.passes.CallResolver"
            )
        assertEquals(2, cpg.passes.size, "Expected size 2 but was ${cpg.passes.size}")
        val passesNames = cpg.passes.map { s -> s.javaClass.name }
        assertContentEquals(expectedPassesNames, passesNames.toTypedArray())

        assertEquals(2, cpg.symbols.size, "Expected size 2 but was ${cpg.passes.size}")
        assertTrue(cpg.symbols.containsKey("&"), "Did not contain \'&\' as a key")
        assertEquals("and", cpg.symbols["&"])
        assertTrue(cpg.symbols.containsKey("+"), "Did not contain \'=\' as a key")
        assertEquals("plus", cpg.symbols["+"])

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

    companion object {
        private lateinit var correctFile: File
        private lateinit var incorrectFile: File
        private lateinit var additionalOptionFile: File

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
        }
    }
}
