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

public class BlockCipherTest {

	public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException, InvalidAlgorithmParameterException {
		Security.addProvider(new BouncyCastleProvider());
		
		String[] algorithms = {
//				"AES",
				//"AES128",
				//"AES_128",
				//"AES-128",
				//"AES(128)"
//				"CCM",
				"AES/CBC/NoPadding"
//				"AES/CCM/NoPadding"
		};
		
		for (String s : algorithms) {
			Cipher c = createCipher(s);
			
			SecureRandom sr = new SecureRandom();
			byte[] iv = sr.generateSeed(16);
			IvParameterSpec ivParamSpec = new IvParameterSpec(iv);
			
			byte[] rawKey = sr.generateSeed(32);
			SecretKeyFactory skf = SecretKeyFactory.getInstance("AES");
			Key k = skf.generateSecret(new SecretKeySpec(rawKey, "AES"));
			
			c.init(Cipher.ENCRYPT_MODE, k, ivParamSpec);
			System.out.println(c);
		}
	}
	
	public static Cipher createCipher(String transform) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
		return Cipher.getInstance(transform, "BC");
	}
	
}
