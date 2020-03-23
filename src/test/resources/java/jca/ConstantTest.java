import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class AESCBC extends BCBase {

    public static void main(String[] args)
            throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
        String stringToEncrypt = "Super secret Squirrel";
        byte[] byteStringToEncrypt = stringToEncrypt.getBytes("UTF-8");
        SecretKeySpec secretKeySpec = new SecretKeySpec(data.getBytes("UTF-8"), "AES");
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
    }
}
