import java.security.spec.AlgorithmParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.SecretKey

plugins { id("cpg") }

interface FileContentCryptor {
    fun decryptChunk(cipher: Any?, chunkNum: Any?, header: Any?, auth: Any?): Op
    fun decryptChunk(cipher: Any?, clear: Any?, chunkNum: Any?, header: Any?, auth: Any?): Op
    fun encryptChunk(cleartext: Any?, cipherText: Any?, chunkNumber: Any?, headerNonce: Any?, fileKey: Any?): Op
}

class FileContentCryptorInterface: FileContentCryptor {
    override fun decryptChunk(cipher: Any?, chunkNum: Any?, header: Any?, auth: Any?): Op =
        op {
            "org.cryptomator.cryptolib.api.FileContentCryptor.decryptChunk" {
                signature(cipher, chunkNum, header, auth)
            }
        }

    override fun decryptChunk(cipher: Any?, clear: Any?, chunkNum: Any?, header: Any?, auth: Any?): Op =
        op {
            "org.cryptomator.cryptolib.api.FileContentCryptor.decryptChunk" {
                signature(cipher, clear, chunkNum, header, auth)
            }
        }

    override fun encryptChunk(cleartext: Any?, cipherText: Any?, chunkNumber: Any?, headerNonce: Any?, fileKey: Any?): Op =
        op {
            "org.cryptomator.cryptolib.api.FileContentCryptor.encryptChunk" {
                signature(cleartext, cipherText, chunkNumber, headerNonce, fileKey)
            }
        }
}

class FileContentCryptorImplv1: FileContentCryptor {
    override fun decryptChunk(cipher: Any?, chunkNum: Any?, header: Any?, auth: Any?): Op =
        op {
            "org.cryptomator.cryptolib.v1.FileContentCryptorImpl.decryptChunk" {
                signature(cipher, chunkNum, header, auth)
            }
        }

    override fun decryptChunk(cipher: Any?, clear: Any?, chunkNum: Any?, header: Any?, auth: Any?): Op =
        op {
            "org.cryptomator.cryptolib.v1.FileContentCryptorImpl.decryptChunk" {
                signature(cipher, clear, chunkNum, header, auth)
            }
        }

    override fun encryptChunk(cleartext: Any?, cipherText: Any?, chunkNumber: Any?, headerNonce: Any?, fileKey: Any?): Op =
        op {
            "org.cryptomator.cryptolib.v1.FileContentCryptorImpl.encryptChunk" {
                signature(cleartext, cipherText, chunkNumber, headerNonce, fileKey)
            }
        }
}

class FileContentCryptorImplv2: FileContentCryptor {
    override fun decryptChunk(cipher: Any?, chunkNum: Any?, header: Any?, auth: Any?): Op =
        op {
            "org.cryptomator.cryptolib.v2.FileContentCryptorImpl.decryptChunk" {
                signature(cipher, chunkNum, header, auth)
            }
        }

    override fun decryptChunk(cipher: Any?, clear: Any?, chunkNum: Any?, header: Any?, auth: Any?): Op =
        op {
            "org.cryptomator.cryptolib.v2.FileContentCryptorImpl.decryptChunk" {
                signature(cipher, clear, chunkNum, header, auth)
            }
        }

    override fun encryptChunk(cleartext: Any?, cipherText: Any?, chunkNumber: Any?, headerNonce: Any?, fileKey: Any?): Op =
        op {
            "org.cryptomator.cryptolib.v2.FileContentCryptorImpl.encryptChunk" {
                signature(cleartext, cipherText, chunkNumber, headerNonce, fileKey)
            }
        }
}

interface FileHeader {
    fun construct(nonce: Any?, payload: Any?): Op
    fun getReserved(): Op
    fun setReserved(reserved: Long?): Op
}

class FileHeaderImplv1: FileHeader {
    override fun construct(nonce: Any?, payload: Any?): Op =
        constructor("org.cryptomator.cryptolib.v1.FileHeaderImpl") {
            signature(nonce, payload)
        }

    override fun getReserved(): Op =
        op {
            "org.cryptomator.cryptolib.v1.FileHeaderImpl.getReserved" {
                signature()
            }
        }

    override fun setReserved(reserved: Long?): Op =
        op {
            "org.cryptomator.cryptolib.v1.FileHeaderImpl.setReserved" {
                signature(reserved)
            }
        }
}

class FileHeaderImplv2: FileHeader {
    override fun construct(nonce: Any?, payload: Any?): Op =
        constructor("org.cryptomator.cryptolib.v2.FileHeaderImpl") {
            signature(nonce, payload)
        }

    override fun getReserved(): Op =
        op {
            "org.cryptomator.cryptolib.v2.FileHeaderImpl.getReserved" {
                signature()
            }
        }

    override fun setReserved(reserved: Long?): Op =
        op {
            "org.cryptomator.cryptolib.v2.FileHeaderImpl.setReserved" {
                signature(reserved)
            }
        }
}

class Cipher {
    fun getInstance(cipherAlgorithm: Any): Op = op {
        "javax.crypto.Cipher.getInstance" {
            signature(cipherAlgorithm)
        }
    }
}

class AlgorithmParameters {
    fun getInstance(algorithm: String): Op = op {
        "java.security.AlgorithmParameters.getInstance" {
            signature(algorithm)
        }
    }

    fun init(ecSpec: Any?): Op = op {
        "java.security.AlgorithmParameters.init" {
            signature(ecSpec)
        }
    }
}

