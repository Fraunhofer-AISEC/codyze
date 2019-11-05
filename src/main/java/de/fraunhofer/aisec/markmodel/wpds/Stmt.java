
package de.fraunhofer.aisec.markmodel.wpds;

import de.fraunhofer.aisec.cpg.graph.Region;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

/** Wrapper for a statement in a CPG to be used as a WPDS control location. */
public class Stmt {
	@NonNull
	private final String stmt;
	@Nullable
	private final Region region;

	public Stmt(@NonNull String code, @Nullable Region region) {
		this.stmt = code;
		this.region = region;
	}

	@NonNull
	public Region getRegion() {
		return region != null ? region : new Region(-1, -1, -1, -1);
	}

	@Override
	public String toString() {
		return (region != null ? region.getStartLine() + ":" + region.getStartColumn() + " " : "") + stmt;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Stmt stmt1 = (Stmt) o;
		boolean samestmt = stmt.equals(stmt1.stmt);
		boolean sameStart = true;
		if (region != null && stmt1.region != null) {
			sameStart = Objects.equals(region.getStartLine(), stmt1.region.getStartLine()) && Objects.equals(region.getStartColumn(), stmt1.region.getStartColumn());
		}
		return samestmt && sameStart;
	}

	@Override
	public int hashCode() {
		if (region == null) {
			return Objects.hash(stmt);
		} else {
			return Objects.hash(stmt, region.getStartLine(), region.getStartColumn());
		}
	}
}
