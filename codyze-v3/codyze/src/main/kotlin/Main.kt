package de.fraunhofer.aisec.codyze

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.CliktHelpFormatter
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.*
import de.fraunhofer.aisec.codyze.options.configFileOption
import de.fraunhofer.aisec.codyze.source.JsonValueSource
import java.io.IOException
import java.net.URL
import java.nio.file.Path
import java.util.*
import java.util.jar.Attributes
import java.util.jar.Manifest
import kotlin.io.path.Path
import mu.KotlinLogging
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin

private val logger = KotlinLogging.logger {}

/**
 * A [CliktCommand] to parse the --config option.
 *
 * This class is only used as a pre-parser to parse the --config option and provide the received
 * config[[Path]] as context to the [[CodyzeCli]] command.
 */
@Suppress("UNUSED")
class ConfigFileParser : CliktCommand(treatUnknownOptionsAsArgs = true) {
    val configFile: Path? by configFileOption()
    val arguments by
        argument()
            .multiple() // necessary when using 'treatUnknownOptionsAsArgs'. Contains all given
    // arguments except for configFile.
    override fun run() {} // does nothing because this command is only used to parse the config file
    // for the [[Codyze]] command.
}

/**
 * Main [CliktCommand]. Provides some options to all included subcommands.
 *
 * @param configFile The configFile is actually parsed in the [ConfigFileParser] command and then
 * passed to this class as an argument
 */
class CodyzeCli(val configFile: Path? = null) :
    CliktCommand(help = "Codyze finds security flaws in source code", printHelpOnEmptyArgs = true) {
    init {
        versionOption(Codyze.version, names = setOf("--version", "-V"))
        context {
            if (configFile != null)
                valueSource = JsonValueSource.from(configFile, requireValid = true)
            helpFormatter = CliktHelpFormatter(showDefaultValues = true, requiredOptionMarker = "*")
        }
    }

    /**
     * Storage for constant application information.
     *
     * Note: Using `object` to keep information as singleton and prevent initialization on each
     * object creation of [CodyzeCli].
     */
    private object Codyze {
        /** Name of our application. */
        private const val IMPLEMENTATION_TITLE: String = "Codyze v3"

        /** Default version to use, when no explicit version has been set. */
        private const val DEFAULT_VERSION: String = "0.0.0-SNAPSHOT"

        /**
         * Determine version from system property `codyze-v3-version` or from Codyzes' JAR file
         * manifests.
         *
         * Note: Using `lazy` delegate to calculate property once, when it is needed. Reduces
         * overhead by not reading in all manifests on the classpath multiple times.
         */
        val version: String by lazy {
            System.getProperty("codyze-v3-version")
                ?: run {
                    var v = DEFAULT_VERSION
                    val resources: Enumeration<URL> =
                        CodyzeCli::class.java.classLoader.getResources("META-INF/MANIFEST.MF")
                    while (resources.hasMoreElements()) {
                        val url = resources.nextElement()
                        try {
                            val manifest = Manifest(url.openStream())
                            val mainAttributes = manifest.mainAttributes
                            if (
                                IMPLEMENTATION_TITLE ==
                                    mainAttributes.getValue(Attributes.Name.IMPLEMENTATION_TITLE)
                            ) {
                                v = mainAttributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION)
                                break
                            }
                        } catch (ex: IOException) {
                            logger.trace { "Unable to read from $url: $ex" }
                        }
                    }
                    v
                }
        }
    }

    override fun run() {
        echo("In CodyzeCli")
    } // TODO: change to NoOpCliktCommand?
}

/** Entry point for Codyze. Hands over control to the chosen subcommand immediately. */
fun main(args: Array<String>) {
    // TODO: move this to the CodyzeCli.run or AnalysisServer.init?
    // TODO: if we move it there, we cannot use Koin to inject the subcommands...
    startKoin { // Initialize the koin dependency injection
        // use Koin logger
        printLogger()
        // declare modules
        modules(executorModule, subcommandModule)
    }

    val configFileParser =
        ConfigFileParser() // create a pre-parser that only parses the config file option
    var codyzeCli =
        CodyzeCli() // create a default codyze parser -> this will be used if the user does not
    // specify a config file
    try {
        configFileParser.parse(args)
        configFileParser.configFile?.let {
            codyzeCli = CodyzeCli(configFile = it)
        } // recreate the codyze parser if the user used the config file option
    } finally {
        codyzeCli
            .subcommands(
                getKoin().getAll<CliktCommand>()
            ) // use koin DI to register all available subcommands
            .main(
                args
            ) // parse the given arguments and run the <run> method of the chosen subcommand.
    }
}
