import Cm_model_codyze;
import java.security.SecureRandom;
import org.cryptomancer.cryptolib.v2.FileContentCryptorImpl;
import org.cryptomancer.cryptolib.common.CipherSupplier;
import org.cryptomancer.cryptolib.common.DecryptingReadableByteChannel;
import org.cryptomancer.cryptolib.common.P384KeyPair;
import org.cryptomancer.cryptolib.common.ReseedingSecureRandom;
import org.cryptomancer.cryptolib.common.Scrypt;

class Test {
    void unauthenticatedFileDecryption1() {
        FileContentCryptorImpl fileCryptor = new FileContentCryptorImpl();
        fileCryptor.decryptChunk(null, null, null, false);
    }

    void unauthenticatedFileDecryption2() {
        FileContentCryptorImpl fileCryptor = new FileContentCryptorImpl();
        fileCryptor.decryptChunk(null, null, null, null, false);
    }

    void validAlgorithm1() {
        CipherSupplier supplier = new CipherSupplier("AES/CTR/NoPadding");
    }

    void validAlgorithm2() {
        CipherSupplier supplier = new CipherSupplier("AES/CBC/PKCS5Padding");
    }

    void invalidAlgorithm1() {
        CipherSupplier supplier = new CipherSupplier("Blowfish/CTR/NoPadding");
    }

    void invalidAlgorithm2() {
        CipherSupplier supplier = new CipherSupplier("AES/CBC/NoPadding");
    }

    void unauthenticatedCipherDecryption1() {
        DecryptingReadableByteChannel channel = new DecryptingReadableByteChannel(null, null, false);
    }

    void unauthenticatedCipherDecryption2() {
        DecryptingReadableByteChannel channel = new DecryptingReadableByteChannel(null, null, false, null, null);
    }

    void validKeyStore() {
        P384KeyPair keypair = new P384KeyPair();
        char[] password = "password".toCharArray();
        keypair.store(null, password);
    }

    void invalidKeyStore() {
        P384KeyPair keypair = new P384KeyPair();
        char[] password = "".toCharArray();
        keypair.store(null, password);
    }

    void weakSecureRandom() {
        SecureRandom random = new SecureRandom();
        ReseedingSecureRandom reseed = new ReseedingSecureRandom(random, random, 1 << 30, 60);
    }

    void rareReseed() {
        SecureRandom strongRandom = SecureRandom.getInstanceStrong();
        SecureRandom random = new SecureRandom();
        ReseedingSecureRandom reseed = new ReseedingSecureRandom(strongRandom, random, 1 << 60, 60);
    }

    void weakSeed() {
        SecureRandom strongRandom = SecureRandom.getInstanceStrong();
        SecureRandom random = new SecureRandom();
        ReseedingSecureRandom reseed = new ReseedingSecureRandom(strongRandom, random, 1 << 30, 30);
    }

    void goodReseedingRandom() {
        SecureRandom strongRandom = java.security.SecureRandom.getInstanceStrong();
        SecureRandom random = new SecureRandom();
        ReseedingSecureRandom reseed = new ReseedingSecureRandom(strongRandom, random, 1 << 30, 60);
    }

    void emptyScryptPasswordChar() {
        Scrypt scrypt = Scrypt();
        char[] password = "".toCharArray();
        byte[] key = scrypt.scrypt(password, null, null, null, null)
    }

    void emptyScryptPasswordBytes() {
        Scrypt scrypt = Scrypt();
        char[] password = "".getBytes()
        byte[] key = scrypt.scrypt(password, null, null, null, null)
    }

    void emptyScryptSalt() {
        Scrypt scrypt = Scrypt();
        char[] salt = "".getBytes();
        byte[] key = scrypt.scrypt(null, salt, null, null, null)
    }
}