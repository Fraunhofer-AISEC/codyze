package de.fraunhofer.aisec.codyze.specification_languages.coko

import java.nio.file.Path
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.host.FileScriptSource

fun Path.toScriptSource(): SourceCode = FileScriptSource(this.toFile())
