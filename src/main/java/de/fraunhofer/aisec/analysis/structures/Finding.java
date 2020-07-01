
package de.fraunhofer.aisec.analysis.structures;

import de.fraunhofer.aisec.cpg.sarif.PhysicalLocation;
import de.fraunhofer.aisec.cpg.sarif.Region;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.json.JSONPropertyIgnore;

import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Representation of a vulnerability/non-vulnerability found in the source code.
 *
 */
public class Finding {
	private String onFailIdentifier;

	/**
	 * True, if this Finding indicates a problem/vulnerability. False, if this Finding indicates a code snippet that has been checked and verified.
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
	public Finding(String logMsg, String onfailIdentifier, @Nullable URI artifactUri, int startLine, int endLine, int startColumn, int endColumn) {
		this.logMsg = logMsg;
		this.onFailIdentifier = onfailIdentifier;
		if (artifactUri != null) {
			this.locations
					.add(new PhysicalLocation(artifactUri, new Region(startLine, startColumn, endLine, endColumn)));
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param logMsg Log message for that specific finding. This message is created by the analysis module and may contain further descriptions and details of the
	 *        finding.
	 * @param artifactUri Absolute URI of the source file.
	 * @param onfailIdentifier Identifier of the generic finding, as given by the "onfail" construct of the MARK rule.
	 * @param ranges List of LSP "ranges" determining the position(s) in code of this finding. Note that a LSP range starts counting at 1, while a CPG "region" starts
	 * @param isProblem true, if this Finding represents a vulnerability/weakness. False, if the Finding confirms that the code is actually correct.
	 */
	public Finding(String logMsg, @Nullable URI artifactUri, String onfailIdentifier, List<Region> ranges, boolean isProblem) {
		this.logMsg = logMsg;
		this.onFailIdentifier = onfailIdentifier;
		for (Region r : ranges) {
			if (artifactUri != null) {
				this.locations.add(new PhysicalLocation(artifactUri, r));
			}
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
	@JSONPropertyIgnore
	public List<Region> getRegions() {
		return locations
				.stream()
				.map(PhysicalLocation::getRegion)
				.collect(Collectors.toUnmodifiableList());
	}

	/**
	 * Returns a non-null, possibly empty list of physical locations of this finding.
	 *
	 * This method is required for a proper result JSON.
	 *
	 * @return
	 */
	@SuppressWarnings("unused")
	public @NonNull List<PhysicalLocation> getLocations() {
		return this.locations;
	}

	public String getOnfailIdentifier() {
		return onFailIdentifier;
	}

	public String toString() {
		// simple for now
		String addIfExists = "";
		String descriptionShort = FindingDescription.getInstance().getDescriptionShort(onFailIdentifier);
		if (descriptionShort != null && !descriptionShort.equals(onFailIdentifier)) {
			addIfExists = ": " + descriptionShort;
		}
		String lines;
		if (locations.size() == 1) {
			lines = (locations.get(0).getRegion().getStartLine() + 1) + "";
		} else {
			lines = "[" + locations.stream().map(loc -> "" + (loc.getRegion().getStartLine() + 1)).sorted().distinct().collect(Collectors.joining(", ")) + "]";
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

	/**
	 * Prints the finding formatted as a single line of text.
	 *
	 * @param out
	 */
	public void prettyPrintShort(@NonNull PrintStream out) {
		String shortMsg = FindingDescription.getInstance().getDescriptionShort(onFailIdentifier);
		if (shortMsg == null) {
			shortMsg = onFailIdentifier;
		}

		String lines;
		if (locations.size() == 1) {
			lines = (locations.get(0).getRegion().getStartLine() + 1) + "";
		} else {
			lines = "[" + locations.stream().map(loc -> "" + (loc.getRegion().getStartLine() + 1)).sorted().distinct().collect(Collectors.joining(", ")) + "]";
		}

		out.println(lines + ": " + (isProblem ? "(BAD)  " : "(GOOD) ") + shortMsg + ": " + logMsg);
	}
}
