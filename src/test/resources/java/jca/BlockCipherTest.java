import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class BlockCipherTest {

	public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
		Security.addProvider(new BouncyCastleProvider());
		
		String[] algorithms = {
				"AES",
				//"AES128",
				//"AES_128",
				//"AES-128",
				//"AES(128)"
				"CCM",
				"AES/CCM/NoPadding"
		};
		
		for (String s : algorithms) {
			createCipher(s);
		}
	}
	
	public static Cipher createCipher(String transform) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
		return Cipher.getInstance(transform, "BC");
	}
	
}
