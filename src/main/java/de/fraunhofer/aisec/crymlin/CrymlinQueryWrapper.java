
package de.fraunhofer.aisec.crymlin;

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.__;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.has;

import de.fraunhofer.aisec.analysis.scp.ConstantResolver;
import de.fraunhofer.aisec.analysis.structures.ConstantValue;
import de.fraunhofer.aisec.analysis.structures.MarkContext;
import de.fraunhofer.aisec.analysis.structures.MarkContextHolder;
import de.fraunhofer.aisec.analysis.structures.Pair;
import de.fraunhofer.aisec.analysis.utils.Utils;
import de.fraunhofer.aisec.cpg.graph.*;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import de.fraunhofer.aisec.crymlin.dsl.__;
import de.fraunhofer.aisec.mark.markDsl.OpStatement;
import de.fraunhofer.aisec.markmodel.Constants;

import java.util.*;
import java.util.regex.Pattern;

import de.fraunhofer.aisec.markmodel.MEntity;
import de.fraunhofer.aisec.markmodel.MOp;
import de.fraunhofer.aisec.analysis.structures.CPGVertexWithValue;
import de.fraunhofer.aisec.markmodel.MRule;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.emf.common.util.EList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.has;

public class CrymlinQueryWrapper {

	private static final Logger log = LoggerFactory.getLogger(CrymlinQueryWrapper.class);

	// do not instantiate
	private CrymlinQueryWrapper() {
	}

	/**
	 * Returns a set of vertices with label <code>ConstructorExpression</code>.
	 * <p>
	 * More precisely, the returned vertices are the targets of "INITIALIZER" edges from a "VariableDeclaration" node, whereas: a) the VariableDeclaration must refer to
	 * the type given by <code>className</code>. b) the arguments of the initializer must comply with the types given by <code>parameterTypes</code>, if not null.
	 *
	 * @param crymlinTraversal
	 * @param className
	 * @param parameterTypes
	 * @return
	 */
	public static Set<Vertex> getInitializers(
			@NonNull CrymlinTraversalSource crymlinTraversal,
			@NonNull String className,
			@Nullable List<String> parameterTypes) {
		//FIXME Parameter types are still ignored. If not null, they should be checked against arguments of Initializer.
		Set<Vertex> result = new HashSet<>();
		Set<Object> constructorExpressionVertices = crymlinTraversal.V()
				.as("constructorExpression")
				.inE("INITIALIZER")
				.outV()
				.hasLabel(VariableDeclaration.class.getSimpleName())
				.has("type", Utils.unifyType(className))
				.select("constructorExpression")
				.toSet();
		// Need to do this manually, as we cannot cast the generic from <Object> to <Vertex>.
		for (Object v : constructorExpressionVertices) {
			result.add((Vertex) v);
		}
		return result;
	}

