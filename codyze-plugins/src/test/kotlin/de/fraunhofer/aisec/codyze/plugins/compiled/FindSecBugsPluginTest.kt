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
package de.fraunhofer.aisec.codyze.plugins.compiled


import de.fraunhofer.aisec.codyze.plugins.FindSecBugsPlugin
import io.github.detekt.sarif4k.*

class FindSecBugsPluginTest : CompiledPluginTest() {
    override val plugin = FindSecBugsPlugin()
    override val resultFileName = "findsecbugs.sarif"
    override val expectedSuccess = false
    override val expectedResults = listOf(
        Result(
            ruleID = "DM_DEFAULT_ENCODING",
            ruleIndex = 0,
            message = Message(
                id = "default",
                text = "Reliance on default encoding",
                arguments = listOf("de.fraunhofer.aisec.codyze.medina.demo.jsse.TlsServer.start()", "new java.io.InputStreamReader(InputStream)")
            ),
            level = Level.Note,
            locations = listOf(
                Location(
                    physicalLocation = PhysicalLocation(
                        region = Region(startLine = 102)
                    ),
                    logicalLocations = listOf(
                        LogicalLocation(
                            name = "new java.io.InputStreamReader(InputStream)",
                            kind = "function",
                            fullyQualifiedName = "new java.io.InputStreamReader(InputStream)"
                        )
                    )
                )
            )
        ),
        Result(
            ruleID = "DM_DEFAULT_ENCODING",
            ruleIndex = 0,
            message = Message(
                id = "default",
                text = "Reliance on default encoding",
                arguments = listOf("de.fraunhofer.aisec.codyze.medina.demo.jsse.TlsServer.start()", "new java.io.OutputStreamWriter(OutputStream)")
            ),
            level = Level.Note,
            locations = listOf(
                Location(
                    physicalLocation = PhysicalLocation(
                        region = Region(startLine = 103)
                    ),
                    logicalLocations = listOf(
                        LogicalLocation(
                            name = "new java.io.OutputStreamWriter(OutputStream)",
                            kind = "function",
                            fullyQualifiedName = "new java.io.OutputStreamWriter(OutputStream)"
                        )
                    )
                )
            )
        ),
        Result(
            ruleID = "PATH_TRAVERSAL_IN",
            ruleIndex = 1,
            message = Message(
                id = "default",
                text = "Potential Path Traversal (file read)",
                arguments = listOf("java/io/File.<init>(Ljava/lang/String;)V")
            ),
            level = Level.Warning,
            locations = listOf(
                Location(
                    physicalLocation = PhysicalLocation(
                        region = Region(startLine = 133)
                    ),
                    logicalLocations = listOf(
                        LogicalLocation(
                            name = "main(String[])",
                            kind = "function",
                            fullyQualifiedName = "de.fraunhofer.aisec.codyze.medina.demo.jsse.TlsServer.main(String[])"
                        )
                    )
                )
            )
        )
    )
}