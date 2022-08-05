package de.fraunhofer.aisec.codyze_core.config

import java.nio.file.Path

public val Path.extensions: String
    get() = fileName?.toString()?.substringAfter('.', "") ?: ""
