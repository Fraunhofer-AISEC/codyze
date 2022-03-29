package de.fraunhofer.aisec.codyze.legacy.config

/**
 * Stores whether the associated package should be disabled and the name of the rules to be disabled
 */
data class DisabledMarkRulesValue(
    var isDisablePackage: Boolean = false,
    val disabledMarkRuleNames: MutableSet<String> = mutableSetOf()
)
