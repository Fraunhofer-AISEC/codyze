
package de.fraunhofer.aisec.crymlin;

import org.junit.jupiter.api.Test;

public class OpenSSLTest extends AbstractMarkTest {

	@Test
	public void testBasic() throws Exception {
		var findings = performTest("openssl/tls/simple.c", "mark/openssl/simple.mark");
	}

	@Test
	public void testSslCtxNew() throws Exception {
		var findings = performTest("openssl/tls/ctx-new");

		for (var f : findings) {
			System.out.println(f);
		}
	}
}
