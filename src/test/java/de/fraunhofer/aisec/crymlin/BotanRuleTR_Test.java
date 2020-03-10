
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.structures.Finding;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class BotanRuleTR_Test extends AbstractMarkTest {

	@Test
	public void test_rule_2_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/2_01.cpp", "mark/botan/");

		expected(findings,
			"line 13: MarkRuleEvaluationFinding: Rule _2_01_BlockCiphers verified", // ok
			"line 13: MarkRuleEvaluationFinding: Rule _2_1_01_Modes verified", // ok

			"line 19: Violation against Order: rng.random_vec(enc->default_nonce_length()) (get_random) is not allowed. Expected one of: r.create (RNGOrder)", //default ctor-problem in CPG
			"line 19: Violation against Order: enc->start(rng.random_vec(enc->default_nonce_length())); (start_iv) is not allowed. Expected one of: cm.create_key_init, cm.create_uninit (Cipher_Mode_Order)", // ok
			"line 13: Violation against Order: Base enc is not correctly terminated. Expected one of [cm.set_key] to follow the correct last call on this base. (Cipher_Mode_Order)", // ok
			"line 13: MarkRuleEvaluationFinding: Rule _2_01_KeyLength violated" // ok

		);
	}

	@Test
	public void test_rule_2_1_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/2_1_01.cpp", "mark/botan/");

		expected(findings,
			"line 13: MarkRuleEvaluationFinding: Rule _2_01_BlockCiphers verified", // ok
			"line 13: MarkRuleEvaluationFinding: Rule _2_1_01_Modes verified", // ok

			"line 19: Violation against Order: rng.random_vec(enc->default_nonce_length()) (get_random) is not allowed. Expected one of: r.create (RNGOrder)", //default ctor-problem in CPG
			"line 19: Violation against Order: enc->start(rng.random_vec(enc->default_nonce_length())); (start_iv) is not allowed. Expected one of: cm.create_key_init, cm.create_uninit (Cipher_Mode_Order)", // ok
			"line 13: Violation against Order: Base enc is not correctly terminated. Expected one of [cm.set_key] to follow the correct last call on this base. (Cipher_Mode_Order)", // ok
			"line 13: MarkRuleEvaluationFinding: Rule _2_01_KeyLength violated" // ok
		);
	}

	@Test
	public void test_rule_2_1_2_1_02() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/2_1_2_1_02.cpp", "mark/botan/");

		expected(findings,
			"line 13: MarkRuleEvaluationFinding: Rule _2_01_BlockCiphers verified", // ok
			"line 19: Violation against Order: rng.random_vec(enc->default_nonce_length()) (get_random) is not allowed. Expected one of: r.create (RNGOrder)", //default ctor-problem in CPG
			"line 19: Violation against Order: enc->start(rng.random_vec(enc->default_nonce_length())); (start_iv) is not allowed. Expected one of: cm.create_key_init, cm.create_uninit (Cipher_Mode_Order)", // ok
			"line 13: Violation against Order: Base enc is not correctly terminated. Expected one of [cm.set_key] to follow the correct last call on this base. (Cipher_Mode_Order)", // ok
			"line 13: MarkRuleEvaluationFinding: Rule _2_1_01_Modes violated", // ok
			"line 13: MarkRuleEvaluationFinding: Rule _2_01_KeyLength violated", // ok
			"line 13: MarkRuleEvaluationFinding: Rule _2_1_2_1_02_CCM_TagSize verified" // ok
		);

	}

	@Test
	public void test_rule_2_1_2_2_03() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/2_1_2_2_03.cpp", "mark/botan/");

		expected(findings,
			"line 13: MarkRuleEvaluationFinding: Rule _2_01_BlockCiphers verified", // ok
			"line 19: Violation against Order: rng.random_vec(enc->default_nonce_length()) (get_random) is not allowed. Expected one of: r.create (RNGOrder)", //default ctor-problem in CPG
			"line 19: Violation against Order: enc->start(rng.random_vec(enc->default_nonce_length())); (start_iv) is not allowed. Expected one of: cm.create_key_init, cm.create_uninit (Cipher_Mode_Order)", // ok
			"line 13: Violation against Order: Base enc is not correctly terminated. Expected one of [cm.set_key] to follow the correct last call on this base. (Cipher_Mode_Order)", // ok
			"line 13: MarkRuleEvaluationFinding: Rule _2_1_01_Modes violated", // ok
			"line 13: MarkRuleEvaluationFinding: Rule _2_01_KeyLength violated", // ok
			"line 13: MarkRuleEvaluationFinding: Rule _2_1_2_2_03_GCM_TagSize verified" // ok
		);
	}

	@Test
	public void test_rule_2_1_2_3_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/2_1_2_3_01.cpp", "mark/botan/");

		/* missing
			"line XX: MarkRuleEvaluationFinding: Rule _2_1_2_3_01_CBC_RandomIV verified"
		*/

		expected(findings,
			"line 13: MarkRuleEvaluationFinding: Rule _2_01_BlockCiphers verified", // ok
			"line 13: MarkRuleEvaluationFinding: Rule _2_1_01_Modes verified", // ok

			"line 19: Violation against Order: rng.random_vec(enc->default_nonce_length()) (get_random) is not allowed. Expected one of: r.create (RNGOrder)", //default ctor-problem in CPG
			"line 19: Violation against Order: enc->start(rng.random_vec(enc->default_nonce_length())); (start_iv) is not allowed. Expected one of: cm.create_key_init, cm.create_uninit (Cipher_Mode_Order)", // ok
			"line 13: Violation against Order: Base enc is not correctly terminated. Expected one of [cm.set_key] to follow the correct last call on this base. (Cipher_Mode_Order)", // ok
			"line 13: MarkRuleEvaluationFinding: Rule _2_01_KeyLength violated" // ok
		);

	}

	@Test
	public void test_rule_3_3_02() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/3_3_02.cpp", "mark/botan/");
		expected(findings,
			"line 19: MarkRuleEvaluationFinding: Rule _3_3_03_ECIES_KDF verified", // ok
			"line 15: MarkRuleEvaluationFinding: Rule _3_3_02_CurveParams verified", // ok
			"line 17: Verified Order: PrivKeyOrder"); // ok
	}

	@Test
	public void test_rule_3_3_03() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/3_3_03.cpp", "mark/botan/");
		expected(findings,
			"line 19: MarkRuleEvaluationFinding: Rule _3_3_03_ECIES_KDF verified", // ok
			"line 15: MarkRuleEvaluationFinding: Rule _3_3_02_CurveParams verified", // ok
			"line 17: Verified Order: PrivKeyOrder"); // ok
	}

	@Test
	public void test_rule_3_4_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/3_4_01.cpp", "mark/botan/");
		expected(findings,
			"line 28: Violation against Order: Base mac is not correctly terminated. Expected one of [m.init] to follow the correct last call on this base. (MACOrder)",
			"line 25: MarkRuleEvaluationFinding: Rule _3_4_01_DLIES_KDF verified", // ok
			"line 17: MarkRuleEvaluationFinding: Rule _3_4_02_DLIES_KEYLEN_2022 verified",
			"line 20: Verified Order: PubKeyOrder",
			"line 19: Verified Order: PrivKeyOrder",
			"line 17: MarkRuleEvaluationFinding: Rule _3_4_02_DLIES_KEYLEN violated",
			"line 28: MarkRuleEvaluationFinding: Rule _5_3_01_MAC verified",
			"line 17: MarkRuleEvaluationFinding: Rule _7_2_2_1_01_DH_KEYLEN_2022 verified",
			"line 17: MarkRuleEvaluationFinding: Rule _7_2_2_1_01_DH_KEYLEN violated");
	}

	@Test
	public void test_rule_3_4_02() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/3_4_02.cpp", "mark/botan/");
		expected(findings,
			"line 17: MarkRuleEvaluationFinding: Rule _3_4_02_DLIES_KEYLEN_2022 verified", // ok
			"line 19: Verified Order: PrivKeyOrder", // ok
			"line 17: MarkRuleEvaluationFinding: Rule _3_4_02_DLIES_KEYLEN violated", // expected verified
			"line 27: MarkRuleEvaluationFinding: Rule _5_3_01_MAC verified", // ok
			"line 27: Violation against Order: Base mac is not correctly terminated. Expected one of [m.init] to follow the correct last call on this base. (MACOrder)", // look at this again
			"line 24: MarkRuleEvaluationFinding: Rule _3_4_01_DLIES_KDF verified", // ok
			"line 17: MarkRuleEvaluationFinding: Rule _7_2_2_1_01_DH_KEYLEN_2022 verified",
			"line 17: MarkRuleEvaluationFinding: Rule _7_2_2_1_01_DH_KEYLEN violated" // expected to be verified
		);
	}

	@Test
	public void test_rule_4_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/4_01.cpp", "mark/botan/");
		expected(findings,
			"line 15: Violation against Order: hash1->update(buf.data(),readcount); (update) is not allowed. Expected one of: hf.create (HashOrder)",
			"line 16: Violation against Order: hash2->update(buf.data(),readcount); (update) is not allowed. Expected one of: hf.create (HashOrder)",
			"line 17: Violation against Order: hash3->update(buf.data(),readcount); (update) is not allowed. Expected one of: hf.create (HashOrder)",
			"line 19: Violation against Order: hash1->final() (finalize) is not allowed. Expected one of: hf.create (HashOrder)",
			"line 20: Violation against Order: hash2->final() (finalize) is not allowed. Expected one of: hf.create (HashOrder)",
			"line 21: Violation against Order: hash3->final() (finalize) is not allowed. Expected one of: hf.create (HashOrder)",
			"line 7: Violation against Order: Base hash3 is not correctly terminated. Expected one of [hf.finalize, hf.process, hf.update] to follow the correct last call on this base. (HashOrder)",
			"line 6: Violation against Order: Base hash2 is not correctly terminated. Expected one of [hf.finalize, hf.process, hf.update] to follow the correct last call on this base. (HashOrder)",
			"line 5: Violation against Order: Base hash1 is not correctly terminated. Expected one of [hf.finalize, hf.process, hf.update] to follow the correct last call on this base. (HashOrder)",
			"line 5: MarkRuleEvaluationFinding: Rule _4_01_HashFunctions violated",
			"line 6: MarkRuleEvaluationFinding: Rule _4_01_HashFunctions violated",
			"line 7: MarkRuleEvaluationFinding: Rule _4_01_HashFunctions violated");
	}

	@Test
	public void test_rule_5_3_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/5_3_01.cpp", "mark/botan/");
		expected(findings, "line 13: Violation against Order: mac->set_key(key); (init) is not allowed. Expected one of: m.create (MACOrder)",
			"line 10: Violation against Order: Base mac is not correctly terminated. Expected one of [m.init] to follow the correct last call on this base. (MACOrder)",
			"line 10: MarkRuleEvaluationFinding: Rule _5_3_01_MAC verified");
	}

	@Test
	public void test_rule_5_3_02() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/5_3_02.cpp", "mark/botan/");
		//expected(findings, "line XX : MarkRuleEvaluationFinding: Rule _5_3_02_MAC_KEYLEN verified"); // actually expected
		expected(findings, "line 12: MarkRuleEvaluationFinding: Rule _5_3_01_MAC verified",
			"line 15: Violation against Order: mac->set_key(key); (init) is not allowed. Expected one of: m.create (MACOrder)",
			"line 12: Violation against Order: Base mac is not correctly terminated. Expected one of [m.init] to follow the correct last call on this base. (MACOrder)");
	}

	@Test
	public void test_rule_5_3_03() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/5_3_03.cpp", "mark/botan/");
		expected(findings,
			"line 16: MarkRuleEvaluationFinding: Rule _5_3_03_MAC_NONCELEN verified",
			"line 12: MarkRuleEvaluationFinding: Rule _5_3_01_MAC verified",
			"line 12: Violation against Order: Base mac is not correctly terminated. Expected one of [m.init] to follow the correct last call on this base. (MACOrder)",
			"line 15: Violation against Order: mac->set_key(key); (init) is not allowed. Expected one of: m.create (MACOrder)");
	}

	@Test
	public void test_rule_5_4_1_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/5_4_1_01.cpp", "mark/botan/");
		expected(findings);

		//expected("line XX : MarkRuleEvaluationFinding: Rule _5_4_1_01_RSA_SIG_Format verified");  // actually expected
	}

	@Test
	public void test_rule_5_4_1_02() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/5_4_1_02.cpp", "mark/botan/");
		expected(findings);

		/* actually expected
		expected(findings,
		"line XX : MarkRuleEvaluationFinding: Rule _5_4_1_02_RSA_SIG_KeyLen verified",
		"line XX : MarkRuleEvaluationFinding: Rule _5_5_4_1_02_RSA_SIG_KeyLen_2022 verified");
		 */
	}

	@Test
	public void test_rule_5_4_2_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/5_4_2_01.cpp", "mark/botan/");
		expected(findings);

		/* actually expected
		expected(findings,
		"line XX : MarkRuleEvaluationFinding: Rule _5_4_2_01_DSA_SIG_KeyLen verified",
		"line XX : MarkRuleEvaluationFinding: Rule _5_4_2_01_DSA_SIG_KeyLen_2022 verified");
		 */
	}

	@Test
	public void test_rule_5_4_3_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/5_4_3_01.cpp", "mark/botan/");
		expected(findings);

		//expected("line XX : MarkRuleEvaluationFinding: Rule _5_4_3_01_ECDSA_SIG verified");  // actually expected
	}

	@Test
	public void test_rule_7_2_2_1_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/7_2_2_1_01.cpp", "mark/botan/");
		expected(findings,
			"line 11: MarkRuleEvaluationFinding: Rule _7_2_2_1_01_DH_KEYLEN violated", // expected to be verified
			"line 11: MarkRuleEvaluationFinding: Rule _7_2_2_1_01_DH_KEYLEN_2022 verified"); // ok
	}

}
