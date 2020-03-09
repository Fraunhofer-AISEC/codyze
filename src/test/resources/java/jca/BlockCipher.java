import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class BlockCipher extends BCBase {

	public Cipher getAES() throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
		return Cipher.getInstance("AES", "BC");
	}

	public Cipher getDES() throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
		return Cipher.getInstance("DES", "BC");
	}

	public Cipher getSerpent() throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
		return Cipher.getInstance("Serpent", "BC");
	}

	public Cipher getTwofish() throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
		return Cipher.getInstance("Twofish", "BC");
	}

	public Cipher getAES_128() throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
		return Cipher.getInstance("AES_128", "BC"); // no such algorithm
	}
	
	/*
	 * for testing
	 */
	public static void main(String[] args) throws Exception {
		BlockCipher bc = new BlockCipher();
		
		bc.getAES();
		bc.getDES();
		bc.getSerpent();
		bc.getTwofish();
		
		try {
			bc.getAES_128(); // NoSuchAlgorithmException
		} catch (NoSuchAlgorithmException nsae) {
			System.err.println(nsae);
		}
		
		System.out.println("Done");
	}
	
}
