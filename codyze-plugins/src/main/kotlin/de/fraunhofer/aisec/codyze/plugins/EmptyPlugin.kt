package de.fraunhofer.aisec.codyze.plugins

import java.io.File
import java.nio.file.Path

/**
 * This class is mapped to unresolved plugin parameters in the [CodyzeOptionGroup].
 * It should not do anything and just exist so the mapping can succeed
 */
class EmptyPlugin : Plugin("None") {
    override fun execute(target: List<Path>, output: File) {
        return
    }
}