
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.structures.Finding;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class BotanRuleTR_Test extends AbstractMarkTest {

	@Test
	public void test_rule_2_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/2_01.cpp", "mark/botan/");

		expected(findings,
			"line 15: Rule _2_01_BlockCiphers verified", // ok
			"line 15: Rule _2_1_01_Modes verified", // ok

			"line 21: Violation against Order: rng.random_vec(enc->default_nonce_length()) (get_random) is not allowed. Expected one of: r.create (RNGOrder)", //default ctor-problem in CPG
			"line 21: Violation against Order: enc->start(rng.random_vec(enc->default_nonce_length())); (start_iv) is not allowed. Expected one of: cm.set_key (Cipher_Mode_Order)", // ok
			"line 15: Violation against Order: Base enc is not correctly terminated. Expected one of [cm.set_key] to follow the correct last call on this base. (Cipher_Mode_Order)", // ok
			"line 15: Rule _2_01_KeyLength violated" // ok

		);
	}

	@Test
	public void test_rule_2_1_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/2_1_01.cpp", "mark/botan/");

		expected(findings,
			"line 15: Rule _2_01_BlockCiphers verified", // ok
			"line 15: Rule _2_1_01_Modes verified", // ok

			"line 21: Violation against Order: rng.random_vec(enc->default_nonce_length()) (get_random) is not allowed. Expected one of: r.create (RNGOrder)", //default ctor-problem in CPG
			"line 21: Violation against Order: enc->start(rng.random_vec(enc->default_nonce_length())); (start_iv) is not allowed. Expected one of: cm.set_key (Cipher_Mode_Order)", // ok
			"line 15: Violation against Order: Base enc is not correctly terminated. Expected one of [cm.set_key] to follow the correct last call on this base. (Cipher_Mode_Order)", // ok
			"line 15: Rule _2_01_KeyLength violated" // ok
		);
	}

	@Test
	public void test_rule_2_1_2_1_02() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/2_1_2_1_02.cpp", "mark/botan/");

		expected(findings,
			"line 15: Rule _2_01_BlockCiphers verified", // ok
			"line 21: Violation against Order: rng.random_vec(enc->default_nonce_length()) (get_random) is not allowed. Expected one of: r.create (RNGOrder)", //default ctor-problem in CPG
			"line 21: Violation against Order: enc->start(rng.random_vec(enc->default_nonce_length())); (start_iv) is not allowed. Expected one of: cm.set_key (Cipher_Mode_Order)", // ok
			"line 15: Violation against Order: Base enc is not correctly terminated. Expected one of [cm.set_key] to follow the correct last call on this base. (Cipher_Mode_Order)", // ok
			"line 15: Rule _2_1_01_Modes violated", // ok
			"line 15: Rule _2_01_KeyLength violated", // ok
			"line 15: Rule _2_1_2_1_02_CCM_TagSize verified" // ok
		);

	}

	@Test
	public void test_rule_2_1_2_2_03() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/2_1_2_2_03.cpp", "mark/botan/");

		expected(findings,
			"line 15: Rule _2_01_BlockCiphers verified", // ok
			"line 21: Violation against Order: rng.random_vec(enc->default_nonce_length()) (get_random) is not allowed. Expected one of: r.create (RNGOrder)", //default ctor-problem in CPG
			"line 21: Violation against Order: enc->start(rng.random_vec(enc->default_nonce_length())); (start_iv) is not allowed. Expected one of: cm.set_key (Cipher_Mode_Order)", // ok
			"line 15: Violation against Order: Base enc is not correctly terminated. Expected one of [cm.set_key] to follow the correct last call on this base. (Cipher_Mode_Order)", // ok
			"line 15: Rule _2_1_01_Modes violated", // ok
			"line 15: Rule _2_01_KeyLength violated", // ok
			"line 15: Rule _2_1_2_2_03_GCM_TagSize verified" // ok
		);
	}

	@Test
	public void test_rule_2_1_2_3_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/2_1_2_3_01.cpp", "mark/botan/");

		/* missing
			"line XX: Rule _2_1_2_3_01_CBC_RandomIV verified" // requires _receives_value_from builtin which requires a data flow analysis
		*/

		expected(findings,
			"line 15: Rule _2_01_BlockCiphers verified", // ok
			"line 15: Rule _2_1_01_Modes verified", // ok

			"line 21: Violation against Order: rng.random_vec(enc->default_nonce_length()) (get_random) is not allowed. Expected one of: r.create (RNGOrder)", //default ctor-problem in CPG
			"line 21: Violation against Order: enc->start(rng.random_vec(enc->default_nonce_length())); (start_iv) is not allowed. Expected one of: cm.set_key (Cipher_Mode_Order)", // ok
			"line 15: Violation against Order: Base enc is not correctly terminated. Expected one of [cm.set_key] to follow the correct last call on this base. (Cipher_Mode_Order)", // ok
			"line 15: Rule _2_01_KeyLength violated" // ok
		);

	}

	@Test
	public void test_rule_3_3_02() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/3_3_02.cpp", "mark/botan/");
		expected(findings,
			"line 21: Rule _3_3_03_ECIES_KDF verified", // ok
			"line 17: Rule _3_3_02_CurveParams verified", // ok
			"line 19: Verified Order: PrivKeyOrder"); // ok
	}

	@Test
	public void test_rule_3_3_03() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/3_3_03.cpp", "mark/botan/");
		expected(findings,
			"line 22: Rule _3_3_03_ECIES_KDF verified", // ok
			"line 18: Rule _3_3_02_CurveParams verified", // ok
			"line 20: Verified Order: PrivKeyOrder"); // ok
	}

	@Test
	public void test_rule_3_4_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/3_4_01.cpp", "mark/botan/");
		expected(findings,
			"line 30: Violation against Order: Base mac is not correctly terminated. Expected one of [m.init] to follow the correct last call on this base. (MACOrder)", // ok
			"line 27: Rule _3_4_01_DLIES_KDF verified", // ok
			"line 19: Rule _3_4_02_DLIES_KEYLEN_2022 verified", // ok
			"line 22: Verified Order: PubKeyOrder",
			"line 21: Verified Order: PrivKeyOrder",
			"line 19: Rule _3_4_02_DLIES_KEYLEN verified", // ok
			"line 30: Rule _5_3_01_MAC verified", // ok
			"line 19: Rule _7_2_2_1_01_DH_KEYLEN_2022 verified", // ok
			"line 19: Rule _7_2_2_1_01_DH_KEYLEN verified" // ok
		);
	}

	@Test
	public void test_rule_3_4_02() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/3_4_02.cpp", "mark/botan/");
		expected(findings,
			"line 17: Rule _3_4_02_DLIES_KEYLEN_2022 verified", // ok
			"line 19: Verified Order: PrivKeyOrder", // ok
			"line 17: Rule _3_4_02_DLIES_KEYLEN verified", // ok
			"line 27: Rule _5_3_01_MAC verified", // ok
			"line 27: Violation against Order: Base mac is not correctly terminated. Expected one of [m.init] to follow the correct last call on this base. (MACOrder)", // ok
			"line 24: Rule _3_4_01_DLIES_KDF verified", // ok
			"line 17: Rule _7_2_2_1_01_DH_KEYLEN_2022 verified", // ok
			"line 17: Rule _7_2_2_1_01_DH_KEYLEN verified" // ok
		);
	}

	@Test
	public void test_rule_4_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/4_01.cpp", "mark/botan/");
		expected(findings,
			"line 8: Verified Order: HashOrder", // ok
			"line 9: Verified Order: HashOrder", // ok
			"line 10: Verified Order: HashOrder", // ok
			"line 8: Rule _4_01_HashFunctions violated", // ok
			"line 9: Rule _4_01_HashFunctions violated", // ok
			"line 10: Rule _4_01_HashFunctions verified" // ok
		);
	}

	@Test
	public void test_rule_5_3_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/5_3_01.cpp", "mark/botan/");
		expected(findings,
			"line 12: Violation against Order: Base mac is not correctly terminated. Expected one of [m.init] to follow the correct last call on this base. (MACOrder)", // ok
			"line 22: Violation against Order: mac->start(iv); (start) is not allowed. Expected one of: END (MACOrder)", // ok
			"line 12: Rule _5_3_01_MAC verified" // ok
		);
	}

	@Test
	public void test_rule_5_3_02() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/5_3_02.cpp", "mark/botan/");
		expected(findings, "line 11: Rule _5_3_02_MAC_KEYLEN verified", // ok
			"line 14: Rule _5_3_01_MAC verified", // ok
			"line 14: Violation against Order: Base mac is not correctly terminated. Expected one of [m.init] to follow the correct last call on this base. (MACOrder)", // ok
			"line 24: Violation against Order: mac->start(iv); (start) is not allowed. Expected one of: END (MACOrder)" // ok
		);
	}

	@Test
	public void test_rule_5_3_03() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/5_3_03.cpp", "mark/botan/");
		expected(findings,
			"line 12: Rule _5_3_02_MAC_KEYLEN verified", // ok
			"line 20: Rule _5_3_03_MAC_NONCELEN verified", // ok
			"line 16: Rule _5_3_01_MAC verified", // ok
			"line 16: Violation against Order: Base mac is not correctly terminated. Expected one of [m.init] to follow the correct last call on this base. (MACOrder)" // ok
		);
	}

	@Test
	public void test_rule_5_4_1_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/5_4_1_01.cpp", "mark/botan/");
		expected(findings,
			"line 10: Verified Order: PrivKeyOrder",
			"line 11: Rule _5_4_1_02_RSA_SIG_KeyLen verified", // ok
			"line 11: Verified Order: SignatureOrder", // ok
			"line 11: Rule _5_4_1_01_RSA_SIG_Format verified", // ok
			"line 11: Rule _5_4_1_02_RSA_SIG_KeyLen_2022 verified" // ok
		);
	}

	@Test
	public void test_rule_5_4_1_02() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/5_4_1_02.cpp", "mark/botan/");
		expected(findings,
			"line 11: Rule _5_4_1_02_RSA_SIG_KeyLen_2022 verified", // ok
			"line 11: Rule _5_4_1_02_RSA_SIG_KeyLen verified", // ok
			"line 11: Rule _5_4_1_01_RSA_SIG_Format verified", // ok
			"line 11: Verified Order: SignatureOrder", // ok
			"line 10: Verified Order: PrivKeyOrder" // ok
		);
	}

	@Test
	public void test_rule_5_4_2_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/5_4_2_01.cpp", "mark/botan/");
		expected(findings,
			"line 14: Verified Order: SignatureOrder", // ok
			"line 13: Verified Order: PrivKeyOrder", // ok
			"line 14: Rule _5_4_2_01_DSA_SIG_KeyLen verified", // ok
			"line 14: Rule _5_4_2_01_DSA_SIG_KeyLen_2022 verified" // ok
		);
	}

	@Test
	public void test_rule_5_4_3_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/5_4_3_01.cpp", "mark/botan/");
		expected(findings,
			"line 14: Verified Order: SignatureOrder", // ok
			"line 13: Verified Order: PrivKeyOrder", // ok
			"line 14: Rule _5_4_3_01_ECDSA_SIG verified" // ok
		);
	}

	@Test
	public void test_rule_7_2_2_1_01() throws Exception {
		Set<Finding> findings = performTest("botan_rule_tr_test/7_2_2_1_01.cpp", "mark/botan/");
		expected(findings,
			"line 13: Rule _7_2_2_1_01_DH_KEYLEN verified", // ok
			"line 13: Rule _7_2_2_1_01_DH_KEYLEN_2022 verified"); // ok
	}

}
