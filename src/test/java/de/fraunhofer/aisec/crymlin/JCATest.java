
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.structures.Finding;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JCATest extends AbstractMarkTest {

	@Disabled
	@Test
	public void testSimpleBlockCipher() throws Exception {
		// Just a very simple test to explore the graph
		Set<Finding> findings = performTest("java/jca/BlockCipher.java", "dist/mark/bouncycastle/");

		System.out.println("\n\n\n");
		findings.stream().filter(f -> f.isProblem()).forEach(System.out::println);
		System.out.println("\n");
		findings.stream().filter(f -> !f.isProblem()).forEach(System.out::println);
		System.out.println("\n\n\n");

		//assertEquals(1, findings.stream().filter(Finding::isProblem).count()); // MockWhen1 results in a finding.
	}

	@Disabled
	@Test
	public void testGCMCipherMode() throws Exception {
		// Just a very simple test to explore the graph
		Set<Finding> findings = performTest("java/jca/AESGCM.java", "dist/mark/bouncycastle/");

		for (Finding f : findings) {
			System.out.printf("%s\t", f.isProblem() ? "PROBLEM" : "NORMAL");
			System.out.printf("%s\t", f.getOnfailIdentifier());
			System.out.print(f.getRanges());
			System.out.printf("\t%s", f.getLogMsg());
		}
		//assertEquals(1, findings.stream().filter(Finding::isProblem).count()); // MockWhen1 results in a finding.
	}

}
