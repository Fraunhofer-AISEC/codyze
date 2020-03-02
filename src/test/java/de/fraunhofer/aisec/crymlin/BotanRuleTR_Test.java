
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.structures.Finding;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class BotanRuleTR_Test extends AbstractMarkTest {

	@Disabled
	@Test
	public void test_rule_2_01() throws Exception { //FIXME
		Set<Finding> findings = performTest("botan_rule_tr_test/2_01.cpp", "dist/mark/botan/");
		expected(findings, "line XX : MarkRuleEvaluationFinding: Rule _2_01_BlockCiphers verified"); // TODO
		expected(findings, "line XX : MarkRuleEvaluationFinding: Rule _2_01_KeyLength verified"); // TODO
	}

	@Disabled
	@Test
	public void test_rule_2_1_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/2_1_01.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_2_1_2_1_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/2_1_2_1_01.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_2_1_2_1_02() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/2_1_2_1_02.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_2_1_2_2_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/2_1_2_2_01.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_2_1_2_2_02() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/2_1_2_2_02.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_2_1_2_2_03() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/2_1_2_2_03.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_2_1_2_3_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/2_1_2_3_01.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_2_1_2_4_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/2_1_2_4_01.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_2_1_3_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/2_1_3_01.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_6_3() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/6_3.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_2_4() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/2_4.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_2_2_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/2_2_01.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_2_2_02() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/2_2_02.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_3_3_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/3_3_01.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_3_3_02() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/3_3_02.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_3_3_03() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/3_3_03.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_3_3_04() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/3_3_04.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_3_4_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/3_4_01.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_3_4_02() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/3_4_02.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_3_4_03() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/3_4_03.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_3_5_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/3_5_01.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_3_5_02() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/3_5_02.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_4_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/4_01.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_5_3_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/5_3_01.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_5_3_02() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/5_3_02.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_5_3_03() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/5_3_03.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_5_4_1_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/5_4_1_01.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_5_4_1_02() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/5_4_1_02.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_5_4_2_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/5_4_2_01.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_5_4_3_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/5_4_3_01.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_5_4_3_02() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/5_4_3_02.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_6_1_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/6_1_01.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_6_2_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/6_2_01.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_7_1_1_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/7_1_1_01.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_7_1_1_02() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/7_1_1_02.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_7_1_1_03() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/7_1_1_03.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_7_1_2_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/7_1_2_01.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_7_2_1_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/7_2_1_01.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_7_2_2_1_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/7_2_2_1_01.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_7_2_2_2_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/7_2_2_2_01.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_9_2_1_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/9_2_1_01.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_9_2_2_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/9_2_2_01.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_9_5_1_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/9_5_1_01.cpp", "dist/mark/botan/");
	}

	@Disabled
	@Test
	public void test_rule_9_5_2_1() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/9_5_2_1.cpp", "dist/mark/botan/");
	}

}