	/**
	 * Returns a set of Vertices representing the <code>CallExpression</code> to a target function.
	 * <p>
	 * Note that this set does not contain calls to constructors.
	 *
	 * @param crymlinTraversal
	 * @param fqnClassName fully qualified name w/o function name itself
	 * @param functionName name of the function
	 * @param markEntity type of the object used to call the function (i.e. method); should be name of the MARK entity
	 * @param parameterTypes list of parameter types; must appear in order (i.e. index 0 = type of first parameter, etc.); currently, types must be precise (i.e. with
	 *        qualifiers, pointer, reference)
	 * @return
	 */
	@NonNull
	public static Set<Vertex> getCalls(
			@NonNull CrymlinTraversalSource crymlinTraversal,
			@NonNull String fqnClassName,
			@NonNull String functionName,
			@Nullable String markEntity,
			@NonNull List<String> parameterTypes) {

		Set<Vertex> ret = new HashSet<>();

		// reconstruct what type of call we're expecting
		boolean isMethod = markEntity != null && fqnClassName.endsWith(markEntity);

		if (isMethod) {
			// it's a method call on an instances
			ret.addAll(crymlinTraversal.calls(functionName, markEntity)
					.toList());
		}
		List<Vertex> calls = crymlinTraversal.calls(functionName, markEntity)
				.toList();

		// it's a function OR a static method call -> name == fqnClassName.functionName
		ret.addAll(crymlinTraversal.calls(fqnClassName + "." + functionName)
				.toList());

		// FIXME we're not setting the default (i.e. global) namespace
		if (fqnClassName.length() == 0) {
			ret.addAll(crymlinTraversal.calls(functionName)
					.toList());
		}

		// now, ret contains possible candidates --> need to filter out calls where params don't match
		ret.removeIf(
			v -> {
				Iterator<Edge> referencedArguments = v.edges(Direction.OUT, "ARGUMENTS");

				if (parameterTypes.isEmpty() && referencedArguments.hasNext()) {
					// expecting no arguments but got at least one -> remove
					return true;
				}

				if (!parameterTypes.isEmpty() && !referencedArguments.hasNext()) {
					// expecting some parameters but got no arguments -> remove
					return true;
				}

				while (referencedArguments.hasNext()) {
					// check each argument against parameter type
					Vertex argument = referencedArguments.next()
							.inVertex();
					long argumentIndex = argument.value("argumentIndex");

					if (argumentIndex >= parameterTypes.size()) {
						// last given parameter type must be "..." or remove
						return !Constants.ELLIPSIS.equals(parameterTypes.get(parameterTypes.size() - 1));
					} else { // remove if types don't match
						// Use "unified" types for Java and C/C++
						String paramType = Utils.unifyType(parameterTypes.get((int) argumentIndex));
						if (!(Constants.UNDERSCORE.equals(paramType)
								|| Constants.ELLIPSIS.equals(paramType))) {
							// it's not a single type wild card -> types must match
							// TODO improve type matching
							// currently, we check for perfect match but we may need to be more fuzzy e.g.
							// ignore
							// type qualifier (e.g. const) or strip reference types
							// FIXME match expects fully-qualified type literal; in namespace 'std',
							// 'std::string' becomes just 'string'
							// FIXME string literals in C++ have type 'const char[{some integer}]' instead of
							// 'std::string'
							if (paramType.equals("std.string")) {
								String argValue = argument.<String> property("type").orElse("");

								if (paramType.equals(argValue)
										|| Pattern.matches("const char\\s*\\[\\d*\\]", argValue)) {
									// it matches C++ string types
									return false;
								}
							} else if (!paramType.equals(argument.value("type"))) {
								// types don't match -> remove
								return true;
							}
						}
					}
				}
				return false;
			});

		return ret;
	}

	public static List<Vertex> lhsVariableOfAssignment(CrymlinTraversalSource crymlin, long id) {
		return crymlin.byID(id)
				.in("RHS")
				.where(
					has(T.label, LabelP.of(BinaryOperator.class.getSimpleName())).and()
							.has("operatorCode", "="))
				.out("LHS")
				.has(T.label,
					LabelP.of(VariableDeclaration.class.getSimpleName()))
				.toList();
	}

	/**
	 * Returns true if the given vertex represents a CallExpression or a subclass thereof.
	 *
	 * @param v
	 * @return
	 */
	public static boolean isCallExpression(Vertex v) {
		if (v.label()
				.equals(CallExpression.class.getSimpleName())) {
			return true;
		}

		for (String subClass : OverflowDatabase.getSubclasses(CallExpression.class)) {
			if (v.label()
					.equals(subClass)) {
				return true;
			}
		}
		return false;
	}

	public static List<Vertex> getNextStatements(CrymlinTraversalSource crymlin, long id) {
		// TODO Needs testing for branches
		return crymlin.byID(id)
				.repeat(__().out("EOG")
						.simplePath())
				.until(__().inE("STATEMENTS"))
				.toList();
	}

