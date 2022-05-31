package util;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class Utils {

	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static String bytesToDecimal(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < bytes.length - 1; i++) {
			sb.append(bytes[i]);
			sb.append(", ");
		}
		if (bytes.length > 0) {
			sb.append(bytes[bytes.length - 1]);
		}
		return "[" + sb.toString() + "]";
	}
	
	public static void loadBouncyCastleProvider() {
		Security.addProvider(new BouncyCastleProvider());
	}
}
