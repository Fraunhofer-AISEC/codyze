package de.fraunhofer.aisec.codyze.config

import de.fraunhofer.aisec.cpg.ExperimentalGolang
import de.fraunhofer.aisec.cpg.ExperimentalPython
import de.fraunhofer.aisec.cpg.frontends.LanguageFrontend
import de.fraunhofer.aisec.cpg.frontends.golang.GoLanguageFrontend
import de.fraunhofer.aisec.cpg.frontends.python.PythonLanguageFrontend
import java.util.function.Supplier

enum class Language(
    val frontend: Supplier<Class<out LanguageFrontend?>>,
    val fileTypes: Supplier<List<String>>
) {
    // TODO: Problem is, that cpg go lib is needed, even if you don't need the go frontend
    @OptIn(ExperimentalPython::class)
    PYTHON(
        Supplier<Class<out LanguageFrontend?>> { PythonLanguageFrontend::class.java },
        Supplier { PythonLanguageFrontend.PY_EXTENSIONS }
    ),
    @OptIn(ExperimentalGolang::class)
    GO(
        Supplier<Class<out LanguageFrontend?>> { GoLanguageFrontend::class.java },
        Supplier { GoLanguageFrontend.GOLANG_EXTENSIONS }
    )
}
