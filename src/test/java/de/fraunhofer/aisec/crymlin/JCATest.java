
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.analysis.utils.Utils;
import de.fraunhofer.aisec.cpg.graph.TypeManager;
import de.fraunhofer.aisec.cpg.graph.type.ObjectType;
import de.fraunhofer.aisec.cpg.graph.type.Type;
import de.fraunhofer.aisec.cpg.graph.type.TypeParser;
import de.fraunhofer.aisec.mark.markDsl.MarkDslFactory;
import de.fraunhofer.aisec.mark.markDsl.Parameter;
import de.fraunhofer.aisec.mark.markDsl.impl.MarkDslFactoryImpl;
import de.fraunhofer.aisec.mark.markDsl.impl.ParameterImpl;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JCATest extends AbstractMarkTest {

	@Test
	public void testBCProviderCipher() throws Exception {
		Set<Finding> findings = performTest("java/jca/BCProviderCipher.java",
			new String[] {
					"java/jca/include/BouncyCastleProvider.java"
			},
			"mark/bouncycastle/");

		// possible lines: 19,22,23,24,27,28
		expected(findings,
			// rule bouncy castle as provider
			"line 19: Rule BouncyCastleProvider_Cipher violated", // ok
			"line 22: Rule BouncyCastleProvider_Cipher verified", // ok
			"line 23: Rule BouncyCastleProvider_Cipher violated", // improv type resolution for BouncyCastleProvider class
			//"line 24: Rule BouncyCastleProvider_Cipher verified", // type hierarchy not available from CPG
			"line 27: Rule BouncyCastleProvider_Cipher violated", // ok
			"line 28: Rule BouncyCastleProvider_Cipher violated", // ok

			// rule allowed ciphers
			"line 19: Rule ID_2_01 verified", // ok
			"line 22: Rule ID_2_01 verified", // ok
			"line 23: Rule ID_2_01 verified", // ok
			// "line 24: Rule ID_2_01 verified", // type hierarchy not available from CPG
			"line 27: Rule ID_2_01 verified", // ok
			"line 28: Rule ID_2_01 verified", // ok

			// rule allowed block cipher modes
			"line 19: Rule ID_2_1_01 violated", // ok, minimal test
			"line 22: Rule ID_2_1_01 violated", // ok, minimal test
			"line 23: Rule ID_2_1_01 violated", // ok, minimal test
			// "line 24: Rule ID_2_1_01 violated", // type hierarchy not available from CPG
			"line 27: Rule ID_2_1_01 violated", // ok, minimal test
			"line 28: Rule ID_2_1_01 violated" // ok, minimal test
		);
	}

	@Test
	public void testBlockCipher() throws Exception {
		Set<Finding> findings = performTest("java/jca/BlockCipher.java", "mark/bouncycastle/");

		// possible lines: 10,14,28,22,26
		expected(findings,
			// rules for Bouncy Castle as provider
			"line 10: Rule BouncyCastleProvider_Cipher verified", // ok
			"line 14: Rule BouncyCastleProvider_Cipher verified", // ok
			"line 18: Rule BouncyCastleProvider_Cipher verified", // ok
			"line 22: Rule BouncyCastleProvider_Cipher verified", // ok
			"line 26: Rule BouncyCastleProvider_Cipher verified", // ok

			// rule allowed ciphers
			"line 10: Rule ID_2_01 verified", // ok
			"line 14: Rule ID_2_01 violated", // ok
			"line 18: Rule ID_2_01 violated", // ok
			"line 22: Rule ID_2_01 violated", // ok
			"line 26: Rule ID_2_01 violated", // ok

			// rules allowed cipher modes
			"line 10: Rule ID_2_1_01 violated" // ok, minimal test
		);
	}

	@Test
	public void testAESCCM() throws Exception {
		Set<Finding> findings = performTest("java/jca/AESCCM.java", "mark/bouncycastle/");

		// possible lines: 18,22,23,24,28,30,31,36
		expected(findings,
			// rule bouncy castle as provider
			"line 18: Rule BouncyCastleProvider_Cipher verified", // ok
			"line 22: Rule BouncyCastleProvider_KeyGenerator verified", // ok
			"line 30: Rule BouncyCastleProvider_SecureRandom verified", // ok

			// rules ccm block cipher mode
			"line 18: Rule ID_2_01 verified", // ok
			"line 18: Rule ID_2_1_01 verified", // ok

			"line [36]: Rule ID_2_1_2_1_02 verified", // ok

			// rules order
			"line 36: Violation against Order: Base c is not correctly terminated. Expected one of [c.aad, c.finalize, c.update] to follow the correct last call on this base. (InvalidOrderforAEAD)" // ok, minimal test
		);
	}

	@Test
	public void testSubTypesOf() throws Exception {
		// Load some source code that uses GCMParameterSpec
		Set<Finding> findings = performTest("java/jca/AESGCM.java",
			new String[] {
					"java/jca/include/GCMParameterSpec.java"
			},
			"mark/bouncycastle/");

		// Get the GCMParameterSpec type from TypeManager
		Set<Type> type = TypeManager.getInstance()
				.getFirstOrderTypes()
				.stream()
				.filter(t -> t.getTypeName().equals("javax.crypto.spec.GCMParameterSpec"))
				.collect(Collectors.toSet());

		// Create a Mark type matching a supertype.
		MarkDslFactory factory = new MarkDslFactoryImpl();
		Parameter markType = factory.createParameter();
		markType.setVar("x");
		markType.getTypes().add("java.security.spec.AlgorithmParameterSpec");

		// Make sure super types are matched.
		assertTrue(Utils.isSubTypeOf(type, markType));
	}

	@Test
	public void testAESGCM() throws Exception {
		Set<Finding> findings = performTest("java/jca/AESGCM.java",
			new String[] {
					"java/jca/include/GCMParameterSpec.java"
			},
			"mark/bouncycastle/");

		expected(findings,
			// rule bouncy castle as provider
			"line 23: Rule BouncyCastleProvider_Cipher verified", // ok
			"line 28: Rule BouncyCastleProvider_SecureRandom verified", // ok
			"line 41: Rule BouncyCastleProvider_Cipher verified", // ok
			"line 46: Rule BouncyCastleProvider_SecureRandom verified", // ok
			"line 62: Rule BouncyCastleProvider_KeyGenerator violated",

			// rule block cipher
			"line 23: Rule ID_2_01 verified", // ok
			"line 41: Rule ID_2_01 verified", // ok

			// rule block cipher mode
			"line 23: Rule ID_2_1_01 verified", // ok
			"line 41: Rule ID_2_1_01 verified", // ok

			"line 23: Violation against Order: Base c is not correctly terminated. Expected one of [c.init] to follow the correct last call on this base. (InvalidOrderforAEAD)", // FP, Type system improv in CPG needed
			"line 34: Violation against Order: c.doFinal(plaintext) (finalize) is not allowed. Expected one of: c.init (InvalidOrderforAEAD)", // FP, Type system improv in CPG needed

			"line 41: Violation against Order: Base c is not correctly terminated. Expected one of [c.init] to follow the correct last call on this base. (InvalidOrderforAEAD)", // FP, Type system improv in CPG needed
			"line 52: Violation against Order: c.doFinal(plaintext) (finalize) is not allowed. Expected one of: c.init (InvalidOrderforAEAD)"); // FP, Type system improv in CPG needed
	}

	@Test
	public void testAESCBC() throws Exception {
		Set<Finding> findings = performTest("java/jca/AESCBC.java", "mark/bouncycastle/");

		expected(findings,
			// rule bouncy castle as provider
			"line 11: Rule BouncyCastleProvider_Cipher verified", // ok
			"line 13: Rule BouncyCastleProvider_Cipher verified", // ok
			"line 14: Rule BouncyCastleProvider_Cipher verified", // ok
			"line 16: Rule BouncyCastleProvider_Cipher verified", // ok

			// rule block cipher
			"line 11: Rule ID_2_01 verified", // ok
			"line 13: Rule ID_2_01 verified", // ok
			"line 14: Rule ID_2_01 verified", // ok
			"line 16: Rule ID_2_01 verified", // ok

			// rule block cipher mode
			"line 11: Rule ID_2_1_01 verified", // ok
			"line 13: Rule ID_2_1_01 verified", // ok
			"line 14: Rule ID_2_1_01 verified", // ok
			"line 16: Rule ID_2_1_01 verified", // ok

			// rule cbc padding
			"line 11: Rule ID_2_1_3_01 violated", // ok
			"line 13: Rule ID_2_1_3_01 verified", // ok
			"line 14: Rule ID_2_1_3_01 verified", // ok
			"line 16: Rule ID_2_1_3_01 verified", // ok

			// rule order basic cipher
			"line 11: Violation against Order: Base c1 is not correctly terminated. Expected one of [c.init] to follow the correct last call on this base. (InvalidOrderOfCipherOperations)", // ok, minimal test
			"line 13: Violation against Order: Base c2 is not correctly terminated. Expected one of [c.init] to follow the correct last call on this base. (InvalidOrderOfCipherOperations)", // ok, minimal test
			"line 14: Violation against Order: Base c3 is not correctly terminated. Expected one of [c.init] to follow the correct last call on this base. (InvalidOrderOfCipherOperations)", // ok, minimal test
			"line 16: Violation against Order: Base c4 is not correctly terminated. Expected one of [c.init] to follow the correct last call on this base. (InvalidOrderOfCipherOperations)" // ok, minimal test
		);
	}

	@Test
	public void testAESCTR() throws Exception {
		Set<Finding> findings = performTest("java/jca/AESCTR.java",
			new String[] {
					"java/jca/include/IvParameterSpec.java",
					"java/jca/include/SecretKey.java"
			},
			"mark/bouncycastle/");

		expected(findings,
			// rule bouncy castle as provider
			"line 23: Rule BouncyCastleProvider_Cipher verified", // ok
			"line 25: Rule BouncyCastleProvider_KeyGenerator verified",
			"line 30: Rule BouncyCastleProvider_SecureRandom verified", // ok
			"line 37: Rule BouncyCastleProvider_Mac verified", // ok
			"line 38: Rule BouncyCastleProvider_KeyGenerator verified",

			// rule block cipher
			"line 23: Rule ID_2_01 verified", // ok

			// rule block cipher mode
			"line 23: Rule ID_2_1_01 verified", // ok

			// rule aes/ctr with mac
			"line [47, 51]: Rule ID_2_2_02 violated", // improv rule
			"line [47, 61]: Rule ID_2_2_02 violated", // improv rule
			"line [51, 59]: Rule ID_2_2_02 violated", // improv rule
			"line [59, 61]: Rule ID_2_2_02 violated", // improv rule

			// rule mac
			"line 37: Rule ID_5_3_01 verified", // ok

			// rule mac key length
			//"line 37: Rule ID_5_3_02_HMAC verified", // improv analysis. Currently, codyze does not know that `kg2.generateKey()` returns a java.security.Key, this needs to be returned from the CPG

			"line 23: Verified Order: Crypt");
	}

	@Test
	public void testBCMac() throws Exception {
		Set<Finding> findings = performTest("java/jca/BCMac.java", "mark/bouncycastle/");

		expected(findings,
			"line 10: Rule BouncyCastleProvider_Mac verified", // ok
			"line 12: Rule BouncyCastleProvider_Mac verified", // ok
			"line 13: Rule BouncyCastleProvider_Mac verified", // ok
			"line 14: Rule BouncyCastleProvider_Mac verified", // ok
			"line 15: Rule BouncyCastleProvider_Mac verified", // ok
			"line 16: Rule BouncyCastleProvider_Mac verified", // ok
			"line 17: Rule BouncyCastleProvider_Mac verified", // ok
			"line 18: Rule BouncyCastleProvider_Mac verified", // ok
			"line 20: Rule BouncyCastleProvider_Mac verified", // ok
			"line 22: Rule BouncyCastleProvider_Mac verified", // ok
			"line 23: Rule BouncyCastleProvider_Mac verified", // ok
			"line 24: Rule BouncyCastleProvider_Mac verified", // ok

			// rule mac

			// rule mac tag length
			"line 10: Rule ID_5_3_03_CMAC verified", // ok
			"line 20: Rule ID_5_3_03_GMAC verified" // ok
		);
	}

	@Test
	public void testRSACipherTest() throws Exception {
		Set<Finding> findings = performTest("java/jca/BCRSACipher.java", "mark/bouncycastle/");

		expected(findings,
			"line 6: Rule BouncyCastleProvider_Cipher verified", // ok
			"line 8: Rule BouncyCastleProvider_Cipher verified", // ok
			"line 7: Rule BouncyCastleProvider_Cipher verified", // ok
			"line 9: Rule BouncyCastleProvider_Cipher verified", // ok
			"line 10: Rule BouncyCastleProvider_Cipher verified", // ok
			"line 11: Rule BouncyCastleProvider_Cipher verified", // ok
			"line 13: Rule BouncyCastleProvider_Cipher verified", // ok

			"line 6: Rule ID_2_01 verified", // ok
			"line 8: Rule ID_2_01 verified", // ok
			"line 7: Rule ID_2_01 verified", // ok
			"line 9: Rule ID_2_01 verified", // ok
			"line 10: Rule ID_2_01 verified", // ok
			"line 11: Rule ID_2_01 verified", // ok
			"line 13: Rule ID_2_01 verified", // ok

			"line 6: Rule ID_3_5_01 verified", // ok
			"line 7: Rule ID_3_5_01 verified", // ok
			"line 8: Rule ID_3_5_01 verified", // ok
			"line 9: Rule ID_3_5_01 verified", // ok
			"line 10: Rule ID_3_5_01 verified", // ok
			"line 11: Rule ID_3_5_01 verified", // ok
			"line 13: Rule ID_3_5_01 violated" // ok
		);
	}

}
