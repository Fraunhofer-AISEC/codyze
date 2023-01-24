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
import java.nio.file.Path

/**
 * The interface to all [OutputBuilder]s. They convert the internally used SARIF [Run] into the chosen output format.
 */
interface OutputBuilder {
    /** the name this output format has in the codyze-cli. */
    val cliName: String

    /** Convert the SARIF Run to the format of this [OutputBuilder] and write it as file to the given [path]. */
    fun toFile(run: Run, path: Path)
}
