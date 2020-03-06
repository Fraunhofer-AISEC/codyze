import java.security.AlgorithmParameterGenerator;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class BCMacs {

	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
		Security.addProvider(new BouncyCastleProvider());
		
		
		SecureRandom sr = new SecureRandom();
		byte[] nonce = sr.generateSeed(12);
		GCMParameterSpec gps = new GCMParameterSpec(12, nonce);
		
		AlgorithmParameterGenerator apg = AlgorithmParameterGenerator.getInstance("AES");
		apg.init(gps);
		
		Mac m = Mac.getInstance("AES-GMAC");
		
		System.out.println(m.getAlgorithm());
		System.out.println(m.getMacLength());
		
		
	}

}
