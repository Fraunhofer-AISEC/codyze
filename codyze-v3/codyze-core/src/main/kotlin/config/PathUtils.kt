package de.fraunhofer.aisec.codyze_core.config

import java.nio.file.Path

val Path.extensions: String
    get() = fileName?.toString()?.substringAfter('.', "") ?: ""
