
package de.fraunhofer.aisec.codyze.crymlin;

import org.junit.jupiter.api.Test;

class IssueTest extends AbstractMarkTest {

	@Test
	void issue219() throws Exception {
		var findings = performTest("issues/219/Main.java", "issues/219/");
		findings.forEach(System.out::println);
	}

}
