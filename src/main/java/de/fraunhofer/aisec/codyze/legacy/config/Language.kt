package de.fraunhofer.aisec.codyze.legacy.config

enum class Language(val frontendClassName: String) {
    PYTHON("de.fraunhofer.aisec.cpg.frontends.python.PythonLanguageFrontend"),
    GO("de.fraunhofer.aisec.cpg.frontends.golang.GoLanguageFrontend")
}
