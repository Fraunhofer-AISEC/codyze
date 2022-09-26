package de.fraunhofer.aisec.codyze_core.config

import de.fraunhofer.aisec.codyze_core.output.localization
import java.nio.file.Path
import kotlin.io.path.isRegularFile

fun validateSpec(spec: List<Path>) {
    require(spec.all { it.isRegularFile() }) { localization.pathsMustBeFiles() }
    // require(spec.all { it.extension == spec[0].extension }) { localization.invalidSpecFileType()
    // }  // disabled for now. The parsers of the Executors must filter the files by file type
}
