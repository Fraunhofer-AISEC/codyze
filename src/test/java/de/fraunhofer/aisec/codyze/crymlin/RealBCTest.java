
package de.fraunhofer.aisec.codyze.crymlin;

import de.fraunhofer.aisec.codyze.analysis.structures.Finding;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class RealBCTest extends AbstractMarkTest {

	@Test
	void testSimple() throws Exception {
		// Just a very simple test of a source file found in the wild.
		Set<Finding> findings = performTest("real-examples/bc/rwedoff.Password-Manager/Main.java", "real-examples/bc/rwedoff.Password-Manager/");

		for (Finding f : findings) {
			System.out.println("  ->" + f.getOnfailIdentifier() + " " + f.getRegions().get(0) + " " + f.getLogMsg());
		}

		// We expect three (positive) verification of the algorithm used
		Set<Finding> wrongAlgorithmsFindings = findings.stream()
				.filter(f -> f.getOnfailIdentifier().equals("Wrong_Algorithm"))
				.collect(Collectors.toSet());
		assertEquals(3, wrongAlgorithmsFindings.size()); // 3 in total
		assertEquals(0, wrongAlgorithmsFindings.stream().filter(Finding::isProblem).collect(Collectors.toSet()).size()); // None is a violation

		// We expect three correct usages of SecureRandom
		Set<Finding> correctOrderSecureRandom = findings.stream()
				.filter(f -> f.getLogMsg().equals("Verified Order: SecureRandomOrder"))
				.collect(Collectors.toSet());
		assertEquals(3, correctOrderSecureRandom.size()); // 3 in total
		assertEquals(0, correctOrderSecureRandom.stream().filter(Finding::isProblem).collect(Collectors.toSet()).size()); // None is a violation

		// We expect three correct usages of SHA 512
		Set<Finding> correctOrderSha512 = findings.stream()
				.filter(f -> f.getLogMsg().equals("Verified Order: SHA512DigestOrder"))
				.collect(Collectors.toSet());
		assertEquals(3, correctOrderSha512.size()); // 3 in total
		assertEquals(0, correctOrderSha512.stream().filter(Finding::isProblem).collect(Collectors.toSet()).size()); // None is a violation
	}

}