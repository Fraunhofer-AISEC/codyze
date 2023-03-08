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
package de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.ordering

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Op
import kotlin.jvm.internal.CallableReference
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

typealias OrderToken = KFunction<Op>

fun OrderToken.getOp(vararg arguments: Any? = Array<Any?>(parameters.size) { null }) = call(*arguments)

/** Convert an [OrderToken] into a TerminalOrderNode */
internal fun OrderToken.toNode(): TerminalOrderNode = TerminalOrderNode(
    baseName = ((this as CallableReference).owner as KClass<*>).qualifiedName ?: error(
        "Local classes or classes of anonymous objects are not supported in Coko!"
    ),
    opName = name,
)
