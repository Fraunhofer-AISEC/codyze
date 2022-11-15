package de.fraunhofer.aisec.cpg.analysis.fsm

import de.fraunhofer.aisec.codyze.crymlin.MarkLoadOutputTest
import de.fraunhofer.aisec.codyze.markmodel.MarkModelLoader
import de.fraunhofer.aisec.mark.XtextParser
import de.fraunhofer.aisec.mark.markDsl.OrderExpression
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FSMBuilderTest {

    @Test
    fun testSimpleOrder() {
        val resource =
            MarkLoadOutputTest::class
                .java
                .classLoader
                .getResource("unittests/fsm_builder/order.mark")
        assertNotNull(resource)
        val markPoC1 = File(resource.file)
        assertNotNull(markPoC1)
        val parser = XtextParser()
        parser.addMarkFile(markPoC1)
        val markModels = parser.parse()
        val mark = MarkModelLoader().load(markModels)
        assertNotNull(mark)
        var dfa = DFA()
        for (rule in mark.rules) {
            if (rule.statement.ensure.exp is OrderExpression) {
                dfa = FSMBuilder().sequenceToDFA(rule.statement.ensure.exp as OrderExpression)
            }
        }

        val expected = DFA()
        val q1 = expected.addState(isStart = true)
        val q2 = expected.addState()
        val q3 = expected.addState(isAcceptingState = true)
        expected.addEdge(q1, q2, "start", "cm")
        expected.addEdge(q2, q3, "finish", "cm")

        assertEquals(expected, dfa)
    }

    @Test
    fun testLoopOrder() {
        val resource =
            MarkLoadOutputTest::class
                .java
                .classLoader
                .getResource("unittests/fsm_builder/order2.mark")
        assertNotNull(resource)
        val markPoC1 = File(resource.file)
        assertNotNull(markPoC1)
        val parser = XtextParser()
        parser.addMarkFile(markPoC1)
        val markModels = parser.parse()
        val mark = MarkModelLoader().load(markModels)
        assertNotNull(mark)
        var dfa = DFA()
        for (rule in mark.rules) {
            if (rule.statement.ensure.exp is OrderExpression) {
                dfa = FSMBuilder().sequenceToDFA(rule.statement.ensure.exp as OrderExpression)
            }
        }

        val expected = DFA()
        val q1 = expected.addState(isStart = true)
        val q2 = expected.addState()
        val q3 = expected.addState()
        val q4 = expected.addState()
        val q5 = expected.addState()
        val q6 = expected.addState(isAcceptingState = true)
        val q7 = expected.addState(isAcceptingState = true)
        expected.addEdge(q1, q2, "createOp", "cm")
        expected.addEdge(q2, q3, "initOp", "cm")
        expected.addEdge(q3, q4, "startOp", "cm")
        expected.addEdge(q4, q5, "processOp", "cm")
        expected.addEdge(q4, q6, "finishOp", "cm")
        expected.addEdge(q5, q5, "processOp", "cm")
        expected.addEdge(q5, q6, "finishOp", "cm")
        expected.addEdge(q6, q4, "startOp", "cm")
        expected.addEdge(q6, q7, "resetOp", "cm")

        assertEquals(expected, dfa)
    }

    @Test
    fun testBranchOrder() {
        val resource =
            MarkLoadOutputTest::class
                .java
                .classLoader
                .getResource("unittests/fsm_builder/order3.mark")
        assertNotNull(resource)
        val markPoC1 = File(resource.file)
        assertNotNull(markPoC1)
        val parser = XtextParser()
        parser.addMarkFile(markPoC1)
        val markModels = parser.parse()
        val mark = MarkModelLoader().load(markModels)
        assertNotNull(mark)
        var dfa = DFA()
        for (rule in mark.rules) {
            if (rule.statement.ensure.exp is OrderExpression) {
                dfa = FSMBuilder().sequenceToDFA(rule.statement.ensure.exp as OrderExpression)
            }
        }

        val expected = DFA()
        val q1 = expected.addState(isStart = true)
        val q2 = expected.addState()
        val q3 = expected.addState(isAcceptingState = true)
        val q4 = expected.addState()
        val q5 = expected.addState()
        val q6 = expected.addState(isAcceptingState = true)
        val q7 = expected.addState(isAcceptingState = true)
        expected.addEdge(q1, q2, "init", "cm")
        expected.addEdge(q2, q3, "bigStep", "cm")
        expected.addEdge(q2, q4, "start", "cm")
        expected.addEdge(q3, q3, "bigStep", "cm")
        expected.addEdge(q3, q4, "start", "cm")
        expected.addEdge(q3, q7, "reset", "cm")
        expected.addEdge(q4, q5, "process", "cm")
        expected.addEdge(q4, q6, "finish", "cm")
        expected.addEdge(q5, q5, "process", "cm")
        expected.addEdge(q5, q6, "finish", "cm")
        expected.addEdge(q6, q3, "bigStep", "cm")
        expected.addEdge(q6, q4, "start", "cm")
        expected.addEdge(q6, q7, "reset", "cm")

        assertEquals(expected, dfa)
    }

    @Test
    fun testFailOrder() {
        val resource =
            MarkLoadOutputTest::class
                .java
                .classLoader
                .getResource("unittests/fsm_builder/order_fail.mark")
        assertNotNull(resource)
        val markPoC1 = File(resource.file)
        assertNotNull(markPoC1)
        val parser = XtextParser()
        parser.addMarkFile(markPoC1)
        val markModels = parser.parse()
        val mark = MarkModelLoader().load(markModels)
        assertNotNull(mark)
        for (rule in mark.rules) {
            if (rule.statement.ensure.exp is OrderExpression) {
                assertThrows<FSMBuilderException> {
                    FSMBuilder().sequenceToDFA(rule.statement.ensure.exp as OrderExpression)
                }
            }
        }
    }
}
