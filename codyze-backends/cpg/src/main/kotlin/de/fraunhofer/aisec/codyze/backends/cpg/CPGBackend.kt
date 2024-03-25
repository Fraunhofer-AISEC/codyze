/*
 * Copyright (c) 2022, Fraunhofer AISEC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("UNUSED")

package de.fraunhofer.aisec.codyze.backends.cpg

import de.fraunhofer.aisec.codyze.core.VersionProvider
import de.fraunhofer.aisec.codyze.core.backend.Backend
import de.fraunhofer.aisec.codyze.core.backend.BackendConfiguration
import de.fraunhofer.aisec.cpg.TranslationConfiguration
import de.fraunhofer.aisec.cpg.TranslationManager
import de.fraunhofer.aisec.cpg.TranslationResult
import io.github.detekt.sarif4k.Artifact
import io.github.detekt.sarif4k.ArtifactLocation
import io.github.detekt.sarif4k.ToolComponent
import kotlin.io.path.absolutePathString

/**
 * A plain CPG backend providing only the [TranslationResult] in the [cpg] property.
 */
open class CPGBackend(config: BackendConfiguration) : Backend {
    private val cpgConfiguration = config as CPGConfiguration
    override val backendData: TranslationResult by lazy {
        TranslationManager.builder()
            .config(config = cpgConfiguration.toTranslationConfiguration())
            .build() // Initialize the CPG, based on the given Configuration
            .analyze()
            .get()
    }
    val cpg: TranslationResult
        get() = backendData

    override val toolInfo = ToolComponent(
        name = "CPG Backend",
        product = "Codyze",
        organization = "Fraunhofer AISEC",
        semanticVersion = VersionProvider.getVersion("cpg"),
        downloadURI = "https://github.com/Fraunhofer-AISEC/codyze/releases",
        informationURI = "https://www.codyze.io",
        language = "en-US",
        isComprehensive = false
    )
    override val artifacts = cpgConfiguration.source.associateWith {
        Artifact(location = ArtifactLocation(uri = it.absolutePathString()))
    }

    /** Return a [TranslationConfiguration] object to pass to the CPG */
    private fun CPGConfiguration.toTranslationConfiguration(): TranslationConfiguration {
        val translationConfiguration =
            TranslationConfiguration.builder()
                .debugParser(debugParser)
                .loadIncludes(loadIncludes)
                .codeInNodes(codeInNodes)
                .processAnnotations(processAnnotations)
                .failOnError(failOnError)
                .useParallelFrontends(useParallelFrontends)
                .sourceLocations(source.map { (it.toFile()) })
                .symbols(symbols)
                .useUnityBuild(useUnityBuild)
                .processAnnotations(processAnnotations)

        // TODO: very hacky, but needed for the Go frontend
        source.firstOrNull()?.parent?.toFile().let { translationConfiguration.topLevel(it) }

        includePaths.forEach { translationConfiguration.includePath(it.toString()) }
        includeAllowlist.forEach { translationConfiguration.includeWhitelist(it.toString()) }
        includeBlocklist.forEach { translationConfiguration.includeBlocklist(it.toString()) }

        if (disableCleanup) translationConfiguration.disableCleanup()

        if (defaultPasses) translationConfiguration.defaultPasses()
        passes.forEach { translationConfiguration.registerPass(it) }

        translationConfiguration.optionalLanguage(
            "de.fraunhofer.aisec.cpg.frontends.cxx.CLanguage"
        )
        translationConfiguration.optionalLanguage(
            "de.fraunhofer.aisec.cpg.frontends.cxx.CPPLanguage"
        )
        translationConfiguration.optionalLanguage(
            "de.fraunhofer.aisec.cpg.frontends.java.JavaLanguage"
        )
        translationConfiguration.optionalLanguage(
            "de.fraunhofer.aisec.cpg.frontends.python.PythonLanguage"
        )
        translationConfiguration.optionalLanguage(
            "de.fraunhofer.aisec.cpg.frontends.golang.GoLanguage"
        )
        translationConfiguration.optionalLanguage(
            "de.fraunhofer.aisec.cpg.frontends.llvm.LLVMIRLanguage"
        )
        translationConfiguration.optionalLanguage(
            "de.fraunhofer.aisec.cpg.frontends.typescript.TypeScriptLanguage"
        )

        additionalLanguages.forEach { translationConfiguration.registerLanguage(it) }

        return translationConfiguration.build()
    }
}
