package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.config.Configuration
import de.fraunhofer.aisec.cpg.TranslationConfiguration
import de.fraunhofer.aisec.cpg.passes.EdgeCachePass
import de.fraunhofer.aisec.cpg.passes.UnreachableEOGPass
import java.io.File
import kotlin.Exception
import kotlin.Throws
import kotlin.test.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal class BuildTranslationConfigTest {

    @Test
    fun enablePythonSupport() {
        val cliParameters = arrayOf("-c", "--enable-python-support")
        val config = Configuration.initConfig(null, *cliParameters)
        config.buildTranslationConfiguration()

        // able to handle missing frontends without crashing
        assert(true)
    }

    @Test
    fun useAdditionalLanguagePython() {
        val cliParameters = arrayOf("-c", "--additional-languages=python")
        val config = Configuration.initConfig(null, *cliParameters)
        config.buildTranslationConfiguration()

        // able to handle missing frontends without crashing
        assert(true)
    }

    @Test
    fun useUnityBuildTest() {
        val cliParameters = arrayOf("-c", "--unity")
        val config = Configuration.initConfig(null, *cliParameters)
        val translationConfig = config.buildTranslationConfiguration()

        assertTrue(
            translationConfig.loadIncludes,
            "LoadIncludes has to be set to true if unityBuild is enabled"
        )
    }

    @Test
    @Throws(Exception::class)
    fun noneTest() {
        val translationConfig =
            Configuration.initConfig(null, "-c", "-s=test.java").buildTranslationConfiguration()
        val expectedTranslationConfig = TranslationConfiguration.builder().defaultPasses().build()
        assertContentEquals(
            expectedTranslationConfig.registeredPasses.map { s -> s.javaClass.name },
            translationConfig.registeredPasses.map { s -> s.javaClass.name },
            "Expected default passes only"
        )
    }

    @Test
    @Throws(Exception::class)
    fun defaultTest() {
        val translationConfig =
            Configuration.initConfig(null, "-c", "--default-passes", "-s=test.java")
                .buildTranslationConfiguration()
        val expectedTranslationConfig = TranslationConfiguration.builder().defaultPasses().build()
        assertContentEquals(
            expectedTranslationConfig.registeredPasses.map { s -> s.javaClass.name },
            translationConfig.registeredPasses.map { s -> s.javaClass.name },
            "Expected default passes only"
        )
    }

    @Test
    @Throws(Exception::class)
    fun noDefaultTest() {
        // TODO: change if there is check that there is at least one pass registered
        val translationConfig =
            Configuration.initConfig(null, "-c", "--no-default-passes", "-s=test.java")
                .buildTranslationConfiguration()
        assertTrue(
            translationConfig.registeredPasses.isEmpty(),
            "Expected to be empty but size was ${translationConfig.registeredPasses.size}"
        )
    }

    @Test
    @Throws(Exception::class)
    fun passesAndNoneTest() {
        val translationConfig =
            Configuration.initConfig(additionalOptionFile, "-c", "-s=test.java")
                .buildTranslationConfiguration()
        val expectedTranslationConfig =
            TranslationConfiguration.builder()
                .registerPass(EdgeCachePass())
                .registerPass(UnreachableEOGPass())
                .build()
        assertContentEquals(
            expectedTranslationConfig.registeredPasses.map { s -> s.javaClass.name },
            translationConfig.registeredPasses.map { s -> s.javaClass.name }
        )
    }

    @Test
    @Throws(Exception::class)
    fun passesAndDefaultTest() {
        val translationConfig =
            Configuration.initConfig(additionalOptionFile, "-c", "--default-passes", "-s=test.java")
                .buildTranslationConfiguration()
        val expectedTranslationConfig =
            TranslationConfiguration.builder()
                .defaultPasses()
                .registerPass(EdgeCachePass())
                .registerPass(UnreachableEOGPass())
                .build()
        assertContentEquals(
            expectedTranslationConfig.registeredPasses.map { s -> s.javaClass.name },
            translationConfig.registeredPasses.map { s -> s.javaClass.name }
        )
    }

    @Test
    fun lspTest() {
        val cliParameters = arrayOf("-l", "--debug-parser", "--source=test.java")
        val config = Configuration.initConfig(null, *cliParameters)
        val translationConfig = config.buildTranslationConfiguration()

        assertFalse(
            translationConfig.debugParser,
            "DebugParser has to be set to false if in lsp mode"
        )
    }

    companion object {
        private lateinit var additionalOptionFile: File

        @BeforeAll
        @JvmStatic
        fun startup() {
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
