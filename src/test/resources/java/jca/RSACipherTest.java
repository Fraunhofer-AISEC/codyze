import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.spec.MGF1ParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class RSACipherTest {

	
	public static void main(String[] args) throws Exception {
		Security.addProvider(new BouncyCastleProvider());
		
		String[] possibleRsaCipherTransforms = {
//				"RSA",
//				"RSA/ /OAEPPADDING",
//				"RSA/None/OAEPPADDING",
//				"RSA/ECB/OAEPPADDING",
//				"1.2.840.113549.1.1.7",
//				"OID.1.2.840.113549.1.1.7",
				"RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING",
				"RSA/ECB/OAEPWITHSHA-384ANDMGF1PADDING",
				"RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING"
//				"RSA/ECB/OAEPWITHSHA-512(256)ANDMGF1PADDING",
//				"RSA/ECB/OAEPWITHSHA-512/256ANDMGF1PADDING"
		};
		
		
		for (String s : possibleRsaCipherTransforms) {
			Cipher c = createCipher(s);
			
			if (!c.getProvider().getName().equalsIgnoreCase("BC")) {
				throw new Exception("wrong provider!");
			}
		}
		
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		KeyPair kp = kpg.generateKeyPair();
		
		Cipher c = createCipher("RSA");
		c.init(Cipher.ENCRYPT_MODE, kp.getPublic(), new OAEPParameterSpec("SHA-512", "MGF1", new MGF1ParameterSpec("SHA-512"), PSource.PSpecified.DEFAULT));
		
		System.out.println(c.getAlgorithm());
		System.out.println(c.getParameters().toString());
	}
	
	public static Cipher createCipher(String transform) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
		Cipher c = Cipher.getInstance(transform, "BC");
		return c;
	}
	
}
