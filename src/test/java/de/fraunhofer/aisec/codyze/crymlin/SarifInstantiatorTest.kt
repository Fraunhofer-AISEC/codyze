package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.SarifInstantiator
import org.junit.jupiter.api.Test

internal class SarifInstantiatorTest {
    @Test
    fun testOutput() {
        val s = SarifInstantiator()

        println(s.getSarif())
    }
}
