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

import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

/**
 * Simple helper function to log how long a given task took.
 * @param message The lambda invoked with the measured duration ([Duration]) of the given [block].
 * @param block The code block to time.
 * @return The result of the given [block]
 */
@OptIn(ExperimentalTime::class)
fun <T> timed(message: (Duration) -> Unit, block: () -> T): T {
    val (result: T, blockDuration: Duration) = measureTimedValue { block() }
    message.invoke(blockDuration)
    return result
}
