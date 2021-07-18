
package de.fraunhofer.aisec.codyze.analysis.wpds;

import de.fraunhofer.aisec.cpg.sarif.Region;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

/** Wrapper for a statement in a CPG to be used as a WPDS control location. */
public class Stmt {
	@NonNull
	private final String stmtName;
	@Nullable
	private final Region region;

	public Stmt(@NonNull String code, @Nullable Region region) {
		this.stmtName = code;
		this.region = region;
	}

	@NonNull
	public Region getRegion() {
		return region != null ? region : new Region(-1, -1, -1, -1);
	}

	@Override
	public String toString() {
		return (region != null ? region.getStartLine() + ":" + region.getStartColumn() + " " : "") + stmtName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Stmt stmt1 = (Stmt) o;
		boolean samestmt = stmtName.equals(stmt1.stmtName);
		boolean sameStart = true;
		if (region != null && stmt1.region != null) {
			sameStart = Objects.equals(region.getStartLine(), stmt1.region.getStartLine()) && Objects.equals(region.getStartColumn(), stmt1.region.getStartColumn());
		}
		return samestmt && sameStart;
	}

	@Override
	public int hashCode() {
		if (region == null) {
			return Objects.hash(stmtName);
		} else {
			return Objects.hash(stmtName, region.getStartLine(), region.getStartColumn());
		}
	}
}
