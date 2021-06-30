
package de.fraunhofer.aisec.crymlin;

import org.junit.jupiter.api.Test;

class CTest extends AbstractMarkTest {

	@Test
	void testDefinition() throws Exception {
		var findings = performTest("c/def.c", "c/s.mark");
		containsFindings(findings, "line 14: Rule some_s verified");
	}

	@Test
	void testDeclAssign() throws Exception {
		var findings = performTest("c/declassign.c", "c/s.mark");
		containsFindings(findings, "line 16: Rule some_s verified");
	}
}
