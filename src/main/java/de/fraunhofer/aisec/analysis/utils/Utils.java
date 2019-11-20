
package de.fraunhofer.aisec.analysis.utils;

import de.fraunhofer.aisec.cpg.graph.MethodDeclaration;
import de.fraunhofer.aisec.cpg.graph.RecordDeclaration;

public class Utils {

	// do not instantiate
	private Utils() {
	}

	/**
	 * Returns the fully qualified signature of a method within a record declaration (e.g., a Java class).
	 */
	public static String toFullyQualifiedSignature(RecordDeclaration r, MethodDeclaration m) {
		return r.getName() + "." + m.getSignature();
	}

	/**
	 * Return a unified type String (i.e. changeing cpp-type-separators to Java-type-separators)
	 *
	 * @param name input type
	 * @return a type string which is separated via "."
	 */
	public static String unifyType(String name) {
		if (name == null) {
			return null;
		}
		return name.replace("::", "\\.");
	}

	public static String extractMethodName(String opName) {
		if (opName.contains("::")) {
			opName = opName.substring(opName.lastIndexOf("::") + 2);
		} else if (opName.contains("->")) {
			opName = opName.substring(opName.lastIndexOf("->") + 2);
		} else if (opName.contains(".")) {
			opName = opName.substring(opName.lastIndexOf('.') + 1);
		}
		return opName;
	}

	public static String extractType(String opName) {
		if (opName.contains("::")) {
			opName = opName.substring(0, opName.lastIndexOf("::"));
		} else if (opName.contains("->")) {
			opName = opName.substring(0, opName.lastIndexOf("->"));
		} else if (opName.contains(".")) {
			opName = opName.substring(0, opName.lastIndexOf('.'));
		} else {
			opName = "";
		}
		return opName;
	}

	public static String stripQuotedString(String s) {
		if (s.startsWith("\"") && s.endsWith("\"")) {
			s = s.substring(1, s.length() - 1);
		}
		return s;
	}

	public static String stripQuotedCharacter(String s) {
		if (s.startsWith("\'") && s.endsWith("\'")) {
			// there should be only a single character here
			s = s.substring(1, s.length() - 1);
		}
		return s;
	}
}
