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
package de.fraunhofer.aisec.codyze.core.output.aggregator

import io.github.detekt.sarif4k.Run
import io.github.detekt.sarif4k.SarifSchema210
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

private val logger = KotlinLogging.logger { }

/**
 * Extracts the last run from a valid SARIF result file
 * @param resultFile The file containing the SARIF report
 * @return Its last run or null on error
 */
fun extractLastRun(resultFile: File): Run? {
    if (!resultFile.exists()) {
        logger.error { "The SARIF file at \"${resultFile.canonicalPath}\" does not exist" }
        return null
    }

    val serializer = Json {
        ignoreUnknownKeys = true
    }

    return try {
        // We do not use sarif4k.SarifSerializer as it does not allow us to ignore unknown fields
        // such as arbitrary content of rule.properties
        val sarif = serializer.decodeFromString<SarifSchema210>(resultFile.readText())
        sarif.runs.last()
    } catch (e: SerializationException) {
        logger.error { "Failed to serialize SARIF file at \"${resultFile.canonicalPath}\": ${e.localizedMessage}" }
        null
    } catch (e: IllegalArgumentException) {
        logger.error { "File at \"${resultFile.canonicalPath}\" is not valid SARIF: ${e.localizedMessage}" }
        null
    } catch (e: IOException) {
        logger.error {
            "Unexpected error while trying to read file at \"${resultFile.canonicalPath}\": ${e.localizedMessage}"
        }
        null
    }
}
