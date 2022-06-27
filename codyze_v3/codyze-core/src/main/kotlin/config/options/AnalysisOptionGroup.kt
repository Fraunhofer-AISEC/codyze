package de.fraunhofer.aisec.codyze_core.config.options

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.*
import de.fraunhofer.aisec.codyze_core.config.ConfigurationRegister
import de.fraunhofer.aisec.codyze_core.config.enums.TypestateMode

@Suppress("UNUSED")
class AnalysisOptions : OptionGroup() {
    val typestate: TypestateMode by
        option(
                "--typestate",
                help =
                    "Typestate analysis mode.\n" +
                        "DFA:  Deterministic finite automaton (faster, intraprocedural)\n" +
                        "WPDS: Weighted pushdown system (slower, interprocedural)"
            )
            .enum<TypestateMode>(ignoreCase = true)
            .default(TypestateMode.DFA)
            .also { ConfigurationRegister.addOption("typestate", it) }
}
