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

        @BeforeAll fun startup() {}
    }
}
