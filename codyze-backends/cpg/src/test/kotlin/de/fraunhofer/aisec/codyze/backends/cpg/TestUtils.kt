package de.fraunhofer.aisec.codyze.backends.cpg

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Evaluator
import de.fraunhofer.aisec.cpg.passes.EdgeCachePass
import de.fraunhofer.aisec.cpg.passes.UnreachableEOGPass
import java.nio.file.Path

fun createCpgConfiguration(vararg sourceFile: Path) =
    CPGConfiguration(
        source = listOf(*sourceFile),
        useUnityBuild = false,
        typeSystemActiveInFrontend = true,
        debugParser = false,
        disableCleanup = false,
        codeInNodes = true,
        matchCommentsToNodes = false,
        processAnnotations = false,
        failOnError = false,
        useParallelFrontends = false,
        defaultPasses = true,
        additionalLanguages = setOf(),
        symbols = mapOf(),
        includeBlocklist = listOf(),
        includePaths = listOf(),
        includeAllowlist = listOf(),
        loadIncludes = false,
        passes = listOf(EdgeCachePass(), UnreachableEOGPass()),
    )

fun dummyRule(): Evaluator = TODO()