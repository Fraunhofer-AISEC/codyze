
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.structures.Finding;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenSSLTest extends AbstractMarkTest {

	@Disabled
	@Test
	// Currently broken because of multiple entities in rule order statement
	public void testSimple() throws Exception {
		// Just a very simple test of a source file found in the wild.
		Set<Finding> findings = performTest("openssl/github.com/DaniloVlad/OpenSSL-AES/aes.c", "openssl/github.com/DaniloVlad/OpenSSL-AES/mark");

		for (Finding f : findings) {
			System.out.println("  ->" + f.getOnfailIdentifier() + " " + f.getRegions().get(0) + " " + f.getLogMsg());
		}

		// We expect three (positive) verification of the algorithm used
		// Set<Finding> _specificFinding = findings.stream()
		//     .filter(f -> f.getOnfailIdentifier().equals("The_onfail_Identifier"))
		//     .collect(Collectors.toSet());
		// assertEquals(-1, wrongAlgorithmsFindings.size()); // expected number of findings
		// assertEquals(-1, wrongAlgorithmsFindings.stream().filter(Finding::isProblem).collect(Collectors.toSet()).size()); // expected number of actual problems
	}
}
