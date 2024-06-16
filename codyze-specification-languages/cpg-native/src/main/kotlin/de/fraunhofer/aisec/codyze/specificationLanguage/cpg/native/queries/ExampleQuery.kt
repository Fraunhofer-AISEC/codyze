/*
 * Copyright (c) 2024, Fraunhofer AISEC. All rights reserved.
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
package de.fraunhofer.aisec.codyze.specificationLanguage.cpg.native.queries

import de.fraunhofer.aisec.codyze.backends.cpg.CPGBackend
import de.fraunhofer.aisec.codyze.specificationLanguage.cpg.native.CpgQueryFinding
import de.fraunhofer.aisec.cpg.graph.Annotation
import de.fraunhofer.aisec.cpg.query.all

class ExampleQuery : CPGQuery() {

    override val id: String = "0"
    override val shortDescription: String = "A short Query Example"
    override val description: String = "This query is an example of a native cpg query that can use the cpg structure to " +
        "identify relevant nodes"
    val message = "An implementation for cryptographic functionality was found: "
    val crypoUses: List<String> = listOf("ENCRYPT", "DECRYPT", "SIGN", "VERIFY", "RANDOM", "RNG", "RAND")

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
