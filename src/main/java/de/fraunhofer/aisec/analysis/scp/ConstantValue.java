
package de.fraunhofer.aisec.analysis.scp;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

/**
 * Representation of a hardcoded constant.
 *
 * Constants are either of type String, Numeric (int, float, double, long, short, byte), or Boolean.
 *
 * @param <T>
 */
public class ConstantValue<T> {
	@NonNull
	private final Type type;
	@NonNull
	protected T value;

	public static final ConstantValue NULL = new ConstantValue(null, Type.NULL);

	private enum Type {
		NUMERIC, BOOLEAN, STRING, NULL
	}

	private ConstantValue(T value, @NonNull Type type) {
		this.value = value;
		this.type = type;
	}

	public static final <T> ConstantValue of(@NonNull T value) {
		if (value instanceof Number) {
			return new ConstantValue<>(value, Type.NUMERIC);
		} else if (value instanceof String) {
			return new ConstantValue<>(value, Type.STRING);
		} else if (value instanceof Boolean) {
			return new ConstantValue<>(value, Type.BOOLEAN);
		} else {
			throw new IllegalArgumentException("Constant value must be numeric, boolean, string");
		}
	}

	@NonNull
	public T getValue() {
		return value;
	}

	public boolean isNumeric() {
		return this.type.equals(Type.NUMERIC);
	}

	public boolean isBoolean() {
		return this.type.equals(Type.BOOLEAN);
	}

	public boolean isString() {
		return this.type.equals(Type.STRING);
	}

	public boolean isNull() {
		return this.type.equals(Type.NULL);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ConstantValue<?> that = (ConstantValue<?>) o;
		return type == that.type &&
				value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, value);
	}
}
