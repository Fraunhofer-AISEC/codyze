package de.fraunhofer.aisec.codyze.specification_languages.coko.nwt.scripting

import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.defaultImports

@KotlinScript(fileExtension = "coko.kts", compilationConfiguration = CoKoScriptConfiguration::class)
abstract class CoKoScript

object CoKoScriptConfiguration :
    ScriptCompilationConfiguration({
        defaultImports("de.fraunhofer.aisec.codyze.specification_languages.coko.nwt.dsl.nwt")
    })
