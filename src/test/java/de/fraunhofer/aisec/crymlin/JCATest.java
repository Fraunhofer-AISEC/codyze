
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.structures.Finding;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JCATest extends AbstractMarkTest {

	@Disabled
	@Test
	public void testSimple() throws Exception {
		// Just a very simple test to explore the graph
		Set<Finding> findings = performTest("java/jca/BlockCipher.java", "dist/mark/bouncycastle/");

		System.out.println("\n\n\n");
		findings.stream().filter(f -> f.isProblem()).forEach(System.out::println);
		System.out.println("\n");
		findings.stream().filter(f -> !f.isProblem()).forEach(System.out::println);
		System.out.println("\n\n\n");

		assertEquals(1, findings.stream().filter(Finding::isProblem).count()); // MockWhen1 results in a finding.
	}

}
