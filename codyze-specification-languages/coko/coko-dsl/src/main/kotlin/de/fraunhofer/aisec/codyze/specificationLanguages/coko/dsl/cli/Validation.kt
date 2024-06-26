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
package de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl.cli

import java.nio.file.Path
import kotlin.io.path.isRegularFile

val Path.fileNameString: String
    get() = fileName?.toString().orEmpty()

fun validateSpec(spec: List<Path>): List<Path> {
    require(spec.all { it.isRegularFile() }) { "All given spec paths must be files." }
    require(spec.all { it.fileNameString.endsWith(".codyze.kts") || it.fileNameString.endsWith(".concepts") }) {
        "All given specification files must be coko specification files (*.codyze.kts) or concept files (*.concepts)."
    }
    return spec
}
