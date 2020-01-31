
package de.fraunhofer.aisec.analysis.structures;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Representation of a hardcoded constant.
 *
 * Constants are either of type String, Numeric (int, float, double, long, short, byte), or Boolean.
 *
 */
public class ErrorValue extends ConstantValue {

	private static final Logger log = LoggerFactory.getLogger(ErrorValue.class);
	private final String description;

	public static ErrorValue newErrorValue(@NonNull String description) {
		return new ErrorValue("", description);
	}

	public static ErrorValue newErrorValue(@NonNull String format, Object... args) {
		;
		return new ErrorValue("", String.format(format, args));
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

}
