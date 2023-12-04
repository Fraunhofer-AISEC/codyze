package de.fraunhofer.aisec.codyze.plugins.executor

import java.io.File
import java.nio.file.Path

interface Executor {
    /**
     * Executes the respective analysis tool.
     * @param target The files to be analyzed
     * @param output The location of the results
     */
    fun execute(target: List<Path>, output: File)
}