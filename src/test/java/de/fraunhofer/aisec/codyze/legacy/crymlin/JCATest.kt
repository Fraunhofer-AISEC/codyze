package de.fraunhofer.aisec.codyze.legacy.crymlin

import org.junit.jupiter.api.Test

internal class JCATest : AbstractMarkTest() {
    @Test
    @Throws(Exception::class)
    fun testBCProviderCipher() {
        val findings =
            performTest(
                "legacy/java/jca/BCProviderCipher.java",
                arrayOf("legacy/java/jca/include/BouncyCastleProvider.java"),
                "mark/bouncycastle/"
            )

        // possible lines: 19,22,23,24,27,28
        expected(
            findings, // rule bouncy castle as provider
            "line 19: Rule BouncyCastleProvider_Cipher violated", // ok
            "line 22: Rule BouncyCastleProvider_Cipher verified", // ok
            "line 23: Rule BouncyCastleProvider_Cipher violated", // improv type resolution for
            // BouncyCastleProvider class
            "line 24: Rule BouncyCastleProvider_Cipher verified", // type hierarchy is now available
            "line 27: Rule BouncyCastleProvider_Cipher violated", // ok
            "line 28: Rule BouncyCastleProvider_Cipher violated", // ok
            // rule allowed ciphers
            "line 19: Rule ID_2_01 verified", // ok
            "line 22: Rule ID_2_01 verified", // ok
            "line 23: Rule ID_2_01 verified", // ok
            "line 24: Rule ID_2_01 verified", // type hierarchy is now available
            "line 27: Rule ID_2_01 verified", // ok
            "line 28: Rule ID_2_01 verified", // ok
            // rule allowed block cipher modes
            "line 19: Rule ID_2_1_01 violated", // ok, minimal test
            "line 22: Rule ID_2_1_01 violated", // ok, minimal test
            "line 23: Rule ID_2_1_01 violated", // ok, minimal test
            "line 24: Rule ID_2_1_01 violated", // type hierarchy is now available
            "line 27: Rule ID_2_1_01 violated", // ok, minimal test
            "line 28: Rule ID_2_1_01 violated" // ok, minimal test
        )
    }

    @Test
    @Throws(Exception::class)
    fun testBlockCipher() {
        val findings = performTest("legacy/java/jca/BlockCipher.java", "mark/bouncycastle/")

        // possible lines: 10,14,28,22,26
        expected(
            findings, // rules for Bouncy Castle as provider
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
        )
    }

    @Test
    @Throws(Exception::class)
    fun testAESCCM() {
        val findings = performTest("legacy/java/jca/AESCCM.java", "mark/bouncycastle/")

        // possible lines: 18,22,23,24,28,30,31,36
        expected(
            findings, // rule bouncy castle as provider
            "line 18: Rule BouncyCastleProvider_Cipher verified", // ok
            "line 22: Rule BouncyCastleProvider_KeyGenerator verified", // ok
            "line 30: Rule BouncyCastleProvider_SecureRandom verified", // ok
            // rules ccm block cipher mode
            "line 18: Rule ID_2_01 verified", // ok
            "line 18: Rule ID_2_1_01 verified", // ok
            "line [36]: Rule ID_2_1_2_1_02 verified", // ok
            // rules order
            "line 36: Violation against Order: Base c is not correctly terminated. Expected one of [c.aad, c.finalize, c.update] to follow the correct last call on this base. (InvalidOrderforAEAD)" // ok, minimal test
        )
    }

    @Test
    @Throws(Exception::class)
    fun testAESGCM() {
        val findings =
            performTest(
                "legacy/java/jca/AESGCM.java",
                arrayOf("legacy/java/jca/include/GCMParameterSpec.java"),
                "mark/bouncycastle/"
            )
        expected(
            findings, // rule bouncy castle as provider
            "line 23: Rule BouncyCastleProvider_Cipher verified", // ok
            "line 28: Rule BouncyCastleProvider_SecureRandom verified", // ok
            "line 41: Rule BouncyCastleProvider_Cipher verified", // ok
            "line 46: Rule BouncyCastleProvider_SecureRandom verified", // ok
            "line 62: Rule BouncyCastleProvider_KeyGenerator violated", // rule block cipher
            "line 23: Rule ID_2_01 verified", // ok
            "line 41: Rule ID_2_01 verified", // ok
            // rule block cipher mode
            "line 23: Rule ID_2_1_01 verified", // ok
            "line 41: Rule ID_2_1_01 verified", // ok
            // GCM nonce length for authentication tag
            "line 31: Rule ID_2_1_2_2_02 verified", // ok
            "line 49: Rule ID_2_1_2_2_02 verified", // ok
            // GCM minimum length of authentication tag
            "line 31: Rule ID_2_1_2_2_03 verified", // ok
            "line 49: Rule ID_2_1_2_2_03 verified", // ok
            "line 23: Verified Order: AEAD_Crypt", // ok
            "line 41: Verified Order: AEAD_Crypt"
        ) // ok
    }

