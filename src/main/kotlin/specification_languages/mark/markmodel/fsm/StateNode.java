
package specification_languages.mark.markmodel.fsm;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a node, i.e. a state, in a finite-state-machine.
 */
public class StateNode {
	private final Set<StateNode> successors = new HashSet<>();

	@Nullable
	private final String base;
	private final String op;

	private boolean isStart = false;
	private boolean isEnd = false;
	private boolean isFake = false; // if this is a fake start/end node
	private boolean isError = false; // If this indicates an invalid type state

	public StateNode(@Nullable String base, String op) {
		this.base = base;
		this.op = op;
	}

	public void setError(boolean isError) {
		this.isError = isError;
	}

	public void addSuccessor(StateNode s) {
		successors.add(s);
	}

	public void addSuccessor(Set<StateNode> s) {
		successors.addAll(s);
	}

	public String getName() {
		if (base == null) {
			return op;
		} else {
			return base + "." + op;
		}
	}

	@Nullable
	public String getBase() {
		return base;
	}

	public String getOp() {
		return op;
	}

	public void setStart(boolean b) {
		this.isStart = b;
	}

	public boolean isStart() {
		return isStart;
	}

	public void setEnd(boolean b) {
		this.isEnd = b;
	}

	public boolean isEnd() {
		return isEnd;
	}

	public void setFake(boolean b) {
		this.isFake = b;
	}

	public boolean isFake() {
		return isFake;
	}

	public Set<StateNode> getSuccessors() {
		return successors;
	}

	public String toStringWithAddress() {
		String addr = super.toString();
		addr = addr.substring(addr.lastIndexOf('@') + 1);
		return getName() + "(" + addr + ")";
	}

	public String toString() {
		return getName() + (isEnd ? " (E)" : ""); // + ", MARKING: " + String.join(", ", markings);
	}

	public boolean isError() {
		return isError;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		StateNode node = (StateNode) o;
		boolean equal = isStart == node.isStart &&
				isEnd == node.isEnd &&
				isFake == node.isFake &&
				isError == node.isError &&
				Objects.equals(base, node.base) &&
				Objects.equals(op, node.op);

		return equal && successors.stream()
				.sorted(Comparator.comparing(StateNode::getName))
				.map(StateNode::getName)
				.collect(Collectors.toList())
				.equals(
					node.getSuccessors().stream().sorted(Comparator.comparing(StateNode::getName)).map(StateNode::getName).collect(Collectors.toList()));
	}

	@Override
	public int hashCode() {
		int prime = 31;
		for (StateNode n : this.successors) {
			prime *= n.getName().hashCode();
		}
		return prime * Objects.hash(base, op, isStart, isEnd, isFake, isError);
	}
}
