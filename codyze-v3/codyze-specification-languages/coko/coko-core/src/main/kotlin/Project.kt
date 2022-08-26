package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core

interface Project : ExtensionAware {
    fun task(name: String, configuration: Action<Task>)
}

open class CokoProject : Project {
    override fun task(name: String, configuration: Action<Task>) =
        TODO("task is an artifact from groddler")
}
