
package de.fraunhofer.aisec.analysis.utils;

import de.fraunhofer.aisec.cpg.graph.MethodDeclaration;
import de.fraunhofer.aisec.cpg.graph.Node;
import de.fraunhofer.aisec.cpg.graph.RecordDeclaration;
import de.fraunhofer.aisec.cpg.graph.Type;
import de.fraunhofer.aisec.cpg.sarif.Region;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import de.fraunhofer.aisec.mark.markDsl.Parameter;
import de.fraunhofer.aisec.markmodel.Constants;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.toIntExact;

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
	 * Return a unified type String (i.e. changeing cpp-type-separators to Java-type-separators)
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
		if (s.startsWith("'") && s.endsWith("'")) {
			// there should be only a single character here
			s = s.substring(1, s.length() - 1);
		}
		return s;
	}

	public static void dumpVertices(Collection<Vertex> vertices) {
		log.debug("Dumping vertices: {}", vertices.size());

		int i = 0;
		for (Vertex v : vertices) {
			log.debug("Vertex {}: {}", i++, v);
		}
	}

	public static void dumpPaths(Collection<Path> paths) {
		log.debug("Number of paths: {}", paths.size());

		for (Path p : paths) {
			log.debug("Path of length: {}", p.size());
			for (Object o : p) {
				log.debug("Path step: {}", o);
			}
		}
	}

	/**
	 * Returns true if the given vertex has a label that equals to the given CPG class or any of its subclasses.
	 *
	 * That is, hasLabel(v, Node.class) will always return true.
	 *
	 * @param v 			A vertex with a label.
	 * @param cpgClass		Any class from the CPG hierarchy.
	 * @return
	 */
	public static boolean hasLabel(@NonNull Vertex v, @NonNull Class<? extends Node> cpgClass) {
		String label = v.label();
		Set<String> subClasses = Set.of(OverflowDatabase.getSubclasses(cpgClass));
		return label.equals(cpgClass.getSimpleName()) || subClasses.contains(label);
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
		boolean result = false;

		if (markParameter.getVar().equals(Constants.ANY_TYPE)) {
			log.debug("Any of {} is a subtype of {}: true", sourceTypes.stream().map(Type::getTypeName).collect(Collectors.joining(",")), markParameter);
			return true;
		}

		anymatch: for (Type sourceType : sourceTypes) {
			String uniSource = unifyType(sourceType.getTypeName());
			if (uniSource.contains(".") && !uniSource.endsWith(".")) {
				uniSource = uniSource.substring(uniSource.lastIndexOf('.') + 1);
			}
			// TODO Currently, the "type" property may contain modifiers such as "const", so we must remove them here. Will change in CPG.
			if (uniSource.contains(" ") && !uniSource.endsWith(" ")) {
				uniSource = uniSource.substring(uniSource.lastIndexOf(' ') + 1);
			}

			if (markParameter.getTypes().isEmpty()) {
				return true; // match all
			} else {
				for (String type : markParameter.getTypes()) {
					String uniMark = unifyType(type);
					if (uniMark.contains(".") && !uniMark.endsWith(".")) {
						uniMark = uniMark.substring(uniMark.lastIndexOf('.') + 1);
					}

					// We do not consider type hierarchies here but simply match for equality plus a few manual mappings
					result = uniSource.equals(uniMark);
					// There are various representations of "string" and we map them manually here.
					if (uniMark.equals("string")) {
						if (uniSource.equals("QString") || uniSource.equals("string") || uniSource.equals("String") || uniSource.equals("char*")) {
							result = true;
						} else {
							log.trace("comparing string from MARK against {} from Sourcefile. Does currently not match", uniSource);
						}
					}

					// If type could not be determined, we err on the false positive side.
					if (sourceType.getTypeName().equals("UNKNOWN")) {
						result = true;
					}

					// If any of the subtypes match, we can return.
					if (result) {
						break anymatch;
					}
				}
			}
		}

		if (sourceTypes.stream().map(Type::getTypeName).collect(Collectors.joining(",")).trim().equals("")) {
			// TODO Empty "possibleSubTypes" means that the type could not be resolved from source code. This is ok. It is however unclear/undefined how this should be handled if MARK requires a type.
			log.warn("CHECK ME: Cannot compare empty argument type in source code to MARK type {}. Will assume it matches.", markParameter);
			result = true;
		}

		log.debug("Any of {} is a subtype of {}: {}", sourceTypes.stream().map(Type::getTypeName).collect(Collectors.joining(",")), markParameter, result);

		return result;
	}

	public static List<Method> getMethodsAnnotatedWith(final Class<?> type, final Class<? extends Annotation> annotation) {
		final List<Method> methods = new ArrayList<>();
		Class<?> klass = type;
		while (klass != Object.class) { // need to iterated thought hierarchy in order to retrieve methods from above the current instance
			// iterate though the list of methods declared in the class represented by klass variable, and add those annotated with the specified annotation
			final List<Method> allMethods = new ArrayList<>(Arrays.asList(klass.getDeclaredMethods()));
			for (final Method method : allMethods) {
				if (method.isAnnotationPresent(annotation)) {
					// TODO process annotInstance
					//Annotation annotInstance = method.getAnnotation(annotation);
					methods.add(method);
				}
			}
			// move to the upper class in the hierarchy in search for more methods
			klass = klass.getSuperclass();
		}
		return methods;
	}

	/**
	 * Returns a <code>Region</code> object from a vertex' startLine, endLine, startColumn, endColumn property.
	 *
	 * Note that these are not the exact property values but start at 0 rather than by 1.
	 * If these properties do not exist, returns -1.
	 *
	 * @param v
	 * @return
	 */
	@NonNull
	public static Region getRegionByVertex(@NonNull Vertex v) {
		int startLine = toIntExact((Long) v.property("startLine").orElse(Long.valueOf(-1))) - 1;
		int endLine = toIntExact((Long) v.property("endLine").orElse(Long.valueOf(-1))) - 1;
		int startColumn = toIntExact((Long) v.property("startColumn").orElse(Long.valueOf(-1))) - 1;
		int endColumn = toIntExact((Long) v.property("endColumn").orElse(Long.valueOf(-1))) - 1;
		return new Region(startLine, startColumn, endLine, endColumn);
	}
}
