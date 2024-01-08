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

import java.io.File
import java.nio.file.Path

interface Plugin {
    /** the name this output format has in the codyze-cli. */
    val cliName: String

    /**
     * Executes the respective analysis tool.
     * @param target The files to be analyzed
     * @param output The location of the results
     */
    fun execute(target: List<Path>, output: File = File("$cliName.sarif"))
}