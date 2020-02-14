import javax.crypto.Cipher;

public class BlockCipher {


    public byte[] encryptAES(Key k, byte[] plaintext) {
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, k);
        c.update(plaintext);
        return c.doFinal();
    }

    public byte[] encryptAES_128(Key k, byte[] plaintext) {
        Cipher c = Cipher.getInstance("AES_128");
        c.init(Cipher.ENCRYPT_MODE, k);
        c.update(plaintext);
        return c.doFinal();
    }

    public byte[] encryptAES_192(Key k, byte[] plaintext) {
        Cipher c = Cipher.getInstance("AES_192");
        c.init(Cipher.ENCRYPT_MODE, k);
        c.update(plaintext);
        return c.doFinal();
    }

    public byte[] encryptAES_256(Key k, byte[] plaintext) {
        Cipher c = Cipher.getInstance("AES_256");
        c.init(Cipher.ENCRYPT_MODE, k);
        c.update(plaintext);
        return c.doFinal();
    }

    public byte[] encryptDES(Key k, byte[] plaintext) {
        Cipher c = Cipher.getInstance("DES");
        c.init(Cipher.ENCRYPT_MODE, k);
        c.update(plaintext);
        return c.doFinal();
    }

    public byte[] encryptDESede(Key k, byte[] plaintext) {
        Cipher c = Cipher.getInstance("DESede");
        c.init(Cipher.ENCRYPT_MODE, k);
        c.update(plaintext);
        return c.doFinal();
    }

    public byte[] encryptBlowfish(Key k, byte[] plaintext) {
        Cipher c = Cipher.getInstance("Blowfish");
        c.init(Cipher.ENCRYPT_MODE, k);
        c.update(plaintext);
        return c.doFinal();
    }

    public byte[] encryptRC2(Key k, byte[] plaintext) {
        Cipher c = Cipher.getInstance("RC2");
        c.init(Cipher.ENCRYPT_MODE, k);
        c.update(plaintext);
        return c.doFinal();
    }

}