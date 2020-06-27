
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.analysis.structures.FindingDescription;
import de.fraunhofer.aisec.cpg.sarif.Region;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the SARIF-like format for Finding outputs.
 */
public class FindingDescriptionTest {

	@Test
	public void testFindingDescription() throws URISyntaxException {
		// Create some Finding object
		String logMsg = "Variable cm not initialized";
		URI artifactUri = URI.create("file:///tmp/test.cpp");
		String onFailId = "WrongUseOfBotan_CipherMode";
		List<Region> regions = List.of(new Region(1, 2, 10, 12));
		boolean isProblem = true;
		Finding f = new Finding(logMsg, artifactUri, onFailId, regions, isProblem);

		// Initialize database with explanations
		FindingDescription fd = FindingDescription.getInstance();
		fd.init(new File(this.getClass().getClassLoader().getResource("unittests/findingDescription.json").toURI()));

		String fullDescription = fd.getDescriptionFull(onFailId);
		String shortDescription = fd.getDescriptionShort(onFailId);
		String helpUri = fd.getHelpUri(onFailId);
		List<String> fixes = fd.getFixes(onFailId);

		assertEquals("Full description", fullDescription);
		assertEquals("The order of called Botan methods is wrong.", shortDescription);
		assertEquals("https://www.codyze.io/explanations/10", helpUri);
		assertEquals(1, fixes.size());
		assertEquals("Just fix it!", fixes.get(0));

	}
}
