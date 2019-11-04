
package de.fraunhofer.aisec.crymlin.structures;

import de.fraunhofer.aisec.crymlin.utils.FindingDescription;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class Finding {
	private static final Logger log = LoggerFactory.getLogger(Finding.class);
	private String identifier;

	private String name;
	private Range range;
	private Range humanRange;

	public Finding(String name, String identifier) {
		this(name, identifier, -1, -1, -1, -1);
	}

	public Finding(String name, String identifier, long startLine, long endLine, long startColumn, long endColumn) {
		this.name = name;
		this.identifier = identifier;
		// lsp can only handle integer-line numbers
		if (startLine > Integer.MAX_VALUE) {
			log.error("startLine is larger than Integer.MAX_VALUE. Set to invalid (-1)");
			startLine = -1;
		}
		if (endLine > Integer.MAX_VALUE) {
			log.error("endLine is larger than Integer.MAX_VALUE. Set to invalid (-1)");
			endLine = -1;
		}
		if (startColumn > Integer.MAX_VALUE) {
			log.error("startColumn is larger than Integer.MAX_VALUE. Set to invalid (-1)");
			startColumn = -1;
		}
		if (endColumn > Integer.MAX_VALUE) {
			log.error("endColumn is larger than Integer.MAX_VALUE. Set to invalid (-1)");
			endColumn = -1;
		}
		// adjust off-by-one
		this.range = new Range(
			new Position((int) startLine - 1, (int) startColumn - 1),
			new Position((int) endLine - 1, (int) endColumn - 1));
		this.humanRange = new Range(
			new Position((int) startLine, (int) startColumn),
			new Position((int) endLine, (int) endColumn));
	}

	public String getName() {
		return name;
	}

	public Range getRange() {
		return range;
	}

	public String getIdentifier() {
		return identifier;
	}

	public String toString() {
		// simple for now
		String addIfExists = "";
		String descriptionBrief = FindingDescription.getInstance().getDescriptionBrief(identifier);
		if (!descriptionBrief.equals(identifier)) {
			addIfExists = ": " + descriptionBrief;
		}
		return "line " + humanRange.getStart().getLine() + ": " + name + addIfExists;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Finding)) {
			return false;
		}
		return Objects.equals(this.name, ((Finding) obj).name) && Objects.equals(this.identifier, ((Finding) obj).identifier);
	}

	public int hashCode() {
		return toString().hashCode();
	}
}
