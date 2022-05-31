package codyze_core

import de.fraunhofer.aisec.cpg.TranslationResult
import java.nio.file.Path

// TODO create concrete implementation for MARK
interface Executor {
    // offer function to get supported spec lang extensions
    val supportedFileExtensions: List<String>

    // offer standard implementation
    // must only be called once
    fun initialize(paths: List<Path>)

    // load speclang files
    // -  create AST from speclang files
    // -  store AST model
    // can be called multiple times to update model
    fun loadSpec(paths: List<Path>)

    // compute results from speclang AST and return findings as SARIF
    fun evaluate(graph: TranslationResult): List<de.fraunhofer.aisec.codyze.sarif.schema.Result>

    // common functionality of reading files from HDD
    private fun loadFiles() {}
}
