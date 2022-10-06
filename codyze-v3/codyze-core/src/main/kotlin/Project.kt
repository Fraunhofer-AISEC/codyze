package de.fraunhofer.aisec.codyze_core

import de.fraunhofer.aisec.codyze_core.config.Configuration
import de.fraunhofer.aisec.codyze_core.helper.VersionProvider
import de.fraunhofer.aisec.cpg.TranslationManager
import io.github.detekt.sarif4k.*

/**
 * An object that saves the context of an analysis.
 *
 * This enables switching between different analyses (e.g. switching between projects in an IDE).
 */
class Project(val config: Configuration) {
    /** The CPG basesd on the given [config] */
    val translationManager =
        TranslationManager.builder()
            .config(config = config.cpgConfiguration.toTranslationConfiguration())
            .build() // Initialize the CPG, based on the given Configuration

    /** [Executor] that is capable of evaluating the [Configuration.spec] given in [config] */
    val executor = config.executor ?: getRandomCapableExecutor()

    private val sarifBuilder = SarifBuilder()

    /** Return the first registered Executor capable of evaluating [config.specFileExtension] */
    private fun getRandomCapableExecutor(): Executor {
        val randomCapableExecutor: Executor? =
            ProjectServer.executors.find { it.supportedFileExtension == config.specFileExtension }
        if (randomCapableExecutor != null) return randomCapableExecutor
        else
            throw RuntimeException(
                "Did not find any Executor supporting '${config.specFileExtension}' files."
            ) // TODO change to custom exception
    }

    fun doStuff(): SarifSchema210 {
        executor.initialize(config.toExecutorConfiguration())

        val results: List<Result> =
            executor.evaluate(
                analyzer = translationManager
            ) // TODO: pass the translation manager instead?

        // complete SARIF model by integrating results, e.g. add "Codyze" as tool name, etc.
        // TODO what format should we give to LSP?

        return sarifBuilder.buildSchema(results)
    }

    /** Builder for the SARIF output of the analysis */
    private inner class SarifBuilder {
        /** Information about Codyze */
        private val organization = "Fraunhofer AISEC"
        private val name = "Codyze v3"
        private val downloadURI = "https://github.com/Fraunhofer-AISEC/codyze/releases"
        private val informationURI = "https://www.codyze.io/docs/"

        private val codyzeVersion: String = VersionProvider.getVersion("codyze-core")

        private val driver: ToolComponent =
            ToolComponent(
                name = name,
                organization = organization,
                version = codyzeVersion,
                downloadURI = downloadURI,
                informationURI = informationURI,
                notifications =
                    listOf(
                        ReportingDescriptor(
                            id = "Codyze Configuration",
                            shortDescription =
                                MultiformatMessageString(
                                    text = "Configuration that was used for the analysis"
                                ),
                            defaultConfiguration = ReportingConfiguration(level = Level.Note)
                        )
                    ),
                language = "en-US",
                isComprehensive = false
            )

        fun buildSchema(results: List<Result>, properties: PropertyBag? = null): SarifSchema210 {
            return SarifSchema210(
                schema = "https://json.schemastore.org/sarif-2.1.0.json",
                version = Version.The210,
                runs = listOf(buildRun(results)),
                properties = properties
            )
        }

        private fun buildRun(results: List<Result>): Run {
            return Run(
                tool = Tool(driver = driver, extensions = listOf(executor.toolExtension)),
                results = results,
                invocations = listOf(buildInvocation()),
                language = "en-US"
            )
        }

        private fun buildInvocation(): Invocation {
            val configNotifications =
                mutableListOf(
                    Notification(
                        ReportingDescriptorReference(
                            id = "Codyze Configuration",
                        ),
                        message = Message(text = config.toString())
                    )
                )
            configNotifications.addAll(executor.configurationNotifications)

            return Invocation(
                executionSuccessful = true,
                toolConfigurationNotifications = configNotifications,
                toolExecutionNotifications = executor.executionNotifications,
            )
        }
    }
}
