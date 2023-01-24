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

import com.github.ajalt.clikt.parameters.groups.OptionGroup

/**
 * The base class for all [OptionGroup]s in Codyze backends.
 *
 * If your [BackendCommand] does not need any [OptionGroup]s, there is no need to implement this interface.
 */
open class BackendOptions(helpName: String?) : OptionGroup(name = helpName)
