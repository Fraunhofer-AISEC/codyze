import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class MacTest {

	
	public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, InvalidAlgorithmParameterException {
		Security.addProvider(new BouncyCastleProvider());
		
		String[] algorithms = {
				"AESCMAC",
				"AESGMAC", "AES-GMAC",
		};
		
		for (String s : algorithms) {
			Mac m = createMAC(s);
			
			System.out.println(m.getAlgorithm() + ": " + m.getMacLength());
			
			if (m.getAlgorithm().equalsIgnoreCase("AESCMAC")) {
				KeyGenerator kg = KeyGenerator.getInstance("AES", "BC");
				kg.init(256);
				
				m.init(kg.generateKey());
				
				System.out.println(m.getAlgorithm() + ": " + m.getMacLength());
			}
		}
	}
	
	
	public static Mac createMAC(String algorithm) throws NoSuchAlgorithmException, NoSuchProviderException {
		return Mac.getInstance(algorithm, "BC");
	}
}
