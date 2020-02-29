import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;

import util.Utils;

public class AESGCM {

	public byte[] encryptAES128(Key k, byte[] plaintext) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		// cipher object with selected cipher and cipher mode
		Cipher c = Cipher.getInstance("AES_128/GCM/NoPadding");

		byte[] iv = new byte[12]; // TR-02102-1, #2.1.2.2.02

		// need non-repeated IV --> approximation: use random IV
		SecureRandom rng = new SecureRandom(); // default seed == 0
		rng.nextBytes(iv);

		GCMParameterSpec gcmParamSpec = new GCMParameterSpec(96, iv);

		c.init(Cipher.ENCRYPT_MODE, k, gcmParamSpec);
		c.update(plaintext);
		return c.doFinal();
	}

	public static void main(String[] args)
			throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		AESGCM crypto = new AESGCM();

		String plaintext = "DEADBEEF";
		
		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
		keyGenerator.init(128);
		
		Key k = keyGenerator.generateKey();
		
		byte[] ciphertext = crypto.encryptAES128(k, plaintext.getBytes());
		System.out.println(Utils.bytesToHex(ciphertext));
	}

}
