import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;

public class AESGMAC extends BCBase {

	public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, InvalidAlgorithmParameterException {
		Mac m = Mac.getInstance("AES-GMAC", "BC");
		
		KeyGenerator kg = KeyGenerator.getInstance("AES", "BC");
		kg.init(128);
		SecretKey sk = kg.generateKey();
		
		byte[] iv = new byte[16];
		SecureRandom sr = SecureRandom.getInstance("Default", "BC");
		sr.nextBytes(iv);
		
		IvParameterSpec ivParamSpec = new IvParameterSpec(iv);
		
		m.init(sk, ivParamSpec);

		System.out.println("Done");
	}

}
