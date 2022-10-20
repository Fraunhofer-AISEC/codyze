package de.fraunhofer.aisec.codyze_core.wrapper

import de.fraunhofer.aisec.codyze_core.config.Configuration

interface BackendConfiguration {
    fun normalize(configuration: Configuration): BackendConfiguration
}
