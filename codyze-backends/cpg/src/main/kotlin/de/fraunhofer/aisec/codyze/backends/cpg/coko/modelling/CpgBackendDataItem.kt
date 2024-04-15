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
package de.fraunhofer.aisec.codyze.backends.cpg.coko.modelling

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling.BackendDataItem
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.Declaration
import de.fraunhofer.aisec.cpg.graph.evaluate
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Expression
import de.fraunhofer.aisec.cpg.graph.types.HasType

/** Implementation of the [BackendDataItem] interface for the CPG */
class CpgBackendDataItem(
    override val name: String?,
    override val value: Any?,
    override val type: String?
) : BackendDataItem

/** Returns the [CpgBackendDataItem] of the given [Node] */
fun Node.toBackendDataItem(): CpgBackendDataItem {
    val value = (this as? Expression)?.evaluate() ?: (this as? Declaration)?.evaluate()
    val type = (this as? HasType)?.type?.typeName

    return CpgBackendDataItem(name = name.toString(), value = value, type = type)
}
