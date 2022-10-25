package de.fraunhofer.aisec.codyze_backends.cpg

/** Enum containing all supported typestate modes. */
enum class TypestateMode {
    /** Non-deterministic finite automaton. Intraprocedural, not alias-aware. */
    DFA,

    /** Weighted Pushdown System. Interprocedural, alias-aware, context-aware. */
    WPDS
}
