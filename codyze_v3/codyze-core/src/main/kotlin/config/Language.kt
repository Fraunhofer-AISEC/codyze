package de.fraunhofer.aisec.codyze_core.config

/**
 * Simple enum that maps all optional language frontends of the CPG to the FQN of the frontend in
 * the CPG package.
 */
enum class Language(val frontendClassName: String) {
    PYTHON("de.fraunhofer.aisec.cpg.frontends.python.PythonLanguageFrontend"),
    GO("de.fraunhofer.aisec.cpg.frontends.golang.GoLanguageFrontend")
}