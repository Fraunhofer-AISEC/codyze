
package specification_languages.mark.analysis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.fraunhofer.aisec.codyze.legacy.analysis.FindingDescription;
import de.fraunhofer.aisec.codyze.sarif.schema.Result.Kind;
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
 */
public class Finding {

	/**
	 * Identifier for this finding.
	 */
	private String id;

	/**
	 * [INFO, WARN, FAIL]
	 */
	private final Action action;

	/**
	 * Simplified description of this finding. Usually state that a rule was
	 * validated or violated.
	 */
	private final String logMsg;

	/**
	 * the Kind of the Finding [FAIL, PASS, OPEN, INFORMATIONAL, NOT_APPLICABLE, REVIEW, FAIL]
	 */
	private final Kind kind;

	/**
	 * Location (lines and columns) in source code files, where this finding was
	 * produced.
	 */
	@NonNull
	private final List<PhysicalLocation> locations = new ArrayList<>();

	/**
	 * Constructor.
	 *
	 * @param logMsg      Log message for that specific finding. This message is created by the analysis module and may contain further descriptions and details of the
	 *                    finding.
	 * @param action      The severity of the result. Using this constructor, this implies that the Kind is FAIL
	 * @param startLine   Line in code where the finding begins. Note that LSP starts counting at 1.
	 * @param endLine     Line in code where the finding ends.
	 * @param startColumn Column in code where the finding begins. Note that LPS start counting at 1.
	 * @param endColumn   Column in code where the finding ends.
	 */
	public Finding(String id, Action action, String logMsg, @Nullable URI artifactUri, int startLine, int endLine, int startColumn,
			int endColumn) {
		this.id = id;
		this.action = action;
		this.logMsg = logMsg;
		if (artifactUri != null) {
			this.locations
					.add(new PhysicalLocation(artifactUri, new Region(startLine, startColumn, endLine, endColumn)));
		}
		// since the Action (level in the SARIF spec) is not none, the Kind must be FAIL by default
		this.kind = Kind.FAIL;
	}

	/**
	 * Constructor.
	 *
	 * @param id
	 * @param action	  The severity of the result. If the kind is anything besides FAIL, this param will be ignored by the SARIF output producer
	 * @param logMsg      Log message for that specific finding. This message is created by the analysis module and may contain further descriptions and details of the
	 *                    finding.
	 * @param artifactUri Absolute URI of the source file.
	 * @param ranges      List of LSP "ranges" determining the position(s) in code of this finding. Note that a LSP range starts counting at 1, while a CPG "region" starts
	 * @param kind   	  The kind of the finding [FAIL, PASS, OPEN, INFORMATIONAL, NOT_APPLICABLE, REVIEW, FAIL]
	 */
	public Finding(String id, Action action, String logMsg, @Nullable URI artifactUri, List<Region> ranges, Kind kind) {
		this.id = id;
		this.logMsg = logMsg;

		for (Region r : ranges) {
			if (artifactUri != null) {
				this.locations.add(new PhysicalLocation(artifactUri, r));
			}
		}
		this.kind = kind;
		// another idea was to null this value whenever kind != FAIL, but to prevent unexpected results this has not been implemented
		this.action = action;
	}

	public String getIdentifier() {
		return id;
	}

	public Action getAction() {
		return action;
	}

	public String getLogMsg() {
		return logMsg;
	}

	public Kind getKind() {
		return kind;
	}

	/**
	 * For dependency reasons this method is still included, returns true unless kind == PASS
	 * DO NOT use this in the future, instead check the KIND and LEVEL of the Finding
	 * This method may be removed at any point in the future
	 */
	public boolean isProblem() {
		return kind != Kind.PASS;
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
	 * <p>
	 * This method is required for a proper result JSON.
	 *
	 * @return
	 */
	@SuppressWarnings("unused")
	public @NonNull List<PhysicalLocation> getLocations() {
		return this.locations;
	}

	public String toString() {
		String addIfExists = "";

		// simple for now
		String description = (getKind() == Kind.PASS) ? FindingDescription.getInstance().getDescriptionPass(id)
				: FindingDescription.getInstance().getDescriptionShort(id);
		if (description != null && !description.equals(id)) {
			addIfExists = ": " + description;
		}

		return toShortMessage() + addIfExists;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Finding)) {
			return false;
		}
		return Objects.equals(this.logMsg, ((Finding) obj).logMsg)
				&& Objects.equals(this.id, ((Finding) obj).id);
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
		String shortMsg = FindingDescription.getInstance().getDescriptionShort(id);
		if (shortMsg == null) {
			shortMsg = id;
		}

		String lines;
		if (locations.size() == 1) {
			lines = (locations.get(0).getRegion().getStartLine() + 1) + "";
		} else {
			lines = "[" + locations.stream().map(loc -> "" + (loc.getRegion().getStartLine() + 1)).sorted().distinct().collect(Collectors.joining(", ")) + "]";
		}

		out.println(lines + ": " + getKind().name() + " " + shortMsg + ": " + logMsg);
	}

	public String toShortMessage() {
		String lines;
		if (locations.size() == 1) {
			lines = (locations.get(0).getRegion().getStartLine() + 1) + "";
		} else {
			lines = "[" + locations.stream().map(loc -> "" + (loc.getRegion().getStartLine() + 1)).sorted().distinct().collect(Collectors.joining(", ")) + "]";
		}
		return "line " + lines + ": " + logMsg;
	}

}