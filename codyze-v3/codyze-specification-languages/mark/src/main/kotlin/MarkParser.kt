package de.fraunhofer.aisec.codyze.specification_languages.mark

import de.fraunhofer.aisec.codyze.config.DisabledMarkRulesValue
import de.fraunhofer.aisec.codyze.markmodel.Mark
import de.fraunhofer.aisec.codyze.markmodel.MarkModelLoader
import de.fraunhofer.aisec.mark.XtextParser
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Parse the given [markFiles] into a [Mark] model.
 *
 * @param markFiles load all mark entities/rules in these files
 * @param packageToDisabledMarkRules skip specified files during loading TODO!
 */
@OptIn(ExperimentalTime::class)
fun Mark.from(
    markFiles: List<Path>,
    packageToDisabledMarkRules: Map<String?, DisabledMarkRulesValue?> = emptyMap() // TODO!
): Mark {
    logger.info { "Parsing MARK files..." }
    val parser = XtextParser()
    val (markModels, parseDuration) =
        measureTimedValue {
            // TODO use file extension from specific executor
            markFiles
                .filter { it.extension == "mark" }
                .forEach { markFile ->
                    logger.info { "Loading MARK from file ${markFile}" }
                    parser.addMarkFile(markFile.toFile())
                }
            parser.parse()
        }
    logger.info { "Done parsing MARK files in ${parseDuration.inWholeMilliseconds} ms" }

    // log all detected parse errors
    for ((key, value) in parser.errors) {
        value?.forEach {
            logger.warn { "Error in ${key.toFileString()}: ${it.line}: ${it.message}" }
        }
    }
    logger.info { "Transforming MARK Xtext to internal format" }
    val (markModel: Mark, markLoadingDuration) =
        measureTimedValue { MarkModelLoader().load(markModels, packageToDisabledMarkRules) }
    logger.info {
        "Done Transforming MARK Xtext to internal format in ${markLoadingDuration.inWholeMilliseconds} ms"
    }
    logger.info { "Loaded ${markModel.entities.size} entities and ${markModel.rules.size} rules." }

    return markModel
}
