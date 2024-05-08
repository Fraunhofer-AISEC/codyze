import org.cryptomancer.cryptolib.v2.FileContentCryptorImpl;

class Test {
    fun unauthenticatedFileDecryption() {
        FileContentCryptorImpl fileCryptor = new FileContentCryptorImpl();
        fileCryptor.decryptChunk(null, null, null, false);
        fileCryptor.decryptChunk(null, null, null, null, false);
    }
}