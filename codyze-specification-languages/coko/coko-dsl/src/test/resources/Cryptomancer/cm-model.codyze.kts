import java.net.URI
import java.security.SecureRandom
import java.security.spec.AlgorithmParameterSpec

import java.io.OutputStream
import java.nio.file.Path
import java.security.KeyPair
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.SecretKey

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
        op {
            constructor("org.cryptomancer.cryptolib.common.DecryptingReadableByteChannel") {
                // maybe this could be combined with "null"?
                signature(src, cryptor, authenticate)
                signature(src, cryptor, authenticate, header, firstChunk)
            }
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

    fun store(file: Path?, passphrase: Array<Char>?): Op =
        op {
            "org.cryptomancer.cryptolib.common.P384KeyPair.store" {
                signature(file, passphrase)
            }
        }

    fun store(out: OutputStream?, passphrase: Array<Char>?): Op =
        op {
            "org.cryptomancer.cryptolib.common.P384KeyPair.store" {
                signature(out, passphrase)
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
        op {
            constructor("org.cryptomancer.cryptolib.common.ReseedingSecureRandom") {
                signature(seeder, csprng, reseedAfter, seedLength)
            }
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
    fun scrypt(passphrase: Array<Byte>?, salt: Array<Byte>?, cost: Int?, blockSize: Int?, keyLength: Int?): Op =
        op {
            "org.cryptomancer.cryptolib.common.Scrypt.scrypt" {
                signature(passphrase, salt, cost, blockSize, keyLength)
            }
        }

    fun scrypt(passphrase: CharSequence?, salt: Array<Byte>?, cost: Int?, blockSize: Int?, keyLength: Int?): Op =
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

val recommendedAlgorithms = setOf("AES", "AES_128", "AES_192", "AES_256")
val recommendedModes = setOf("CCM", "GCM", "CBC", "CTR")
val recommendedPaddings = setOf("...")
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

// TODO: are algorithms in DestroyableSecretKey actually relevant?
//  one of them is literally called "MASTERKEY"...

@Rule("Only use recommended algorithms")
fun enforceRecommendedAlgorithms(supplier: CipherSupplier) =
    run {
        val x = validParameters().map {
            supplier.construct(it)
        }.toTypedArray()
        only(*x)
    }



