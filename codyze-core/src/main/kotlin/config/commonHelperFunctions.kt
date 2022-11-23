package de.fraunhofer.aisec.codyze_core.config

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.streams.asSequence

/**
 * Combine all given sources by going through the given Paths recursively.
 *
 * This function normalizes the given paths to filter out duplicates and only returns files and not
 * directories.
 */
fun combineSources(vararg sources: List<Path>): Set<Path> {
    val allSources = mutableSetOf<Path>()
    sources.toList().flatten().forEach { path ->
        // it is necessary to make the paths absolute because this function is used to combine paths
        // that might be
        // relative to different paths (relative to config file path <-> relative to CWD)
        allSources.addAll(
            Files.walk(path.normalize().toAbsolutePath()).asSequence().filter { it.isRegularFile() }
        )
    }
    return allSources
}
