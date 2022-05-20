package de.fraunhofer.aisec.codyze

import de.fraunhofer.aisec.codyze.legacy.analysis.Finding
import de.fraunhofer.aisec.codyze.legacy.config.Configuration
import de.fraunhofer.aisec.cpg.TranslationConfiguration
import de.fraunhofer.aisec.cpg.TranslationManager
import de.fraunhofer.aisec.cpg.TranslationResult

// @FW
class Project(config : Configuration) {

    // TODO are there more fields or methods required?

    // TODO give good name
    fun doStuff() : List<de.fraunhofer.aisec.codyze.sarif.schema.Result> {
        // TODO must be initialized
        val executor : Executor() // TODO use concrete implementation

        // TODO get it using our configuration
        val translationConfiguration : TranslationConfiguration
        val translationManager = TranslationManager.builder().config(translationConfiguration).build()
        val translationResult = translationManager.analyze().get()

        return executor.evaluate(translationResult)
    }
}