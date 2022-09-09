package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core

typealias Action<T> = T.() -> Unit

interface ExtensionAware

interface Project : ExtensionAware {
    fun task(name: String, configuration: Action<Any>)
}

open class CokoProject : Project {
    override fun task(name: String, configuration: Action<Any>) =
        TODO("task is an artifact from groddler")
}
