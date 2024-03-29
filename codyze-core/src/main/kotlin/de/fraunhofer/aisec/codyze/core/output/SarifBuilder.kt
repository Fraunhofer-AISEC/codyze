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
package de.fraunhofer.aisec.codyze.core.output

import io.github.detekt.sarif4k.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.writeText

/**
 * An [OutputBuilder] for the SARIF format.
 */
class SarifBuilder : OutputBuilder {
    override val cliName = "sarif"

    override fun toFile(run: Run, path: Path) {
        val format = Json(builderAction = { prettyPrint = true })
        val sarifSchema = SarifSchema210(
            schema = "https://json.schemastore.org/sarif-2.1.0.json",
            version = Version.The210,
            runs = listOf(run)
        )
        path.writeText(format.encodeToString(sarifSchema))
    }
}
