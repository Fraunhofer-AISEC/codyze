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
            constructor("org.cryptomator.cryptolib.api.Masterkey") {
                signature(key)
            }
        }

    fun generate(csprng: SecureRandom?): Op =
        op {
            "org.cryptomator.cryptolib.api.Masterkey.generate" {
                signature(csprng)
            }
        }

    fun copy(): Op =
        op {
            "org.cryptomator.cryptolib.api.Masterkey.copy" {
                signature()
            }
        }

    fun destroy(): Op =
        op {
            "org.cryptomator.cryptolib.api.Masterkey.destroy" {
                signature()
            }
        }

    fun close(): Op =
        op {
            "org.cryptomator.cryptolib.api.Masterkey.close" {
                signature()
            }
        }
}

class MasterKeyLoader {
    fun loadKey(keyID: URI?): Op =
        op {
            "org.cryptomator.cryptolib.api.MasterkeyLoader.loadKey" {
                signature(keyID)
            }
        }
}

class CryptorProviderImpl {
    fun provide(key: MasterKey?, random: SecureRandom): Op =
        op {
            "org.cryptomator.cryptolib.v2.CryptorProviderImpl.provide" {
                signature(key, random)
            }
        }
}

class FileContentCryptorImpl {
    fun decryptChunk(cipher: Any?, chunkNum: Any?, header: Any?, auth: Any?): Op =
        op {
            "org.cryptomator.cryptolib.v2.FileContentCryptorImpl.decryptChunk" {
                signature(cipher, chunkNum, header, auth)
            }
        }

    fun decryptChunk(cipher: Any?, clear: Any?, chunkNum: Any?, header: Any?, auth: Any?): Op =
        op {
            "org.cryptomator.cryptolib.v2.FileContentCryptorImpl.decryptChunk" {
                signature(cipher, clear, chunkNum, header, auth)
            }
        }

    fun encryptChunk(cleartext: Any?, cipherText: Any?, chunkNumber: Any?, headerNonce: Any?, fileKey: Any?): Op =
        op {
            "org.cryptomator.cryptolib.v2.FileContentCryptorImpl.encryptChunk" {
                signature(cleartext, cipherText, chunkNumber, headerNonce, fileKey)
            }
        }
}

class FileHeaderImpl {
    fun construct(nonce: Any?, payload: Any?): Op =
        constructor("org.cryptomator.cryptolib.v2.FileHeaderImpl") {
            signature(nonce, payload)
        }

    fun getReserved(): Op =
        op {
            "org.cryptomator.cryptolib.v2.FileHeaderImpl.getReserved" {
                signature()
            }
        }

    fun setReserved(reserved: Long?): Op =
        op {
            "org.cryptomator.cryptolib.v2.FileHeaderImpl.setReserved" {
                signature(reserved)
            }
        }
}

class CypherSupplier {
    fun forEncryption(key: SecretKey?, params: AlgorithmParameterSpec?): Op =
        op {
            "org.cryptomator.cryptolib.common.CipherSupplier.forEncryption" {
                signature(key, params)
            }
        }

    fun forDecryption(key: SecretKey?, params: AlgorithmParameterSpec?): Op =
        op {
            "org.cryptomator.cryptolib.common.CipherSupplier.forDecryption" {
                signature(key, params)
            }
        }

    fun forWrapping(kek: SecretKey?): Op =
        op {
            "org.cryptomator.cryptolib.common.CipherSupplier.forWrapping" {
                signature(kek)
            }
        }

    fun forUnwrapping(kek: SecretKey?): Op =
        op {
            "org.cryptomator.cryptolib.common.CipherSupplier.forUnwrapping" {
                signature(kek)
            }
        }
}

class DecryptingReadableByteChannel {
    fun construct(src: Any?, cryptor: Any?, authenticate: Any?, header: Any?, firstChunk: Any?): Op =
        constructor("org.cryptomator.cryptolib.common.DecryptingReadableByteChannel") {
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
            "org.cryptomator.cryptolib.common.MasterKeyFileAccess.readAllegedVaultVersion" {
                signature(masterKey)
            }
        }
}

class MessageDigestSupplier {
    fun get(): Op =
        op {
            "org.cryptomator.cryptolib.common.MessageDigestSupplier.get" {
                signature()
            }
        }
}

class P384KeyPair {
    fun create(pubKeySpec: X509EncodedKeySpec?, privKeySpec: PKCS8EncodedKeySpec): Op =
        op {
            "org.cryptomator.cryptolib.common.P384KeyPair.create" {
                signature(pubKeySpec, privKeySpec)
            }
        }

    fun store(file: Any?, passphrase: Any?): Op =
        op {
            "org.cryptomator.cryptolib.common.P384KeyPair.store" {
                signature(file, passphrase)
            }
        }
}

class PKCS12Helper {
    fun exportTo(keypair: KeyPair?, out: OutputStream?, passphrase: Array<Char>?, sigAlg: String?): Op =
        op {
            "org.cryptomator.cryptolib.common.Pkcs12Helper.exportTo" {
                signature(keypair, out, passphrase, sigAlg)
            }
        }
}

