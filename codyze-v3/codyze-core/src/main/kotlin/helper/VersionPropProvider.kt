package de.fraunhofer.aisec.codyze_core.helper

import java.util.*

object VersionPropProvider {
    fun getVersion(): String {
        val props = Properties()
        val file = javaClass.classLoader.getResourceAsStream("project.properties")
        props.load(file)
        return props.getProperty("project.version")
    }
}
