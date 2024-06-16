package de.fraunhofer.aisec.codyze.specificationLanguage.cpg.native

import de.fraunhofer.aisec.codyze.core.VersionProvider
import de.fraunhofer.aisec.codyze.core.backend.Backend
import de.fraunhofer.aisec.codyze.specificationLanguage.cpg.native.queries.CPGQuery
import io.github.detekt.sarif4k.*

private fun CPGQuery.toReportingDescriptor() = ReportingDescriptor(
    id = id,
    name = javaClass.simpleName,
    shortDescription = MultiformatMessageString(text = shortDescription),
    fullDescription = MultiformatMessageString(text = description),
    defaultConfiguration = ReportingConfiguration(level = level),
    help = MultiformatMessageString(text = help),
    properties =
    PropertyBag(
        tags = tags.toSet(),
        map = emptyMap()
    ),
)

class CPGQuerySarifBuilder(val queries: List<CPGQuery>, val backend: Backend) {
    val reportingDescriptors = queries.map { it.toReportingDescriptor() }
    val toolComponent = ToolComponent(
        name = "CPGQueryExecutor",
        product = "Codyze",
        organization = "Fraunhofer AISEC",
        semanticVersion = VersionProvider.getVersion("cpg-queries"),
        downloadURI = "https://github.com/Fraunhofer-AISEC/codyze/releases",
        informationURI = "https://www.codyze.io",
        rules = reportingDescriptors,
    )

    fun buildRun(findings: Map<CPGQuery, List<CpgQueryFinding>>): Run {
        // build the SARIF run based on the received results
        return Run(
            tool = Tool(
                driver = toolComponent,
                extensions = listOf(backend.toolInfo)
            ),
            artifacts = backend.artifacts.values.toList(),
            results = findings.entries.flatMap { entry ->
                entry.value.map { it.toSarif(entry.key, queries, backend.artifacts) }
            }
        )
    }
}
