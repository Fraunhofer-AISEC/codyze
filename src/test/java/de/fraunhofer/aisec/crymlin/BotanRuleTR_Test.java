
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.structures.Finding;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Set;

@Disabled
public class BotanRuleTR_Test extends AbstractMarkTest {

	@Test
	public void test_rule_2_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/2_01.cpp", "dist/mark/botan/");

		/* actually expected
		expected(findings,
						"line XX: MarkRuleEvaluationFinding: Rule _2_01_BlockCiphers verified",
						"line XX : MarkRuleEvaluationFinding: Rule _2_01_KeyLength verified");
		*/

		/* actually not expected */
		expected(findings,
			"line 19: Violation against Order: rng.random_vec(enc->default_nonce_length()) (get_random) is not allowed. Expected one of: r.create (RNGOrder)");
	}

	@Test
	public void test_rule_2_1_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/2_1_01.cpp", "dist/mark/botan/");

		/* actually expected
		expected(findings, "line XX: MarkRuleEvaluationFinding: Rule _2_1_01_Modes verified");
		*/

		/* actually not expected */
		expected(findings,
			"line 19: Violation against Order: rng.random_vec(enc->default_nonce_length()) (get_random) is not allowed. Expected one of: r.create (RNGOrder)");
	}

	@Disabled
	@Test
	public void test_rule_2_1_2_1_01() throws Exception {
		// TODO: there is no sufficient MARK-rule yet
	}

	@Test
	public void test_rule_2_1_2_1_02() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/2_1_2_1_02.cpp", "dist/mark/botan/");

		/* actually expected
		expected(findings, "line XX: MarkRuleEvaluationFinding: Rule _2_1_2_1_02_CCM_TagSize verified");
		*/

		/* actually not expected */
		expected(findings,
			"line 19: Violation against Order: rng.random_vec(enc->default_nonce_length()) (get_random) is not allowed. Expected one of: r.create (RNGOrder)");

	}

	@Disabled
	@Test
	public void test_rule_2_1_2_2_01() throws Exception {
		// TODO: there is no sufficient MARK-rule yet
	}

	@Disabled
	@Test
	public void test_rule_2_1_2_2_02() throws Exception {
		// TODO: there is no sufficient MARK-rule yet
	}

	@Test
	public void test_rule_2_1_2_2_03() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/2_1_2_2_03.cpp", "dist/mark/botan/");

		/* actually expected
		expected(findings, "line XX: MarkRuleEvaluationFinding: Rule _2_1_2_2_03_GCM_TagSize verified");
		*/

		/* actually not expected */
		expected(findings,
			"line 19: Violation against Order: rng.random_vec(enc->default_nonce_length()) (get_random) is not allowed. Expected one of: r.create (RNGOrder)");
	}

	@Test
	public void test_rule_2_1_2_3_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/2_1_2_3_01.cpp", "dist/mark/botan/");

		/* actually expected
		expected(findings, "line XX: MarkRuleEvaluationFinding: Rule _2_1_2_3_01_CBC_RandomIV verified");
		*/

		/* actually not expected */
		expected(findings,
			"line 19: Violation against Order: rng.random_vec(enc->default_nonce_length()) (get_random) is not allowed. Expected one of: r.create (RNGOrder)");

	}

	@Disabled
	@Test
	public void test_rule_2_1_2_4_01() throws Exception {
		// TODO: there is no sufficient MARK-rule yet
	}

	@Disabled
	@Test
	public void test_rule_2_1_3_01() throws Exception {
		// TODO: there is no sufficient MARK-rule yet
	}

	@Disabled
	@Test
	public void test_rule_2_2_01() throws Exception {
		// TODO: there is no sufficient MARK-rule yet
	}

	@Disabled
	@Test
	public void test_rule_2_2_02() throws Exception {
		// TODO: there is no sufficient MARK-rule yet
	}

	@Disabled
	@Test
	public void test_rule_3_3_01() throws Exception {
		// Note: will not be checked. Is part of Botan
	}

	@Test
	public void test_rule_3_3_02() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/3_3_02.cpp", "dist/mark/botan/");
		expected(findings, "line 18: MarkRuleEvaluationFinding: Rule _3_3_02_CurveParams verified");
	}

	@Disabled
	@Test
	public void test_rule_3_3_03() throws Exception {
		// TODO: there is no sufficient MARK-rule yet
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

	@Test
	public void test_rule_4_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/4_01.cpp", "dist/mark/botan/");
		expected(findings,
			"line 7: MarkRuleEvaluationFinding: Rule _4_01_HashFunctions violated", // TODO this is expected to be verified
			"line 5: MarkRuleEvaluationFinding: Rule _4_01_HashFunctions violated",
			"line 6: MarkRuleEvaluationFinding: Rule _4_01_HashFunctions violated");
	}

	@Test
	public void test_rule_5_3_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/5_3_01.cpp", "dist/mark/botan/");
		expected(findings, "line 10: MarkRuleEvaluationFinding: Rule _5_3_01_MAC verified");
	}

	@Test
	public void test_rule_5_3_02() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/5_3_02.cpp", "dist/mark/botan/");
		expected(findings, "line XX : MarkRuleEvaluationFinding: Rule _5_3_02_MAC_KEYLEN verified"); // actually expected
	}

	@Test
	public void test_rule_5_3_03() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/5_3_03.cpp", "dist/mark/botan/");
		expected(findings,
			"line 16: MarkRuleEvaluationFinding: Rule _5_3_03_MAC_NONCELEN verified", // actually expected
			"line 12: MarkRuleEvaluationFinding: Rule _5_3_01_MAC verified", // actually expected
			"line 15: Violation against Order: mac->set_key(key); (init) is not allowed. Expected one of: m.create (Order)"); // actually NOT expected
	}

	@Test
	public void test_rule_5_4_1_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/5_4_1_01.cpp", "dist/mark/botan/");
		//expected("line XX : MarkRuleEvaluationFinding: Rule _5_4_1_01_RSA_SIG_Format verified");  // actually expected
	}

	@Test
	public void test_rule_5_4_1_02() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/5_4_1_02.cpp", "dist/mark/botan/");

		/* actually expected
		expected(findings,
		"line XX : MarkRuleEvaluationFinding: Rule _5_4_1_02_RSA_SIG_KeyLen verified",
		"line XX : MarkRuleEvaluationFinding: Rule _5_5_4_1_02_RSA_SIG_KeyLen_2022 verified");
		 */
	}

	@Test
	public void test_rule_5_4_2_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/5_4_2_01.cpp", "dist/mark/botan/");
		/* actually expected
		expected(findings,
		"line XX : MarkRuleEvaluationFinding: Rule _5_4_2_01_DSA_SIG_KeyLen verified",
		"line XX : MarkRuleEvaluationFinding: Rule _5_4_2_01_DSA_SIG_KeyLen_2022 verified");
		 */
	}

	@Test
	public void test_rule_5_4_3_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/5_4_3_01.cpp", "dist/mark/botan/");
		//expected("line XX : MarkRuleEvaluationFinding: Rule _5_4_3_01_ECDSA_SIG verified");  // actually expected
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
	public void test_rule_6_3() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/6_3.cpp", "dist/mark/botan/");
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
