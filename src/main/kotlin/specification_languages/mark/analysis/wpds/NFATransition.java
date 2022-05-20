
package specification_languages.mark.analysis.wpds;

import specification_languages.mark.markmodel.fsm.StateNode;

import java.util.Objects;

/**
 * Transitions between two states of a non-deterministic automaton (NFA).
 *
 * Transitions may optionally be labeled.
 */
public class NFATransition {
	private final StateNode source;
	private final StateNode target;
	private final String label;

	public NFATransition(StateNode source, StateNode target, String label) {
		this.source = source;
		this.target = target;
		this.label = label;
	}

	public StateNode getSource() {
		return source;
	}

	public StateNode getTarget() {
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

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		NFATransition that = (NFATransition) o;
		return source.equals(that.source) &&
				target.equals(that.target) &&
				label.equals(that.label);
	}

	@Override
	public int hashCode() {
		return Objects.hash(source, target, label);
	}
}
