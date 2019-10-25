
package de.fraunhofer.aisec.markmodel.wpds;

import java.util.Objects;

/**
 * Transitions between two states of a non-deterministic automaton (NFA).
 *
 * Transitions may optionally be labeled.
 */
class NFATransition<N> {
	private final N source;
	private final N target;
	private final String label;

	public NFATransition(N source, N target, String label) {
		this.source = source;
		this.target = target;
		this.label = label;
	}

	public N getSource() {
		return source;
	}

	public N getTarget() {
		return target;
	}

	public String getLabel() {
		return label;
	}

	public boolean isCycle() {
		return source.equals(target);
	}

	public String toString() {
		return source.toString() + " -- [" + label + "] --> " + target;
	}

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		NFATransition<?> that = (NFATransition<?>) o;
		return Objects.equals(source, that.source) &&
				Objects.equals(target, that.target) &&
				Objects.equals(label, that.label);
	}

	@Override public int hashCode() {
		return Objects.hash(source, target, label);
	}
}
