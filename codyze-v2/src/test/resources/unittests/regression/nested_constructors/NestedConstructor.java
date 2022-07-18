package botan;

public class NestedConstructor {

	/* Example of nested constructors */
	public static void main(String... args) {
		AlgorithmIdentifier id = new AlgorithmIdentifier();
		PK_Verifier sig_verifier = new PK_Verifier(new RSA_PublicKey(id, 123), "I AM INCORRECT EMSA4(SHA-256)");
	}
}

class AlgorithmIdentifier {}

class RSA_PublicKey {
	public RSA_PublicKey(AlgorithmIdentifier id, int length) {}
}

class PK_Verifier{
	public PK_Verifier(RSA_PublicKey pk, String algorithm) {

	}
}