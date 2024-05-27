// FIXME: following imports fail the script compilation
//import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
//import de.fraunhofer.aisec.cpg.graph.statements.DeclarationStatement
//import de.fraunhofer.aisec.cpg.graph.statements.expressions.Reference
import java.net.URI
import java.security.spec.AlgorithmParameterSpec

import java.io.OutputStream
import java.security.KeyPair
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.SecretKey

plugins { id("cpg") }

class MasterKey {
    fun construct(key: Any?): Op =
        op {
            constructor("org.cryptomancer.cryptolib.api.Masterkey") {
                signature(key)
            }
        }

    fun generate(csprng: SecureRandom?): Op =
        op {
            "org.cryptomancer.cryptolib.api.Masterkey.generate" {
                signature(csprng)
            }
        }

    fun copy(): Op =
        op {
            "org.cryptomancer.cryptolib.api.Masterkey.copy" {
                signature()
            }
        }

    fun destroy(): Op =
        op {
            "org.cryptomancer.cryptolib.api.Masterkey.destroy" {
                signature()
            }
        }

    fun close(): Op =
        op {
            "org.cryptomancer.cryptolib.api.Masterkey.close" {
                signature()
            }
        }
}

class MasterKeyLoader {
    fun loadKey(keyID: URI?): Op =
        op {
            "org.cryptomancer.cryptolib.api.MasterkeyLoader.loadKey" {
                signature(keyID)
            }
        }
}

class CryptorProviderImpl {
    fun provide(key: MasterKey?, random: SecureRandom): Op =
        op {
            "org.cryptomancer.cryptolib.v2.CryptorProviderImpl.provide" {
                signature(key, random)
            }
        }
}

class FileContentCryptorImpl {
    fun decryptChunk(cipher: Any?, chunkNum: Any?, header: Any?, auth: Any?): Op =
        op {
            "org.cryptomancer.cryptolib.v2.FileContentCryptorImpl.decryptChunk" {
                signature(cipher, chunkNum, header, auth)
            }
        }

    fun decryptChunk(cipher: Any?, clear: Any?, chunkNum: Any?, header: Any?, auth: Any?): Op =
        op {
            "org.cryptomancer.cryptolib.v2.FileContentCryptorImpl.decryptChunk" {
                signature(cipher, clear, chunkNum, header, auth)
            }
        }
}

class FileHeaderImpl {
    fun getReserved(): Op =
        op {
            "org.cryptomancer.cryptolib.v2.FileHeaderImpl.getReserved" {
                signature()
            }
        }

    fun setReserved(reserved: Long?): Op =
        op {
            "org.cryptomancer.cryptolib.v2.FileHeaderImpl.setReserved" {
                signature(reserved)
            }
        }
}

class CypherSupplier {
    fun forEncryption(key: SecretKey?, params: AlgorithmParameterSpec?): Op =
        op {
            "org.cryptomancer.cryptolib.common.CipherSupplier.forEncryption" {
                signature(key, params)
            }
        }

    fun forDecryption(key: SecretKey?, params: AlgorithmParameterSpec?): Op =
        op {
            "org.cryptomancer.cryptolib.common.CipherSupplier.forDecryption" {
                signature(key, params)
            }
        }

    fun forWrapping(kek: SecretKey?): Op =
        op {
            "org.cryptomancer.cryptolib.common.CipherSupplier.forWrapping" {
                signature(kek)
            }
        }

    fun forUnwrapping(kek: SecretKey?): Op =
        op {
            "org.cryptomancer.cryptolib.common.CipherSupplier.forUnwrapping" {
                signature(kek)
            }
        }
}

class DecryptingReadableByteChannel {
    fun construct(src: Any?, cryptor: Any?, authenticate: Any?, header: Any?, firstChunk: Any?): Op =
        constructor("org.cryptomancer.cryptolib.common.DecryptingReadableByteChannel") {
            // maybe this could be combined with "null"?
            signature(src, cryptor, authenticate)
            signature(src, cryptor, authenticate, header, firstChunk)
        }
}

class DestroyableSecretKey {
    // TODO: just as with master key... can we check usage of k?
}

class ECKeyPair {
    // TODO: Do we want to assert that parameters are correct and do not throw?
}

class MasterKeyFileAccess {
    // can we map java byte onto Kotlin Byte?
    fun readAllegedVaultVersion(masterKey: Array<Byte>?): Op =
        op {
            "org.cryptomancer.cryptolib.common.MasterKeyFileAccess.readAllegedVaultVersion" {
                signature(masterKey)
            }
        }
}

class MessageDigestSupplier {
    fun get(): Op =
        op {
            "org.cryptomancer.cryptolib.common.MessageDigestSupplier.get" {
                signature()
            }
        }
}

class P384KeyPair {
    fun create(pubKeySpec: X509EncodedKeySpec?, privKeySpec: PKCS8EncodedKeySpec): Op =
        op {
            "org.cryptomancer.cryptolib.common.P384KeyPair.create" {
                signature(pubKeySpec, privKeySpec)
            }
        }

    fun store(file: Any?, passphrase: Any?): Op =
        op {
            "org.cryptomancer.cryptolib.common.P384KeyPair.store" {
                signature(file, passphrase)
            }
        }
}

class PKCS12Helper {
    fun exportTo(keypair: KeyPair?, out: OutputStream?, passphrase: Array<Char>?, sigAlg: String?): Op =
        op {
            "org.cryptomancer.cryptolib.common.Pkcs12Helper.exportTo" {
                signature(keypair, out, passphrase, sigAlg)
            }
        }
}

