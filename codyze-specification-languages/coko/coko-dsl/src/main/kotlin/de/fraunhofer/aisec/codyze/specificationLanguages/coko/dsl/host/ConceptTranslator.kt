package de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl.host

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.GroupingOp
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.name
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.host.StringScriptSource

class ConceptTranslator() {
    private val conceptTranslations: MutableMap<Path, String> = mutableMapOf()

    fun transformConceptFile(specFile: Path): StringScriptSource {
        val absolutePath = specFile.normalize().absolute()
        val cachedTranslation = conceptTranslations[absolutePath]
        if(cachedTranslation != null)
            return StringScriptSource(cachedTranslation, absolutePath.fileName.name)

        val fileScriptSource = FileScriptSource(specFile.toFile())

        val fileText = fileScriptSource.text
        val preliminaryScriptText = fileText
            // Remove all comments
            // TODO: Is there a way to specify that all Regexes should not include matches found in comments?
            .replace(Regex("//.*\\n"), "\n")
            // Handle all `op` keywords that represent an operationPointer (`'op' <name> '=' <name> ('|' <name>)*`)
            .replace(Regex("\\s+op\\s+.+=.+\\s")) { opPointerMatchResult ->
                handleOpPointer(opPointerMatchResult, fileText)
            }

        val scriptText = preliminaryScriptText
            // Replace all `concept` keywords with `interface` and ensure that the name is capitalized
            .replace(Regex("concept\\s+.")) {
                makeLastCharUpperCase(
                    it.value.replace("concept", "interface")
                )
            }
            // Replace all `enum` keywords with `enum class` and ensure that the name is capitalized
            .replace(Regex("enum\\s+.")) {
                makeLastCharUpperCase(
                    it.value.replace("enum", "enum class")
                )
            }
            // Ensure that all type names are capitalized
            .replace(Regex(":\\s*[a-zA-Z]")) {
                it.value.uppercase()
            }
            // Replace all `op` keywords that represent an operation with `fun` and add types for the parameters
            .replace(Regex("\\s+op\\s.+\\)\\s?")) { opMatchResult ->
                opMatchResult.value
                    .replace("op", "fun")
                    .replace(Regex(",( )?.*\\.\\.\\.")) {
                        it.value.dropLast(3).replace(Regex(",( )?",), ", vararg ")
                    }
                    .replace(",", ": Any?,")
                    .replace(Regex("[^(]\\)")) {
                        "${it.value.dropLast(1)}: Any?): Op"
                    }
                    .plus("\n")
            }
            // Replace all `var` keywords with `val` and add a type if none is given
            .replace(Regex("\\s+var\\s+.+\\s")) { varMatchResult ->
                val property = varMatchResult.value.replace("var", "val").dropLast(1)
                if (property.contains(':')) {
                    "$property\n"
                } else {
                    "$property: Any\n"
                }
            }

        conceptTranslations[absolutePath] = scriptText
        return StringScriptSource(scriptText, fileScriptSource.name)
    }


    /**
     * This translates a match of a `op` keyword that represent an operationPointer (`'op' <name> '=' <name> ('|' <name>)*`)
     * into a Kotlin function that returns a [GroupingOp].
     *
     * Example:
     * ```
     * op log = info | warn
     * op info(msg)
     * op warn(msg)
     * ```
     * is translated into
     * ```
     * fun log(msg: Any?): GroupingOp = opGroup(info(msg), warn(msg))
     * ```
     */
    private fun handleOpPointer(opPointerMatchResult: MatchResult, fileText: String): String {
        val preliminaryResult = opPointerMatchResult.value
            // remove the whitespace character at the end of the matchResult
            .dropLast(1)
            // Replace all `op` keywords with `fun`
            .replace("op", "fun")

        // The index of the '{' character that starts the body of the concept that the "op" resides in
        val conceptBodyStart = fileText.lastIndexOf('{', opPointerMatchResult.range.first)
        // The index of the '}' character that ends the body of the concept that the "op" resides in
        val conceptBodyEnd = fileText.indexOf('}', opPointerMatchResult.range.last)

        // The body of the concept as string
        val conceptBody = fileText.subSequence(conceptBodyStart, conceptBodyEnd)

        // Split the definition of the operationPointer at the `=`
        val (firstHalf, secondHalf) = preliminaryResult.split(Regex("\\s*=\\s*"), limit = 2)
        // Split the grouped ops into separate strings
        val opNames = secondHalf.split(Regex("\\s*\\|\\s*"))

        val sb = StringBuilder(firstHalf)

        sb.append("(")
        // Find the definitions of the ops that are used for this opPointer
        val opDefinitions = opNames.map { (Regex("\\s+op\\s+$it\\(.*\\)").find(conceptBody)?.value ?: "") }
        // Append parameters needed for the function which are built by combining
        // the parameters of the grouped ops
        sb.append(buildFunctionParameters(opDefinitions))
        sb.append("): Op = opGroup(")

        // Append calls to ops
        val functionCalls = opDefinitions.map { opDefinition ->
            // remove the `op` keyword and all `...`
            opDefinition.replace(Regex("(\\s+op\\s+)|(\\.\\.\\.)"), "")
        }
        sb.append(functionCalls.joinToString())

        sb.append(")\n")
        return sb.toString()
    }

    /**
     * This combines all parameters of the ops in [opDefinitions] into a set of parameters
     * that are needed to call all functions representing the ops and combines them into a string.
     */
    private fun buildFunctionParameters(opDefinitions: List<String>): String {
        // Find out all needed parameters
        val functionParameters = opDefinitions.flatMap { opDefinition ->
            // find the parameters that are used for the op
            val opParameters = Regex("\\(.*\\)").find(opDefinition)?.value ?: ""
            // remove the `(` and `)` and then split the parameters
            removeFirstAndLastChar(opParameters).split(Regex("\\s*,\\s*"))
        }
            .filter { it.isNotEmpty() }
            .toSet()
            // add types to the parameters
            .map {
                if (it.endsWith("...")) {
                    // translate vararg parameters to the correct Kotlin syntax
                    "vararg ${it.substring(0, it.length - 3)}: Any?"
                } else {
                    "$it: Any?"
                }
            }

        return functionParameters.joinToString()
    }

    private fun makeLastCharUpperCase(string: String): String {
        val lastCharAsUpper = string.last().uppercaseChar()
        return string.dropLast(1) + lastCharAsUpper
    }

    private fun removeFirstAndLastChar(string: String): String =
        if (string.isEmpty()) {
            string
        } else {
            string.substring(1, string.length - 1)
        }

}