class ECGenParameterSpec {
    fun construct(ecCurve: Any): Op =
        constructor("java.security.spec.ECGenParameterSpec") {
            signature(ecCurve)
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

// -- Variants for both types of calls and all three base classes --
@Rule("Never skip authentication when decrypting a file chunk")
fun enforceFileDecryptAuthentication1v1(cryptor: FileContentCryptorImplv1) =
    never(cryptor.decryptChunk(Wildcard, Wildcard, Wildcard, Wildcard, false))

@Rule("Never skip authentication when decrypting a file chunk")
fun enforceFileDecryptAuthentication1v2(cryptor: FileContentCryptorImplv2) =
    never(cryptor.decryptChunk(Wildcard, Wildcard, Wildcard, Wildcard, false))

@Rule("Never skip authentication when decrypting a file chunk")
fun enforceFileDecryptAuthentication1Int(cryptor: FileContentCryptorInterface) =
    never(cryptor.decryptChunk(Wildcard, Wildcard, Wildcard, Wildcard, false))

@Rule("Never skip authentication when decrypting a file chunk")
fun enforceFileDecryptAuthentication2v1(cryptor: FileContentCryptorImplv1) =
    never(cryptor.decryptChunk(Wildcard, Wildcard, Wildcard, false))

@Rule("Never skip authentication when decrypting a file chunk")
fun enforceFileDecryptAuthentication2v2(cryptor: FileContentCryptorImplv2) =
    never(cryptor.decryptChunk(Wildcard, Wildcard, Wildcard, false))

@Rule("Never skip authentication when decrypting a file chunk")
fun enforceFileDecryptAuthentication2Int(cryptor: FileContentCryptorInterface) =
    never(cryptor.decryptChunk(Wildcard, Wildcard, Wildcard, false))
// -------------------------------------------------------------------

@Rule("Never skip authentication when decrypting a ciphertext")
fun enforceCipherDecryptAuthentication(channel: DecryptingReadableByteChannel) =
    never(channel.construct(Wildcard, Wildcard, false, Wildcard, Wildcard))

// See BSI TR-02102-1 3.1.
@Rule("Only use recommended algorithms")
fun enforceRecommendedAlgorithms(supplier: CipherSupplier) =
    run {
        val x = validParameters().map {
            supplier.construct(it)
        }.toTypedArray()
        only(*x)
    }

// Parameters from NIST SP 800-90A Rev 1: http://dx.doi.org/10.6028/NIST.SP.800-90Ar1
@Rule("Do not use short seed length for reseeding parameters")
fun forbidShortReseedingSeed(reseeding: ReseedingSecureRandom) =
    never(reseeding.construct(Wildcard, Wildcard, Wildcard, 0..<55))

@Rule("Do not create a short key with scrypt")
fun enforceScryptKeyLength(scrypt: Scrypt) =
    never(scrypt.scrypt(Wildcard, Wildcard, Wildcard, Wildcard, 0..<32))

@Rule("Do not use empty passphrase to store the key pair")
fun forbidEmptyPassphrase(keypair: P384KeyPair) =
    never(keypair.store(Wildcard, Length(0..0)))

// Parameters from NIST SP 800-90A Rev 1: http://dx.doi.org/10.6028/NIST.SP.800-90Ar1
@Rule("Enforce maximum reseed interval for reseeding parameters")
fun enforceStrongReseedingInterval(reseeding: ReseedingSecureRandom) =
    run {
        val maxReseedInterval = 1L shl 48
        only(reseeding.construct(Wildcard, Wildcard, 0..<maxReseedInterval, Wildcard))
    }

@Rule("Do not use short scrypt password")
fun forbidShortScryptPassword(scrypt: Scrypt) =
    never(scrypt.scrypt(Length(0..<8), Wildcard, Wildcard, Wildcard, Wildcard))

// See BSI TR-02102-1 B.1.3.
@Rule("Do not use short scrypt salt")
fun forbidShortScryptSalt(scrypt: Scrypt) =
    never(scrypt.scrypt(Wildcard, Length(0..<32), Wildcard, Wildcard, Wildcard))

// See BSI TR-02102-1 3.1.2.
@Rule("Do not use a short Nonce in the v1 FileHeader")
fun forbidShortGCMNonceV1(fileHeaderImpl: FileHeaderImplv1) =
    never(fileHeaderImpl.construct(Length(0..<12), Wildcard))

// See BSI TR-02102-1 3.1.2.
@Rule("Do not use a short Nonce in the v2 FileHeader")
fun forbidShortGCMNonceV2(fileHeaderImpl: FileHeaderImplv2) =
    never(fileHeaderImpl.construct(Length(0..<12), Wildcard))

// See BSI TR-02102-1 3.1.2.
@Rule("Do not use a short Nonce in the v1 FileContentCryptor")
fun forbidShortGCMNonceV1(fileContentCryptorImpl: FileContentCryptorImplv1) =
    never(fileContentCryptorImpl.encryptChunk(Wildcard, Wildcard, Wildcard, Length(0..<12), Wildcard))

// See BSI TR-02102-1 3.1.2.
@Rule("Do not use a short Nonce in the v2 FileContentCryptor")
fun forbidShortGCMNonceV2(fileContentCryptorImpl: FileContentCryptorImplv2) =
    never(fileContentCryptorImpl.encryptChunk(Wildcard, Wildcard, Wildcard, Length(0..<12), Wildcard))


@Rule("Only allow good ciphers in javax.crypto.Cipher")
fun enforceRecommendedAlgorithms(cipher: Cipher) =
    run {
        val x = validParameters().map {
            cipher.getInstance(it)
        }.toTypedArray()
        only(*x)
    }

val recommendedCurves = setOf("secp384r1")

@Rule("Only allow good EC curves")
fun onlyGoodCurves(alg: AlgorithmParameters, paramSpec: ECGenParameterSpec) =
    run {
        val x = recommendedCurves.map {
             alg.init(paramSpec.construct(it))
        }.toTypedArray()
        only(*x)
    }
