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
@file:Suppress("UNUSED")

package de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl

/** Matches any value. */
object Wildcard

typealias wildcard = Wildcard

/** Stores the fully qualified name of a class */
data class Type(val fqn: String)

data class ParamWithType(val param: Any, val type: Type)

infix fun Any.withType(fqn: String) = ParamWithType(this, Type(fqn))
