@file:Import("bsi-tr.concepts")

enum class Algorithm {
    AES, DES, TripleDES
}

enum class Mode{
    // Cipher Block Chaining Message Authentication
    CCM,
    // Galois/Counter Mode
    GCM,
    // Cipher Block Chaining
    CBC,
    // Counter Mode
    CTR
}

interface Encryption {
    val cypher: Cypher
    val iv: Op

    fun encrypt(plaintext: Any?): Op
    fun decrypt(ciphertext: Any?): Op
}

interface Cypher {
    var algo: Algorithm
    var mode: Mode
    var keySize: Int // in bits
    var tagSize: Int // in bits; only applicable for mode == CCM
}

@RuleSet
class GoodCrypto {
    @Rule
    fun `must be AES`(enc: Encryption) =
        whenever(enc.encrypt(Wildcard)) {
            ensure { enc.cypher.algo == Algorithm.AES}
            ensure { enc.cypher.mode in list[Mode.CCM, Mode.GCM, Mode.CTR] }
        }

}

