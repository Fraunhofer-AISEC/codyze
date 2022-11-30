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
package de.fraunhofer.aisec.codyze_core.config

import de.fraunhofer.aisec.codyze_core.output.localization
import java.nio.file.Path
import kotlin.io.path.isRegularFile

fun validateSpec(spec: List<Path>) {
    require(spec.all { it.isRegularFile() }) { localization.pathsMustBeFiles() }
    // require(spec.all { it.extension == spec[0].extension }) { localization.invalidSpecFileType()
    // }  // disabled for now. The parsers of the Executors must filter the files by file type
}