	/**
	 * TODO JS->FW: Write a comment here, describing what vertices this method returns for given operand. Maybe give an example.
	 *
	 * @param operand
	 * @param rule
	 * @param crymlin
	 * @return
	 */
	public static List<Vertex> getMatchingVertices(@NonNull String operand, @NonNull MRule rule, @NonNull CrymlinTraversalSource crymlin) {
		final List<Vertex> matchingVertices = new ArrayList<>();

		if (StringUtils.countMatches(operand, ".") != 1) {
			// "." separates the mark var name and the mark var object (e.g. cm.algorithm)
			log.error("operand contains more than one '.' which is not supported.");
			return matchingVertices;
		}

		// Split operand "myInstance.attribute" into "myInstance" and "attribute".
		final String[] operandParts = operand.split("\\.");
		final String instance = operandParts[0];
		final String attribute = operandParts[1];

		// Get the MARK entity corresponding to the operator's instance.
		Pair<String, MEntity> ref = rule.getEntityReferences()
				.get(instance);
		String entityName = ref.getValue0();
		MEntity referencedEntity = ref.getValue1();

		if (referencedEntity == null) {
			log.warn("Unexpected: rule {} without referenced entity for instance {}", rule.getName(), instance);
			return matchingVertices;
		}

		List<Pair<MOp, Set<OpStatement>>> usesAsVar = new ArrayList<>();
		List<Pair<MOp, Set<OpStatement>>> usesAsFunctionArgs = new ArrayList<>();

		// Collect *variables* assigned in Ops of this entity and *arguments* used in Ops.
		for (MOp operation : referencedEntity.getOps()) {
			Set<OpStatement> vars = new HashSet<>();
			Set<OpStatement> args = new HashSet<>();

			// Iterate over all statements of that op
			for (OpStatement opStmt : operation.getStatements()) {
				// simple assignment, i.e. "var = something()"
				if (attribute.equals(opStmt.getVar())) {
					vars.add(opStmt);
				}
				// Function parameter, i.e. "something(..., var, ...)"
				if (opStmt.getCall()
						.getParams()
						.stream()
						.anyMatch(p -> p.equals(attribute))) {
					args.add(opStmt);
				}
			}

			if (!vars.isEmpty()) {
				usesAsVar.add(new Pair<>(operation, vars));
			}

			if (!args.isEmpty()) {
				usesAsFunctionArgs.add(new Pair<>(operation, args));
			}
		}

		for (Pair<MOp, Set<OpStatement>> p : usesAsVar) {
			for (OpStatement opstmt : p.getValue1()) {

				String fqFunctionName = opstmt.getCall()
						.getName();

				String functionName = Utils.extractMethodName(fqFunctionName);
				String fqNamePart = Utils.extractType(fqFunctionName);

				List<String> functionArgumentTypes = referencedEntity.replaceArgumentVarsWithTypes(opstmt.getCall()
						.getParams());

				Set<Vertex> vertices = CrymlinQueryWrapper.getCalls(
					crymlin, fqNamePart, functionName, null, functionArgumentTypes);

				for (Vertex v : vertices) {
					// check if there was an assignment

					// todo: move this to crymlintraversal. For some reason, the .toList() blocks if the
					// step is in the crymlin traversal
					List<Vertex> nextVertices = CrymlinQueryWrapper.lhsVariableOfAssignment(crymlin, (long) v.id());

					if (!nextVertices.isEmpty()) {
						log.info("found RHS traversals: {}", nextVertices);
						matchingVertices.addAll(nextVertices);
					}

					// check if there was a direct initialization (i.e., int i = call(foo);)
					nextVertices = crymlin.byID((long) v.id())
							.initializerVariable()
							.toList();

					if (!nextVertices.isEmpty()) {
						log.info("found Initializer traversals: {}", nextVertices);
						matchingVertices.addAll(nextVertices);
					}
				}
			}
		}

		for (Pair<MOp, Set<OpStatement>> p : usesAsFunctionArgs) {
			for (OpStatement opstmt : p.getValue1()) {

				String fqFunctionName = opstmt.getCall()
						.getName();
				String functionName = Utils.extractMethodName(fqFunctionName);
				String fqName = Utils.extractType(fqFunctionName);
				if (fqName.equals(functionName)) {
					fqName = "";
				}

				EList<String> params = opstmt.getCall()
						.getParams();
				List<String> argumentTypes = referencedEntity.replaceArgumentVarsWithTypes(params);
				OptionalInt argumentIndexOptional = IntStream.range(0, params.size())
						.filter(i -> attribute.equals(params.get(i)))
						.findFirst();
				if (argumentIndexOptional.isEmpty()) {
					log.error("argument not found in parameters. This should not happen");
					continue;
				}
				int argumentIndex = argumentIndexOptional.getAsInt();

				Set<Vertex> vertices = CrymlinQueryWrapper.getCalls(
					crymlin, fqName, functionName, entityName, argumentTypes);

				vertices.addAll(CrymlinQueryWrapper.getInitializers(crymlin, fqFunctionName, argumentTypes));
				for (Vertex v : vertices) {
					List<Vertex> argumentVertices = crymlin.byID((long) v.id())
							.argument(argumentIndex)
							.toList();

					if (argumentVertices.size() == 1) {
						matchingVertices.add(argumentVertices.get(0));
					} else {
						log.warn("Did not find one matching argument node, got {}", argumentVertices.size());
					}
				}
			}
		}

		return matchingVertices;
	}

