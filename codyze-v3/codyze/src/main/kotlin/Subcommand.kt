package de.fraunhofer.aisec.codyze

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import de.fraunhofer.aisec.codyze.options.*
import java.nio.file.Path

@Suppress("UNUSED")
abstract class Subcommand(help: String = "", epilog: String = "", name: String? = null, invokeWithoutSubcommand: Boolean = false, printHelpOnEmptyArgs: Boolean = false, helpTags: Map<String, String> = emptyMap(), autoCompleteEnvvar: String? = "", allowMultipleSubcommands: Boolean = false, treatUnknownOptionsAsArgs: Boolean = false):
    CliktCommand(help, epilog, name, invokeWithoutSubcommand, printHelpOnEmptyArgs, helpTags, autoCompleteEnvvar, allowMultipleSubcommands, treatUnknownOptionsAsArgs) {
    // This is only here to correctly display the help message
    private val unusedConfigFile: Path by configFileOption()

    val codyzeOptions by CodyzeOptions()
    val analysisOptions by AnalysisOptions()
    val cpgOptions by CPGOptions()
    val translationOptions by TranslationOptions()
}