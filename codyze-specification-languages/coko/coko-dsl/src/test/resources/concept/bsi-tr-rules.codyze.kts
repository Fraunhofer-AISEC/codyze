@file:Import("bsi-tr.concepts")

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling.HasTransformation

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

interface Cypher {
    val algo: HasTransformation<Algorithm>
    val mode: HasTransformation<Mode>
    val keySize: HasTransformation<Int> // in bits
    val tagSize: HasTransformation<Int> // in bits; only applicable for mode == CCM
}

interface InitializationVector {
    val size: Int // in bits
}

interface Encryption {
    val cypher: Cypher
    val iv: InitializationVector

    fun encrypt(plaintext: Any?): Op
    fun decrypt(ciphertext: Any?): Op
}


@RuleSet
class GoodCrypto {
    @Rule
    fun `must be AES`(enc: Encryption) =
        whenever(enc.encrypt(Wildcard)) {
            ensure { enc.cypher.algo eq Algorithm.AES }
            ensure { enc.cypher.mode within list[Mode.CCM, Mode.GCM, Mode.CTR] }
            ensure { enc.cypher.keySize within list[128, 192, 256]}
        }

    @Rule
    fun `modes of operation`(enc: Encryption) =
        whenever({ enc.encrypt(Wildcard) and (enc.cypher.mode eq Mode.CCM) }) {
            ensure { enc.cypher.tagSize geq 96 }
        }
}

