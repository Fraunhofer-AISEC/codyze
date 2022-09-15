package de.fraunhofer.aisec.codyze_core.helper

import java.util.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Provides the version for Codyze modules. The versions are read from a properties file.
 */
object VersionPropProvider {
    private const val PROPS_FILE = "codyze.properties"
    private const val DEFAULT_VERSION = "0.0.0-SNAPSHOT"

    private val props = Properties()
    init {
        val file = javaClass.classLoader.getResourceAsStream(PROPS_FILE)
        props.load(file)

        // Check if the correct properties file was loaded
        if (
            !props.containsKey("project.name") || props.getProperty("project.name") != "codyze-v3"
        ) {
            logger.warn("Could not find correct version properties file")
            props.clear()
        }
    }

    /**
     * Get the version of a Codyze module.
     *
     * @param moduleName The name of the module
     */
    fun getVersion(moduleName: String): String {
        // Append ".version" to the key since it is stored that way
        val propKey = "$moduleName.version"
        return props.getProperty(propKey, DEFAULT_VERSION)
    }
}
