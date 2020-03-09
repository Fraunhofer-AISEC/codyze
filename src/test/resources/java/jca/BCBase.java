import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class BCBase {

	static {
		Security.addProvider(new BouncyCastleProvider());
	}
	
}
