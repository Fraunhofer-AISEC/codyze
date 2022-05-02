package de.fraunhofer.aisec.codyze.legacy.crymlin

import de.fraunhofer.aisec.codyze.legacy.analysis.TypestateMode
import de.fraunhofer.aisec.codyze.legacy.config.Configuration
import de.fraunhofer.aisec.cpg.TranslationConfiguration
import de.fraunhofer.aisec.cpg.passes.EdgeCachePass
import de.fraunhofer.aisec.cpg.passes.UnreachableEOGPass
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
        val serverConfig = config.buildServerConfiguration()
        val translationConfig = config.buildTranslationConfiguration()
        val configFileBasePath = correctFile.absoluteFile.parent

        // assert that the data in the config file was parsed and set correctly
        assertContentEquals(arrayOf(File(configFileBasePath, "source.java")), config.source)
        assertContentEquals(
            arrayOf("mark1", "mark4", "mark3", "mark2")
                .map { s -> File(configFileBasePath, s).absolutePath }
                .toTypedArray(),
            serverConfig.markModelFiles
        )
        assertEquals(File(configFileBasePath, "result.out").absolutePath, config.output)
        assertEquals(140L, config.timeout)
        assertTrue(config.sarifOutput)
        assertEquals(TypestateMode.WPDS, serverConfig.typestateAnalysis)

        assertFalse(translationConfig.loadIncludes)
        assertContentEquals(
            arrayOf("include1", "include2")
                .map { s -> File(configFileBasePath, s).absolutePath }
                .toTypedArray(),
            translationConfig.includePaths
        )

        // Test for additional languages doesn't really work because the LanguageFrontends
        // are not in the library, so they won't be registered
        //        assertEquals(
        //            1,
        //            translationConfig.frontends.size,
        //            "Size of set of additional languages is not 1"
        //        )
        //        assertTrue(translationConfig.frontends.containsKey())

        // assert that nothing else was changed from the default values
        assertFalse(serverConfig.disableGoodFindings)
        assertFalse(serverConfig.pedantic)

        // no way to access useUnityBuild in TranslationConfiguration
        //        assertFalse(translationConfig.useUnityBuild)
    }

    @Test
    @Throws(Exception::class)
    fun incorrectConfigFileTest() {
        // we just parse whatever we can understand from config file; the rest is initialized to
        // default values
        val config = Configuration.initConfig(incorrectFile, "-c")
        val serverConfig = config.buildServerConfiguration()
        val translationConfig = config.buildTranslationConfiguration()
        val configFileBasePath = correctFile.absoluteFile.parent

        // assert that nothing was changed from the default values
        assertEquals(
            listOf(File(configFileBasePath, "source.java").absolutePath),
            config.source.map { f -> f.absolutePath }
        )
        assertEquals(120L, config.timeout)
        assertEquals(File(configFileBasePath, "result.out").absolutePath, config.output)
        assertTrue(config.sarifOutput)

        assertContentEquals(
            arrayOf("./").map { s -> File(s).absolutePath }.toTypedArray(),
            serverConfig.markModelFiles
        )
        assertEquals(TypestateMode.DFA, serverConfig.typestateAnalysis)
        assertFalse(serverConfig.disableGoodFindings)
        assertFalse(serverConfig.pedantic)

        assertFalse(translationConfig.loadIncludes)
        assertEquals(2, translationConfig.includePaths.size)
        assertEquals(
            2,
            translationConfig.frontends.size,
            "List of frontends did not only contain default frontends"
        )

        // no way to access useUnityBuild in TranslationConfiguration
        //        assertFalse(translationConfig.useUnityBuild)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun additionalOptionsConfigFileTest() {
        val config = Configuration.initConfig(additionalOptionFile, "-c")
        val translationConfiguration = config.buildTranslationConfiguration(File("test.java"))
        val expectedTranslationConfig =
            TranslationConfiguration.builder()
                .typeSystemActiveInFrontend(false)
                .registerPass(EdgeCachePass())
                .registerPass(UnreachableEOGPass())
                .build()

        // Can't test typeSystemActiveInFrontend

        assertEquals(
            expectedTranslationConfig.registeredPasses.size,
            translationConfiguration.registeredPasses.size,
            "Expected size ${expectedTranslationConfig.registeredPasses.size} but was ${translationConfiguration.registeredPasses.size}"
        )
        val passesNames =
            translationConfiguration.registeredPasses.map { s -> s.javaClass.name }.toTypedArray()
        val expectedPassesNames =
            expectedTranslationConfig.registeredPasses.map { s -> s.javaClass.name }.toTypedArray()
        assertContentEquals(expectedPassesNames, passesNames)

        assertEquals(
            3,
            translationConfiguration.symbols.size,
            "Expected size 3 but was ${translationConfiguration.symbols.size}"
        )
        assertTrue(
            translationConfiguration.symbols.containsKey("#"),
            "Did not contain \'#\' as a key"
        )
        assertEquals("hash", translationConfiguration.symbols["#"])
        assertTrue(
            translationConfiguration.symbols.containsKey("$"),
            "Did not contain \'$\' as a key"
        )
        assertEquals("dollar", translationConfiguration.symbols["$"])
        assertTrue(
            translationConfiguration.symbols.containsKey("*"),
            "Did not contain \'*\' as a key"
        )
        assertEquals("star", translationConfiguration.symbols["*"])

        val expectedIncludes =
            arrayOf("include1", "include7", "include3", "include5")
                .map { s -> File(additionalOptionFile.absoluteFile.parent, s).absolutePath }
                .toTypedArray()
        assertContentEquals(expectedIncludes, translationConfiguration.includePaths)

        val expectedEnabledIncludes =
            arrayOf("include3", "include5", "include1").map { s ->
                File(additionalOptionFile.absoluteFile.parent, s).absolutePath
            }
        assertContentEquals(expectedEnabledIncludes, translationConfiguration.includeWhitelist)

        val expectedDisabledIncludes =
            arrayOf("include7", "include3").map { s ->
                File(additionalOptionFile.absoluteFile.parent, s).absolutePath
            }
        assertContentEquals(expectedDisabledIncludes, translationConfiguration.includeBlacklist)
    }

    @Test
    @Throws(Exception::class)
    fun unknownLanguageTest() {
        Configuration.initConfig(unknownLanguageFile, "-c")

        // able to handle unknown languages
        assert(true)
    }

    @Test
    fun pathsTest() {
        var config = Configuration.initConfig(paths1File, "-c")
        assertNotNull(config.source)
        assertContentEquals(
            listOf(File("/absolute/path/to/source").absolutePath),
            config.source.map { f -> f.absolutePath }
        )
        assertEquals(
            File(paths1File.absoluteFile.parent, "../relative/path/to/output").absolutePath,
            config.output
        )

        config = Configuration.initConfig(paths2File, "-c")
        assertNotNull(config.source)
        assertContentEquals(
            listOf(File(paths2File.absoluteFile.parent, "../relative/path/to/source").absolutePath),
            config.source.map { f -> f.absolutePath }
        )
        assertEquals("/absolute/path/to/output", config.output)
    }

    companion object {
        private lateinit var correctFile: File
        private lateinit var incorrectFile: File
        private lateinit var additionalOptionFile: File
        private lateinit var unknownLanguageFile: File
        private lateinit var paths1File: File
        private lateinit var paths2File: File

        @BeforeAll
        @JvmStatic
        fun startup() {
            val correctStructureResource =
                ConfigLoadTest::class
                    .java.classLoader.getResource("legacy/config-files/correct_structure.yml")
            assertNotNull(correctStructureResource)
            correctFile = File(correctStructureResource.file)
            assertNotNull(correctFile)

            val incorrectStructureResource =
                ConfigLoadTest::class
                    .java.classLoader.getResource("legacy/config-files/incorrect_structure.yml")
            assertNotNull(incorrectStructureResource)
            incorrectFile = File(incorrectStructureResource.file)
            assertNotNull(incorrectFile)

            val additionalOptionResource =
                ConfigLoadTest::class
                    .java.classLoader.getResource("legacy/config-files/additional_options.yml")
            assertNotNull(additionalOptionResource)
            additionalOptionFile = File(additionalOptionResource.file)
            assertNotNull(additionalOptionFile)

            val unknownLanguageResource =
                ConfigLoadTest::class
                    .java.classLoader.getResource("legacy/config-files/unknown_language.yml")
            assertNotNull(unknownLanguageResource)
            unknownLanguageFile = File(unknownLanguageResource.file)
            assertNotNull(unknownLanguageFile)

            val paths1Resource =
                ConfigLoadTest::class.java.classLoader.getResource("legacy/config-files/paths1.yml")
            assertNotNull(paths1Resource)
            paths1File = File(paths1Resource.file)
            assertNotNull(paths1File)

            val paths2Resource =
                ConfigLoadTest::class.java.classLoader.getResource("legacy/config-files/paths2.yml")
            assertNotNull(paths2Resource)
            paths2File = File(paths2Resource.file)
            assertNotNull(paths2File)
        }
    }
}
