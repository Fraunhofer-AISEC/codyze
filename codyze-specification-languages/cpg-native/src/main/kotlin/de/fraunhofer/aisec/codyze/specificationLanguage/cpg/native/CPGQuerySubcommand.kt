package de.fraunhofer.aisec.codyze.specificationLanguage.cpg.native

import com.github.ajalt.clikt.parameters.groups.provideDelegate
import de.fraunhofer.aisec.codyze.backends.cpg.CPGBackend
import de.fraunhofer.aisec.codyze.core.backend.Backend
import de.fraunhofer.aisec.codyze.core.executor.ExecutorCommand

@Suppress("UNUSED")
class CPGQuerySubcommand : ExecutorCommand<CPGQueryExecutor>("runNativeQueries") {
    val executorOptions by CPGQueryOptionGroup()

    init {
        // allow only the backends that implement the [CokoBackend] interface as subcommands
        registerBackendOptions<CPGBackend>()
    }

    override fun getExecutor(goodFindings: Boolean, pedantic: Boolean, backend: Backend?) = with(executorOptions) {
        CPGQueryExecutor(CPGQueryConfiguration(true), backend as CPGBackend)
    }
}
