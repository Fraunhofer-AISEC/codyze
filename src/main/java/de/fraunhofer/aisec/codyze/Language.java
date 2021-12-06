
package de.fraunhofer.aisec.codyze;

import de.fraunhofer.aisec.cpg.frontends.LanguageFrontend;
import de.fraunhofer.aisec.cpg.frontends.golang.GoLanguageFrontend;
import de.fraunhofer.aisec.cpg.frontends.python.PythonLanguageFrontend;

import java.util.List;

public enum Language {
	PYTHON (PythonLanguageFrontend.class, PythonLanguageFrontend.PY_EXTENSIONS),
	GO(GoLanguageFrontend.class, GoLanguageFrontend.GOLANG_EXTENSIONS);

	private final Class<? extends LanguageFrontend> languageFrontend;
	private final List<String> fileTypes;

	Language(Class<? extends LanguageFrontend> languageFrontend, List<String> fileTypes) {
		this.languageFrontend = languageFrontend;
		this.fileTypes = fileTypes;
	}
}
