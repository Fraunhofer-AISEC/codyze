package de.fraunhofer.aisec.codyze_core

import de.fraunhofer.aisec.codyze_core.config.Configuration
import io.github.detekt.sarif4k.*

// switching projects inside the IDE
class Project(val config: Configuration) {

    // TODO are there more fields or methods required?

    // TODO give good name
    fun doStuff(): SarifSchema210 {
        // TODO must be initialized
        //            val executor : Executor() // TODO use concrete implementation
        //
        //            // TODO get it using our configuration
        //            val translationConfiguration : TranslationConfiguration
        //            val translationManager =
        //     TranslationManager.builder().config(translationConfiguration).build()
        //            val translationResult = translationManager.analyze().get()
        //
        //        return executor.evaluate(translationResult)

        return SarifSchema210(
            schema = "https://json.schemastore.org/sarif-2.1.0.json",
            version = Version.The210,
            runs = listOf(Run(tool = Tool(driver = ToolComponent(name = "Codyze v3"))))
        )

        // complete SARIF model by integrating results, e.g. add "Codyze" as tool name, etc.
        // return or print SARIF model
        // TODO what format should we give to LSP?
    }
}
