package de.fraunhofer.aisec.codyze.specificationLanguage.cpg.native.queries

import de.fraunhofer.aisec.codyze.backends.cpg.CPGBackend
import de.fraunhofer.aisec.codyze.specificationLanguage.cpg.native.CpgQueryFinding
import de.fraunhofer.aisec.cpg.graph.Annotation
import de.fraunhofer.aisec.cpg.query.all
import de.fraunhofer.aisec.cpg.query.allExtended

class ExampleQuery: CPGQuery() {

    override val id: String = "0"
    override val shortDescription: String = "A short Query Example"
    override val description: String = "This query is an example of a native cpg query that can use the cpg structure to " +
            "identify relevant nodes"
    val message = "An implementation for cryptographic functionality was found: "
    val crypoUses:List<String> = listOf("ENCRYPT", "DECRYPT", "SIGN","VERIFY", "RANDOM", "RNG", "RAND")

    override fun query(backend: CPGBackend): List<CpgQueryFinding> {

        val findings: MutableList<CpgQueryFinding> = mutableListOf()

        backend.cpg.all<Annotation>
            { crypoUses.contains(it.name.localName.toUpperCase()) }
            .second.forEach {
                findings.add(CpgQueryFinding(message + it.name.localName, node = it, relatedNodes = listOf(it)))
            }

        return findings
    }
}