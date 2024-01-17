/*
 * Copyright (c) 2023, Fraunhofer AISEC. All rights reserved.
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
package de.fraunhofer.aisec.codyze.plugin.plugins

import de.fraunhofer.aisec.codyze.core.output.aggregator.extractLastRun
import de.fraunhofer.aisec.codyze.plugins.Plugin
import io.github.detekt.sarif4k.Result
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

abstract class PluginTest {
    abstract val plugin: Plugin
    abstract val resultFileName: String
    abstract val expectedResults: List<Result>

    @Test
    fun testExample() {
        scanFiles()

        val resultURI = PluginTest::class.java.classLoader.getResource("generatedReports/$resultFileName")?.toURI()
        assertNotNull(resultURI)
        val run = extractLastRun(File(resultURI))
        assertNotNull(run)

        if (!run.invocations.isNullOrEmpty()) {
            // FIXME: this fails for FindSecBugs as lambdas are marked as missing references (known issue)
            // run.invocations!!.forEach { assertTrue { it.executionSuccessful } }
        }

        var results = run.results
        assertNotNull(results)
        assertEquals(expectedResults.size, results.size)
        // do not test the physical artifact location as it differs per system
        results = results.map {
            it.copy(locations = it.locations?.map {
                location -> location.copy(physicalLocation = location.physicalLocation?.copy(artifactLocation = null))}
            )
        }
        assertContentEquals(expectedResults, results)
    }

    @AfterEach
    fun cleanup() {
        val resultURI = PluginTest::class.java.classLoader.getResource(resultFileName)?.toURI()
        if (resultURI != null) {
            File(resultURI).delete()
        }
    }

    /**
     * Executes the respective executor with the correct Paths
     */
    abstract fun scanFiles()
}

// <Result(analysisTarget=null, attachments=null, baselineState=null, codeFlows=null, correlationGUID=null, fingerprints=null, fixes=null, graphs=null, graphTraversals=null, guid=null, hostedViewerURI=null, kind=null, level=null, locations=[Location(annotations=null, id=null, logicalLocations=null, message=null, physicalLocation=PhysicalLocation(address=null, artifactLocation=ArtifactLocation(description=null, index=null, properties=null, uri=file:///home/robert/AISEC/codyze/codyze-plugins/src/test/resources/targets/TlsServer.java, uriBaseID=null), contextRegion=null, properties=null, region=Region(byteLength=null, byteOffset=null, charLength=null, charOffset=null, endColumn=19, endLine=24, message=null, properties=null, snippet=null, sourceLanguage=null, startColumn=13, startLine=24)), properties=null, relationships=null)], message=Message(arguments=null, id=null, markdown=null, properties=null, text=Usage of System.out/err), occurrenceCount=null, partialFingerprints=null, properties=null, provenance=null, rank=null, relatedLocations=null, rule=null, ruleID=SystemPrintln, ruleIndex=0, stacks=null, suppressions=null, taxa=null, webRequest=null, webResponse=null, workItemUris=null)>
//