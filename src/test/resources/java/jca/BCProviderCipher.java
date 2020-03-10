

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class BCProviderCipher {

	public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
		// add provider to find it by name
		Security.addProvider(new BouncyCastleProvider());
		
		// Any matching provider 
		Cipher c1 = Cipher.getInstance("AES");
		
		// BouncyCastle provider
		Cipher c2 = Cipher.getInstance("AES", "BC");
		Cipher c3 = Cipher.getInstance("AES", Security.getProvider("BC"));
		Cipher c4 = Cipher.getInstance("AES", new BouncyCastleProvider());
		
		// Wrong provider
		Cipher c5 = Cipher.getInstance("AES", "SunJCE");
		Cipher c6 = Cipher.getInstance("AES", Security.getProvider("SunJCE"));
	}

}
