
package de.fraunhofer.aisec.analysis.structures;

import de.fraunhofer.aisec.cpg.graph.Node;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Representation of a hardcoded constant.
 *
 * Constants are either of type String, Numeric (int, float, double, long, short, byte), or Boolean.
 *
 */
public class ConstantValue extends MarkIntermediateResult {

	private static final Logger log = LoggerFactory.getLogger(ConstantValue.class);

	@NonNull
	private final Type type;
	@NonNull
	protected Object value;

	@Deprecated
	private final Set<Vertex> responsibleVertices = new HashSet<>();
	private final Set<Node> responsibleNodes = new HashSet<>();

	public static boolean isError(Object o) {
		return o instanceof ErrorValue;
	}

	public static Object unbox(Object value) {
		if (value instanceof ConstantValue) {
			return ((ConstantValue) value).getValue();
		} else {
			return value;
		}
	}

	public static ConstantValue newUninitialized() {
		return new ConstantValue("", Type.UNINITIALIZED);
	}

	protected enum Type {
		NUMERIC, BOOLEAN, STRING, ERROR, UNINITIALIZED
	}

	protected ConstantValue(@NonNull Object value, @NonNull Type type) {
		super(ResultType.SINGLEVALUE);
		this.value = value;
		this.type = type;
	}

	public static ConstantValue of(@NonNull Object value) {
		if (value instanceof ConstantValue) {
			return (ConstantValue) value;
		} else if (value instanceof Number) {
			return new ConstantValue(value, Type.NUMERIC);
		} else if (value instanceof String) {
			return new ConstantValue(value, Type.STRING);
		} else if (value instanceof Boolean) {
			return new ConstantValue(value, Type.BOOLEAN);
		} else {
			throw new IllegalArgumentException("Constant value must be numeric, boolean, string");
		}
	}

	/**
	 * Converts a specific value into a ConstantValue.
	 *
	 * Specific values must be non-null and of a type that can be converted into Type.NUMERIC, Type.STRING, or Type.BOOLEAN.
	 *
	 * If conversion is not possible, returns Optional.empty.
	 *
	 * @param o Specific object.
	 * @return
	 */
	@NonNull
	public static Optional<ConstantValue> tryOf(@Nullable Object o) {
		if (o == null) {
			return Optional.empty();
		}
		Class<?> vClass = o.getClass();

		if (vClass.equals(Long.class) || vClass.equals(Integer.class)) {
			return Optional.of(ConstantValue.of(((Number) o).intValue()));
		}

		if (vClass.equals(Double.class) || vClass.equals(Float.class)) {
			return Optional.of(ConstantValue.of(((Number) o).floatValue()));
		}

		if (vClass.equals(Boolean.class)) {
			return Optional.of(ConstantValue.of(o));
		}

		if (vClass.equals(String.class)) {
			// character and string literals both have value of type String
			return Optional.of(ConstantValue.of(o));
		}

		log.warn("Unknown literal type encountered: {} (value: {})", o.getClass(), o);
		return Optional.empty();
	}

	@NonNull
	public Object getValue() {
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

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ConstantValue that = (ConstantValue) o;
		return type == that.type &&
				value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, value);
	}

	@Deprecated
	public void addResponsibleVertex(Vertex v) {
		responsibleVertices.add(v);
	}

	public void addResponsibleNode(Node node) {
		responsibleNodes.add(node);
	}

	@Deprecated
	public void addResponsibleVertices(Collection<Vertex> responsibleVertices) {
		this.responsibleVertices.addAll(responsibleVertices);
	}

	public void addResponsibleNodes(Collection<Node> nodes) {
		responsibleNodes.addAll(nodes);
	}

	@Deprecated
	public void addResponsibleVertices(Vertex... responsibleVertices) {
		this.responsibleVertices.addAll(Arrays.asList(responsibleVertices));
	}

	public void addResponsibleNodes(Node... nodes) {
		responsibleNodes.addAll(Arrays.asList(nodes));
	}

	@Deprecated
	public void addResponsibleVerticesFrom(ConstantValue... other) {
		if (other != null) {
			for (ConstantValue cv : other) {
				responsibleVertices.addAll(cv.responsibleVertices);
			}
		}
	}

	public void addResponsibleNodesFrom(ConstantValue... other) {
		if (other != null) {
			for (ConstantValue cv : other) {
				responsibleNodes.addAll(cv.responsibleNodes);
			}
		}
	}

	@Deprecated
	public Set<Vertex> getResponsibleVertices() {
		return responsibleVertices;
	}

	public Set<Node> getResponsibleNodes() {
		return responsibleNodes;
	}

	@Override
	public String toString() {
		return this.value + " (" + this.type + ")";
	}
}
