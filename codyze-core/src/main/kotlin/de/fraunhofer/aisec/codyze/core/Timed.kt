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
package de.fraunhofer.aisec.codyze.core

import mu.KotlinLogging
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

val log = KotlinLogging.logger {}

@OptIn(ExperimentalTime::class)
/**
 * Simply helper function to log how long a given task took.
 * @param message The message to log. The resulting log has the format: '{message} {time the task
 * took in milliseconds} ms.'
 * @param block The code block to time.
 * @return The result of the given task
 */
inline fun <reified T> timed(message: String? = null, block: () -> T): T {
    val (result: T, blockDuration: Duration) = measureTimedValue { block() }
    log.debug { message + " ${ blockDuration.inWholeMilliseconds } ms." }
    return result
}
