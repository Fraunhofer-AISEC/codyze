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
package de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.order

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.ordering.*
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class OrderNodeTest {

    @Test
    fun `test applyToAll`() {
        val terminalNodes = listOf(
            TerminalOrderNode("base", "op1"),
            TerminalOrderNode("base", "op2"),
            TerminalOrderNode("base", "op3"),
        )
        val sequenceOrderNode = SequenceOrderNode(
            left = terminalNodes[0],
            right = terminalNodes[1]
        )
        val quantifierOrderNode = QuantifierOrderNode(
            child = terminalNodes[2],
            type = OrderQuantifier.MAYBE
        )
        val orderNode = AlternativeOrderNode(
            left = sequenceOrderNode,
            right = quantifierOrderNode
        )

        val expectedList = listOf(
            orderNode.toString(),
            quantifierOrderNode.toString(),
            terminalNodes[2].toString(),
            sequenceOrderNode.toString(),
            terminalNodes[1].toString(),
            terminalNodes[0].toString(),

        )
        val actualList = mutableListOf<String>()

        orderNode.applyToAll {
            actualList.add(this.toString())
        }

        assertContentEquals(expectedList, actualList)
    }
}
