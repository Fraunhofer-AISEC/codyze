package de.fraunhofer.aisec.codyze_core

import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    println("Hello World!")
    logger.warn { "Hello World!" }

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications:
    // https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")

    // TODO implement
}