class ReseedingSecureRandom {
    fun construct(seeder: Any?, csprng: Any?, reseedAfter: Any?, seedLength: Any?): Op =
        constructor("org.cryptomancer.cryptolib.common.ReseedingSecureRandom") {
            signature(seeder, csprng, reseedAfter, seedLength)
        }

    fun create(csprng: SecureRandom?): Op =
        op {
            "org.cryptomancer.cryptolib.common.ReseedingSecureRandom.create" {
                signature(csprng)
            }
        }
}

class Scrypt {
    // TODO: can we combine the following two?
    fun scrypt(passphrase: Any?, salt: Any?, cost: Any?, blockSize: Any?, keyLength: Any?): Op =
        op {
            "org.cryptomancer.cryptolib.common.Scrypt.scrypt" {
                signature(passphrase, salt, cost, blockSize, keyLength)
            }
        }
}

class X509CertBuilder {
    fun init(keypair: KeyPair?, sigAlg: String): Op =
        op {
            "org.cryptomancer.cryptolib.common.X509CertBuilder.init" {
                signature(keypair, sigAlg)
            }
        }
}

class CipherSupplier {
    fun construct(algorithm: Any?): Op =
        constructor("org.cryptomancer.cryptolib.common.CipherSupplier") {
            signature(algorithm)
        }
}

class SecureRandom {
    fun getInstanceStrong(): Op =
        op {
            "java.security.SecureRandom.getInstanceStrong" {
                signature()
            }
        }
}

val recommendedAlgorithms = setOf("AES", "AES_128", "AES_192", "AES_256")
val recommendedModes = setOf("CCM", "GCM", "CBC", "CTR")
val recommendedPaddings = setOf("ISO10126Padding", "PKCS5Padding")
val recommendedWrappings = setOf("AESWrap, AESWrap_128, AESWrap_192, AESWrap_256")

val validParameters = {
    val combinations = mutableSetOf<String>()
    val first = recommendedAlgorithms
    val second = recommendedModes
    val third = recommendedPaddings

    first.forEach { f ->
        second.forEach { s ->
            if (s == "CBC") {
                third.forEach { t ->
                    combinations.add("$f/$s/$t")
                }
            } else {
                combinations.add("$f/$s/NoPadding")
            }
        }
    }
    combinations.addAll(recommendedWrappings)

    combinations.toSet()
}

@Rule("Never skip authentication when decrypting a file chunk")
fun enforceFileDecryptAuthentication1(cryptor: FileContentCryptorImpl) =
    never(cryptor.decryptChunk(Wildcard, Wildcard, Wildcard, Wildcard, false))

@Rule("Never skip authentication when decrypting a file chunk")
fun enforceFileDecryptAuthentication2(cryptor: FileContentCryptorImpl) =
    never(cryptor.decryptChunk(Wildcard, Wildcard, Wildcard, false))

@Rule("Never skip authentication when decrypting a ciphertext")
fun enforceCipherDecryptAuthentication(channel: DecryptingReadableByteChannel) =
    never(channel.construct(Wildcard, Wildcard, false, Wildcard, Wildcard))

@Rule("Only use recommended algorithms")
fun enforceRecommendedAlgorithms(supplier: CipherSupplier) =
    run {
        val x = validParameters().map {
            supplier.construct(it)
        }.toTypedArray()
        only(*x)
    }

// FIXME
@Rule("Do not use empty passphrase to store the key pair")
fun forbidEmptyPassphrase(keypair: P384KeyPair) =
    never(keypair.store(Wildcard, arrayOf<Char>()))

// FIXME NIST SP 800-90A Rev 1: http://dx.doi.org/10.6028/NIST.SP.800-90Ar1
@Rule("Use minimum strength for reseeding parameters")
fun enforceStrongReseedingParameters(reseeding: ReseedingSecureRandom) =
    run {
        val minSeedBytes = 440 / 8
        val maxValue = Long.MAX_VALUE
        val maxReseedInterval = 1L shl 48
        // FIXME: CPG does not terminate when we use wildcard as the first argument?
        only(reseeding.construct(null, Wildcard, 0..maxReseedInterval, minSeedBytes..maxValue))
    }

// FIXME test
@Rule("Use SecureRandom.getInstanceStrong() as the seeder")
fun enforceStrongReseedingSeeder(reseeding: ReseedingSecureRandom, secureRandom: SecureRandom) =
    // FIXME: CPG does not terminate when we use wildcard as the first argument?
    // FIXME: Index out of bounds exception when using java.security.SecureRandom as rule parameter
    argumentOrigin(reseeding::construct, 0, secureRandom::getInstanceStrong)

// FIXME
@Rule("Do not use empty scrypt password")
fun forbitEmptyScryptPassword(scrypt: Scrypt) =
    never(scrypt.scrypt(arrayOf<Byte>(), Wildcard, Wildcard, Wildcard, Wildcard))

// FIXME
@Rule("Do not use empty scrypt password")
fun forbitEmptyScryptPassword2(scrypt: Scrypt) =
    never(scrypt.scrypt(arrayOf<Char>(), Wildcard, Wildcard, Wildcard, Wildcard))

// FIXME
@Rule("Do not use empty scrypt salt")
fun forbitEmptyScryptSalt(scrypt: Scrypt) =
    never(scrypt.scrypt(Wildcard, arrayOf<Byte>(), Wildcard, Wildcard, Wildcard))
