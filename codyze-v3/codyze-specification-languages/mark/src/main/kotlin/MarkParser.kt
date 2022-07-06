package de.fraunhofer.aisec.codyze.specification_languages.mark

import de.fraunhofer.aisec.codyze.legacy.config.DisabledMarkRulesValue
import de.fraunhofer.aisec.codyze.legacy.markmodel.Mark
import de.fraunhofer.aisec.codyze.legacy.markmodel.MarkModelLoader
import de.fraunhofer.aisec.mark.XtextParser
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.extension
import kotlin.time.Duration
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Parse the given [markFiles] into a [Mark] model.
 *
 * @param markFiles load all mark entities/rules in these files
 * @param packageToDisabledMarkRules skip specified files during loading TODO!
 */
fun Mark.from(
    markFiles: List<Path>,
    packageToDisabledMarkRules: Map<String?, DisabledMarkRulesValue?> = emptyMap() // TODO!
): Mark {
    var start = Instant.now()
    logger.info { "Parsing MARK files..." }
    val parser = XtextParser()
    // TODO use file extension from specific executor
    markFiles
        .filter { it.extension == "mark" }
        .forEach { markFile ->
            logger.info { "Loading MARK from file ${markFile}" }
            parser.addMarkFile(markFile.toFile())
        }
    val markModels = parser.parse()
    logger.info {
        "Done parsing MARK files in ${ java.time.Duration.between(start, Instant.now()) } ms"
    }

    // log all detected parse errors
    for ((key, value) in parser.errors) {
        value?.forEach {
            logger.warn { "Error in ${key.toFileString()}: ${it.line}: ${it.message}" }
        }
    }

    start = Instant.now()
    logger.info { "Transforming MARK Xtext to internal format" }
    val markModel = MarkModelLoader().load(markModels, packageToDisabledMarkRules)
    logger.info {
        "Done Transforming MARK Xtext to internal format in ${ java.time.Duration.between(start, Instant.now()) } ms"
    }
    logger.info { "Loaded ${markModel.entities.size} entities and ${markModel.rules.size} rules." }

    return markModel
}
