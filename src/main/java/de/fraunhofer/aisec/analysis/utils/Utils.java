
package de.fraunhofer.aisec.analysis.utils;

import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import de.fraunhofer.aisec.cpg.graph.MethodDeclaration;
import de.fraunhofer.aisec.cpg.graph.Node;
import de.fraunhofer.aisec.cpg.graph.RecordDeclaration;
import de.fraunhofer.aisec.cpg.graph.type.PointerType;
import de.fraunhofer.aisec.cpg.graph.type.Type;
import de.fraunhofer.aisec.cpg.graph.type.UnknownType;
import de.fraunhofer.aisec.cpg.sarif.Region;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import de.fraunhofer.aisec.mark.markDsl.Parameter;
import de.fraunhofer.aisec.markmodel.Constants;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static de.fraunhofer.aisec.crymlin.dsl.CrymlinConstants.*;
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
		while (klass != Object.class) { // need to iterated thought hierarchy in order to retrieve methods from above the current instance
			// iterate though the list of methods declared in the class represented by class variable, and add those annotated with the specified annotation
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
		//TODO May return -2, must be -1.
		int startLine = toIntExact((Long) v.property(START_LINE).orElse(Long.valueOf(-1))) - 1;
		int endLine = toIntExact((Long) v.property(END_LINE).orElse(Long.valueOf(-1))) - 1;
		int startColumn = toIntExact((Long) v.property(START_COLUMN).orElse(Long.valueOf(-1))) - 1;
		int endColumn = toIntExact((Long) v.property(END_COLUMN).orElse(Long.valueOf(-1))) - 1;
		return new Region(startLine, startColumn, endLine, endColumn);
	}

	/**
	 * Returns an (unmodifiable) possibly empty list of all types this vertex might have.
	 *
	 * @param next
	 * @return
	 */
	@NonNull
	public static Set<Type> getPossibleSubTypes(@NonNull Vertex next) {

		Set<Type> types = new HashSet<>();
		UnmodifiableIterator<Edge> it = Iterators.filter(next.edges(Direction.OUT, TYPE, POSSIBLE_SUB_TYPES), v -> Utils.hasLabel(v.inVertex(), Type.class));
		it.forEachRemaining(e -> types.add((Type) OverflowDatabase.getInstance().vertexToNode(e.inVertex())));

		return types;
	}

	/**
	 * Returns a brief human-readable representation of a vertex as a string.
	 *
	 * @param base The vertex. If null, this method will return the string "null".
	 * @return A brief representation of the vertex.
	 */
	@NonNull
	public static String prettyPrint(@Nullable Vertex base) {
		if (base == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();
		if (base.property("labels").isPresent()) {
			Object labels = base.property("labels").value();
			String label;
			if (labels instanceof List) {
				label = (String) ((List) labels).get(0);
			} else {
				label = labels.toString();
			}
			sb.append(label);
		}
		sb.append("  [");
		if (base.property("code").isPresent()) {
			sb.append(base.property("code").value());
		}
		sb.append("]");
		return sb.toString();
	}

	public static String prettyPrint(Set<Vertex> responsibleVertices) {
		StringBuilder sb = new StringBuilder();
		Iterator<Vertex> it = responsibleVertices.iterator();
		while (it.hasNext()) {
			Vertex v = it.next();
			sb.append(prettyPrint(v));
			if (it.hasNext()) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}
}
