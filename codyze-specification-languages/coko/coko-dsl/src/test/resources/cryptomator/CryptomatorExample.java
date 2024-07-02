import java.security.SecureRandom;
import org.cryptomator.cryptolib.v2.FileContentCryptorImpl;
import org.cryptomator.cryptolib.v2.FileHeaderImpl;
import org.cryptomator.cryptolib.common.CipherSupplier;
import org.cryptomator.cryptolib.common.DecryptingReadableByteChannel;
import org.cryptomator.cryptolib.common.P384KeyPair;
import org.cryptomator.cryptolib.common.ReseedingSecureRandom;
import org.cryptomator.cryptolib.common.Scrypt;

class CryptomatorExample {
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

    void validAlgorithm3() {
        CipherSupplier supplier = new CipherSupplier("AESWrap");
    }

    void invalidAlgorithm1() {
        CipherSupplier supplier = new CipherSupplier("Blowfish/CTR/NoPadding");
    }

    void invalidAlgorithm2() {
        CipherSupplier supplier = new CipherSupplier("AES/CBC/NoPadding");
    }

    void validAlgorithm3() {
        CipherSupplier supplier = new CipherSupplier("AESWrap/CCM/NoPadding");
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

    void invalidKeyStore1() {
        P384KeyPair keypair = new P384KeyPair();
        char[] password = "".toCharArray();
        keypair.store(null, password);
    }

    void invalidKeyStore2() {
        P384KeyPair keypair = new P384KeyPair();
        char[] password = {};
        keypair.store(null, password);
    }

    void invalidKeyStore3() {
        P384KeyPair keypair = new P384KeyPair();
        char[] password = { "", "" };
        keypair.store(null, password);
    }

    void invalidKeyStore4() {
        P384KeyPair keypair = new P384KeyPair();
        keypair.store(null, "");
    }

    void invalidKeyStore5() {
        P384KeyPair keypair = new P384KeyPair();
        keypair.store(null, new Object[]{ });
    }

    void invalidKeyStore6() {
        P384KeyPair keypair = new P384KeyPair();
        keypair.store(null, new Object[]{ "".toCharArray() });
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
        char[] salt = "thisIsASaltThatIsJustLongEnough".getBytes();
        byte[] key = scrypt.scrypt(password, salt, null, null, null);
    }

    void emptyScryptPasswordBytes() {
        Scrypt scrypt = Scrypt();
        char[] password = "".getBytes();
        char[] salt = "thisIsASaltThatIsJustLongEnough".getBytes();
        byte[] key = scrypt.scrypt(password, salt, null, null, null);
    }

    void emptyScryptSalt() {
        Scrypt scrypt = Scrypt();
        char[] salt = "".getBytes();
        byte[] key = scrypt.scrypt("password", salt, null, null, null);
    }

    void longScryptKey() {
        Scrypt scrypt = Scrypt();
        char[] salt = "thisIsASaltThatIsJustLongEnough".getBytes();
        byte[] key = scrypt.scrypt("password", salt, null, null, 64);
    }

    void shortScryptKey() {
        Scrypt scrypt = Scrypt();
        char[] salt = "thisIsASaltThatIsJustLongEnough".getBytes();
        byte[] key = scrypt.scrypt("password", salt, null, null, 16);
    }

    void longNonce() {
        byte[] nonce = "thisIsAnExampleNonceThatIsLongEnough".getBytes();
        FileHeaderImpl fh = new FileHeaderImpl(nonce, null);
    }

    void longNonce2() {
        FileHeaderImpl fh = new FileHeaderImpl(new Object[]{ "thisIsAnExampleNonceThatIsLong".toCharArray() } + new Object[]{ "Enough".toCharArray() }, null);
    }

    void shortNonce() {
        byte[] nonce = "thisIsAnExampleNonceThatIsShort".getBytes();
        FileHeaderImpl fh = new FileHeaderImpl(nonce, null);
    }

    void shortNonce2() {
        byte[] nonce = "thisIsAnExampleNonceThatIsShort".getBytes() + "".getBytes();
        FileHeaderImpl fh = new FileHeaderImpl(nonce, null);
    }

    void shortNonce3() {
        FileHeaderImpl fh = new FileHeaderImpl(new Object[]{ "thisIsAnExampleNonceThatIsShort".toCharArray() } + new Object[]{ "".toCharArray() }, null);
    }
}