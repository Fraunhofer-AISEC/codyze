package de.fraunhofer.aisec.codyze_core.config

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.path
import kotlin.io.path.Path

fun CliktCommand.configFileOption() =
    option(
        "--config",
        help = "Parse configuration settings from this file. If no file path is specified, Codyze will try to load the configuration file from the default path",
    )
        .path(mustExist = true, canBeDir = false, mustBeReadable = true)
        .default(Path(System.getProperty("user.dir"), "codyze.json"))

// TODO: the compiler does not detect changes when changing the message --> requires clean build to
// be recompiled
/**
 * Clikt extension function to provide a 'option.validate' statement for functions that either
 * return [[Any]] or throw an exception instead of returning [[true]]/[[false]].
 */
inline fun <AllT : Any, EachT, ValueT> OptionWithValues<AllT, EachT, ValueT>.validateFromError(
    crossinline errorValidator: (AllT) -> Any
): OptionDelegate<AllT> {
    return validate {
        val (result, message) =
            try {
                errorValidator(it)
                true to ""
            } catch (e: IllegalArgumentException) {
                false to (e.message ?: "")
            }
        require(result) { message }
    }
}
