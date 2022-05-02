package de.fraunhofer.aisec.codyze.legacy.crymlin

import de.fraunhofer.aisec.codyze.legacy.analysis.TypestateMode
import kotlin.test.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class OrderTestCStyle : AbstractMarkTest() {

    // this test case fails because currently, the WPDS cannot handle the direct assignment of the
    // variable to the call of the rule
    @Disabled
    @Test
    fun checkOrderWPDS_foo1() {
        this.tsMode = TypestateMode.WPDS
        val results =
            performTest("legacy/unittests/order_foo1.c", "legacy/unittests/order_cstyle.mark")

        // only good findings
        assertTrue(results.all { !it.isProblem })
    }

    @Test
    fun checkOrderWPDS_foo2() {
        this.tsMode = TypestateMode.WPDS
        val results =
            performTest("legacy/unittests/order_foo2.c", "legacy/unittests/order_cstyle.mark")

        // only good findings
        assertTrue(results.all { !it.isProblem })
    }

    // still fails, because foo1 fails
    @Disabled
    @Test
    fun checkOrderWPDS_both() {
        // fail
        this.tsMode = TypestateMode.WPDS
        val results = performTest("legacy/unittests/order.c", "legacy/unittests/order_cstyle.mark")

        // only good findings
        assertTrue(results.all { !it.isProblem })
    }

    @Test
    fun checkOrderNFA_foo1() {
        this.tsMode = TypestateMode.DFA
        val results =
            performTest("legacy/unittests/order_foo1.c", "legacy/unittests/order_cstyle.mark")

        // only good findings
        assertTrue(results.all { !it.isProblem })
    }
    @Test
    fun checkOrderNFA_foo2() {
        this.tsMode = TypestateMode.DFA
        val results =
            performTest("legacy/unittests/order_foo2.c", "legacy/unittests/order_cstyle.mark")

        // only good findings
        assertTrue(results.all { !it.isProblem })
    }
    @Test
    fun checkOrderNFA_both() {
        this.tsMode = TypestateMode.DFA
        val results = performTest("legacy/unittests/order.c", "legacy/unittests/order_cstyle.mark")

        // only good findings
        assertTrue(results.all { !it.isProblem })
    }
}
