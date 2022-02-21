package de.fraunhofer.aisec.codyze.printer

import com.fasterxml.jackson.databind.ObjectMapper
import de.fraunhofer.aisec.codyze.analysis.Finding
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LegacyPrinter(findings: Set<Finding>) : Printer(findings) {
    private val mapper = ObjectMapper()

    override var log: Logger = LoggerFactory.getLogger(LegacyPrinter::class.java)
    override var output: String = mapper.writeValueAsString(findings)
}
