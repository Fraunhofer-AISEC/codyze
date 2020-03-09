
package de.fraunhofer.aisec.markmodel.fsm;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@NodeEntity
public class Node {
	@GeneratedValue
	@Id
	private Long id;

	@Relationship(value = "s")
	private Set<Node> successors = new HashSet<>();

	@Nullable
	private String base;
	private String op;

	private boolean isStart = false;
	private boolean isEnd = false;
	private boolean isFake = false; // if this is a fake start/end node
	private boolean isError = false; // If this indicates an invalid type state

	public Node() {
	}

	public Node(@Nullable String base, String op) {
		this.base = base;
		this.op = op;
	}

	public void setError(boolean isError) {
		this.isError = isError;
	}

	public void addSuccessor(Node s) {
		successors.add(s);
	}

	public void addSuccessor(Set<Node> s) {
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

	public Set<Node> getSuccessors() {
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
		Node node = (Node) o;
		boolean equal = isStart == node.isStart &&
				isEnd == node.isEnd &&
				isFake == node.isFake &&
				isError == node.isError &&
				Objects.equals(id, node.id) &&
				Objects.equals(base, node.base) &&
				Objects.equals(op, node.op);

		return equal && successors.stream()
				.sorted(Comparator.comparing(s -> s.getName()))
				.map(n -> n.getName())
				.collect(Collectors.toList())
				.equals(
					node.getSuccessors().stream().sorted(Comparator.comparing(s -> s.getName())).map(n -> n.getName()).collect(Collectors.toList()));
	}

	@Override
	public int hashCode() {
		int prime = 31;
		for (Node n : this.successors) {
			prime *= n.getName().hashCode();
		}
		return prime * Objects.hash(id, base, op, isStart, isEnd, isFake, isError);
	}
}
