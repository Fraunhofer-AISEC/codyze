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
package de.fraunhofer.aisec.codyze.specificationLanguage.cpg.native

import de.fraunhofer.aisec.codyze.backends.cpg.CPGBackend
import de.fraunhofer.aisec.codyze.core.executor.Executor
import de.fraunhofer.aisec.codyze.specificationLanguage.cpg.native.queries.CPGQuery
import de.fraunhofer.aisec.codyze.specificationLanguage.cpg.native.queries.ExampleQuery
import io.github.detekt.sarif4k.Run
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.FileOutputStream
import java.io.PrintStream

private val logger = KotlinLogging.logger {}

/**
 * The [Executor] to run natively defined CPG queries on the cpg backend, generating Sarif output
 */

class CPGQueryExecutor(private val configuration: CPGQueryConfiguration, private val backend: CPGBackend) :
    Executor {
    private val queries: MutableList<CPGQuery> = mutableListOf()

    init {
        queries.add(ExampleQuery())
    }

    override fun evaluate(): Run {
        logger.info { "Running CPG Queries" }
        val findings: MutableMap<CPGQuery, List<CpgQueryFinding>> = mutableMapOf()

        queries.forEach {
            findings.put(it, it.query(backend))
        }
        val informationExtractor = TSFIInformationExtractor()
        informationExtractor.extractInformation(backend.cpg)

        informationExtractor.printInformation(
            XMLFormatter(),
            PrintStream(FileOutputStream("sf.xml")),
            PrintStream(FileOutputStream("tsfi.xml"))
        )

        val cpgQuerySarifBuilder = CPGQuerySarifBuilder(queries = queries, backend = backend)
        return cpgQuerySarifBuilder.buildRun(findings = findings)
    }
}
