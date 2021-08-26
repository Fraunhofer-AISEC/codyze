package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.TypestateMode
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

class OrderIssueTest : AbstractMarkTest() {

    @Test
    fun checkOrderWPDS_foo1() {
        this.tsMode = TypestateMode.WPDS
        val results = performTest("unittests/order_issue_foo1.c", "unittests/order_issue.mark")

        // only good findings
        assertTrue(results.all { !it.isProblem })
    }
    @Test
    fun checkOrderWPDS_foo2() {
        this.tsMode = TypestateMode.WPDS
        val results = performTest("unittests/order_issue_foo2.c", "unittests/order_issue.mark")

        // only good findings
        assertTrue(results.all { !it.isProblem })
    }
    @Test
    fun checkOrderWPDS_both() {
        // TODO this must not pass, if either of the tests checkOrderWPDS_foo1 checkOrderWPDS_foo2
        // fail
        this.tsMode = TypestateMode.WPDS
        val results = performTest("unittests/order_issue.c", "unittests/order_issue.mark")

        // only good findings
        assertTrue(results.all { !it.isProblem })
    }

    @Test
    fun checkOrderNFA_foo1() {
        this.tsMode = TypestateMode.NFA
        val results = performTest("unittests/order_issue_foo1.c", "unittests/order_issue.mark")

        // only good findings
        assertTrue(results.all { !it.isProblem })
    }
    @Test
    fun checkOrderNFA_foo2() {
        this.tsMode = TypestateMode.NFA
        val results = performTest("unittests/order_issue_foo2.c", "unittests/order_issue.mark")

        // only good findings
        assertTrue(results.all { !it.isProblem })
    }
    @Test
    fun checkOrderNFA_both() {
        this.tsMode = TypestateMode.NFA
        val results = performTest("unittests/order_issue.c", "unittests/order_issue.mark")

        // only good findings
        assertTrue(results.all { !it.isProblem })
    }
}
