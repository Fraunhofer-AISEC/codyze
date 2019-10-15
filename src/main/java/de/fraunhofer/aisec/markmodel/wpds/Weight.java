package de.fraunhofer.aisec.markmodel.wpds;

import com.google.common.collect.Sets;
import de.breakpoint.pushdown.weights.Semiring;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * A "weight domain" for a weighted pushdown system.
 *
 * <p>A weight domain is a bounded idempotent semiring. In our case, the semiring's operations
 * "combine" and "extend" are linked to a nondeterministic finite automaton (FSM), created from the
 * typedef definition in a Mark file (=a regular expression).
 */
public class Weight extends Semiring {
  private @NonNull Set<NFATransition> value = new HashSet<>();
  @Nullable private NFA nfa = null;
  @Nullable private Element fixedElement = null;

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
    if (other.equals(one())) return this;
    if (this.equals(one())) return other;
    if (other.equals(zero()) || this.equals(zero())) {
      return zero();
    }

    if (!(other instanceof Weight)) {
      throw new IllegalArgumentException("Expected Weight but got " + other.getClass());
    }

    Weight otherW = (Weight) other;

    Set<NFATransition> resultSet = new HashSet<>();
    for (NFATransition my : this.value) {
      for (NFATransition theirs : otherW.value) {
        if (my.getSource().equals(theirs.getTarget())) {
          resultSet.add(new NFATransition(my.getSource(), theirs.getTarget(), my.getLabel()));
        }
      }
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
      if (other instanceof Weight)
        return new Weight(Sets.union(this.value, ((Weight) other).value));

      throw new IllegalArgumentException("Expected Weight but got " + other.getClass().getName());
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

    // TODO Write this shorter
    String result = "";
    for (NFATransition t : this.value) {
      result = result + t.toString() + ", ";
    }
    return result;
  }

  public String toString() {
    return value().toString();
  }
}
