package de.fraunhofer.aisec.codyze.specification_languages.mark

import de.fraunhofer.aisec.codyze.legacy.config.DisabledMarkRulesValue
import de.fraunhofer.aisec.codyze.legacy.markmodel.Mark
import de.fraunhofer.aisec.codyze.legacy.markmodel.MarkModelLoader
import de.fraunhofer.aisec.codyze_core.util.fromInstant
import de.fraunhofer.aisec.mark.XtextParser
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.div
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
    packageToDisabledMarkRules: Map<String?, DisabledMarkRulesValue?> = emptyMap()
): Mark {
    var markDescriptionFile: Path

    var start = Duration.fromInstant(Instant.now())
    logger.info { "Parsing MARK files..." }
    val parser = XtextParser()
    markFiles.forEach { markFile ->
        logger.info { "Loading MARK from file ${markFile}" }
        parser.addMarkFile(markFile.toFile())
        // markDescriptionFile = markFile.parent.div(File.separator).div("findingDescription.json")
        // // TODO!
    }
    val markModels = parser.parse()
    logger.info { "Done parsing MARK files in ${ start - Duration.fromInstant(Instant.now()) } ms" }

    // log all detected parse errors
    for ((key, value) in parser.errors) {
        value?.forEach {
            logger.warn { "Error in ${key.toFileString()}: ${it.line}: ${it.message}" }
        }
    }

    start = Duration.fromInstant(Instant.now())
    logger.info { "Transforming MARK Xtext to internal format" }
    val markModel = MarkModelLoader().load(markModels, packageToDisabledMarkRules)
    logger.info {
        "Done Transforming MARK Xtext to internal format in ${ start - Duration.fromInstant(Instant.now()) } ms"
    }
    logger.info { "Loaded ${markModel.entities.size} entities and ${markModel.rules.size} rules." }

    // TODO: what is this description file?!
    //    if (markDescriptionFile != null && markDescriptionFile.exists()) {
    //        getInstance.getInstance().init(markDescriptionFile)
    //    } else {
    //        logger.info("MARK description file does not exist")
    //    }

    return markModel
}