	public static List<CPGVertexWithValue> getAssignmentsForVertices(List<Vertex> vertices) {
		final List<CPGVertexWithValue> ret = new ArrayList<>();
		// try to resolve them
		for (Vertex v : vertices) {
			Iterator<Edge> refersTo = v.edges(Direction.OUT, "REFERS_TO");
			if (refersTo.hasNext()) {
				Edge next = refersTo.next();
				// vertices (SHOULD ONLY BE ONE) representing a variable declaration for the
				// argument we're using in the function call
				Vertex variableDeclarationVertex = next.inVertex();
				Declaration variableDeclaration = (Declaration) OverflowDatabase.getInstance()
						.vertexToNode(variableDeclarationVertex);
				ConstantResolver cResolver = new ConstantResolver(TraversalConnection.Type.OVERFLOWDB);
				Optional<ConstantValue> constantValue = cResolver.resolveConstantValueOfFunctionArgument(variableDeclaration, v);
				// fixme what if we do not know the value, shouldnt we then still return the argumentvertex?
				if (constantValue.isPresent()) {
					CPGVertexWithValue mva = new CPGVertexWithValue(v, constantValue.get());
					ret.add(mva);
				} else {
					log.warn("Could not constant resolve node {}", v.id());
				}
			}
		}
		return ret;
	}

	public static Set<Vertex> getVerticesForFunctionDeclaration(
			de.fraunhofer.aisec.mark.markDsl.FunctionDeclaration functionDeclaration,
			MEntity ent,
			CrymlinTraversalSource crymlinTraversal) {
		String functionName = Utils.extractMethodName(functionDeclaration.getName());
		String baseType = Utils.extractType(functionDeclaration.getName());

		// resolve parameters which have a corresponding var part in the entity
		List<String> args = ent.replaceArgumentVarsWithTypes(functionDeclaration.getParams());
		Set<Vertex> initializers = CrymlinQueryWrapper.getInitializers(crymlinTraversal, Utils.unifyType(functionDeclaration.getName()), args);
		Set<Vertex> calls = CrymlinQueryWrapper.getCalls(crymlinTraversal, baseType, functionName, null, args);

		Set<Vertex> callsAndInitializers = new HashSet<>();
		callsAndInitializers.addAll(calls);
		callsAndInitializers.addAll(initializers);
		return callsAndInitializers;
	}

	/**
	 * Given a Vertex v, try to find the function or method in which v is contained.
	 * <p>
	 * The resulting Vertex will be of type FunctionDeclaration or MethodDeclaration.
	 * <p>
	 * If v is not contained in a function, this method returns an empty Optional.
	 *
	 * @param v
	 * @param crymlinTraversal
	 * @return
	 */
	public static Optional<Vertex> getContainingFunction(Vertex v, CrymlinTraversalSource crymlinTraversal) {
		return crymlinTraversal.byID((long) v.id())
				.repeat(__.__()
						.inE()
						.has("sub-graph", "AST")
						.outV())
				.until(__.__()
						.or(
							__.__()
									.hasLabel(FunctionDeclaration.class.getSimpleName()),
							__.__()
									.hasLabel(MethodDeclaration.class.getSimpleName()))) // FIXME can also be MethoDeclaration
				.tryNext();
	}

