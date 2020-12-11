
package de.fraunhofer.aisec.crymlin;

import org.junit.jupiter.api.Test;

public class OpenSSLTest extends AbstractMarkTest {

	@Test
	public void testBasic() throws Exception {
		var findings = performTest("openssl/tls/simple.c", "mark/openssl/simple.mark");
	}

	@Test
	public void testSslCtxNew() throws Exception {
		var findings = performTest("openssl/tls/ctx-new", "mark/openssl/tls_version.mark");

		for (var f : findings) {
			System.out.println(f);
		}
	}

	@Test
	public void testSslSetMinMaxProtocolNoFindings() throws Exception {
		var findings = performTest("openssl/tls/set-minmax-protocol/ctx-setminmax-no-findings.c", "mark/openssl/tls_version.mark");

		for (var f : findings) {
			System.out.println(f);
		}
	}

	@Test
	public void testSslSetMinMaxProtocolFindings() throws Exception {
		var findings = performTest("openssl/tls/set-minmax-protocol/ctx-setminmax-findings.c", "mark/openssl/tls_version.mark");

		for (var f : findings) {
			System.out.println(f);
		}
	}
}
