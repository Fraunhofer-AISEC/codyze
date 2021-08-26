package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.TypestateMode
import kotlin.test.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class OrderIssueTest : AbstractMarkTest() {

    // this test case fails because currently, the WPDS cannot handle the direct assignment of the
    // variable to the call of the rule
    @Disabled
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

    // still fails, because foo1 fails
    @Disabled
    @Test
    fun checkOrderWPDS_both() {
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
