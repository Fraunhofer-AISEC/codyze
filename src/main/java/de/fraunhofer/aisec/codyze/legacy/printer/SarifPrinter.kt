package de.fraunhofer.aisec.codyze.legacy.printer

import de.fraunhofer.aisec.codyze.legacy.analysis.Finding
import de.fraunhofer.aisec.codyze.legacy.sarif.SarifInstantiator
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SarifPrinter(findings: Set<Finding>) : Printer(findings) {
    private var si = SarifInstantiator()

    override var log: Logger = LoggerFactory.getLogger(SarifPrinter::class.java)
    override var output: String = si.toString()

    init {
        si.pushRun(findings)
        output = si.toString()
    }
}
