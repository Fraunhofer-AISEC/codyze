package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.TypestateMode
import de.fraunhofer.aisec.codyze.config.Configuration
import de.fraunhofer.aisec.cpg.TranslationConfiguration
import de.fraunhofer.aisec.cpg.passes.CallResolver
import de.fraunhofer.aisec.cpg.passes.FilenameMapper
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
                "-m=mark5:mark7:mark6"
            )
        val config = Configuration.initConfig(correctFile, *options)
        val serverConfig = config.buildServerConfiguration()
        val translationConfig = config.buildTranslationConfiguration()
        val configFileBasePath = correctFile.absoluteFile.parent

        // assert that CLI configurations have a higher priority than config file configurations
        assertNotEquals(
            File("source.java"),
            config.source,
            "Option specified in CLI should be prioritized"
        )
        assertEquals(File("new_source.java"), config.source)
        assertContentEquals(
            arrayOf("mark5", "mark7", "mark6").map { s -> File(s).absolutePath }.toTypedArray(),
            serverConfig.markModelFiles,
            "Option specified in CLI should be prioritized"
        )
        assertNotEquals(140L, config.timeout, "Option specified in CLI should be prioritized")
        assertEquals(160L, config.timeout)
        assertTrue(config.sarifOutput)
        // loadIncludes is true because of sarifOutput
        assertTrue(translationConfig.loadIncludes)

        // no way to access useUnityBuild in TranslationConfiguration
        //        assertTrue(translationConfig.useUnityBuild)

        // assert that rest is either default value or data from config file
        assertEquals(File(configFileBasePath, "result.out").absolutePath, config.output)
        assertEquals(TypestateMode.WPDS, serverConfig.typestateAnalysis)
        assertContentEquals(
            arrayOf("include1", "include2")
                .map { s -> File(configFileBasePath, s).absolutePath }
                .toTypedArray(),
            translationConfig.includePaths
        )

        // Test for additional languages doesn't work anymore because the LanguageFrontends
        // are not in the library, so they won't be registered
        //        assertEquals(
        //            1,
        //            translationConfig.frontends.size,
        //            "Size of set of additional languages is not 1"
        //        )
        //        assertTrue(translationConfig.frontends.containsKey())

        // assert that nothing else was changed from the default values
        assertFalse(serverConfig.disableGoodFindings)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun additionalOptionsConfigFileTest() {
        val config =
            Configuration.initConfig(
                additionalOptionFile,
                "-c",
                "--passes=de.fraunhofer.aisec.cpg.passes.FilenameMapper:" +
                    "de.fraunhofer.aisec.cpg.passes.CallResolver",
                "--symbols=&=and:+=plus",
                "--no-type-system-in-frontend",
                "--default-passes"
            )
        val translationConfiguration = config.buildTranslationConfiguration(File("test.java"))
        val expectedTranslationConfig =
            TranslationConfiguration.builder()
                .defaultPasses()
                .registerPass(FilenameMapper())
                .registerPass(CallResolver())
                .build()

        val configFileBasePath = additionalOptionFile.absoluteFile.parent

        // Can't test typeSystemActiveInFrontend

        assertEquals(
            expectedTranslationConfig.registeredPasses.size,
            translationConfiguration.registeredPasses.size,
            "Expected size 2 but was ${translationConfiguration.registeredPasses.size}"
        )
        val passesNames =
            translationConfiguration.registeredPasses.map { s -> s.javaClass.name }.toTypedArray()
        val expectedPassesNames =
            expectedTranslationConfig.registeredPasses.map { s -> s.javaClass.name }.toTypedArray()
        assertContentEquals(expectedPassesNames, passesNames)

        assertEquals(
            2,
            translationConfiguration.symbols.size,
            "Expected size 2 but was ${translationConfiguration.symbols.size}"
        )
        assertTrue(
            translationConfiguration.symbols.containsKey("&"),
            "Did not contain \'&\' as a key"
        )
        assertEquals("and", translationConfiguration.symbols["&"])
        assertTrue(
            translationConfiguration.symbols.containsKey("+"),
            "Did not contain \'=\' as a key"
        )
        assertEquals("plus", translationConfiguration.symbols["+"])

        val expectedIncludes =
            arrayOf("include1", "include7", "include3", "include5")
                .map { s -> File(configFileBasePath, s).absolutePath }
                .toTypedArray()
        assertContentEquals(expectedIncludes, translationConfiguration.includePaths)

        val expectedEnabledIncludes =
            arrayOf("include3", "include5", "include1").map { s ->
                File(configFileBasePath, s).absolutePath
            }
        assertContentEquals(expectedEnabledIncludes, translationConfiguration.includeWhitelist)

        val expectedDisabledIncludes =
            arrayOf("include7", "include3").map { s -> File(configFileBasePath, s).absolutePath }
        assertContentEquals(expectedDisabledIncludes, translationConfiguration.includeBlacklist)
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
