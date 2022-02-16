package de.fraunhofer.aisec.codyze.config

data class DisabledMarkRulesValue(
    var isDisablePackage: Boolean = false,
    val disabledMarkRuleNames: MutableSet<String> = mutableSetOf()
)
