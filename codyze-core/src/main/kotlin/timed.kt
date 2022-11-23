package de.fraunhofer.aisec.codyze_core

import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue
import mu.KotlinLogging

val log = KotlinLogging.logger {}

@OptIn(ExperimentalTime::class)
/**
 * Simply helper function to log how long a given task took.
 * @param message The message to log. The resulting log has the format: '{message} {time the task
 * took in milliseconds} ms.'
 * @return The result of the given task
 */
inline fun <reified T> timed(message: String? = null, block: () -> T): T {
    val (result: T, blockDuration: Duration) = measureTimedValue { block() }
    log.debug { message + " ${ blockDuration.inWholeMilliseconds } ms." }
    return result
}
