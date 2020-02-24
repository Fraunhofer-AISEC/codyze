
package de.fraunhofer.aisec.analysis.wpds;

import com.google.common.collect.Sets;
import de.breakpointsec.pushdown.weights.Semiring;
import de.fraunhofer.aisec.markmodel.fsm.Node;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A "weight domain" for a weighted pushdown system.
 *
 * <p>
 * A weight domain is a bounded idempotent semiring. In our case, the semiring's operations "combine" and "extend" are linked to a nondeterministic finite automaton
 * (FSM), created from the typedef definition in a Mark file (=a regular expression).
 */
public class Weight extends Semiring {
	private @NonNull Set<NFATransition> value = new HashSet<>();
	@Nullable
	private NFA nfa = null;
	@Nullable
	private Element fixedElement = null;

	public enum Element {
		ZERO,
		ONE
	}

	public Weight(@NonNull NFA nfa) {
		this.nfa = nfa;
	}

	public Weight(@NonNull Set<NFATransition> typestateTransitions) {
		this.value = typestateTransitions;
	}

	public Weight(@NonNull Element fixedEle) {
		this.fixedElement = fixedEle;
	}

	public static Weight one() {
		return new Weight(Element.ONE);
	}

	public static Weight zero() {
		return new Weight(Element.ZERO);
	}

	/**
	 * The extend-operator ⊗T is defined as follows: w1 ⊗T w2 ∶= {s ↦ u ∣ s ↦ t ∈ w1, t ↦ u ∈ w2}.
	 *
	 * @param other
	 * @return
	 */
	@Override
	public Semiring extendWith(@NonNull Semiring other) {
		if (other.equals(one()))
			return this;
		if (this.equals(one()))
			return other;
		if (other.equals(zero()) || this.equals(zero())) {
			return zero();
		}

		if (!(other instanceof Weight)) {
			throw new IllegalArgumentException("Expected Weight but got " + other.getClass());
		}

		Weight otherW = (Weight) other;

		Set<NFATransition> resultSet = new HashSet<>();
		for (NFATransition my : this.value) {
			boolean validTsTransition = false;
			for (NFATransition theirs : otherW.value) {
				// 1-step transitive hull. Avoid endless loops by skipping cyclic transitions.
				if (my.getTarget().equals(theirs.getSource())) {
					// NFATransition.equal() returns false for non-same Nodes, thus we check manually:
					NFATransition newTsTran = new NFATransition(my.getSource(), theirs.getTarget(), my.getLabel());
					if (resultSet.stream().noneMatch(tr ->
							   tr.getSource().toString().equals(newTsTran.getSource().toString())
							&& tr.getLabel().equals(newTsTran.getLabel())
							&& tr.getTarget().toString().equals(newTsTran.getTarget().toString()))) {
						resultSet.add(newTsTran);
					} else {
						System.out.println("Skipping duplicate");
					}
					validTsTransition = true;
					System.out.println("Found further transition from " + my.toString() + " AND " + theirs.toString() + " : \t\t" + newTsTran);
				}
			}
			if (!validTsTransition) {
				resultSet.add(new NFATransition(my.getSource(), new Node("ERROR", "ERROR"), my.getLabel()));
			}
		}
		if (resultSet.isEmpty()) {
			return Weight.zero();
		}
		return new Weight(resultSet);
	}

	/**
	 * The binary operator ⊕T is set union.
	 *
	 * @param other
	 * @return
	 */
	@Override
	public Semiring combineWith(Semiring other) {
		if (this.equals(one()) && other.equals(one())) {
			return one();
		}

		if (this.equals(zero()) && other.equals(zero())) {
			return zero();
		}

		if (other instanceof Weight) {
			Set<NFATransition> union = Sets.union(this.value, ((Weight) other).value);
			if (union.isEmpty()) {
				System.out.println("EMPTY UNION OF " + this + " and " + other);
			}
			return new Weight(union);
		}

		return new Weight(Set.of());
		//throw new IllegalArgumentException("Expected Weight but got " + other.getClass().getName());
	}

	@Override
	public Object value() {
		// Returns current state of FSM
		if (this.fixedElement != null) {
			return this.fixedElement;
		}

		if (nfa != null) {
			// return string representation of the current state of the FSM.
			return nfa.getCurrentConfiguration();
		}

		// Concatenate transitions' toString(), joined by comma
		return value;
	}

	public String toString() {
		// Returns current state of FSM
		if (this.fixedElement != null) {
			return this.fixedElement.toString();
		}

		return this.value.stream().map(NFATransition::toString).collect(Collectors.joining(", "));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		for (NFATransition v : this.value) {
			result = prime * result + v.hashCode();
		}
		if (this.fixedElement != null) {
			result = prime * result + this.fixedElement.hashCode();
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Weight))
			return false;
		Weight other = (Weight) obj;
		if (this.fixedElement != null && other.fixedElement == null)
			return false;
		if (this.fixedElement == null && other.fixedElement != null)
			return false;
		if (this.fixedElement != null && !this.fixedElement.equals(other.fixedElement))
			return false;

		return this.value.equals(other.value);
	}

}
