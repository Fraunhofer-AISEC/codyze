import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Mac;

public class BCMac extends BCBase {

	public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException {
		
		Mac m1 = Mac.getInstance("AESCMAC", "BC");
		
		Mac m2 = Mac.getInstance("HMACSHA256", "BC");
		Mac m3 = Mac.getInstance("HMACSHA512/256", "BC");
		Mac m4 = Mac.getInstance("HMACSHA384", "BC");
		Mac m5 = Mac.getInstance("HMACSHA512", "BC");
		Mac m6 = Mac.getInstance("HMACSHA3-256", "BC");
		Mac m7 = Mac.getInstance("HMACSHA3-384", "BC");
		Mac m8 = Mac.getInstance("HMACSHA3-512", "BC");
		
		Mac m9 = Mac.getInstance("AES-GMAC", "BC");
		
		Mac m10 = Mac.getInstance("DESCMAC", "BC");
		Mac m11 = Mac.getInstance("HMACSHA1", "BC");
		Mac m12 = Mac.getInstance("TWOFISH-GMAC", "BC");
		
		System.out.println("Done");
	}

}
