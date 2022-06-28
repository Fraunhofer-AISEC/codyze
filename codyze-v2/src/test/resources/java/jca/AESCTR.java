import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import util.Utils;

public class AESCTR extends BCBase {

	public static void main(String[] args)
			throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		Cipher c = Cipher.getInstance("AES/CTR/NoPadding", "BC");

		KeyGenerator kg = KeyGenerator.getInstance("AES", "BC");
		kg.init(128);
		SecretKey sk = kg.generateKey();

		byte[] iv = new byte[c.getBlockSize()];
		SecureRandom sr = SecureRandom.getInstance("Default", "BC");
		sr.nextBytes(iv);

		IvParameterSpec ivparamspec = new IvParameterSpec(iv);

		c.init(Cipher.ENCRYPT_MODE, sk, ivparamspec);

		Mac m = Mac.getInstance("HMACSHA256", "BC");
		KeyGenerator kg2 = KeyGenerator.getInstance("HMACSHA256", "BC");
		m.init(kg2.generateKey());

		byte[] input = new byte[160];
		sr.nextBytes(input);

		byte[] ciphertextblock;
		int i = 0;
		while (i < (input.length - 1) / 16) {
			ciphertextblock = c.update(input, i * 16, 16);

			byte[] mac = null;
			if (ciphertextblock != null) {
				m.update(ciphertextblock);
				mac = m.doFinal();
			}

			System.out.printf("update  %2d: %32s [%s]\n", i, Utils.bytesToHex(ciphertextblock), Utils.bytesToHex(mac));

			i++;
		}
		ciphertextblock = c.doFinal(input, i * 16, (input.length % 16 == 0) ? 16 : input.length % 16);

		m.update(ciphertextblock);
		byte[] mac = m.doFinal();

		System.out.printf("dofinal %2d: %32s [%s]\n", i, Utils.bytesToHex(ciphertextblock), Utils.bytesToHex(mac));
	}

}