	/**
	 * Given a vertex that represents a <code>CallExpression</code>, return the base(s) that this call expression uses.
	 *
	 * The result will be either an Optional.empty() in case of static method calls or function calls, or contain a single element.
	 *
	 * @param callExpression
	 * @return
	 */
	@NonNull
	public static Optional<Vertex> getBaseOfCallExpression(@NonNull Vertex callExpression) {
		Optional<Vertex> base = Optional.empty();
		Iterator<Edge> it = callExpression.edges(Direction.OUT, "BASE");
		Vertex ref = null;
		if (it.hasNext()) {
			Vertex baseVertex = it.next().inVertex();
			Iterator<Edge> refIterator = baseVertex.edges(Direction.OUT, "REFERS_TO");
			if (refIterator.hasNext()) {
				// if the node refers to another node, return the node it refers to
				ref = refIterator.next().inVertex();
				base = Optional.of(ref);
			} else {
				base = Optional.of(baseVertex);
			}
		}
		return base;
	}

	public static Optional<Vertex> getBaseOfCallOfArgumentExpression(@NonNull Vertex expr) {
		Optional<Vertex> base = Optional.empty();
		Iterator<Edge> refIterator = expr.edges(Direction.IN, "ARGUMENTS");
		if (refIterator.hasNext()) {
			base = getBaseOfCallExpression(refIterator.next().outVertex());
		}
		return base;
	}

	public static Optional<Vertex> getAssigneeOfConstructExpression(Vertex vertex) {
		Iterator<Edge> it = vertex.edges(Direction.IN, "INITIALIZER");
		if (it.hasNext()) {
			Vertex variableDeclaration = it.next().outVertex();
			if (!VariableDeclaration.class.getSimpleName().equals(variableDeclaration.label())) {
				log.warn("Unexpected: Source of INITIALIZER edge to ConstructExpression is not a VariableDeclaration. Trying to continue anyway");
			}
			return Optional.of(variableDeclaration);
		}
		return Optional.empty();
	}

	public static HashMap<Integer, List<CPGVertexWithValue>> resolveOperand(MarkContextHolder context, @NonNull String operand, @NonNull MRule rule,
			@NonNull CrymlinTraversalSource crymlin) {

		// first get all vertices for the operand
		List<Vertex> matchingVertices = CrymlinQueryWrapper.getMatchingVertices(operand, rule, crymlin);

		List<CPGVertexWithValue> vertices = new ArrayList<>();

		// The arguments themselves may be constants ("Literal"). In that case, add the value.
		for (Vertex v : matchingVertices) {
			if (v.label().equals(Literal.class.getSimpleName())) {
				vertices.add(new CPGVertexWithValue(v, ConstantValue.of(v.property("value").value())));
			}
		}

		// Use Constant resolver to resolve assignments to arguments
		vertices.addAll(CrymlinQueryWrapper.getAssignmentsForVertices(matchingVertices));

		// now split them up to belong to each instance

		final String instance = operand.split("\\.")[0];

		// precompute a list mapping
		// from a nodeID (representing the varabledecl for the instance)
		// to a List of contexts where this base is referenced
		HashMap<Long, List<Integer>> nodeIDToContextIDs = new HashMap<>();
		for (Map.Entry<Integer, MarkContext> entry : context.getAllContexts().entrySet()) {
			Vertex opInstance = entry.getValue().getInstanceContext().getVertex(instance);
			if (opInstance == null) {
				log.warn("Instance not found in context");
			} else {
				List<Integer> contextIDs = nodeIDToContextIDs.computeIfAbsent((Long) opInstance.id(), x -> new ArrayList<>());
				contextIDs.add(entry.getKey());
			}
		}

		// now calculate a list of contextID to matching vertices which fill the base we are looking for
		HashMap<Integer, List<CPGVertexWithValue>> verticesPerContext = new HashMap<>();
		for (CPGVertexWithValue vertexWithValue : vertices) {
			Optional<Vertex> base = getBaseOfCallOfArgumentExpression(vertexWithValue.getArgumentVertex());
			if (base.isPresent()) {
				List<Integer> contextIDs = nodeIDToContextIDs.get((Long) base.get().id());
				if (contextIDs == null) {
					log.warn("Base not found in any context. Following expressionevaluation will be incomplete");
				} else {
					for (Integer c : contextIDs) {
						List<CPGVertexWithValue> verts = verticesPerContext.computeIfAbsent(c, x -> new ArrayList<>());
						verts.add(vertexWithValue);
					}
				}
			} else {
				log.warn("Base not found for argument. Following expressionevaluation will be incomplete");
			}
		}

		return verticesPerContext;
	}
}
