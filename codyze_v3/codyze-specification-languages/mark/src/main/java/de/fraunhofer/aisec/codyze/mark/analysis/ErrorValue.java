
package de.fraunhofer.aisec.codyze.mark.analysis;

import org.checkerframework.checker.nullness.qual.NonNull;
import de.fraunhofer.aisec.codyze.mark.analysis.resolution.ConstantValue;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Representation of a hardcoded constant.
 *
 * Constants are either of type String, Numeric (int, float, double, long, short, byte), or Boolean.
 *
 */
public class ErrorValue extends ConstantValue {

	private final String description;

	public static ErrorValue newErrorValue(@NonNull String description) {
		return new ErrorValue("", description);
	}

	public static ErrorValue newErrorValue(String description, List<MarkIntermediateResult> oldErrorMessages) {
		StringBuilder sb = new StringBuilder();
		sb.append(description);
		sb.append("\n");
		for (MarkIntermediateResult cv : oldErrorMessages) {
			if (cv instanceof ErrorValue
					&&
					!((ErrorValue) cv).getDescription().isEmpty()) {
				sb.append(((ErrorValue) cv).getDescription());
				sb.append("\n");
			}
		}

		return new ErrorValue("", sb.substring(0, sb.length() - 1)); // remove last newline
	}

	public static ErrorValue newErrorValue(@NonNull String description, MarkIntermediateResult... oldErrorMessages) {
		return newErrorValue(description, Arrays.asList(oldErrorMessages));
	}

	protected ErrorValue(@NonNull Object value, @NonNull String description) {
		super(value, Type.ERROR);
		this.value = value;
		this.description = description;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ErrorValue that = (ErrorValue) o;
		return description.equals(that.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(description);
	}

	public String getDescription() {
		return description;
	}
}