class ReseedingSecureRandom {
    fun construct(seeder: Any?, csprng: Any?, reseedAfter: Any?, seedLength: Any?): Op =
        constructor("org.cryptomator.cryptolib.common.ReseedingSecureRandom") {
            signature(seeder, csprng, reseedAfter, seedLength)
        }

    fun create(csprng: SecureRandom?): Op =
        op {
            "org.cryptomator.cryptolib.common.ReseedingSecureRandom.create" {
                signature(csprng)
            }
        }
}

class Scrypt {
    fun scrypt(passphrase: Any?, salt: Any?, cost: Any?, blockSize: Any?, keyLength: Any?): Op =
        op {
            "org.cryptomator.cryptolib.common.Scrypt.scrypt" {
                signature(passphrase, salt, cost, blockSize, keyLength)
            }
        }
}

class X509CertBuilder {
    fun init(keypair: KeyPair?, sigAlg: String): Op =
        op {
            "org.cryptomator.cryptolib.common.X509CertBuilder.init" {
                signature(keypair, sigAlg)
            }
        }
}

class CipherSupplier {
    fun construct(algorithm: Any?): Op =
        constructor("org.cryptomator.cryptolib.common.CipherSupplier") {
            signature(algorithm)
        }
}

class SecureRandom {
    fun getInstanceStrong(): Op =
        op {
            // FIXME: cpg finds either SecureRandom.getInstanceStrong or java.security.getInstanceStrong
            //  depending on whether a import or the full name is used
            "SecureRandom.getInstanceStrong" {
                signature()
            }
        }
}

val recommendedAlgorithms = setOf("AES", "AES_128", "AES_192", "AES_256")
val recommendedModes = setOf("CCM", "GCM", "CBC", "CTR")
val recommendedPaddings = setOf("ISO10126Padding", "PKCS5Padding")
val recommendedWrappings = setOf("AESWrap", "AESWrap_128", "AESWrap_192", "AESWrap_256")

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

@Rule("Do not use empty passphrase to store the key pair")
fun forbidEmptyPassphrase(keypair: P384KeyPair) =
    never(keypair.store(Wildcard, Length(0..0)))

// Parameters from NIST SP 800-90A Rev 1: http://dx.doi.org/10.6028/NIST.SP.800-90Ar1
//@Rule("Enforce maximum reseed interval for reseeding parameters")
//fun enforceStrongReseedingInterval(reseeding: ReseedingSecureRandom) =
//    run {
//        val maxReseedInterval = 1L shl 48
//        // FIXME: CPG does not terminate for the huge number of possible values
//        only(reseeding.construct(Wildcard, Wildcard, 0..maxReseedInterval, Wildcard))
//    }

// Parameters from NIST SP 800-90A Rev 1: http://dx.doi.org/10.6028/NIST.SP.800-90Ar1
@Rule("Do not use short seed length for reseeding parameters")
fun forbidShortReseedingSeed(reseeding: ReseedingSecureRandom) =
    run {
        val minSeedBytes = 440 / 8
        never(reseeding.construct(Wildcard, Wildcard, Wildcard, 0..<minSeedBytes))
    }

//@Rule("Use SecureRandom.getInstanceStrong() as the seeder")
//fun enforceStrongReseedingSeeder(reseeding: ReseedingSecureRandom, secureRandom: SecureRandom) =
//    // FIXME: FQN of SecureRandom.getInstanceStrong is not consistent
//    // FIXME: To prevent false positives the seeder must be set within the analyzed files
//    argumentOrigin(reseeding::construct, 0, secureRandom::getInstanceStrong)


@Rule("Do not use empty scrypt password")
fun forbidEmptyScryptPassword(scrypt: Scrypt) =
    never(scrypt.scrypt(Length(0..0), Wildcard, Wildcard, Wildcard, Wildcard))

@Rule("Do not use empty scrypt salt")
fun forbidEmptyScryptSalt(scrypt: Scrypt) =
    never(scrypt.scrypt(Wildcard, Length(0..0), Wildcard, Wildcard, Wildcard))

@Rule("Do not create a short key with scrypt")
fun enforceScryptKeyLength(scrypt: Scrypt) =
    never(scrypt.scrypt(Wildcard, Wildcard, Wildcard, Wildcard, 0..<32))

// See BSI TR-02102-1 3.1.2.
@Rule("Do not use a short Nonce in the FileHeader")
fun forbidShortGCMNonce(fileHeaderImpl: FileHeaderImpl) =
    run {
        never(fileHeaderImpl.construct(Length(0..<32), Wildcard))
    }

// See BSI TR-02102-1 3.1.2.
@Rule("Do not use a short Nonce in the FileContentCryptor")
fun forbidShortGCMNonce(fileContentCryptorImpl: FileContentCryptorImpl) =
    run {
        never(fileContentCryptorImpl.encryptChunk(Wildcard, Wildcard, Wildcard, Length(0..<32), Wildcard))
    }


