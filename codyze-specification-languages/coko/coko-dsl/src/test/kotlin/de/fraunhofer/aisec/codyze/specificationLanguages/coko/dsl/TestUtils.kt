/*
 * Copyright (c) 2024, Fraunhofer AISEC. All rights reserved.
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
package de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl

import java.nio.file.Path
import kotlin.io.path.invariantSeparatorsPathString

/**
 *  Forces any path into an absolute path String that is system invariant.
 *
 *  This is especially important when testing with Windows, as paths with backward slashes are not allowed
 *  as Coko imports.
 */
fun Path.toAbsoluteInvariant(): String {
    return this.toAbsolutePath().invariantSeparatorsPathString
}
