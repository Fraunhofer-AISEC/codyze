package de.fraunhofer.aisec.codyze_core.config.options

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import java.io.File

fun CliktCommand.configFileOption() = option(
    "--config",
    help =
    "Parse configuration settings from this file. If no file path is" +
            "specified, Codyze will try to load the configuration file from the default path",
)
    .file(mustExist = true, canBeDir = false, mustBeReadable = true)
    .default(File(System.getProperty("user.dir"), "config.json"))