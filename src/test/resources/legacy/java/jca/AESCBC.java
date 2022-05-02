import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class AESCBC extends BCBase {

	public static void main(String[] args)
			throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
		Cipher c1 = Cipher.getInstance("AES/CBC/NoPadding", "BC");

		Cipher c2 = Cipher.getInstance("AES/CBC/PKCS5Padding", "BC");
		Cipher c3 = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");

		Cipher c4 = Cipher.getInstance("AES/CBC/ISO7816-4Padding", "BC");
	}
}
