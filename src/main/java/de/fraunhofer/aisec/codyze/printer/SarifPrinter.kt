package de.fraunhofer.aisec.codyze.printer

import de.fraunhofer.aisec.codyze.analysis.Finding
import de.fraunhofer.aisec.codyze.sarif.SarifInstantiator
import java.io.File
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SarifPrinter(findings: Set<Finding>) : Printer(findings) {
    private var si = SarifInstantiator()

    override var log: Logger = LoggerFactory.getLogger(SarifPrinter::class.java)
    override var output: String = si.toString()

    override fun printToFile(path: String) {
        si.generateOutput(File(path))
        log.info("printed output to file: {}", path)
    }

    init {
        si.pushRun(findings)
    }
}
