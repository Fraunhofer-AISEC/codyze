package de.fraunhofer.aisec.codyze.specificationLanguage.cpg.native.queries

import de.fraunhofer.aisec.codyze.backends.cpg.CPGBackend
import de.fraunhofer.aisec.codyze.specificationLanguage.cpg.native.CpgQueryFinding
import io.github.detekt.sarif4k.Level

open abstract class CPGQuery {


    abstract val id: String
    abstract val shortDescription: String
    abstract val description: String
    val level: Level = Level.Note
    var help: String = ""
    val tags: Set<String> = mutableSetOf()

    abstract fun query(backend: CPGBackend): List<CpgQueryFinding>

}