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
package de.fraunhofer.aisec.codyze.core.backend

import io.github.detekt.sarif4k.Artifact
import io.github.detekt.sarif4k.ToolComponent
import java.nio.file.Path

/**
 * Interface for all Codyze [Backend]s.
 */
interface Backend {
    val backendData: Any // implement using 'by lazy {}'

    // these properties are necessary for the SARIF construction
    val toolInfo: ToolComponent
    val artifacts: Map<Path, Artifact>
}
