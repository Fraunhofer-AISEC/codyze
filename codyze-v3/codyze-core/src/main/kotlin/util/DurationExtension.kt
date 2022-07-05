package de.fraunhofer.aisec.codyze_core.util

import java.time.Instant
import kotlin.time.Duration

fun Duration.Companion.fromInstant(instant: Instant): Duration {
    return parseIsoString(instant.toString())
}
