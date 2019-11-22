
package de.fraunhofer.aisec.analysis.structures;

import de.fraunhofer.aisec.cpg.graph.Region;
import org.apache.commons.collections.list.UnmodifiableList;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Representation of a vulnerability/non-vulnerability found in the source code.
 *
 */
public class Finding {
	private static final Logger log = LoggerFactory.getLogger(Finding.class);
	private String onFailIdentifier;

	/**
	 * True, if this Finding indicates a problem/vulnerability.
	 * False, if this Finding indicates a code snippet that has been checked and verified.
	 */
	private boolean isProblem = true;

	private String logMsg;
	@NonNull
	private List<PhysicalLocation> locations = new ArrayList<>();

	/**
	 * Constructor.
	 *
	 * @param logMsg Log message for that specific finding. This message is created by the analysis module and may contain further descriptions and details of the
	 *        finding.
	 * @param onfailIdentifier Identifier of the generic finding, as given by the "onfail" construct of the MARK rule.
	 * @param startLine Line in code where the finding begins. Note that LSP starts counting at 1.
	 * @param endLine Line in code where the finding ends.
	 * @param startColumn Column in code where the finding begins. Note that LPS start counting at 1.
	 * @param endColumn Column in code where the finding ends.
	 */
	public Finding(String logMsg, String onfailIdentifier, URI artifactUri, int startLine, int endLine, int startColumn, int endColumn) {
		this.logMsg = logMsg;
		this.onFailIdentifier = onfailIdentifier;
		this.locations
				.add(new PhysicalLocation(new ArtifactLocation(artifactUri, null), new Range(new Position(startLine, startColumn), new Position(endLine, endColumn))));
	}

	/**
	 * Constructor.
	 *  @param logMsg Log message for that specific finding. This message is created by the analysis module and may contain further descriptions and details of the
	 *        finding.
	 * @param artifactUri Absolute URI of the source file.
	 * @param onfailIdentifier Identifier of the generic finding, as given by the "onfail" construct of the MARK rule.
	 * @param ranges List of LSP "ranges" determining the position(s) in code of this finding. Note that a LSP range starts counting at 1, while a CPG "region" starts
	 * @param isProblem true, if this Finding represents a vulnerability/weakness. False, if the Finding confirms that the code is actually correct.
	 */
	public Finding(String logMsg, URI artifactUri, String onfailIdentifier, List<Range> ranges, boolean isProblem) {
		this.logMsg = logMsg;
		this.onFailIdentifier = onfailIdentifier;
		for (Range r : ranges) {
			this.locations.add(new PhysicalLocation(new ArtifactLocation(artifactUri, null), r));
		}
		this.isProblem = isProblem;
	}

	public String getLogMsg() {
		return logMsg;
	}

	public boolean isProblem() {
		return isProblem;
	}

	/**
	 * Returns an unmodifiable list of the associated LSP "ranges" (~regions).
	 */
	public List<Range> getRanges() {
		return locations
				.stream()
				.map(PhysicalLocation::getRange)
				.collect(Collectors.toUnmodifiableList());
	}

	public String getOnfailIdentifier() {
		return onFailIdentifier;
	}

	public String toString() {
		// simple for now
		String addIfExists = "";
		String descriptionBrief = FindingDescription.getInstance().getDescriptionBrief(onFailIdentifier);
		if (!descriptionBrief.equals(onFailIdentifier)) {
			addIfExists = ": " + descriptionBrief;
		}
		String lines;
		if (locations.size() == 1) {
			lines = (locations.get(0).getRange().getStart().getLine() + 1) + "";
		} else {
			lines = "[" + locations.stream().map(loc -> "" + (loc.getRange().getStart().getLine() + 1)).collect(Collectors.joining(", ")) + "]";
		}
		return "line " + lines + ": " + logMsg + addIfExists;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Finding)) {
			return false;
		}
		return Objects.equals(this.logMsg, ((Finding) obj).logMsg) && Objects.equals(this.onFailIdentifier, ((Finding) obj).onFailIdentifier);
	}

	public int hashCode() {
		return toString().hashCode();
	}
}
