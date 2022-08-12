package de.fraunhofer.aisec.codyze.specification_languages.nwt.scripting

import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.defaultImports

@KotlinScript(
    displayName = "NWT Example",
    fileExtension = "nwt.kts",
    compilationConfiguration = NwtScriptConfiguration::class
)
abstract class NwtScript

object NwtScriptConfiguration :
    ScriptCompilationConfiguration({
        defaultImports("de.fraunhofer.aisec.codyze.specification_languages.nwt.dsl.nwt")
    })
