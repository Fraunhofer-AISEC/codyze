
package de.fraunhofer.aisec.codyze.analysis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.fraunhofer.aisec.cpg.sarif.PhysicalLocation;
import de.fraunhofer.aisec.cpg.sarif.Region;
import de.fraunhofer.aisec.mark.markDsl.Action;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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

	/**
	 * Identifier following `onfail` in a MARK  rule. Identifies the
	 * corresponding description for this finding.
	 *
	 * <p>
	 *     To be removed as rule names are sufficiently unique.
	 * </p>
	 */
	@Deprecated(since = "2.0.0-alpha3", forRemoval = true)
	private final String onFailIdentifier;

	/**
	 * True, if this finding indicates a problem/vulnerability. False, if this
	 * finding indicates a code snippet that has been checked and verified.
	 */
	private boolean isProblem = true;

	/**
	 * Simplified description of this finding. Usually state that a rule was
	 * validated or violated.
	 */
	private final String logMsg;

	/**
	 * Location (lines and columns) in source code files, where this finding was
	 * produced.
	 */
	@NonNull
	private final List<PhysicalLocation> locations = new ArrayList<>();

	/**
	 * 
	 */
	private Action action;

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
		action = Action.FAIL;
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
		this.action = Action.FAIL;
	}

	public Finding(String identifier, String logMsg, @Nullable URI artifactUri, Action action, List<Region> ranges, boolean isProblem) {
		this.onFailIdentifier = identifier;
		this.logMsg = logMsg;
		for (Region r : ranges) {
			if (artifactUri != null) {
				this.locations.add(new PhysicalLocation(artifactUri, r));
			}
		}
		this.isProblem = isProblem;
		this.action = action;
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
	@JsonIgnore
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

	public Action getAction() {
		return action;
	}

	public String toString() {
		String addIfExists = "";

		if (onFailIdentifier != null) {
			// simple for now
			String descriptionShort = FindingDescription.getInstance().getDescriptionShort(onFailIdentifier);
			if (descriptionShort != null && !descriptionShort.equals(onFailIdentifier)) {
				addIfExists = ": " + descriptionShort;
			}
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
