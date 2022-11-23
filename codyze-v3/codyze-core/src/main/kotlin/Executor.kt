package de.fraunhofer.aisec.codyze_core

import de.fraunhofer.aisec.codyze_core.config.ExecutorConfiguration
import de.fraunhofer.aisec.codyze_core.wrapper.BackendConfiguration
import io.github.detekt.sarif4k.Result
import java.nio.file.Path

/**
 * An executor that drives the validation of a specification language against source code and
 * provides evaluation results.
 *
 * For our dependency injection library (Koin), this interface is a 'service interface' and all
 * implementations are 'services'.
 *
 * @since v3.0.0
 */
interface Executor {
    /** Name of executor for a specification language */
    val name: String

    /** Supported file extension for specification language files */
    val supportedFileExtension: String

    // offer standard implementation
    // must only be called once
    // TODO: proper initialization parameters
    fun initialize(backendConfiguration: BackendConfiguration, configuration: ExecutorConfiguration)

    fun evaluate(): List<Result>

    // load speclang files
    // -  create AST from speclang files
    // -  store AST model
    // can be called multiple times to update model
    fun loadSpec(paths: List<Path>) {}

    // compute results from speclang AST and return findings as SARIF
    // fun evaluate(graph: TranslationResult): List<Result>

    // common functionality of reading files from HDD
    private fun loadFiles() {}
}
