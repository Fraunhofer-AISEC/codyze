/*
 * Copyright (c) 2022, Fraunhofer AISEC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
