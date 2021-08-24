package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.TypestateMode
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

class OrderIssueTest : AbstractMarkTest() {

    @Test
    fun checkOrderWPDS() {
        this.tsMode = TypestateMode.WPDS
        val results = performTest("unittests/order_issue.c", "unittests/order_issue.mark")

        // only good findings
        assertTrue(results.all { !it.isProblem })
    }

    @Test
    fun checkOrderNFA() {
        this.tsMode = TypestateMode.NFA
        val results = performTest("unittests/order_issue.c", "unittests/order_issue.mark")

        // only good findings
        assertTrue(results.all { !it.isProblem })
    }
}
