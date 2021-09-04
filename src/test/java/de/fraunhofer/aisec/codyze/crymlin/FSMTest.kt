package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.markmodel.MRule
import de.fraunhofer.aisec.codyze.markmodel.Mark
import de.fraunhofer.aisec.codyze.markmodel.MarkModelLoader
import de.fraunhofer.aisec.codyze.markmodel.fsm.FSM
import de.fraunhofer.aisec.mark.XtextParser
import de.fraunhofer.aisec.mark.markDsl.MarkDslFactory
import de.fraunhofer.aisec.mark.markDsl.OrderExpression
import de.fraunhofer.aisec.mark.markDsl.impl.MarkDslFactoryImpl
import java.io.*
import org.junit.jupiter.api.*

internal class FSMTest {
    @Test
    fun parseTest() {
        Assertions.assertEquals(5, mark!!.rules.size) // 5 total
        Assertions.assertEquals(
            3,
            mark!!
                .rules
                .stream()
                .filter { rule: MRule ->
                    rule.statement != null &&
                        rule.statement.ensure != null &&
                        rule.statement.ensure.exp is OrderExpression
                }
                .count()
        ) // 3 order
        Assertions.assertEquals(
            1,
            mark!!.rules.stream().filter { x: MRule -> x.name == "BlockCiphers" }.count()
        )
        Assertions.assertEquals(
            1,
            mark!!.rules.stream().filter { x: MRule -> x.name == "UseOfBotan_CipherMode" }.count()
        )
        Assertions.assertEquals(
            1,
            mark!!
                .rules
                .stream()
                .filter { x: MRule -> x.name == "SimpleUseOfBotan_CipherMode" }
                .count()
        )
        Assertions.assertEquals(
            1,
            mark!!
                .rules
                .stream()
                .filter { x: MRule -> x.name == "SimpleUseOfBotan2_CipherMode" }
                .count()
        )
        Assertions.assertEquals(
            1,
            mark!!.rules.stream().filter { x: MRule -> x.name == "UseRandomIV" }.count()
        )
    }

    //  @Test
    //  void fsmTest() {
    //
    //    FSM.clearDB();
    //    for (MRule rule : mark.getRules()) {
    //      if (rule.getStatement() != null
    //          && rule.getStatement().getEnsure() != null
    //          && rule.getStatement().getEnsure().getExp() instanceof OrderExpression) {
    //        OrderExpression inner = (OrderExpression) rule.getStatement().getEnsure().getExp();
    //        FSM fsm = new FSM();
    //        fsm.sequenceToFSM(inner.getExp());
    //        fsm.pushToDB();
    //      }
    //    }
    //  }
    private fun load(ruleName: String): FSM {
        val opt = mark!!.rules.stream().filter { x: MRule -> x.name == ruleName }.findFirst()
        Assertions.assertTrue(opt.isPresent)
        val rule = opt.get()
        Assertions.assertNotNull(rule.statement)
        Assertions.assertNotNull(rule.statement.ensure)
        Assertions.assertNotNull(rule.statement.ensure.exp)
        Assertions.assertTrue(rule.statement.ensure.exp is OrderExpression)
        val inner = rule.statement.ensure.exp as OrderExpression
        val fsm = FSM()
        Assertions.assertNotNull(fsm)
        fsm.sequenceToFSM(inner.exp)
        return fsm
    }

    @Test
    fun testUseOfBotan_CipherMode() {
        val fsm = load("UseOfBotan_CipherMode")
        Assertions.assertEquals(
            """cm.create (0)
	-> cm.init(1)
cm.init (1)
	-> cm.start(2)
cm.start (2)
	-> cm.finish(3)
	-> cm.process(4)
cm.finish (3)
	-> END (E)(5)
	-> cm.reset(6)
	-> cm.start(2)
cm.process (4)
	-> cm.finish(3)
	-> cm.process(4)
END (E) (5)
cm.reset (6)
	-> END (E)(5)
""",
            fsm.toString()
        )
    }

    @Test
    fun testSimpleUseOfBotan_CipherMode() {
        val fsm = load("SimpleUseOfBotan_CipherMode")
        Assertions.assertEquals(
            """cm.create (0)
	-> cm.init(1)
cm.init (1)
	-> cm.finish(2)
	-> cm.start(3)
cm.finish (2)
	-> END (E)(4)
cm.start (3)
	-> cm.finish(2)
	-> cm.start(3)
END (E) (4)
""",
            fsm.toString()
        )
    }

    @Test
    fun testSimpleUseOfBotan2_CipherMode() {
        val fsm = load("SimpleUseOfBotan2_CipherMode")
        Assertions.assertEquals(
            """cm.create (0)
	-> cm.init(1)
cm.init (1)
	-> cm.start(2)
cm.start (2)
	-> cm.finish(3)
	-> cm.start(2)
cm.finish (3)
	-> END (E)(4)
END (E) (4)
""",
            fsm.toString()
        )
    }

    @Test
    fun testRegexToFsm() {
        val fsm = FSM()

        // Create a regular expression: AB?(CD)*E+
        val f: MarkDslFactory = MarkDslFactoryImpl()
        val a = f.createTerminal()
        a.entity = "A"
        val b = f.createTerminal()
        b.entity = "B"
        val c = f.createTerminal()
        c.entity = "C"
        val d = f.createTerminal()
        d.entity = "D"
        val e = f.createTerminal()
        e.entity = "E"
        val someB = f.createRepetitionExpression()
        someB.op = "?"
        someB.expr = b
        val cThenD = f.createSequenceExpression()
        cThenD.left = c
        cThenD.right = d
        val anyCThenD = f.createRepetitionExpression()
        anyCThenD.op = "*"
        anyCThenD.expr = cThenD
        val someE = f.createRepetitionExpression()
        someE.op = "+"
        someE.expr = e
        val tail2 = f.createSequenceExpression()
        tail2.left = anyCThenD
        tail2.right = someE
        val tail1 = f.createSequenceExpression()
        tail1.left = someB
        tail1.right = tail2
        val aThenTail = f.createSequenceExpression()
        aThenTail.left = a
        aThenTail.right = tail1

        // Transform regex into NFA (FSM)
        fsm.sequenceToFSM(aThenTail)
        println(fsm)
        val start = fsm.start
        Assertions.assertEquals(1, start.size)
        val startNode = start.iterator().next()
        val expectB = startNode.successors
        println(expectB)
    }

    companion object {
        private var mark: Mark? = null
        @BeforeAll
        @JvmStatic
        fun startup() {
            val resource =
                MarkLoadOutputTest::class.java.classLoader.getResource(
                    "mark/PoC_MS1/Botan_CipherMode.mark"
                )
            Assertions.assertNotNull(resource)
            val markPoC1 = File(resource.file)
            Assertions.assertNotNull(markPoC1)
            val parser = XtextParser()
            parser.addMarkFile(markPoC1)
            val markModels = parser.parse()
            mark = MarkModelLoader().load(markModels, null)
            Assertions.assertNotNull(mark)
        }
    }
}