    @Test
    @Throws(Exception::class)
    fun testAESCBC() {
        val findings = performTest("legacy/java/jca/AESCBC.java", "mark/bouncycastle/")
        expected(
            findings, // rule bouncy castle as provider
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
            // CBC unpredictable IV
            "line 11: Rule ID_2_1_2_3_01 violated", // ok
            "line 13: Rule ID_2_1_2_3_01 violated", // ok
            "line 14: Rule ID_2_1_2_3_01 violated", // ok
            "line 16: Rule ID_2_1_2_3_01 violated", // ok
            // rule order basic cipher
            //            "line 11: Violation against Order: Base c1 is not correctly terminated.
            // Expected one of [c.init] to follow the correct last call on this base.
            // (InvalidOrderOfCipherOperations)", // ok, minimal test
            //            "line 13: Violation against Order: Base c2 is not correctly terminated.
            // Expected one of [c.init] to follow the correct last call on this base.
            // (InvalidOrderOfCipherOperations)", // ok, minimal test
            //            "line 14: Violation against Order: Base c3 is not correctly terminated.
            // Expected one of [c.init] to follow the correct last call on this base.
            // (InvalidOrderOfCipherOperations)", // ok, minimal test
            //            "line 16: Violation against Order: Base c4 is not correctly terminated.
            // Expected one of [c.init] to follow the correct last call on this base.
            // (InvalidOrderOfCipherOperations)" // ok, minimal test
            )
    }

    @Test
    @Throws(Exception::class)
    fun testAESCTR() {
        val findings =
            performTest(
                "legacy/java/jca/AESCTR.java",
                arrayOf(
                    "legacy/java/jca/include/IvParameterSpec.java",
                    "legacy/java/jca/include/SecretKey.java"
                ),
                "mark/bouncycastle/"
            )
        expected(
            findings, // rule bouncy castle as provider
            "line 23: Rule BouncyCastleProvider_Cipher verified", // ok
            "line 25: Rule BouncyCastleProvider_KeyGenerator verified",
            "line 30: Rule BouncyCastleProvider_SecureRandom verified", // ok
            "line 37: Rule BouncyCastleProvider_Mac verified", // ok
            "line 38: Rule BouncyCastleProvider_KeyGenerator verified", // rule block cipher
            "line 23: Rule ID_2_01 verified", // ok
            // rule block cipher mode
            "line 23: Rule ID_2_1_01 verified", // ok*/
            // rule aes/ctr with mac (these seem to be broken because of the _is builtin does not
            // work the way it is assumed!)
            "line [47, 51]: Rule ID_2_2_02 verified", // improv rule
            "line [47, 61]: Rule ID_2_2_02 verified", // improv rule
            "line [51, 59]: Rule ID_2_2_02 verified", // improv rule
            "line [59, 61]: Rule ID_2_2_02 verified", // improv rule
            // rule mac
            "line 37: Rule ID_5_3_01 verified", // ok
            // rule mac key length
            "line [37, 38]: Rule ID_5_3_02_HMAC_Keygen verified", // ok
            "line 23: Verified Order: Crypt"
        )
    }

    @Test
    @Throws(Exception::class)
    fun testBCMac() {
        val findings = performTest("legacy/java/jca/BCMac.java", "mark/bouncycastle/")
        expected(
            findings,
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
            "line 10: Rule ID_5_3_01 verified", // ok
            "line 12: Rule ID_5_3_01 verified", // ok
            "line 13: Rule ID_5_3_01 verified", // ok
            "line 14: Rule ID_5_3_01 verified", // ok
            "line 15: Rule ID_5_3_01 verified", // ok
            "line 16: Rule ID_5_3_01 verified", // ok
            "line 17: Rule ID_5_3_01 verified", // ok
            "line 18: Rule ID_5_3_01 verified", // ok
            "line 20: Rule ID_5_3_01 verified", // ok
            "line 22: Rule ID_5_3_01 violated", // ok
            "line 23: Rule ID_5_3_01 violated", // ok
            "line 24: Rule ID_5_3_01 violated", // ok
            // rule mac tag length
            "line 10: Rule ID_5_3_03_CMAC verified", // ok
            "line 20: Rule ID_5_3_03_GMAC verified" // ok
        )
    }

    @Test
    @Throws(Exception::class)
    fun testRSACipherTest() {
        val findings = performTest("legacy/java/jca/BCRSACipher.java", "mark/bouncycastle/")
        expected(
            findings,
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
        )
    }
}
