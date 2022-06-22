package de.fraunhofer.aisec.codyze_core.config.options

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.streams.asSequence

/**
 * Combine all given sources by going through the given Paths recursively.
 *
 * This function normalizes the given paths to filter out duplicates and only returns files and not directories.
 */
internal fun combineSources(vararg sources: List<Path>): Set<Path> {
    val allSources = mutableSetOf<Path>()
    sources.toList().flatten().forEach { path ->
        allSources.addAll(Files.walk(path.normalize().toAbsolutePath()).asSequence().filter { it.isRegularFile() })
    }
    return allSources
}
