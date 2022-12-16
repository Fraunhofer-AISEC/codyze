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

import de.fraunhofer.aisec.codyze.core.wrapper.Backend
import de.fraunhofer.aisec.codyze.core.wrapper.BackendConfiguration
import de.fraunhofer.aisec.cpg.TranslationConfiguration
import de.fraunhofer.aisec.cpg.TranslationManager

open class CPGBackend(config: BackendConfiguration) : Backend {
    override val graph: Any by lazy {
        TranslationManager.builder()
            .config(config = (config as CPGConfiguration).toTranslationConfiguration())
            .build() // Initialize the CPG, based on the given Configuration
            .analyze()
            .get()
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
                .typeSystemActiveInFrontend(typeSystemActiveInFrontend)
                .defaultLanguages()
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
