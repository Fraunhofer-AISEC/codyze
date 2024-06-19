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
package de.fraunhofer.aisec.codyze.specificationLanguage.cpg.native.queries

import de.fraunhofer.aisec.codyze.backends.cpg.CPGBackend
import de.fraunhofer.aisec.codyze.specificationLanguage.cpg.native.CpgQueryFinding
import io.github.detekt.sarif4k.Level

open abstract class CPGQuery {

    abstract val id: String
    abstract val shortDescription: String
    abstract val description: String
    val level: Level = Level.Note
    var help: String = ""
    val tags: Set<String> = mutableSetOf()

    abstract fun query(backend: CPGBackend): List<CpgQueryFinding>
}
