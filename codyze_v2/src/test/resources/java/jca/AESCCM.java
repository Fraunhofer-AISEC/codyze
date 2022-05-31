import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import util.Utils;

public class AESCCM extends BCBase {

	public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException, InvalidKeyException, InvalidAlgorithmParameterException {
		Cipher c = Cipher.getInstance("AES/CCM/NoPadding", "BC");
		
		System.out.println(c.getAlgorithm());
		
		KeyGenerator kg = KeyGenerator.getInstance("AES", "BC");
		kg.init(128);
		SecretKey sk = kg.generateKey();
		
		System.out.println("Key:\t" + Utils.bytesToDecimal(sk.getEncoded()));
		
		byte[] iv = new byte[12];
		
		SecureRandom sr = SecureRandom.getInstance("Default", "BC");
		sr.nextBytes(iv);
		
		System.out.println("IV:\t" + Utils.bytesToDecimal(iv));

		//c.init(Cipher.ENCRYPT_MODE, sk, new IvParameterSpec(iv));
		c.init(Cipher.ENCRYPT_MODE, sk, new GCMParameterSpec(128, iv));
	}

}
