
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.structures.Finding;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JCATest extends AbstractMarkTest {

	@Test
	public void testBlockCipher() throws Exception {
		Set<Finding> findings = performTest("java/jca/BlockCipher.java", "mark/bouncycastle/");

		expected(findings,
			"line 23: MarkRuleEvaluationFinding: Rule ID_2_01 verified", // ok
			"line 32: MarkRuleEvaluationFinding: Rule ID_2_01 verified", // ok
			"line 41: MarkRuleEvaluationFinding: Rule ID_2_01 verified", // ok

			"line 23: MarkRuleEvaluationFinding: Rule ID_2_2_01 violated", // todo: rule broken, might also be a problem in the analysis
			"line 32: MarkRuleEvaluationFinding: Rule ID_2_2_01 violated", // todo: rule broken, might also be a problem in the analysis
			"line 41: MarkRuleEvaluationFinding: Rule ID_2_2_01 violated", // todo: rule broken, might also be a problem in the analysis
			"line 23: MarkRuleEvaluationFinding: Rule ID_2_1_01 violated", // ok
			"line 32: MarkRuleEvaluationFinding: Rule ID_2_1_01 violated", // ok
			"line 41: MarkRuleEvaluationFinding: Rule ID_2_1_01 violated", // ok
			"line 14: MarkRuleEvaluationFinding: Rule ID_2_01 violated", // ok
			"line 50: MarkRuleEvaluationFinding: Rule ID_2_01 violated", // ok
			"line 59: MarkRuleEvaluationFinding: Rule ID_2_01 violated", // ok
			"line 68: MarkRuleEvaluationFinding: Rule ID_2_01 violated", // ok
			"line 77: MarkRuleEvaluationFinding: Rule ID_2_01 violated" // ok
		);
	}

	@Test
	public void testAESGCM() throws Exception {
		Set<Finding> findings = performTest("java/jca/AESGCM.java", "mark/bouncycastle/");

		expected(findings, "line 22: MarkRuleEvaluationFinding: Rule ID_2_01 verified", // ok
			"line 22: MarkRuleEvaluationFinding: Rule ID_2_1_01 verified", // ok

			"line [22, 30]: MarkRuleEvaluationFinding: Rule ID_2_1_2_2_01 violated" // todo rule needs to be improved
		);
	}

	@Test
	public void testBCMacs() throws Exception {
		Set<Finding> findings = performTest("java/jca/BCMacs.java", "mark/bouncycastle/");

		expected(findings, "line 25: MarkRuleEvaluationFinding: Rule ID_5_3_01 verified", // ok

			"line 25: MarkRuleEvaluationFinding: Rule ID_5_3_03_CMAC violated", // todo rule needs to be improved
			"line 25: MarkRuleEvaluationFinding: Rule ID_5_3_02_GMAC violated", // todo rule needs to be improved
			"line 25: MarkRuleEvaluationFinding: Rule ID_5_3_03_GMAC violated" // todo rule needs to be improved
		);
	}

	@Test
	public void testMacTest() throws Exception {
		Set<Finding> findings = performTest("java/jca/MacTest.java", "mark/bouncycastle/");

		expected(findings, "line 41: MarkRuleEvaluationFinding: Rule ID_5_3_01 violated", // ok
			"line 41: MarkRuleEvaluationFinding: Rule ID_5_3_03_CMAC violated", // TODO: rule needs to be improved
			"line 41: MarkRuleEvaluationFinding: Rule ID_5_3_03_GMAC violated" // TODO: rule needs to be improved
		);
	}

	@Test
	public void testRSACipherTest() throws Exception {
		Set<Finding> findings = performTest("java/jca/RSACipherTest.java", "mark/bouncycastle/");

		expected(findings,
			"line 55: MarkRuleEvaluationFinding: Rule ID_3_5_02 violated", // TODO: rule needs to be improved
			"line 55: MarkRuleEvaluationFinding: Rule ID_3_5_01 violated", // ok, unknown parameter
			"line 55: MarkRuleEvaluationFinding: Rule ID_2_01 violated" // ok, unknown parameter
		);
	}

}
