package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.CodyzeConfiguration
import de.fraunhofer.aisec.codyze.Configuration
import de.fraunhofer.aisec.codyze.CpgConfiguration
import de.fraunhofer.aisec.codyze.Language
import java.io.File
import kotlin.Throws
import kotlin.test.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import kotlin.Exception

internal class ConfigLoadTest {

    @Test
    @Throws(Exception::class)
    fun correctConfigFileTest() {
        val config = Configuration.initConfig(correctFile, "-c")
        val codyze = config.codyze
        val cpg = config.cpg

        val expectedCodyze = CodyzeConfiguration()
        expectedCodyze.source = File("source.java")
        expectedCodyze.output = "result.out"
        expectedCodyze.setExecutionMode(true, false, false)
        assertEquals(expectedCodyze, codyze)
        // test static fields
        assertContentEquals(
            arrayOf("mark1", "mark4", "mark3", "mark2").map { s -> File(s) }.toTypedArray(),
            codyze.mark
        )
        assertEquals(120L, codyze.timeout)
        assertFalse(codyze.isNoGoodFindings)

        val expectedCpg = CpgConfiguration()
        expectedCpg.setTranslation(
            false,
            arrayOf("include1", "include2").map { s -> File(s) }.toTypedArray()
        )
        assertEquals(expectedCpg.translation, config.cpg.translation)
        assertEquals(
            1,
            cpg.additionalLanguages.size,
            "Size of set of additional languages is not 1"
        )
        assertContains(cpg.additionalLanguages, Language.PYTHON)
        assertFalse(cpg.isUseUnityBuild)
    }

    @Test
    @Throws(Exception::class)
    fun incorrectConfigFileTest() {
        val config = Configuration.initConfig(incorrectFile, "-c")
        val expected = Configuration()
        expected.codyze.setExecutionMode(true, false, false)
        assertEquals(expected, config)
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
        @BeforeAll
        fun startup() {
            val logger = LoggerFactory.getLogger(Configuration::class.java)
        }
    }
}
