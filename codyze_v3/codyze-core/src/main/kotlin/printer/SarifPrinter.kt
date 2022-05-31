package de.fraunhofer.aisec.codyze_core.printer

import de.fraunhofer.aisec.codyze.analysis.Finding
import de.fraunhofer.aisec.codyze.sarif.SarifInstantiator
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
