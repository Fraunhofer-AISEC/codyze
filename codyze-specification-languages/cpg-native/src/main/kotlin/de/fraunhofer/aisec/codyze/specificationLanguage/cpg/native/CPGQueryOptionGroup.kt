package de.fraunhofer.aisec.codyze.specificationLanguage.cpg.native

import de.fraunhofer.aisec.codyze.core.executor.ExecutorOptions
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Contains all the options specific to the [CPGQueryExecutor]. For now this option group is an empty dummy.
 */
@Suppress("UNUSED")
class CPGQueryOptionGroup : ExecutorOptions("CPG Query Options")
