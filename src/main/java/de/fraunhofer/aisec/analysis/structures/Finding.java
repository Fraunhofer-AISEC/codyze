
package de.fraunhofer.aisec.analysis.structures;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Finding {
	private static final Logger log = LoggerFactory.getLogger(Finding.class);
	private String identifier;

	private String name;
	private List<Range> ranges = new ArrayList<>();

	public Finding(String name, String identifier) {
		this(name, identifier, -1, -1, -1, -1);
	}

	public Finding(String name, String identifier, int startLine, int endLine, int startColumn, int endColumn) {
		this.name = name;
		this.identifier = identifier;
		this.ranges.add(new Range(
			new Position(startLine, startColumn),
			new Position(endLine, endColumn)));
	}

	public Finding(String name, String identifier, List<Range> ranges) {
		this.name = name;
		this.identifier = identifier;
		this.ranges.addAll(ranges);
	}

	public String getName() {
		return name;
	}

	public Range getFirstRange() {
		return ranges.get(0);
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
		String lines;
		if (ranges.size() == 1) {
			lines = (getFirstRange().getStart().getLine() + 1) + "";
		} else {
			lines = "[" + ranges.stream().map(x -> "" + (x.getStart().getLine() + 1)).collect(Collectors.joining(", ")) + "]";
		}
		return "line " + lines + ": " + name + addIfExists;
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
