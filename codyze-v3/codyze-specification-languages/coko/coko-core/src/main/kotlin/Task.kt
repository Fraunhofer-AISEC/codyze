package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core

interface Task {
    fun perform(work: () -> Unit)
    fun dependsOn(taskNotation: Any)
}
