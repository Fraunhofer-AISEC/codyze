package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.TypestateMode
import org.junit.jupiter.api.Test

class OrderIssueTest : AbstractMarkTest() {

    @Test
    fun checkOrderWPDS() {
        this.tsMode = TypestateMode.WPDS
        val results = performTest("unittests/order_issue.c", "unittests/order_issue.mark")
        expected(results, "TODO")
    }

    @Test
    fun checkOrderNFA() {
        this.tsMode = TypestateMode.NFA
        val results = performTest("unittests/order_issue.c", "unittests/order_issue.mark")
        expected(results, "TODO")
    }
}
