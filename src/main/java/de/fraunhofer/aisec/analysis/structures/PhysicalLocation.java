
package de.fraunhofer.aisec.analysis.structures;

import org.eclipse.lsp4j.Range;

/**
 * Location of a finding in a physical source file.
 *
 * Similar to a SARIF physicalLocation element. The difference is that the location in sourcecode is given as an LSP "Range" here, not as a SARIF "Region".
 */
public class PhysicalLocation {
	private ArtifactLocation artifactLocation;
	private Range range;

	public PhysicalLocation(ArtifactLocation artifactLocation, Range range) {
		this.artifactLocation = artifactLocation;
		this.range = range;
	}

	public ArtifactLocation getArtifactLocation() {
		return artifactLocation;
	}

	public Range getRange() {
		return range;
	}
}
