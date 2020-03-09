
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.structures.Finding;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JCATest extends AbstractMarkTest {

	@Test
	public void testBCProviderCipher() throws Exception {
		Set<Finding> findings = performTest("java/jca/BCProviderCipher.java", "mark/bouncycastle/");

		expected(findings,
			// rule bouncy castle as provider
			"line 19: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Cipher violated",
			"line 22: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Cipher verified",
			"line 23: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Cipher violated", // fixme broken missing type resolution for BouncyCastleProvider class
			//"line 24: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Cipher verified", // fixme missing type resolution for BouncyCastleProvider class
			"line 27: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Cipher violated",
			"line 28: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Cipher violated",

			// rule allowed block ciphers
			"line 19: MarkRuleEvaluationFinding: Rule ID_2_01 verified",
			"line 22: MarkRuleEvaluationFinding: Rule ID_2_01 verified",
			"line 23: MarkRuleEvaluationFinding: Rule ID_2_01 verified",
			"line 27: MarkRuleEvaluationFinding: Rule ID_2_01 verified",
			"line 28: MarkRuleEvaluationFinding: Rule ID_2_01 verified",

			// rule allowed block cipher modes
			"line 19: MarkRuleEvaluationFinding: Rule ID_2_1_01 violated",
			"line 22: MarkRuleEvaluationFinding: Rule ID_2_1_01 violated",
			"line 23: MarkRuleEvaluationFinding: Rule ID_2_1_01 violated",
			"line 27: MarkRuleEvaluationFinding: Rule ID_2_1_01 violated",
			"line 28: MarkRuleEvaluationFinding: Rule ID_2_1_01 violated");
	}

	@Test
	public void testBlockCipher() throws Exception {
		Set<Finding> findings = performTest("java/jca/BlockCipher.java", "mark/bouncycastle/");

		expected(findings,
			// rules for Bouncy Castle as provider
			"line 10: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Cipher verified",
			"line 14: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Cipher verified",
			"line 18: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Cipher verified",
			"line 22: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Cipher verified",
			"line 26: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Cipher verified",

			// rules allowed block cipher
			"line 10: MarkRuleEvaluationFinding: Rule ID_2_01 verified",
			"line 14: MarkRuleEvaluationFinding: Rule ID_2_01 violated",
			"line 18: MarkRuleEvaluationFinding: Rule ID_2_01 violated",
			"line 22: MarkRuleEvaluationFinding: Rule ID_2_01 violated",
			"line 26: MarkRuleEvaluationFinding: Rule ID_2_01 violated",

			// rules allowed cipher modes
			"line 10: MarkRuleEvaluationFinding: Rule ID_2_1_01 violated");
	}

	@Test
	public void testAESCCM() throws Exception {
		Set<Finding> findings = performTest("java/jca/AESCCM.java", "mark/bouncycastle/");

		expected(findings,
			// rule bouncy castle as provider
			"line 18: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Cipher verified",
			"line 32: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_SecureRandom violated",

			// rules ccm block cipher mode
			"line 18: MarkRuleEvaluationFinding: Rule ID_2_01 verified",
			"line 18: MarkRuleEvaluationFinding: Rule ID_2_1_01 verified",
			"line 18: MarkRuleEvaluationFinding: Rule ID_2_1_2_1_01 violated",

			"line 20: Violation against Order: Base c is not correctly terminated. Expected one of [c.init] to follow the correct last call on this base. (InvalidOrderforAEAD)");
	}

	@Test
	public void testAESGCM() throws Exception {
		Set<Finding> findings = performTest("java/jca/AESGCM.java", "mark/bouncycastle/");

		expected(findings,
			// rule bouncy castle as provider
			"line 23: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Cipher verified",
			"line 28: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_SecureRandom verified",
			"line 42: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Cipher verified",
			"line 47: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_SecureRandom verified",

			// rule block cipher
			"line 23: MarkRuleEvaluationFinding: Rule ID_2_01 verified",
			"line 42: MarkRuleEvaluationFinding: Rule ID_2_01 verified",

			// rule block cipher mode
			"line 23: MarkRuleEvaluationFinding: Rule ID_2_1_01 verified",
			"line 42: MarkRuleEvaluationFinding: Rule ID_2_1_01 verified",

			// rule gcm iv
			"line [23, 31]: MarkRuleEvaluationFinding: Rule ID_2_1_2_2_01 violated",
			"line [23, 50]: MarkRuleEvaluationFinding: Rule ID_2_1_2_2_01 violated",
			"line [31, 42]: MarkRuleEvaluationFinding: Rule ID_2_1_2_2_01 violated",
			"line [42, 50]: MarkRuleEvaluationFinding: Rule ID_2_1_2_2_01 violated",

			"line 23: Violation against Order: Base c is not correctly terminated. Expected one of [c.init] to follow the correct last call on this base. (InvalidOrderforAEAD)",
			"line 34: Violation against Order: c.update(plaintext); (update) is not allowed. Expected one of: c.instantiate (InvalidOrderforAEAD)",
			"line 42: Violation against Order: Base c is not correctly terminated. Expected one of [c.init] to follow the correct last call on this base. (InvalidOrderforAEAD)",
			"line 53: Violation against Order: c.update(plaintext); (update) is not allowed. Expected one of: c.instantiate (InvalidOrderforAEAD)");
	}

	@Test
	public void testAESCBC() throws Exception {
		Set<Finding> findings = performTest("java/jca/AESCBC.java", "mark/bouncycastle/");

		expected(findings,
			// rule bouncy castle as provider
			"line 11: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Cipher verified",
			"line 13: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Cipher verified",
			"line 14: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Cipher verified",
			"line 16: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Cipher verified",

			// rule block cipher
			"line 11: MarkRuleEvaluationFinding: Rule ID_2_01 verified",
			"line 13: MarkRuleEvaluationFinding: Rule ID_2_01 verified",
			"line 14: MarkRuleEvaluationFinding: Rule ID_2_01 verified",
			"line 16: MarkRuleEvaluationFinding: Rule ID_2_01 verified",

			// rule block cipher mode
			"line 11: MarkRuleEvaluationFinding: Rule ID_2_1_01 verified",
			"line 13: MarkRuleEvaluationFinding: Rule ID_2_1_01 verified",
			"line 14: MarkRuleEvaluationFinding: Rule ID_2_1_01 verified",
			"line 16: MarkRuleEvaluationFinding: Rule ID_2_1_01 verified",

			// rule iv for block ciphers
			"line 11: MarkRuleEvaluationFinding: Rule ID_2_1_2_3_01 violated",
			"line 13: MarkRuleEvaluationFinding: Rule ID_2_1_2_3_01 violated",
			"line 14: MarkRuleEvaluationFinding: Rule ID_2_1_2_3_01 violated",
			"line 16: MarkRuleEvaluationFinding: Rule ID_2_1_2_3_01 violated",

			// rule cbc padding
			"line 11: MarkRuleEvaluationFinding: Rule ID_2_1_3_01 violated",
			"line 13: MarkRuleEvaluationFinding: Rule ID_2_1_3_01 verified",
			"line 14: MarkRuleEvaluationFinding: Rule ID_2_1_3_01 verified",
			"line 16: MarkRuleEvaluationFinding: Rule ID_2_1_3_01 verified",

			// rule order basic cipher
			"line 11: Violation against Order: Base c1 is not correctly terminated. Expected one of [c.init] to follow the correct last call on this base. (InvalidOrderOfCipherOperations)",
			"line 13: Violation against Order: Base c2 is not correctly terminated. Expected one of [c.init] to follow the correct last call on this base. (InvalidOrderOfCipherOperations)",
			"line 14: Violation against Order: Base c3 is not correctly terminated. Expected one of [c.init] to follow the correct last call on this base. (InvalidOrderOfCipherOperations)",
			"line 16: Violation against Order: Base c4 is not correctly terminated. Expected one of [c.init] to follow the correct last call on this base. (InvalidOrderOfCipherOperations)");
	}

	@Test
	public void testAESCTR() throws Exception {
		Set<Finding> findings = performTest("java/jca/AESCTR.java", "mark/bouncycastle/");

		expected(findings,
			// rule bouncy castle as provider
			"line 23: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Cipher verified",
			"line 30: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_SecureRandom verified",
			"line 37: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Mac verified",

			// rule block cipher
			"line 23: MarkRuleEvaluationFinding: Rule ID_2_01 verified",

			// rule block cipher mode
			"line 23: MarkRuleEvaluationFinding: Rule ID_2_1_01 verified",

			// rule ctr counter
			"line 23: MarkRuleEvaluationFinding: Rule ID_2_1_2_4_01 violated", // fixme

			// rule aes/ctr with mac
			"line [47, 51]: MarkRuleEvaluationFinding: Rule ID_2_2_02 violated", // fixme
			"line [47, 61]: MarkRuleEvaluationFinding: Rule ID_2_2_02 violated", // fixme
			"line [51, 59]: MarkRuleEvaluationFinding: Rule ID_2_2_02 violated", // fixme
			"line [59, 61]: MarkRuleEvaluationFinding: Rule ID_2_2_02 violated", // fixme

			// rule mac
			"line 37: MarkRuleEvaluationFinding: Rule ID_5_3_01 verified",

			// rule mac key length
			//"line 37: MarkRuleEvaluationFinding: Rule ID_5_3_02_HMAC verified", // fixme

			// rule order basic cipher
			"line 23: Violation against Order: Base c is not correctly terminated. Expected one of [c.init] to follow the correct last call on this base. (InvalidOrderOfCipherOperations)",
			"line 47: Violation against Order: c.update(input, i * 16, 16) (update) is not allowed. Expected one of: c.instantiate (InvalidOrderOfCipherOperations)",
			"line 59: Violation against Order: c.doFinal(input, i * 16, (input.length % 16 == 0) ? 16 : input.length % 16) (finalize) is not allowed. Expected one of: c.instantiate (InvalidOrderOfCipherOperations)");
	}

	@Test
	public void testBCMac() throws Exception {
		Set<Finding> findings = performTest("java/jca/BCMac.java", "mark/bouncycastle/");

		expected(findings,
			"line 10: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Mac verified",
			"line 12: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Mac verified",
			"line 13: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Mac verified",
			"line 14: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Mac verified",
			"line 15: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Mac verified",
			"line 16: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Mac verified",
			"line 17: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Mac verified",
			"line 18: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Mac verified",
			"line 20: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Mac verified",
			"line 22: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Mac verified",
			"line 23: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Mac verified",
			"line 24: MarkRuleEvaluationFinding: Rule BouncyCastleProvider_Mac verified",

			// rule mac
			"line 10: MarkRuleEvaluationFinding: Rule ID_5_3_01 verified",
			"line 12: MarkRuleEvaluationFinding: Rule ID_5_3_01 verified",
			"line 13: MarkRuleEvaluationFinding: Rule ID_5_3_01 verified",
			"line 14: MarkRuleEvaluationFinding: Rule ID_5_3_01 verified",
			"line 15: MarkRuleEvaluationFinding: Rule ID_5_3_01 verified",
			"line 16: MarkRuleEvaluationFinding: Rule ID_5_3_01 verified",
			"line 17: MarkRuleEvaluationFinding: Rule ID_5_3_01 verified",
			"line 18: MarkRuleEvaluationFinding: Rule ID_5_3_01 verified",
			"line 20: MarkRuleEvaluationFinding: Rule ID_5_3_01 verified",
			"line 22: MarkRuleEvaluationFinding: Rule ID_5_3_01 violated", // ok
			"line 23: MarkRuleEvaluationFinding: Rule ID_5_3_01 violated", // ok
			"line 24: MarkRuleEvaluationFinding: Rule ID_5_3_01 violated", // ok

			// rule mac key length
			"line 10: MarkRuleEvaluationFinding: Rule ID_5_3_02_CMAC violated", // ok
			"line 20: MarkRuleEvaluationFinding: Rule ID_5_3_02_GMAC violated", // ok

			// rule mac tag length
			"line 10: MarkRuleEvaluationFinding: Rule ID_5_3_03_CMAC violated", // ok
			"line 20: MarkRuleEvaluationFinding: Rule ID_5_3_03_GMAC violated" // ok
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
