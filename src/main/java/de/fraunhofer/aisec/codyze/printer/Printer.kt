package de.fraunhofer.aisec.codyze.printer

import de.fraunhofer.aisec.codyze.analysis.Finding
import java.io.FileNotFoundException
import java.io.PrintWriter
import org.slf4j.Logger

abstract class Printer(val findings: Set<Finding>) {
    abstract var output: String
    abstract var log: Logger

    fun printToConsole() {
        print(output)
        log.info("printed output to console")
    }

    fun printToFile(path: String) {
        try {
            PrintWriter(path).use { out -> out.println(output) }
            log.info("printed output to file: {}", path)
        } catch (e: FileNotFoundException) {
            log.error("could not find file path: {}", path)
        }
    }
}
