package de.fraunhofer.aisec.markmodel.wpds;

import de.breakpoint.pushdown.weights.Semiring;
import de.fraunhofer.aisec.markmodel.fsm.FSM;
import de.fraunhofer.aisec.markmodel.fsm.Node;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A "weight domain" for a weighted pushdown system.
 *
 * A weight domain is a bounded idempotent semiring. In our case, the semiring's operations "combine" and "extend" are
 * linked to a nondeterministic finite automaton (FSM), created from the typedef definition in a Mark file (=a regular
 * expression).
 */
public class Weight extends Semiring {
    public enum Element { ZERO, ONE}
    @Nullable
    private Element fixedElement = null;
    @Nullable
    private FSM fsm = null;

    public Weight() {
    }

    public Weight(@NonNull FSM fsm) {
        this.fsm = fsm;
    }

    private Weight(@NonNull Element fixedEle) {
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
  public Semiring extendWith(Semiring other) {
        if (other.equals(one()))
            return this;
        if (this.equals(one()))
            return other;
        if (other.equals(zero()) || this.equals(zero())) {
            return zero();
        }

        // TODO send event into Typestate FSM
        Node newFsmState = null;
        Weight w = new Weight();
        return w;
    }

    /**
     * The binary operator ⊕T is set union.
     *
     * @param other
     * @return
     */
    @Override
    public Semiring combineWith(Semiring other) {
        return this; // TODO TBD: Set union.

    }

    @Override
    @Nullable
    public Object value() {
        // TODO Return current state of FSM
        return null;
    }

    public String toString() {
        if (this.fixedElement != null) {
            return this.fixedElement.toString();
        }

        Object v = this.value();
        if (v != null) {
            return v.toString();
        }

        if (fsm != null) {
            // TODO return string representation of the current state of the FSM.
        }
        return "UNDEFINED";
    }

}
