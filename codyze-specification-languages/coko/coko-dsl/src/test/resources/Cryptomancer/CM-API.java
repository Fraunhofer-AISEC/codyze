import org.cryptomancer.cryptolib.v2.FileContentCryptorImpl;
import org.cryptomancer.cryptolib.common.CipherSupplier;

class Test {
    void unauthenticatedFileDecryption() {
        FileContentCryptorImpl fileCryptor = new FileContentCryptorImpl();
        fileCryptor.decryptChunk(null, null, null, false);
        fileCryptor.decryptChunk(null, null, null, null, false);
    }

    void validAlgorithm() {
        CipherSupplier supplier = new CipherSupplier("AES/CTR/NoPadding");
    }

    void invalidAlgorithm() {
        CipherSupplier supplier = new CipherSupplier("Blowfish/CTR/NoPadding");
    }
}