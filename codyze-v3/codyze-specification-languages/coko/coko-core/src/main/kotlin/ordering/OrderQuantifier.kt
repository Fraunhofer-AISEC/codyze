package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering

/** All the available quantifiers for this simple regex like DSL. */
enum class OrderQuantifier {
    COUNT,
    BETWEEN,
    ATLEAST,
    SOME,
    MAYBE,
    OPTION
}
