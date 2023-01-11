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
package de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl

import de.fraunhofer.aisec.codyze.core.VersionProvider
import de.fraunhofer.aisec.codyze.core.backend.BackendWithOutput
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.CokoRule
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.EvaluationResult
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Rule
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.toResultLevel
import io.github.detekt.sarif4k.*
import kotlin.reflect.full.findAnnotation

private fun CokoRule.toReportingDescriptor() = ReportingDescriptor(
    id = toString(),
    name = name,
    shortDescription = findAnnotation<Rule>()?.shortDescription?.let { desc ->
        MultiformatMessageString(
            text = desc
        )
    },
    fullDescription = findAnnotation<Rule>()?.description?.let { desc ->
        MultiformatMessageString(
            text = desc
        )
    },
    defaultConfiguration = ReportingConfiguration(level = findAnnotation<Rule>()?.severity?.toResultLevel()),
    help = findAnnotation<Rule>()?.help?.let { desc -> MultiformatMessageString(text = desc) },
    properties = PropertyBag(
        tags = findAnnotation<Rule>()?.tags?.toList()
    )
    // TODO: add precision, severity
)

class CokoSarifBuilder(val rules: List<CokoRule>, val backend: BackendWithOutput) {
    val reportingDescriptors = rules.map { it.toReportingDescriptor() }
    val toolComponent = ToolComponent(
        name = "CokoExecutor",
        product = "Codyze",
        organization = "Fraunhofer AISEC",
        semanticVersion = VersionProvider.getVersion("coko-dsl"),
        downloadURI = "https://github.com/Fraunhofer-AISEC/codyze/releases",
        informationURI = "https://www.codyze.io",
        rules = reportingDescriptors,
//        notifications = listOf(
//            ReportingDescriptor(
//                id = "Codyze Configuration",
//                shortDescription = MultiformatMessageString(text = "Configuration that was used for the analysis"),
//                defaultConfiguration = ReportingConfiguration(level = Level.Note)
//            )
//        ),
    )

    fun buildRun(findings: Map<CokoRule, List<EvaluationResult<*>>>): Run {
        // build the SARIF run based on the received results
        return Run(
            tool = Tool(
                driver = toolComponent,
                extensions = listOf(backend.toolInfo)
            ),
            artifacts = backend.artifacts.values.toList(),
            results = findings.entries.flatMap { entry ->
                entry.value.flatMap { it.toSarif(entry.key, rules, backend.artifacts) }
            }
        )
    }
}