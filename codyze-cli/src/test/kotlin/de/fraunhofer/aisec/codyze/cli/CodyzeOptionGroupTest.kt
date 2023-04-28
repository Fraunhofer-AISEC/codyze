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
package de.fraunhofer.aisec.codyze.cli

import com.github.ajalt.clikt.core.BadParameterValue
import de.fraunhofer.aisec.codyze.core.output.SarifBuilder
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension
import kotlin.test.*

class CodyzeOptionGroupTest : KoinTest {

    // starting koin is necessary because some options (e.g., --executor)
    // dynamically look up available choices for the by options(...).choice() command
    @JvmField
    @RegisterExtension
    val koinTestExtension =
        KoinTestExtension.create { // Initialize the koin dependency injection
            // declare modules necessary for testing
            modules(outputBuilders)
        }

    /** Test that all available [outputBuilders] are available as choices. */
    @Test
    fun outputBuilderOptionTest() {
        val argv: Array<String> =
            arrayOf(
                "--output-format",
                "txt" // invalid choice
            )
        val cli = CodyzeCli(null)

        val exception: Exception =
            Assertions.assertThrows(BadParameterValue::class.java) { cli.parse(argv) }

        val expectedMessage =
            "Invalid value for \"--output-format\": invalid choice: txt. (choose from "
        val actualMessage = exception.message.orEmpty()

        assertContains(actualMessage, expectedMessage)
    }

    /** Test that [OutputBuilder] choices are cast correctly. */
    @Test
    fun outputBuilderOptionCastTest() {
        val argv: Array<String> =
            arrayOf(
                "--output-format",
                "sarif" // valid choice
            )
        val cli = CodyzeCli(null)
        cli.parse(argv)

        assertTrue(cli.codyzeOptions.outputBuilder is SarifBuilder)
    }

    /** Test that [OutputBuilder] choices are cast correctly. */
    @Test
    fun outputBuilderOptionToConfigurationTest() {
        val argv: Array<String> =
            arrayOf(
                "--output-format",
                "sarif",
                "--no-good-findings",
                "--no-pedantic"
            )
        val cli = CodyzeCli(null)
        cli.parse(argv)

        val config = cli.codyzeOptions.asConfiguration()

        assertEquals(cli.codyzeOptions.output, config.output)
        assertEquals(cli.codyzeOptions.outputBuilder, config.outputBuilder)
        assertEquals(cli.codyzeOptions.goodFindings, config.goodFindings)
        assertEquals(cli.codyzeOptions.pedantic, config.pedantic)
    }
}
