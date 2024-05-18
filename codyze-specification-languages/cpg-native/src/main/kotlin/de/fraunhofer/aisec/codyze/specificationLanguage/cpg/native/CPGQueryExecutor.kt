package de.fraunhofer.aisec.codyze.specificationLanguage.cpg.native

import de.fraunhofer.aisec.codyze.core.executor.Executor
import de.fraunhofer.aisec.codyze.backends.cpg.CPGBackend
import de.fraunhofer.aisec.codyze.specificationLanguage.cpg.native.queries.CPGQuery
import de.fraunhofer.aisec.codyze.specificationLanguage.cpg.native.queries.ExampleQuery
import io.github.detekt.sarif4k.Run
import io.github.oshai.kotlinlogging.KotlinLogging


private val logger = KotlinLogging.logger {}

/**
 * The [Executor] to run natively defined CPG queries on the cpg backend, generating Sarif output
 */

class CPGQueryExecutor(private val configuration: CPGQueryConfiguration, private val backend: CPGBackend) :
    Executor {
    private val queries: MutableList<CPGQuery> = mutableListOf()

    init {
        queries.add(ExampleQuery())
    }

    override fun evaluate(): Run {
        logger.info { "Running CPG Queries" }
        val findings: MutableMap<CPGQuery,List<CpgQueryFinding>> = mutableMapOf()

        queries.forEach {
            findings.put(it, it.query(backend))
        }

        val cpgQuerySarifBuilder = CPGQuerySarifBuilder(queries = queries, backend = backend)
        return cpgQuerySarifBuilder.buildRun(findings = findings)
    }
}