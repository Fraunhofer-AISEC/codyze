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
package de.fraunhofer.aisec.codyze.plugins.source

import de.fraunhofer.aisec.codyze.plugins.PMDPlugin
import io.github.detekt.sarif4k.*

class PMDPluginTest: SourcePluginTest() {
    override val plugin = PMDPlugin()
    override val resultFileName = "pmd.sarif"
    override val expectedResults = listOf(
        Result(
            ruleID = "SystemPrintln",
            ruleIndex = 0,
            message = Message(
                text = "Usage of System.out/err",
            ),
            locations = listOf(
                Location(
                    physicalLocation = PhysicalLocation(
                        region = Region(
                            startLine = 24,
                            startColumn = 13,
                            endLine = 24,
                            endColumn = 19
                        )
                    )
                )
            )
        ),
        Result(
            ruleID = "SystemPrintln",
            ruleIndex = 0,
            message = Message(
                text = "Usage of System.out/err",
            ),
            locations = listOf(
                Location(
                    physicalLocation = PhysicalLocation(
                        region = Region(
                            startLine = 27,
                            startColumn = 17,
                            endLine = 27,
                            endColumn = 23
                        )
                    )
                )
            )
        ),
        Result(
            ruleID = "SystemPrintln",
            ruleIndex = 0,
            message = Message(
                text = "Usage of System.out/err",
            ),
            locations = listOf(
                Location(
                    physicalLocation = PhysicalLocation(
                        region = Region(
                            startLine = 29,
                            startColumn = 13,
                            endLine = 29,
                            endColumn = 19
                        )
                    )
                )
            )
        ),
        Result(
            ruleID = "SystemPrintln",
            ruleIndex = 0,
            message = Message(
                text = "Usage of System.out/err",
            ),
            locations = listOf(
                Location(
                    physicalLocation = PhysicalLocation(
                        region = Region(
                            startLine = 44,
                            startColumn = 13,
                            endLine = 44,
                            endColumn = 19
                        )
                    )
                )
            )
        ),
        Result(
            ruleID = "SystemPrintln",
            ruleIndex = 0,
            message = Message(
                text = "Usage of System.out/err",
            ),
            locations = listOf(
                Location(
                    physicalLocation = PhysicalLocation(
                        region = Region(
                            startLine = 45,
                            startColumn = 13,
                            endLine = 45,
                            endColumn = 19
                        )
                    )
                )
            )
        ),
        Result(
            ruleID = "SystemPrintln",
            ruleIndex = 0,
            message = Message(
                text = "Usage of System.out/err",
            ),
            locations = listOf(
                Location(
                    physicalLocation = PhysicalLocation(
                        region = Region(
                            startLine = 46,
                            startColumn = 13,
                            endLine = 46,
                            endColumn = 19
                        )
                    )
                )
            )
        ),
        Result(
            ruleID = "SystemPrintln",
            ruleIndex = 0,
            message = Message(
                text = "Usage of System.out/err",
            ),
            locations = listOf(
                Location(
                    physicalLocation = PhysicalLocation(
                        region = Region(
                            startLine = 48,
                            startColumn = 13,
                            endLine = 48,
                            endColumn = 19
                        )
                    )
                )
            )
        ),
        Result(
            ruleID = "SystemPrintln",
            ruleIndex = 0,
            message = Message(
                text = "Usage of System.out/err",
            ),
            locations = listOf(
                Location(
                    physicalLocation = PhysicalLocation(
                        region = Region(
                            startLine = 49,
                            startColumn = 13,
                            endLine = 49,
                            endColumn = 19
                        )
                    )
                )
            )
        ),
        Result(
            ruleID = "SystemPrintln",
            ruleIndex = 0,
            message = Message(
                text = "Usage of System.out/err",
            ),
            locations = listOf(
                Location(
                    physicalLocation = PhysicalLocation(
                        region = Region(
                            startLine = 50,
                            startColumn = 13,
                            endLine = 50,
                            endColumn = 19
                        )
                    )
                )
            )
        ),
        Result(
            ruleID = "SystemPrintln",
            ruleIndex = 0,
            message = Message(
                text = "Usage of System.out/err",
            ),
            locations = listOf(
                Location(
                    physicalLocation = PhysicalLocation(
                        region = Region(
                            startLine = 54,
                            startColumn = 13,
                            endLine = 54,
                            endColumn = 19
                        )
                    )
                )
            )
        ),
        Result(
            ruleID = "SystemPrintln",
            ruleIndex = 0,
            message = Message(
                text = "Usage of System.out/err",
            ),
            locations = listOf(
                Location(
                    physicalLocation = PhysicalLocation(
                        region = Region(
                            startLine = 56,
                            startColumn = 13,
                            endLine = 56,
                            endColumn = 19
                        )
                    )
                )
            )
        ),
        Result(
            ruleID = "SystemPrintln",
            ruleIndex = 0,
            message = Message(
                text = "Usage of System.out/err",
            ),
            locations = listOf(
                Location(
                    physicalLocation = PhysicalLocation(
                        region = Region(
                            startLine = 58,
                            startColumn = 13,
                            endLine = 58,
                            endColumn = 19
                        )
                    )
                )
            )
        ),
        Result(
            ruleID = "SystemPrintln",
            ruleIndex = 0,
            message = Message(
                text = "Usage of System.out/err",
            ),
            locations = listOf(
                Location(
                    physicalLocation = PhysicalLocation(
                        region = Region(
                            startLine = 59,
                            startColumn = 13,
                            endLine = 59,
                            endColumn = 19
                        )
                    )
                )
            )
        ),
        Result(
            ruleID = "SystemPrintln",
            ruleIndex = 0,
            message = Message(
                text = "Usage of System.out/err",
            ),
            locations = listOf(
                Location(
                    physicalLocation = PhysicalLocation(
                        region = Region(
                            startLine = 60,
                            startColumn = 13,
                            endLine = 60,
                            endColumn = 19
                        )
                    )
                )
            )
        ),
        Result(
            ruleID = "SystemPrintln",
            ruleIndex = 0,
            message = Message(
                text = "Usage of System.out/err",
            ),
            locations = listOf(
                Location(
                    physicalLocation = PhysicalLocation(
                        region = Region(
                            startLine = 62,
                            startColumn = 13,
                            endLine = 62,
                            endColumn = 19
                        )
                    )
                )
            )
        ),
        Result(
            ruleID = "SystemPrintln",
            ruleIndex = 0,
            message = Message(
                text = "Usage of System.out/err",
            ),
            locations = listOf(
                Location(
                    physicalLocation = PhysicalLocation(
                        region = Region(
                            startLine = 64,
                            startColumn = 13,
                            endLine = 64,
                            endColumn = 19
                        )
                    )
                )
            )
        ),
        Result(
            ruleID = "SystemPrintln",
            ruleIndex = 0,
            message = Message(
                text = "Usage of System.out/err",
            ),
            locations = listOf(
                Location(
                    physicalLocation = PhysicalLocation(
                        region = Region(
                            startLine = 87,
                            startColumn = 13,
                            endLine = 87,
                            endColumn = 19
                        )
                    )
                )
            )
        ),
        Result(
            ruleID = "SystemPrintln",
            ruleIndex = 0,
            message = Message(
                text = "Usage of System.out/err",
            ),
            locations = listOf(
                Location(
                    physicalLocation = PhysicalLocation(
                        region = Region(
                            startLine = 89,
                            startColumn = 13,
                            endLine = 89,
                            endColumn = 19
                        )
                    )
                )
            )
        ),
        Result(
            ruleID = "SystemPrintln",
            ruleIndex = 0,
            message = Message(
                text = "Usage of System.out/err",
            ),
            locations = listOf(
                Location(
                    physicalLocation = PhysicalLocation(
                        region = Region(
                            startLine = 91,
                            startColumn = 13,
                            endLine = 91,
                            endColumn = 19
                        )
                    )
                )
            )
        ),
        Result(
            ruleID = "SystemPrintln",
            ruleIndex = 0,
            message = Message(
                text = "Usage of System.out/err",
            ),
            locations = listOf(
                Location(
                    physicalLocation = PhysicalLocation(
                        region = Region(
                            startLine = 93,
                            startColumn = 13,
                            endLine = 93,
                            endColumn = 19
                        )
                    )
                )
            )
        ),
        Result(
            ruleID = "SystemPrintln",
            ruleIndex = 0,
            message = Message(
                text = "Usage of System.out/err",
            ),
            locations = listOf(
                Location(
                    physicalLocation = PhysicalLocation(
                        region = Region(
                            startLine = 95,
                            startColumn = 13,
                            endLine = 95,
                            endColumn = 19
                        )
                    )
                )
            )
        ),
        Result(
            ruleID = "AvoidPrintStackTrace",
            ruleIndex = 1,
            message = Message(
                text = "Avoid printStackTrace(); use a logger call instead.",
            ),
            locations = listOf(
                Location(
                    physicalLocation = PhysicalLocation(
                        region = Region(
                            startLine = 109,
                            startColumn = 17,
                            endLine = 109,
                            endColumn = 36
                        )
                    )
                )
            )
        ),
        Result(
            ruleID = "SystemPrintln",
            ruleIndex = 0,
            message = Message(
                text = "Usage of System.out/err",
            ),
            locations = listOf(
                Location(
                    physicalLocation = PhysicalLocation(
                        region = Region(
                            startLine = 120,
                            startColumn = 13,
                            endLine = 120,
                            endColumn = 19
                        )
                    )
                )
            )
        ),
        Result(
            ruleID = "SystemPrintln",
            ruleIndex = 0,
            message = Message(
                text = "Usage of System.out/err",
            ),
            locations = listOf(
                Location(
                    physicalLocation = PhysicalLocation(
                        region = Region(
                            startLine = 124,
                            startColumn = 17,
                            endLine = 124,
                            endColumn = 23
                        )
                    )
                )
            )
        ),
        Result(
            ruleID = "SystemPrintln",
            ruleIndex = 0,
            message = Message(
                text = "Usage of System.out/err",
            ),
            locations = listOf(
                Location(
                    physicalLocation = PhysicalLocation(
                        region = Region(
                            startLine = 126,
                            startColumn = 13,
                            endLine = 126,
                            endColumn = 19
                        )
                    )
                )
            )
        ),
    )
}