
package de.fraunhofer.aisec.analysis.utils;

import de.fraunhofer.aisec.cpg.graph.Node;
import de.fraunhofer.aisec.cpg.graph.declarations.MethodDeclaration;
import de.fraunhofer.aisec.cpg.graph.declarations.RecordDeclaration;
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression;
import de.fraunhofer.aisec.cpg.graph.types.PointerType;
import de.fraunhofer.aisec.cpg.graph.types.Type;
import de.fraunhofer.aisec.cpg.graph.types.UnknownType;
import de.fraunhofer.aisec.cpg.sarif.PhysicalLocation;
import de.fraunhofer.aisec.cpg.sarif.Region;
import de.fraunhofer.aisec.mark.markDsl.Parameter;
import de.fraunhofer.aisec.markmodel.Constants;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class Utils {
	private static final Logger log = LoggerFactory.getLogger(Utils.class);

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
	 * Return a unified type String (i.e. changing cpp-type-separators to Java-type-separators)
	 *
	 * @param name input type
	 * @return a type string which is separated via "."
	 */
	public static String unifyType(@NonNull String name) {
		return name.replace("::", ".");
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

	public static String stripQuotedString(String s) {
		if (s.startsWith("\"") && s.endsWith("\"")) {
			s = s.substring(1, s.length() - 1);
		}
		return s;
	}

	public static String stripQuotedCharacter(String s) {
		if (s.startsWith("'") && s.endsWith("'")) {
			// there should be only a single character here
			s = s.substring(1, s.length() - 1);
		}
		return s;
	}

	/**
	 * Returns true if a type from the source language is equal to a type in MARK.
	 *
	 * Namespaces are ignored.
	 *
	 * For instance:
	 *
	 * isSubTypeOf("uint8", "uint8")  -> true
	 * isSubTypeOf("string", "std.string")  -> true
	 * isSubTypeOf("std::string", "std.string")  -> true
	 * isSubTypeOf("random::string", "std.string")  -> true
	 *
	 * @param sourceTypes
	 * @param markParameter
	 * @return
	 */
	public static boolean isSubTypeOf(@NonNull Set<Type> sourceTypes, Parameter markParameter) {
		String logMsg = "Any of " + sourceTypes.stream().map(Type::getTypeName).collect(Collectors.joining(",")) + "  is a subtype of "
				+ String.join(",", markParameter.getTypes()) + ": {}";

		if (markParameter.getVar().equals(Constants.ANY_TYPE)) {
			log.debug(logMsg, "true");
			return true;
		}

		if (markParameter.getTypes().isEmpty()) {
			log.debug(logMsg, "true");
			return true;
		}
		Deque<Type> sourceSuperTypes = new ArrayDeque<>();
		sourceSuperTypes.addAll(sourceTypes);
		while (!sourceSuperTypes.isEmpty()) {
			Type sourceType = sourceSuperTypes.pop();

			/* Note:
			Java frontend is able to resolve imports and will provide fully qualified names.
			C++ frontend is not able to resolve namespaces and we must thus compare "nonQualifiedName"s.
			 */
			boolean match = markParameter.getTypes()
					.stream()
					.anyMatch(markType -> Utils.toNonQualifiedName(markType).equals(Utils.toNonQualifiedName(sourceType.getTypeName())));
			if (match) {
				log.debug(logMsg, "true");
				return true;
			}

			// There are various representations of "string" and we map them manually here.
			if (markParameter.getTypes()
					.stream()
					.map(Utils::toNonQualifiedName)
					.anyMatch(t -> t.equalsIgnoreCase("string"))) {
				if (isStringType(sourceType)) {
					log.debug(logMsg, "true");
					return true;
				}
			}

			// If type could not be determined, we err on the false positive side.
			if (sourceType instanceof UnknownType) {
				log.debug(logMsg, "true");
				return true;
			}

			sourceSuperTypes.addAll(sourceType.getSuperTypes());
		}
		log.debug(logMsg, "false");

		return false;

		//		anymatch: for (Type sourceType : sourceSuperTypes) {
		//			String uniSource = unifyType(sourceType.getTypeName());
		//			if (uniSource.contains(".") && !uniSource.endsWith(".")) {
		//				uniSource = uniSource.substring(uniSource.lastIndexOf('.') + 1);
		//			}
		//			// TODO Currently, the "type" property may contain modifiers such as "const", so we must remove them here. Will change in CPG.
		//			if (uniSource.contains(" ") && !uniSource.endsWith(" ")) {
		//				uniSource = uniSource.substring(uniSource.lastIndexOf(' ') + 1);
		//			}
		//
		//			if (markParameter.getTypes().isEmpty()) {
		//				return true; // match all
		//			} else {
		//				for (String type : markParameter.getTypes()) {
		//					String uniMark = unifyType(type);
		//					if (uniMark.contains(".") && !uniMark.endsWith(".")) {
		//						uniMark = uniMark.substring(uniMark.lastIndexOf('.') + 1);
		//					}
		//
		//					// We do not consider type hierarchies here but simply match for equality plus a few manual mappings
		//					result = uniSource.equals(uniMark);
		//					// There are various representations of "string" and we map them manually here.
		//					if (uniMark.equals("string")) {
		//						if (uniSource.equals("QString") || uniSource.equals("string") || uniSource.equals("String") || uniSource.equals("char*")) {
		//							result = true;
		//						} else {
		//							log.trace("comparing string from MARK against {} from Sourcefile. Does currently not match", uniSource);
		//						}
		//					}
		//
		//					// If type could not be determined, we err on the false positive side.
		//					if (sourceType.getTypeName().equals("UNKNOWN")) {
		//						result = true;
		//					}
		//
		//					// If any of the subtypes match, we can return.
		//					if (result) {
		//						break anymatch;
		//					}
		//				}
		//			}
		//		}
		//
		//		if (sourceSuperTypes.isEmpty()) {
		//			// TODO Empty "possibleSubTypes" means that the type could not be resolved from source code. This is ok. It is however unclear/undefined how this should be handled if MARK requires a type.
		//			log.warn("CHECK ME: Cannot compare empty argument type in source code to MARK type {}. Will assume it matches.", String.join(",", markParameter.getTypes()));
		//			result = true;
		//		}
		//
		//		log.debug("Any of {} is a subtype of {}: {}", sourceSuperTypes.stream().map(Type::getTypeName).collect(Collectors.joining(",")),
		//			String.join(",", markParameter.getTypes()), result);
		//
		//		return result;
	}

	/**
	 * Strips off the leading part of a (possibly fully qualified) type.
	 *
	 * str.string       -> string
	 * std::string      -> string
	 * string           -> string
	 * java.lang.String -> String
	 *
	 * @param typeStr
	 * @return
	 */
	@NonNull
	public static String toNonQualifiedName(@NonNull String typeStr) {
		int posDot = typeStr.lastIndexOf('.');
		int posColon = typeStr.lastIndexOf(':');
		int pos = Math.max(posDot, posColon);
		if (pos > -1 && pos < typeStr.length() - 1) {
			typeStr = typeStr.substring(pos + 1);
		}
		return typeStr;
	}

	/**
	 * Counterpart to toNonQualifiedName().
	 *
	 * @param fqn
	 * @return
	 */
	@NonNull
	public static String getScope(@NonNull String fqn) {
		int posDot = fqn.lastIndexOf('.');
		int posColon = fqn.lastIndexOf("::");
		int pos = Math.max(posDot, posColon);
		if (pos > -1 && pos < fqn.length() - 1) {
			fqn = fqn.substring(0, pos);
		}
		return fqn;
	}

	private static boolean isStringType(Type sourceType) {
		while (sourceType instanceof PointerType) {
			sourceType = ((PointerType) sourceType).getElementType();
		}

		String uniSource = unifyType(sourceType.getTypeName());
		if (uniSource.contains(".") && !uniSource.endsWith(".")) {
			uniSource = uniSource.substring(uniSource.lastIndexOf('.') + 1);
		}

		return uniSource.equals("QString") || uniSource.equals("string") || uniSource.equals("String") || uniSource.equals("char");
	}

	public static List<Method> getMethodsAnnotatedWith(final Class<?> type, final Class<? extends Annotation> annotation) {
		final List<Method> methods = new ArrayList<>();
		Class<?> klass = type;
		while (klass != null && klass != Object.class) { // need to iterated thought hierarchy in order to retrieve methods from above the current instance
			// iterate though the list of methods declared in the class represented by class variable, and add those annotated with the specified annotation
			final List<Method> allMethods = new ArrayList<>(Arrays.asList(klass.getDeclaredMethods()));
			for (final Method method : allMethods) {
				if (method.isAnnotationPresent(annotation)) {
					methods.add(method);
				}
			}
			// move to the upper class in the hierarchy in search for more methods
			klass = klass.getSuperclass();
		}
		return methods;
	}

	/**
	 * Returns a <code>Region</code> object from a node's startLine, endLine, startColumn, endColumn property.
	 *
	 * Note that these are not the exact property values but start at 0 rather than by 1.
	 * If these properties do not exist, returns -1.
	 *
	 * @param n the node
	 *
	 * @return the region
	 */
	@NonNull
	public static Region getRegionByNode(@NonNull Node n) {
		if (n.getLocation() == null) {
			return new Region(-1, -1, -1, -1);
		}

		int startLine = n.getLocation().getRegion().getStartLine() - 1;
		int endLine = n.getLocation().getRegion().getEndLine() - 1;
		int startColumn = n.getLocation().getRegion().getStartColumn() - 1;
		int endColumn = n.getLocation().getRegion().getEndLine() - 1;
		return new Region(startLine, startColumn, endLine, endColumn);
	}

	/**
	 * Returns true if the given CallExpression refers to a function whose Body is not available.
	 *
	 * @param callExpression
	 * @return
	 */
	public static boolean isPhantom(CallExpression callExpression) {
		return callExpression.getInvokes().isEmpty();
	}

	/**
	 * Returns the {@code Region} of a FunctionDeclaration or a Region of (-1, -1, -1, -1).
	 *
	 * Never returns null.
	 *
	 * @param fd
	 * @return
	 */
	@NonNull
	public static Region getRegion(@NonNull Node node) {
		Region region = new Region(-1, -1, -1, -1);
		PhysicalLocation loc = node.getLocation();
		if (loc != null) {
			return loc.getRegion();
		}
		return region;
	}
}
