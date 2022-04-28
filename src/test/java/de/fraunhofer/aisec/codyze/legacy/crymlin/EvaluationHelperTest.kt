package de.fraunhofer.aisec.codyze.legacy.crymlin

import de.fraunhofer.aisec.codyze.legacy.analysis.markevaluation.followNextDFG
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.sarif.PhysicalLocation
import de.fraunhofer.aisec.cpg.sarif.Region
import java.net.URI
import kotlin.test.*
import org.junit.jupiter.api.Test

class EvaluationHelperTest {
    @Test
    fun followNextDFGTest() {
        val node = Node()
        node.id = 0
        node.location = PhysicalLocation(URI("0"), Region(0, 0, 0, 0))

        val expectedResult = mutableListOf<Node>()
        var current = node
        for (i in 1..4) {
            val next = Node()
            next.id = i.toLong()
            next.location = PhysicalLocation(URI("$i"), Region(i, i, i, i))
            if (i <= 3) expectedResult.add(next)

            current.addNextDFG(next)
            current = next
        }

        val result = node.followNextDFG { n -> n.id == 3.toLong() }
        assertNotNull(result)
        assertEquals(expectedResult.size, result.size, "Size was not as expected")
        for (n in expectedResult) {
            assertContains(result, n, "Did not contain node $n")
        }
    }
}
