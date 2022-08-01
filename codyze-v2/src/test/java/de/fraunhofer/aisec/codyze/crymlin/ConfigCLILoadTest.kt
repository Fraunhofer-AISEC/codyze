package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.Main.ConfigFilePath
import de.fraunhofer.aisec.codyze.analysis.TypestateMode
import de.fraunhofer.aisec.codyze.config.Configuration
import de.fraunhofer.aisec.codyze.config.Configuration.Companion.initConfig
import de.fraunhofer.aisec.codyze.config.DisabledMarkRulesValue
import de.fraunhofer.aisec.cpg.TranslationConfiguration
import de.fraunhofer.aisec.cpg.passes.CallResolver
import de.fraunhofer.aisec.cpg.passes.EdgeCachePass
import de.fraunhofer.aisec.cpg.passes.FilenameMapper
import de.fraunhofer.aisec.cpg.passes.UnreachableEOGPass
import java.io.File
import kotlin.test.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import picocli.CommandLine

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
                "-m=mark5${File.pathSeparator}mark7${File.pathSeparator}mark6"
            )
        val config = Configuration.initConfig(correctFile, *options)
        val serverConfig = config.buildServerConfiguration()
        val translationConfig = config.buildTranslationConfiguration()
        val configFileBasePath = correctFile.absoluteFile.parent

        // assert that CLI configurations have a higher priority than config file configurations
        assertFalse(
            config.source.contains(File("source.java")),
            "Option specified in CLI should be prioritized"
        )
        assertContentEquals(arrayOf(File("new_source.java")), config.source)
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
                "--passes=de.fraunhofer.aisec.cpg.passes.FilenameMapper${File.pathSeparator}" +
                    "de.fraunhofer.aisec.cpg.passes.CallResolver",
                "--symbols=&=and${File.pathSeparator}+=plus",
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

    @Test
    @Throws(Exception::class)
    fun appendTest() {
        val options =
            arrayOf(
                "--mark+=mark5${File.pathSeparator}mark7${File.pathSeparator}mark6",
                "--includes+=include7${File.pathSeparator}include193${File.pathSeparator}include3",
                "--source+=cliSource.java${File.pathSeparator}cliDir"
            )
        val config = Configuration.initConfig(correctFile, *options)
        val serverConfig = config.buildServerConfiguration()
        val translationConfig = config.buildTranslationConfiguration()
        val configFileBasePath = correctFile.absoluteFile.parent

        val expectedConfigMark =
            listOf("mark1", "mark4", "mark3", "mark2")
                .map { s -> File(configFileBasePath, s).absolutePath }
                .toTypedArray()
        assertContentEquals(
            arrayOf(*expectedConfigMark, "mark5", "mark7", "mark6")
                .map { s -> File(s).absolutePath }
                .toTypedArray(),
            serverConfig.markModelFiles,
            "Option specified in config file should be appended to CLI option"
        )

        val expectedConfigIncludes =
            listOf("include1", "include2")
                .map { s -> File(configFileBasePath, s).absolutePath }
                .toTypedArray()
        assertContentEquals(
            arrayOf(*expectedConfigIncludes, "include7", "include193", "include3")
                .map { s -> File(s).absolutePath }
                .toTypedArray(),
            translationConfig.includePaths,
            "Option specified in config file should be appended to CLI option"
        )

        assertContentEquals(
            arrayOf(
                File(configFileBasePath, "source.java"),
                File("cliSource.java"),
                File("cliDir")
            ),
            config.source,
            "Option specified in config file should be appended to CLI option"
        )
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun additionalAppendTest() {
        val config =
            Configuration.initConfig(
                additionalOptionFile,
                "-c",
                "--passes+=de.fraunhofer.aisec.cpg.passes.FilenameMapper${File.pathSeparator}" +
                    "de.fraunhofer.aisec.cpg.passes.CallResolver",
                "--symbols+=&=and${File.pathSeparator}+=plus",
                "--default-passes",
                "--includes+=include9${File.pathSeparator}include193${File.pathSeparator}include13",
                "--enabled-includes+=include9${File.pathSeparator}include52",
                "--disabled-includes+=include13"
            )
        val translationConfiguration = config.buildTranslationConfiguration(File("test.java"))
        val expectedTranslationConfig =
            TranslationConfiguration.builder()
                .defaultPasses()
                .registerPass(EdgeCachePass())
                .registerPass(UnreachableEOGPass())
                .registerPass(FilenameMapper())
                .registerPass(CallResolver())
                .build()

        val configFileBasePath = additionalOptionFile.absoluteFile.parent

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
            5,
            translationConfiguration.symbols.size,
            "Expected size 5 but was ${translationConfiguration.symbols.size}"
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
        assertContentEquals(
            arrayOf(*expectedIncludes, "include9", "include193", "include13")
                .map { s -> File(s).absolutePath }
                .toTypedArray(),
            translationConfiguration.includePaths
        )

        val expectedEnabledIncludes =
            arrayOf("include3", "include5", "include1")
                .map { s -> File(configFileBasePath, s).absolutePath }
                .toTypedArray()
        assertContentEquals(
            arrayOf(*expectedEnabledIncludes, "include9", "include52").map { s ->
                File(s).absolutePath
            },
            translationConfiguration.includeWhitelist
        )

        val expectedDisabledIncludes =
            arrayOf("include7", "include3")
                .map { s -> File(configFileBasePath, s).absolutePath }
                .toTypedArray()
        assertContentEquals(
            arrayOf(*expectedDisabledIncludes, "include13").map { s -> File(s).absolutePath },
            translationConfiguration.includeBlacklist
        )
    }

    @Test
    fun disabledMarkAppendTest() {
        val config =
            Configuration.initConfig(
                disabledMarkFile,
                "--disabled-mark-rules+=package.mark0${File.pathSeparator}package2.*"
            )
        val serverConfiguration = config.buildServerConfiguration()
        val expectedMap =
            mapOf(
                Pair("package", DisabledMarkRulesValue(false, mutableSetOf("mark1", "mark0"))),
                Pair("", DisabledMarkRulesValue(false, mutableSetOf("mark2"))),
                Pair("package0", DisabledMarkRulesValue(false, mutableSetOf("mark123"))),
                Pair("package2", DisabledMarkRulesValue(true))
            )

        for (expectedKey in expectedMap.keys) {
            assertContains(
                serverConfiguration.packageToDisabledMarkRules,
                expectedKey,
                "\"$expectedKey\" was not a key in map"
            )

            val expectedValue = expectedMap[expectedKey]
            val actualValue = serverConfiguration.packageToDisabledMarkRules[expectedKey]
            if (expectedValue != null && actualValue != null) {
                assertEquals(
                    expectedValue.isDisablePackage,
                    actualValue.isDisablePackage,
                    "Boolean to disable entire package was not equal"
                )
                for (rule in expectedValue.disabledMarkRuleNames) {
                    assertTrue(
                        actualValue.disabledMarkRuleNames.contains(rule),
                        "Rule $rule was not in set"
                    )
                }
            }
        }
    }

    @Test
    fun disabledSourceAppendTest() {
        val options =
            arrayOf(
                "--source+=cliDir1${File.pathSeparator}cliDir2/cliSource.java${File.pathSeparator}cliSource.java",
                "--disabled-sources+=cliDir2${File.pathSeparator}cliSource.java"
            )
        val config = Configuration.initConfig(sourceDisablingFile, *options)
        val configFileBasePath = sourceDisablingFile.absoluteFile.parent

        val expectedConfigSource =
            arrayOf(
                    "../real-examples/botan/blockciphers/Antidote1911.Arsenic",
                    "../real-examples/botan/blockciphers/obraunsdorf.playbook-creator",
                    "../real-examples/botan/MARK",
                    "../real-examples/botan/MARK",
                    "../real-examples/botan/streamciphers",
                    "../directory-structure"
                )
                .map { s -> File(configFileBasePath, s).absolutePath }
                .toTypedArray()
        assertContentEquals(
            arrayOf(*expectedConfigSource, "cliDir1", "cliDir2/cliSource.java", "cliSource.java")
                .map { s -> File(s).absolutePath },
            config.source.map { f -> f.absolutePath }
        )

        val expectedConfigDisabledSource =
            arrayOf(
                    "../real-examples/botan/blockciphers",
                    "../config-files/additional_options.yml",
                    "../real-examples/botan/MARK",
                    "../directory-structure/dir2/dir2dir1/dir2dir1file1.java"
                )
                .map { s -> File(configFileBasePath, s).absolutePath }
                .toTypedArray()
        assertContentEquals(
            arrayOf(*expectedConfigDisabledSource, "cliDir2", "cliSource.java").map { s ->
                File(s).absolutePath
            },
            config.disabledSource.map { f -> f.absolutePath }
        )
    }

    companion object {
        private lateinit var correctFile: File
        private lateinit var incorrectFile: File
        private lateinit var additionalOptionFile: File
        private lateinit var disabledMarkFile: File
        private lateinit var sourceDisablingFile: File

        @BeforeAll
        @JvmStatic
        fun startup() {
            val correctStructureResource =
                ConfigLoadTest::class
                    .java
                    .classLoader
                    .getResource("config-files/correct_structure.yml")
            assertNotNull(correctStructureResource)
            correctFile = File(correctStructureResource.file)
            assertNotNull(correctFile)

            val incorrectStructureResource =
                ConfigLoadTest::class
                    .java
                    .classLoader
                    .getResource("config-files/incorrect_structure.yml")
            assertNotNull(incorrectStructureResource)
            incorrectFile = File(incorrectStructureResource.file)
            assertNotNull(incorrectFile)

            val additionalOptionResource =
                ConfigLoadTest::class
                    .java
                    .classLoader
                    .getResource("config-files/additional_options.yml")
            assertNotNull(additionalOptionResource)
            additionalOptionFile = File(additionalOptionResource.file)
            assertNotNull(additionalOptionFile)

            val disabledMarkResource =
                ConfigLoadTest::class.java.classLoader.getResource("config-files/disabled_mark.yml")
            assertNotNull(disabledMarkResource)
            disabledMarkFile = File(disabledMarkResource.file)
            assertNotNull(disabledMarkFile)

            val sourceDisablingResource =
                ConfigLoadTest::class
                    .java
                    .classLoader
                    .getResource("config-files/source_disabling.yml")
            assertNotNull(sourceDisablingResource)
            sourceDisablingFile = File(sourceDisablingResource.file)
            assertNotNull(sourceDisablingFile)
        }
    }

    @Test
    fun firstStep() {
        val args = arrayOf("--config", "config-files/source_disabling.yml")

        val firstPass = ConfigFilePath()
        val cmd = CommandLine(firstPass)
        cmd.parseArgs(*args)

        assertDoesNotThrow { initConfig(firstPass.configFile, *firstPass.remainder.toTypedArray()) }
    }
}
