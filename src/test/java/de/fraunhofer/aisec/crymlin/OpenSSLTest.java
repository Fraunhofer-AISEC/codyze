
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
		expected(findings,

			"line 10: Rule max_version verified",
			"line 28: Rule max_version verified",
			"line 21: Rule max_version verified",
			"line 17: Rule max_version verified",
			"line 14: Rule max_version verified",
			"line 25: Rule max_version verified",
			"line 17: Rule min_version verified",
			"line 10: Rule min_version verified",
			"line 14: Rule min_version verified",
			"line 25: Rule min_version verified",
			"line 28: Rule min_version verified",
			"line 21: Rule min_version verified");
	}

	@Test
	public void testSslSetMinMaxProtocolFindings() throws Exception {
		var findings = performTest("openssl/tls/set-minmax-protocol/ctx-setminmax-findings.c", "mark/openssl/tls_version.mark");
		expected(findings,
			"line 10: Rule max_version verified",
			"line 19: Rule max_version verified",
			"line 14: Rule min_version violated",
			"line 33: Rule max_version violated",
			"line 29: Rule min_version violated",
			"line 10: Rule min_version violated",
			"line 42: Rule max_version violated",
			"line 23: Rule min_version violated",
			"line 38: Rule min_version violated",
			"line 14: Rule max_version violated",
			"line 29: Rule max_version verified",
			"line 23: Rule max_version violated",
			"line 42: Rule min_version violated",
			"line 19: Rule min_version violated",
			"line 33: Rule min_version violated",
			"line 38: Rule max_version verified");
	}
}
