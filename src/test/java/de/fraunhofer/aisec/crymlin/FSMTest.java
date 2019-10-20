
package de.fraunhofer.aisec.crymlin;

import static org.junit.jupiter.api.Assertions.*;

import de.fraunhofer.aisec.mark.XtextParser;
import de.fraunhofer.aisec.mark.markDsl.*;
import de.fraunhofer.aisec.mark.markDsl.impl.MarkDslFactoryImpl;
import de.fraunhofer.aisec.markmodel.MRule;
import de.fraunhofer.aisec.markmodel.Mark;
import de.fraunhofer.aisec.markmodel.MarkModelLoader;
import de.fraunhofer.aisec.markmodel.fsm.FSM;
import de.fraunhofer.aisec.markmodel.fsm.Node;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FSMTest {

	private static Mark mark;

	@BeforeAll
	static void startup() throws Exception {

		URL resource = MarkLoadOutputTest.class.getClassLoader().getResource("mark/PoC_MS1/Botan_CipherMode.mark");
		assertNotNull(resource);
		File markPoC1 = new File(resource.getFile());
		assertNotNull(markPoC1);

		XtextParser parser = new XtextParser();
		parser.addMarkFile(markPoC1);

		HashMap<String, MarkModel> markModels = parser.parse();
		mark = new MarkModelLoader().load(markModels, null);
		assertNotNull(mark);
	}

	@Test
	void parseTest() {
		assertEquals(5, mark.getRules().size()); // 5 total
		assertEquals(3, mark.getRules().stream().filter(rule -> rule.getStatement() != null && rule.getStatement().getEnsure() != null
				&& rule.getStatement().getEnsure().getExp() instanceof OrderExpression).count()); // 3 order

		assertEquals(1, mark.getRules().stream().filter(x -> x.getName().equals("BlockCiphers")).count());
		assertEquals(1, mark.getRules().stream().filter(x -> x.getName().equals("UseOfBotan_CipherMode")).count());
		assertEquals(1, mark.getRules().stream().filter(x -> x.getName().equals("SimpleUseOfBotan_CipherMode")).count());
		assertEquals(1, mark.getRules().stream().filter(x -> x.getName().equals("SimpleUseOfBotan2_CipherMode")).count());
		assertEquals(1, mark.getRules().stream().filter(x -> x.getName().equals("UseRandomIV")).count());
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

	private FSM load(String ruleName) {
		Optional<MRule> opt = mark.getRules().stream().filter(x -> x.getName().equals(ruleName)).findFirst();
		assertTrue(opt.isPresent());
		MRule rule = opt.get();
		assertNotNull(rule.getStatement());
		assertNotNull(rule.getStatement().getEnsure());
		assertNotNull(rule.getStatement().getEnsure().getExp());
		assertTrue(rule.getStatement().getEnsure().getExp() instanceof OrderExpression);
		OrderExpression inner = (OrderExpression) rule.getStatement().getEnsure().getExp();
		FSM fsm = new FSM();
		assertNotNull(fsm);
		fsm.sequenceToFSM(inner.getExp());

		return fsm;
	}

	@Test
	void testUseOfBotan_CipherMode() {
		FSM fsm = load("UseOfBotan_CipherMode");

		assertEquals("cm.create (0)\n" + "\t-> cm.init(1)\n" + "cm.init (1)\n" + "\t-> cm.start(2)\n" + "cm.start (2)\n" + "\t-> cm.finish(3)\n" + "\t-> cm.process(4)\n"
				+ "cm.finish (3)\n" + "\t-> END(5)\n" + "\t-> cm.reset(6)\n" + "\t-> cm.start(2)\n" + "cm.process (4)\n" + "\t-> cm.finish(3)\n" + "\t-> cm.process(4)\n"
				+ "END (5)\n" + "cm.reset (6)\n" + "\t-> END(5)\n",
			fsm.toString());
	}

	@Test
	void testSimpleUseOfBotan_CipherMode() {
		FSM fsm = load("SimpleUseOfBotan_CipherMode");

		assertEquals("cm.create (0)\n" + "\t-> cm.init(1)\n" + "cm.init (1)\n" + "\t-> cm.finish(2)\n" + "\t-> cm.start(3)\n" + "cm.finish (2)\n" + "\t-> END(4)\n"
				+ "cm.start (3)\n" + "\t-> cm.finish(2)\n" + "\t-> cm.start(3)\n" + "END (4)\n",
			fsm.toString());
	}

	@Test
	void testSimpleUseOfBotan2_CipherMode() {
		FSM fsm = load("SimpleUseOfBotan2_CipherMode");

		assertEquals("cm.create (0)\n" + "\t-> cm.init(1)\n" + "cm.init (1)\n" + "\t-> cm.start(2)\n" + "cm.start (2)\n" + "\t-> cm.finish(3)\n" + "\t-> cm.start(2)\n"
				+ "cm.finish (3)\n" + "\t-> END(4)\n" + "END (4)\n",
			fsm.toString());
	}

	@Test
	void testRegexToFsm() {
		FSM fsm = new FSM();

		// Create a regular expression: AB?(CD)*E+
		MarkDslFactory f = new MarkDslFactoryImpl();
		Terminal a = f.createTerminal();
		a.setEntity("A");
		Terminal b = f.createTerminal();
		b.setEntity("B");
		Terminal c = f.createTerminal();
		c.setEntity("C");
		Terminal d = f.createTerminal();
		d.setEntity("D");
		Terminal e = f.createTerminal();
		e.setEntity("E");
		RepetitionExpression someB = f.createRepetitionExpression();
		someB.setOp("?");
		someB.setExpr(b);
		SequenceExpression cThenD = f.createSequenceExpression();
		cThenD.setLeft(c);
		cThenD.setRight(d);
		RepetitionExpression anyCThenD = f.createRepetitionExpression();
		anyCThenD.setOp("*");
		anyCThenD.setExpr(cThenD);
		RepetitionExpression someE = f.createRepetitionExpression();
		someE.setOp("+");
		someE.setExpr(e);
		SequenceExpression tail2 = f.createSequenceExpression();
		tail2.setLeft(anyCThenD);
		tail2.setRight(someE);
		SequenceExpression tail1 = f.createSequenceExpression();
		tail1.setLeft(someB);
		tail1.setRight(tail2);
		SequenceExpression aThenTail = f.createSequenceExpression();
		aThenTail.setLeft(a);
		aThenTail.setRight(tail1);

		// Transform regex into NFA (FSM)
		fsm.sequenceToFSM(aThenTail);
		System.out.println(fsm.toString());

		HashSet<Node> start = fsm.getStart();
		assertEquals(1, start.size());
		Node startNode = start.iterator().next();
		HashSet<Node> expectB = startNode.getSuccessors();
		System.out.println(expectB);
	}
}
