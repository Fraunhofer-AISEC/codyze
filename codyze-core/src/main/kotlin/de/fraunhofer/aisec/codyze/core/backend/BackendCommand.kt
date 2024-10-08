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

import com.github.ajalt.clikt.core.NoOpCliktCommand
import kotlin.reflect.KClass

/**
 * This abstract class must be implemented by all [Backend]s that want to be selectable in the codyze-cli.
 * Remember to add the newly created [BackendCommand] to the dependency injection.
 */
abstract class BackendCommand<T : Backend>(cliName: String? = null) : NoOpCliktCommand(name = cliName) {

    /*
     * Configure Clikt command
     */
    override val hiddenFromHelp: Boolean = true

    abstract val backend: KClass<T>
    abstract fun getBackend(): T
}
