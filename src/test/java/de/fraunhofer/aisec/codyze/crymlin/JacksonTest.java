
package de.fraunhofer.aisec.codyze.crymlin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JacksonTest extends AbstractMarkTest {

	@Test
	void testBasic() throws Exception {
		var findings = performTest("java/jackson/Serialization.java", "mark/jackson/");

		assertNotNull(findings);

		assertEquals(1, findings.size());

		var finding = findings.iterator().next();

		assertEquals(9, finding.getLocations().get(0).getRegion().getStartLine());
		assertEquals("FORBIDDEN", finding.getOnfailIdentifier());
	}
}
