package de.fraunhofer.aisec.codyze_core

import de.fraunhofer.aisec.codyze_core.config.Configuration
import io.github.detekt.sarif4k.*
import org.koin.core.component.KoinComponent

/**
 * An object that saves the context of an analysis.
 *
 * This enables switching between different analyses (e.g. switching between projects in an IDE).
 */
class Project(val config: Configuration) : KoinComponent {
    /** [Executor] that is capable of evaluating the [Configuration.spec] given in [config] */
    val executor = config.executor ?: getRandomCapableExecutor()

    /** Return the first registered Executor capable of evaluating [config.specFileExtension] */
    private fun getRandomCapableExecutor(): Executor {
        val randomCapableExecutor: Executor? =
            getKoin().getAll<Executor>().find {
                it.supportedFileExtension == config.specFileExtension
            }
        if (randomCapableExecutor != null) return randomCapableExecutor
        else
            throw RuntimeException(
                "Did not find any Executor supporting '${config.specFileExtension}' files."
            ) // TODO change to custom exception
    }

    fun doStuff(): SarifSchema210 {
        executor.initialize(config.backendConfiguration, config.toExecutorConfiguration())
        val results: List<Result> = executor.evaluate()

        // complete SARIF model by integrating results, e.g. add "Codyze" as tool name, etc.
        // TODO what format should we give to LSP?
        return SarifSchema210(
            schema = "https://json.schemastore.org/sarif-2.1.0.json",
            version = Version.The210,
            runs =
                listOf(
                    Run(
                        tool = Tool(driver = ToolComponent(name = "Codyze v3")),
                        results = results,
                    )
                )
        )
    }
}
