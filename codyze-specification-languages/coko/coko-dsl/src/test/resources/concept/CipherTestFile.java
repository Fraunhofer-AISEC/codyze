import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class CipherTestFile {

    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException, InvalidAlgorithmParameterException {
        Security.addProvider(new BouncyCastleProvider());

        String transform = "AES/CBC/NoPadding";
        Cipher c = Cipher.getInstance(transform, "BC");

        SecureRandom sr = new SecureRandom();
        byte[] iv = sr.generateSeed(16);
        IvParameterSpec ivParamSpec = new IvParameterSpec(iv);

        byte[] rawKey = sr.generateSeed(32);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("AES");
        Key k = skf.generateSecret(new SecretKeySpec(rawKey, "AES"));

        c.init(Cipher.ENCRYPT_MODE, k, ivParamSpec);
        c.doFinal();
        System.out.println(c);
    }

}
