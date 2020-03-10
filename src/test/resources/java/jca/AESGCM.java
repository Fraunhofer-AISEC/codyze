import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;

import util.Utils;

public class AESGCM extends BCBase {

	public byte[] encryptAESGCM(Key k, byte[] plaintext)
			throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		// cipher object with selected cipher and cipher mode
		Cipher c = Cipher.getInstance("AES/GCM/NoPadding", "BC");

		byte[] iv = new byte[12]; // TR-02102-1, #2.1.2.2.02

		// need non-repeated IV --> approximation: use random IV
		SecureRandom rng = SecureRandom.getInstance("Default", "BC");
		rng.nextBytes(iv);

		GCMParameterSpec gcmParamSpec = new GCMParameterSpec(96, iv);

		c.init(Cipher.ENCRYPT_MODE, k, gcmParamSpec);
		return c.doFinal(plaintext);
	}

	public byte[] encryptAESGCM2(Key k, byte[] plaintext)
			throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		// cipher object with selected cipher and cipher mode
		Cipher c = Cipher.getInstance("AES/GCM/NoPadding", "BC");

		byte[] iv = new byte[12]; // TR-02102-1, #2.1.2.2.02

		// need non-repeated IV --> approximation: use random IV
		SecureRandom rng = SecureRandom.getInstance("Default", "BC");
		rng.nextBytes(iv);

		GCMParameterSpec gcmParamSpec = new GCMParameterSpec(128, iv);

		c.init(Cipher.ENCRYPT_MODE, k, gcmParamSpec);
		return c.doFinal(plaintext);
	}
	
	public static void main(String[] args)
			throws NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		AESGCM crypto = new AESGCM();

		String plaintext = "DEADBEEF";

		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
		keyGenerator.init(128);

		Key k = keyGenerator.generateKey();
		
		byte[] ciphertext = crypto.encryptAESGCM(k, plaintext.getBytes());
		System.out.println(Utils.bytesToHex(ciphertext));
		
		byte[] ciphertext2 = crypto.encryptAESGCM2(k, plaintext.getBytes());
		System.out.println(Utils.bytesToHex(ciphertext2));
	}

}
