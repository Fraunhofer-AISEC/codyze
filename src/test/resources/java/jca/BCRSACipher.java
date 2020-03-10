import javax.crypto.Cipher;

public class BCRSACipher extends BCBase {

	public static void main(String[] args) throws Exception {
		Cipher c1 = Cipher.getInstance("RSA/None/OAEPWITHSHA256ANDMGF1PADDING", "BC");
		Cipher c2 = Cipher.getInstance("RSA/None/OAEPWITHSHA-256ANDMGF1PADDING", "BC");
		Cipher c3 = Cipher.getInstance("RSA/None/OAEPWITHSHA384ANDMGF1PADDING", "BC");
		Cipher c4 = Cipher.getInstance("RSA/None/OAEPWITHSHA-384ANDMGF1PADDING", "BC");
		Cipher c5 = Cipher.getInstance("RSA/None/OAEPWITHSHA512ANDMGF1PADDING", "BC");
		Cipher c6 = Cipher.getInstance("RSA/None/OAEPWITHSHA-512ANDMGF1PADDING", "BC");

		Cipher c7 = Cipher.getInstance("RSA/None/OAEPWITHSHA3-256ANDMGF1PADDING", "BC");

		System.out.println("Done.");
	}

}
