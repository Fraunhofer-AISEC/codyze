package de.fraunhofer.aisec.codyze.specificationLanguage.cpg.native

import de.fraunhofer.aisec.codyze.core.executor.ExecutorConfiguration
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

data class CPGQueryConfiguration(
    val runQueries: Boolean // Queries may be turned of, if all executors are run and queries shoul be excluded
) : ExecutorConfiguration
