
package de.fraunhofer.aisec.crymlin;

import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import de.fraunhofer.aisec.analysis.scp.SimpleConstantResolver;
import de.fraunhofer.aisec.analysis.structures.CPGVertexWithValue;
import de.fraunhofer.aisec.analysis.structures.ConstantValue;
import de.fraunhofer.aisec.analysis.structures.ErrorValue;
import de.fraunhofer.aisec.analysis.structures.MarkContext;
import de.fraunhofer.aisec.analysis.structures.MarkContextHolder;
import de.fraunhofer.aisec.analysis.structures.Pair;
import de.fraunhofer.aisec.analysis.utils.Utils;
import de.fraunhofer.aisec.cpg.graph.BinaryOperator;
import de.fraunhofer.aisec.cpg.graph.CallExpression;
import de.fraunhofer.aisec.cpg.graph.ConstructExpression;
import de.fraunhofer.aisec.cpg.graph.DeclaredReferenceExpression;
import de.fraunhofer.aisec.cpg.graph.FunctionDeclaration;
import de.fraunhofer.aisec.cpg.graph.Literal;
import de.fraunhofer.aisec.cpg.graph.MemberExpression;
import de.fraunhofer.aisec.cpg.graph.MethodDeclaration;
import de.fraunhofer.aisec.cpg.graph.NewExpression;
import de.fraunhofer.aisec.cpg.graph.Node;
import de.fraunhofer.aisec.cpg.graph.ValueDeclaration;
import de.fraunhofer.aisec.cpg.graph.VariableDeclaration;
import de.fraunhofer.aisec.cpg.graph.type.Type;
import de.fraunhofer.aisec.crymlin.connectors.db.Database;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import de.fraunhofer.aisec.mark.markDsl.OpStatement;
import de.fraunhofer.aisec.mark.markDsl.Parameter;
import de.fraunhofer.aisec.markmodel.Constants;
import de.fraunhofer.aisec.markmodel.MEntity;
import de.fraunhofer.aisec.markmodel.MOp;
import de.fraunhofer.aisec.markmodel.MRule;
import de.fraunhofer.aisec.markmodel.MVar;
import de.fraunhofer.aisec.markmodel.Mark;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.emf.common.util.EList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.fraunhofer.aisec.crymlin.dsl.CrymlinConstants.ARGUMENTS;
import static de.fraunhofer.aisec.crymlin.dsl.CrymlinConstants.DFG;
import static de.fraunhofer.aisec.crymlin.dsl.CrymlinConstants.EOG;
import static de.fraunhofer.aisec.crymlin.dsl.CrymlinConstants.FIELDS;
import static de.fraunhofer.aisec.crymlin.dsl.CrymlinConstants.INITIALIZER;
import static de.fraunhofer.aisec.crymlin.dsl.CrymlinConstants.POSSIBLE_SUB_TYPES;
import static de.fraunhofer.aisec.crymlin.dsl.CrymlinConstants.REFERS_TO;
import static de.fraunhofer.aisec.crymlin.dsl.CrymlinConstants.TYPE;
import static de.fraunhofer.aisec.crymlin.dsl.__.hasLabel;
import static de.fraunhofer.aisec.crymlin.dsl.__.or;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.has;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.inE;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.out;

public class CrymlinQueryWrapper {

	private static final Logger log = LoggerFactory.getLogger(CrymlinQueryWrapper.class);

	// do not instantiate
	private CrymlinQueryWrapper() {

	}

	/**
	 * Returns a set of Vertices representing the <code>ConstructExpression</code> to an initialized type.
	 * <p>
	 *
	 * @param crymlinTraversal
	 * @param fqnName          fully qualified name
	 * @param parameters       list of parameter types; must appear in order (i.e. index 0 = type of first parameter, etc.); currently, types must be precise (i.e. with
	 *                         qualifiers, pointer, reference)
	 * @return
	 */
	@NonNull
	private static Set<Vertex> getCtors(
			@NonNull Database<Node> db,
			@NonNull CrymlinTraversalSource crymlinTraversal,
			@NonNull String fqnName,
			EList<Parameter> parameters) {

		fqnName = Utils.unifyType(fqnName);

		// In case of constructors, "functionName" holds the name of the constructed type.
		Set<Vertex> ret = new HashSet<>(crymlinTraversal.ctors(fqnName).toSet());

		// now, ret contains possible candidates --> need to filter out calls where params don't match
		ret.removeIf(
			v -> {
				// ConstructExpression needs a special treatment because the argument of a ConstructExpression is the CallExpression to the constructor and we are interested in its arguments.
				if (Utils.hasLabel(v, ConstructExpression.class)) {
					List<Vertex> args = getArguments(v);
					if (args.size() == 1 && Utils.hasLabel(args.get(0), CallExpression.class)) {
						return !argumentsMatchParameters(db, parameters, getArguments(args.get(0)));
					}
				}

				return !argumentsMatchParameters(db, parameters, getArguments(v));
			});

		return ret;
	}

	public static List<Vertex> getArguments(@NonNull Vertex v) {
		Iterator<Edge> referencedArguments = v.edges(Direction.OUT, ARGUMENTS);

		// Collect all argument Vertices into a list
		List<Vertex> arguments = new ArrayList<>();
		while (referencedArguments.hasNext()) {
			Edge e = referencedArguments.next();
			arguments.add(e.inVertex());
		}
		return arguments;
	}

	/**
	 * Returns a set of Vertices representing the <code>CallExpression</code> to a target function
	 * <p>
	 *
	 * @param crymlinTraversal
	 * @param fqnName          fully qualified name
	 * @param parameters       list of parameter types; must appear in order (i.e. index 0 = type of first parameter, etc.); currently, types must be precise (i.e. with
	 *                         qualifiers, pointer, reference)
	 * @return
	 */
	@NonNull
	private static Set<Vertex> getCalls(
			@NonNull Database<Node> db,
			@NonNull CrymlinTraversalSource crymlinTraversal,
			@NonNull String fqnName,
			EList<Parameter> parameters) {

		fqnName = Utils.unifyType(fqnName);

		// since fqn is a fully qualified name, this includes method calls on an instance, static calls, and functioncalls
		Set<Vertex> ret = new HashSet<>(crymlinTraversal.callsFqn(fqnName).toSet());

		// now, ret contains possible candidates --> need to filter out calls where params don't match
		ret.removeIf(
			v -> {
				Iterator<Edge> referencedArguments = v.edges(Direction.OUT, ARGUMENTS);

				// Collect all argument Vertices into a list
				List<Vertex> arguments = new ArrayList<>();
				while (referencedArguments.hasNext()) {
					Edge e = referencedArguments.next();
					arguments.add(e.inVertex());
				}

				return !argumentsMatchParameters(db, parameters, arguments);
			});

		return ret;
	}

	/**
	 * Returns true if the given list of arguments (of a function or method or constructor call) matches the given list of parameters in MARK.
	 *
	 * @param markParameters  List of types.
	 * @param sourceArguments List of Vertices. Each Vertex is expected to have an "argumentIndex" property.
	 * @return
	 */
	private static boolean argumentsMatchParameters(@NonNull Database<Node> db, EList<Parameter> markParameters, @NonNull List<Vertex> sourceArguments) {
		int i = 0;

		while (i < markParameters.size() && i < sourceArguments.size()) {
			Parameter markParam = markParameters.get(i);

			Set<Type> sourceArgs = new HashSet<>();
			/* We cannot assume that the position in sourceArgument corresponds with the actual order. Must rather check "argumentIndex" property. */
			for (Vertex vArg : sourceArguments) {
				long sourceArgPos = (long) vArg.property("argumentIndex")
						.orElse(-1);
				if (sourceArgPos == i) {
					sourceArgs.addAll(getPossibleSubTypes(db, vArg));
				}
			}

			if (sourceArgs.isEmpty()) {
				log.error("Cannot compare function arguments to MARK parameters. Unexpectedly null element or no argument types: {}",
					String.join(", ", MOp.paramsToString(markParameters)));
				return false;
			}

			if (Constants.ELLIPSIS.equals(markParam.getVar())) {
				return true;
			}

			// UNDERSCORE means we do not care about this specific argument at all
			if (Constants.UNDERSCORE.equals(markParam.getVar())) {
				i++;
				continue;
			}

			// ANY_TYPE means we have a MARK variable, but do not care about its type
			if (Constants.ANY_TYPE.equals(markParam.getVar())) {
				i++;
				continue;
			}

			if (!Utils.isSubTypeOf(sourceArgs, markParam)) {
				return false;
			}

			i++;
		}

		// If parameter list ends with an ELLIPSIS, we ignore the remaining arguments
		boolean endsWithEllipsis = false;
		if (i < markParameters.size()) {
			List<Parameter> sublist = markParameters.subList(i, markParameters.size());
			endsWithEllipsis = sublist.stream().allMatch(markParm -> Constants.ELLIPSIS.equals(markParm.getVar()));
		}

		return (i == markParameters.size() || endsWithEllipsis) && i == sourceArguments.size();
	}

	private static List<Vertex> lhsVariableOfAssignment(CrymlinTraversalSource crymlin, long id) {
		return crymlin.byID(id)
				.in("RHS")
				.where(
					has(T.label, LabelP.of(BinaryOperator.class.getSimpleName())).and()
							.has("operatorCode", "="))
				.out("LHS")
				.hasLabel(
					VariableDeclaration.class.getSimpleName(),
					DeclaredReferenceExpression.class.getSimpleName())
				.toList();
	}

	/**
	 * Returns true if the given vertex represents a CallExpression or a subclass thereof.
	 *
	 * @param v
	 * @return
	 */
	public static boolean isCallExpression(@NonNull Database<Node> db, Vertex v) {
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
				.repeat(out("EOG")
						.simplePath())
				.until(inE("STATEMENTS"))
				.toList();
	}

	/**
	 * Returns a List of Vertices with label "DeclaredReferenceExpression" that correspond to a given MARK variable in a given rule.
	 *
	 * @param markVar   The MARK variable.
	 * @param rule      The MARK rule using the MARK variable.
	 * @param markModel The current MARK model.
	 * @param crymlin   A CrymlinTraversalSource for querying the CPG.
	 * @return List of Vertex objects whose label is "DeclaredReferenceExpression".
	 */
	public static List<CPGVertexWithValue> getMatchingVertices(@NonNull Database<Node> db, @NonNull String markVar, @NonNull MRule rule, Mark markModel,
			@NonNull CrymlinTraversalSource crymlin) {
		final List<CPGVertexWithValue> matchingVertices = new ArrayList<>();
		// Split MARK variable "myInstance.attribute" into "myInstance" and "attribute".
		final String[] markVarParts = markVar.split("\\.");
		String instance = markVarParts[0];
		String attribute = markVarParts[1];

		// Get the MARK entity corresponding to the MARK instance variable.
		Pair<String, MEntity> ref = rule.getEntityReferences().get(instance);
		if (ref == null || ref.getValue1() == null) {
			log.warn("Unexpected: rule {} without referenced entity for instance {}", rule.getName(), instance);
			return matchingVertices;
		}
		MEntity referencedEntity = ref.getValue1();

		if (StringUtils.countMatches(markVar, ".") > 1) {
			log.info("{} References an entity inside an entity", markVar);
			for (int i = 1; i < markVarParts.length - 1; i++) {
				instance += "." + markVarParts[i];
				attribute = markVarParts[i + 1];

				// sanity-checking the references entity
				MVar match = null;
				for (MVar var : referencedEntity.getVars()) {
					if (var.getName().equals(markVarParts[i])) {
						match = var;
						break;
					}
				}
				if (match == null) {
					log.warn("Entity does not contain var {}", markVarParts[i]);
					return matchingVertices;
				}
				referencedEntity = markModel.getEntity(match.getType());
				if (referencedEntity == null) {
					log.warn("No Entity with name {} found", match.getType());
					return matchingVertices;
				}
			}
		}
		String finalAttribute = attribute;

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
						.anyMatch(p -> p.getVar().equals(finalAttribute))) {
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

		// get vertices for all usesAsVar (i.e., simple assignment, i.e. "var = something()")
		for (Pair<MOp, Set<OpStatement>> p : usesAsVar) {
			if (p.getValue1() == null) {
				log.warn("Unexpected: Null value for usesAsFunctionArg {}", p.getValue0());
				continue;
			}
			for (OpStatement opstmt : p.getValue1()) {

				String fqFunctionName = opstmt.getCall().getName();

				Set<Vertex> vertices = CrymlinQueryWrapper.getCalls(db, crymlin, fqFunctionName, opstmt.getCall().getParams());
				vertices.addAll(CrymlinQueryWrapper.getCtors(db, crymlin, fqFunctionName, opstmt.getCall().getParams()));

				for (Vertex v : vertices) {
					// precalculate base
					Optional<Vertex> baseOfCallExpression = CrymlinQueryWrapper.getBaseOfCallExpression(v);

					boolean foundTargetVertex = false;

					// check if there was an assignment (i.e., i = call(foo);)
					List<Vertex> varDeclarations = CrymlinQueryWrapper.lhsVariableOfAssignment(crymlin, (long) v.id());
					if (!varDeclarations.isEmpty()) {
						foundTargetVertex = true;
						log.info("found assignment: {}", varDeclarations);
						varDeclarations.forEach(vertex -> {
							CPGVertexWithValue cpgVertexWithValue = new CPGVertexWithValue(vertex, ConstantValue.newUninitialized());
							cpgVertexWithValue.setBase(baseOfCallExpression.orElse(null));
							matchingVertices.add(cpgVertexWithValue);
						});
					}

					// check if there was a direct initialization (i.e., int i = call(foo);)
					varDeclarations = crymlin.byID((long) v.id())
							.initializerVariable()
							.toList();
					if (!varDeclarations.isEmpty()) {
						foundTargetVertex = true;
						log.info("found direct initialization: {}", varDeclarations);
						varDeclarations.forEach(vertex -> {
							CPGVertexWithValue cpgVertexWithValue = new CPGVertexWithValue(vertex, ConstantValue.newUninitialized());
							cpgVertexWithValue.setBase(baseOfCallExpression.orElse(null));
							matchingVertices.add(cpgVertexWithValue);
						});
					}

					if (!foundTargetVertex) { // this can be a directly used return value from a call
						CPGVertexWithValue cpgVertexWithValue = new CPGVertexWithValue(v, ConstantValue.newUninitialized());
						cpgVertexWithValue.setBase(baseOfCallExpression.orElse(null));
						matchingVertices.add(cpgVertexWithValue);
					}
				}
			}
		}

		// get vertices for all usesAsFunctionArgs (i.e., Function parameter, i.e. "something(..., var, ...)")
		for (Pair<MOp, Set<OpStatement>> p : usesAsFunctionArgs) {
			if (p.getValue1() == null) {
				log.warn("Unexpected: Null value for usesAsFunctionArg {}", p.getValue0());
				continue;
			}
			for (OpStatement opstmt : p.getValue1()) { // opstatement is one possible method call/ctor inside an op

				String fqFunctionName = opstmt.getCall().getName();

				EList<Parameter> params = opstmt.getCall().getParams();
				int[] paramPositions = IntStream.range(0, params.size()).filter(i -> finalAttribute.equals(params.get(i).getVar())).toArray();
				if (paramPositions.length > 1) {
					log.warn("Invalid op signature: MarkVar is referenced more than once. Only the first one will be used.");
				}
				if (paramPositions.length == 0) {
					log.error("argument not found in parameters. This should not happen");
					continue;
				}
				int argumentIndex = paramPositions[0];

				log.debug("Checking for call/ctor. fqname: {} - markParams: {}", fqFunctionName, String.join(", ", MOp.paramsToString(params)));

				for (Vertex v : CrymlinQueryWrapper.getCalls(db, crymlin, fqFunctionName, params)) {
					List<Vertex> argumentVertices = crymlin.byID((long) v.id())
							.argument(argumentIndex)
							.toList();

					if (argumentVertices.size() == 1) {
						// get base of call expression
						Optional<Vertex> baseOfCallExpression;
						if (v.property("nodeType").isPresent() && v.value("nodeType").equals("de.fraunhofer.aisec.cpg.graph.StaticCallExpression")) {
							baseOfCallExpression = CrymlinQueryWrapper.getDFGTarget(v);
						} else {
							baseOfCallExpression = CrymlinQueryWrapper.getBaseOfCallExpression(v);
							if (baseOfCallExpression.isEmpty()) { // if we did not find a base the "easy way", try to find a base using the simple-DFG
								baseOfCallExpression = CrymlinQueryWrapper.getDFGTarget(v);
							}
						}
						CPGVertexWithValue cpgVertexWithValue = new CPGVertexWithValue(argumentVertices.get(0), ConstantValue.newUninitialized());
						cpgVertexWithValue.setBase(baseOfCallExpression.orElse(null));
						matchingVertices.add(cpgVertexWithValue);
					} else {
						log.warn("multiple arguments for function {} have the same argument_id. Invalid cpg.", fqFunctionName);
					}
				}

				for (Vertex v : CrymlinQueryWrapper.getCtors(db, crymlin, fqFunctionName, params)) {
					List<Vertex> argumentVertices = crymlin.byID((long) v.id())
							.argument(argumentIndex)
							.toList();

					if (argumentVertices.size() == 1) {
						// get base of initializer for ctor
						Optional<Vertex> baseOfCallExpression = CrymlinQueryWrapper.getBaseOfInitializerArgument(argumentVertices.get(0));
						CPGVertexWithValue cpgVertexWithValue = new CPGVertexWithValue(argumentVertices.get(0), ConstantValue.newUninitialized());
						cpgVertexWithValue.setBase(baseOfCallExpression.orElse(null));
						matchingVertices.add(cpgVertexWithValue);
					} else {
						log.warn("multiple arguments for function {} have the same argument_id. Invalid cpg.", fqFunctionName);
					}
				}
			}
		}
		log.debug("GETMATCHINGVERTICES for {} returns {}",
			markVar,
			matchingVertices.stream()
					.map(v -> v.getArgumentVertex().label() + ": " + v.getArgumentVertex().property("code").value())
					.collect(Collectors.joining(", ")));
		return matchingVertices;
	}

	/**
	 * Given a MARK variable and a list of vertices, attempts to find constant values that would be assigned to these variables at runtime.
	 * <p>
	 * The precision of this resolution depends on the implementation of the ConstantResolver.
	 *
	 * @param vertices
	 * @param markVar
	 * @return
	 */
	private static List<CPGVertexWithValue> resolveValuesForVertices(@NonNull Database<Node> db, List<CPGVertexWithValue> vertices, @NonNull String markVar) {
		List<CPGVertexWithValue> ret = new ArrayList<>();

		for (CPGVertexWithValue v : vertices) {
			if (Utils.hasLabel(v.getArgumentVertex(), Literal.class) && v.getArgumentVertex().property("value").isPresent()) {
				// The vertices may already be constants ("Literal"). In that case, immediately add the value.
				CPGVertexWithValue add = CPGVertexWithValue.of(v);
				add.setValue(ConstantValue.of(v.getArgumentVertex()
						.property("value")
						.value()));
				ret.add(add);
			} else if (Utils.hasLabel(v.getArgumentVertex(), DeclaredReferenceExpression.class)) {
				// Otherwise we use ConstantResolver to find concrete values of a DeclaredReferenceExpression.
				ConstantResolver cResolver = new SimpleConstantResolver(db);
				DeclaredReferenceExpression declExpr = (DeclaredReferenceExpression) db
						.vertexToNode(v.getArgumentVertex());
				if (declExpr == null) {
					continue;
				}

				Set<ConstantValue> constantValue = cResolver.resolveConstantValues(declExpr);

				if (!constantValue.isEmpty()) {
					constantValue.forEach(cv -> {
						CPGVertexWithValue add = CPGVertexWithValue.of(v);
						add.setValue(cv);
						ret.add(add);
					});
				} else {
					CPGVertexWithValue add = CPGVertexWithValue.of(v);
					v.setValue(ErrorValue.newErrorValue(String.format("could not resolve %s", markVar)));
					ret.add(add);
				}
			} else if (Utils.hasLabel(v.getArgumentVertex(), MemberExpression.class)) {
				// When resolving to a member ("javax.crypto.Cipher.ENCRYPT_MODE") we resolve to the member's name.
				MemberExpression memberExpression = (MemberExpression) db.vertexToNode(v.getArgumentVertex());
				String fqn = memberExpression.getBase().getName() + '.' + memberExpression.getMember().getName();
				ConstantValue cv = ConstantValue.of(fqn);
				CPGVertexWithValue add = CPGVertexWithValue.of(v);
				add.setValue(cv);
				ret.add(add);
			} else {
				log.info("Cannot resolve concrete value of a node that is not a DeclaredReferenceExpression or a Literal: {} Returning NULL",
					v.getArgumentVertex().label());
				CPGVertexWithValue add = CPGVertexWithValue.of(v);
				v.setValue(ErrorValue.newErrorValue(String.format("could not resolve %s", markVar)));
				ret.add(add);
			}
		}
		return ret;
	}

	public static Set<Vertex> getVerticesForFunctionDeclaration(
			@NonNull Database<Node> db,
			de.fraunhofer.aisec.mark.markDsl.FunctionDeclaration functionDeclaration,
			CrymlinTraversalSource crymlinTraversal) {

		// resolve parameters which have a corresponding var part in the entity
		Set<Vertex> callsAndInitializers = new HashSet<>(getCalls(db, crymlinTraversal, functionDeclaration.getName(), functionDeclaration.getParams()));
		callsAndInitializers.addAll(getCtors(db, crymlinTraversal, functionDeclaration.getName(), functionDeclaration.getParams()));

		// fix for Java. In java, a ctor is always accompanied with a newexpression
		callsAndInitializers.removeIf(c -> Utils.hasLabel(c, NewExpression.class));

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
				.repeat(inE()
						.has("sub-graph", "AST")
						.outV())
				.until(
					or(
						hasLabel(FunctionDeclaration.class.getSimpleName()),
						hasLabel(MethodDeclaration.class.getSimpleName())))
				.tryNext();
	}

	/**
	 * Given a vertex that represents a <code>CallExpression</code>, return the base(s) that this call expression uses.
	 * <p>
	 * The result will be either an Optional.empty() in case of static method calls or function calls, or contain a single element.
	 *
	 * @param callExpression
	 * @return
	 */
	@NonNull
	public static Optional<Vertex> getBaseOfCallExpression(@NonNull Vertex callExpression) {
		Optional<Vertex> base = Optional.empty();
		Iterator<Edge> it = callExpression.edges(Direction.OUT, "BASE");
		if (it.hasNext()) {
			Vertex baseVertex = it.next().inVertex();
			Iterator<Edge> refIterator = baseVertex.edges(Direction.OUT, "REFERS_TO");
			if (refIterator.hasNext()) {
				// if the node refers to another node, return the node it refers to
				Vertex ref = refIterator.next().inVertex();
				base = Optional.of(ref);
			} else {
				base = Optional.of(baseVertex);
			}
		}
		return base;
	}

	public static Optional<Vertex> getBaseOfInitializerArgument(@NonNull Vertex expr) {
		Optional<Vertex> base = Optional.empty();
		Iterator<Edge> refIterator = expr.edges(Direction.IN, ARGUMENTS);
		if (refIterator.hasNext()) {
			Iterator<Edge> it = refIterator.next().outVertex().edges(Direction.IN, INITIALIZER);
			if (it.hasNext()) {
				Vertex baseVertex = it.next().outVertex();
				// for java, an initializer is contained in another
				it = baseVertex.edges(Direction.IN, INITIALIZER);
				if (it.hasNext()) {
					baseVertex = it.next().outVertex();
				}
				refIterator = baseVertex.edges(Direction.OUT, REFERS_TO);
				if (refIterator.hasNext()) {
					// if the node refers to another node, return the node it refers to
					base = Optional.of(refIterator.next().inVertex());
				} else {
					base = Optional.of(baseVertex);
				}
			}
		}
		return base;
	}

	public static Optional<Vertex> getBaseOfCallOfArgumentExpression(@NonNull Vertex expr) {
		Optional<Vertex> base = Optional.empty();
		Iterator<Edge> refIterator = expr.edges(Direction.IN, ARGUMENTS);
		if (refIterator.hasNext()) {
			base = getBaseOfCallExpression(refIterator.next().outVertex());
		}
		return base;
	}

	public static Optional<Vertex> getAssigneeOfConstructExpression(Vertex vertex) {
		Iterator<Edge> it = vertex.edges(Direction.IN, INITIALIZER);
		if (it.hasNext()) {
			Vertex variableDeclaration = it.next().outVertex();
			it = variableDeclaration.edges(Direction.IN, INITIALIZER);
			if (it.hasNext()) {
				variableDeclaration = it.next().outVertex();
			}
			Iterator<Edge> refIterator = variableDeclaration.edges(Direction.OUT, REFERS_TO);
			if (refIterator.hasNext()) {
				// if the node refers to another node, return the node it refers to
				variableDeclaration = refIterator.next().inVertex();
			}

			if (!VariableDeclaration.class.getSimpleName().equals(variableDeclaration.label())) {
				log.warn("Unexpected: Source of INITIALIZER edge to ConstructExpression is not a VariableDeclaration. Trying to continue anyway");
			}
			return Optional.of(variableDeclaration);
		}
		return Optional.empty();
	}

	public static Map<Integer, List<CPGVertexWithValue>> resolveOperand(@NonNull Database<Node> db, MarkContextHolder context, @NonNull String markVar, @NonNull MRule rule,
																		Mark markModel, @NonNull CrymlinTraversalSource crymlin) {

		HashMap<Integer, List<CPGVertexWithValue>> verticesPerContext = new HashMap<>();

		// first get all vertices for the operand
		List<CPGVertexWithValue> matchingVertices = CrymlinQueryWrapper.getMatchingVertices(db, markVar, rule, markModel, crymlin);

		if (matchingVertices.isEmpty()) {
			log.warn("Did not find matching vertices for {}", markVar);
			return verticesPerContext;
		}

		// Use Constant resolver to resolve assignments to arguments
		List<CPGVertexWithValue> vertices = new ArrayList<>(resolveValuesForVertices(db, matchingVertices, markVar));

		// now split them up to belong to each instance (t) or markvar (t.foo)
		final String instance = markVar.substring(0, markVar.lastIndexOf('.'));

		// precompute a list mapping
		// from a nodeID (representing the varabledecl for the instance)
		// to a List of contexts where this base is referenced
		HashMap<Long, List<Integer>> nodeIDToContextIDs = new HashMap<>();
		if (StringUtils.countMatches(instance, '.') >= 1) {
			// if the instance itself is a markvar,
			// precalculate the variabledecl where each of the corresponding instance points to
			// i.e., precalculate the variabledecl for t.foo for each instance

			for (Map.Entry<Integer, MarkContext> entry : context.getAllContexts().entrySet()) {
				CPGVertexWithValue opInstance = entry.getValue().getOperand(instance);
				if (opInstance == null) {
					log.warn("Instance not found in context");
				} else if (opInstance.getArgumentVertex() == null) {
					log.warn("MARK variable {} does not correspond to a vertex", markVar);
				} else {
					Vertex vertex = opInstance.getArgumentVertex();
					// if available, get the variabledeclaration, this declaredreference refers_to
					Iterator<Edge> refersTo = opInstance.getArgumentVertex().edges(Direction.OUT, REFERS_TO);
					if (refersTo.hasNext()) {
						Edge next = refersTo.next();
						vertex = next.inVertex();
					}
					List<Integer> contextIDs = nodeIDToContextIDs.computeIfAbsent((Long) vertex.id(), x -> new ArrayList<>());
					contextIDs.add(entry.getKey());
				}
			}
		} else {
			// if the instance is entity referenced in the op, precalculate the variabledecl for each used op

			for (Map.Entry<Integer, MarkContext> entry : context.getAllContexts().entrySet()) {
				if (!entry.getValue().getInstanceContext().containsInstance(instance)) {
					log.warn("Instance not found in context");
				} else {
					Vertex opInstance = entry.getValue().getInstanceContext().getVertex(instance);
					Long id = -1L;
					if (opInstance != null) {
						id = (Long) opInstance.id();
					}
					List<Integer> contextIDs = nodeIDToContextIDs.computeIfAbsent(id, x -> new ArrayList<>());
					contextIDs.add(entry.getKey());
				}
			}
		}

		// now calculate a list of contextID to matching vertices which fill the base we are looking for

		for (CPGVertexWithValue vertexWithValue : vertices) {
			Long id = -1L; // -1 = null
			if (vertexWithValue.getBase() != null) {
				id = (Long) vertexWithValue.getBase().id();
			}
			List<Integer> contextIDs = nodeIDToContextIDs.get(id);
			if (contextIDs == null) {
				log.warn("Base not found in any context. Following expressionevaluation will be incomplete");
			} else {
				for (Integer c : contextIDs) {
					List<CPGVertexWithValue> verts = verticesPerContext.computeIfAbsent(c, x -> new ArrayList<>());
					verts.add(vertexWithValue);
				}
			}
		}

		return verticesPerContext;
	}

	/**
	 * Returns a set of ValueDeclarations where the variable/field/argument given by <code>delRefExpr</code> is declared.
	 *
	 * @param declRefExpr
	 * @return
	 */
	public static Set<ValueDeclaration> getDeclarationSites(@NonNull Database<Node> db, @NonNull DeclaredReferenceExpression declRefExpr) {
		Set<Vertex> refersTo = db.getGraph().traversal().V(declRefExpr.getId()).outE(REFERS_TO).inV().toBulkSet();
		Set<ValueDeclaration> varDecls = new HashSet<>();
		for (Vertex v : refersTo) {
			ValueDeclaration varDecl = (ValueDeclaration) db.vertexToNode(v);
			varDecls.add(varDecl);
		}
		return varDecls;
	}

	public static URI getFileLocation(Vertex v) {
		String path = v.value("file");
		return new File(path).toURI();
	}

	public static boolean eogConnection(Vertex source, Vertex sink, boolean branchesAllowed) {

		if (Objects.equals(source, sink)) {
			return true;
		}

		HashSet<Vertex> workList = new HashSet<>();
		HashSet<Vertex> seen = new HashSet<>();
		workList.add(source);

		while (!workList.isEmpty()) {
			HashSet<Vertex> newWorkList = new HashSet<>();
			for (Vertex v : workList) {
				seen.add(v);
				Iterator<Edge> eog = v.edges(Direction.OUT, EOG);
				int numEdges = 0;
				while (eog.hasNext()) {
					numEdges++;
					if (numEdges > 1 && !branchesAllowed) {
						return false;
					}
					Vertex next = eog.next().inVertex();
					if (next.equals(sink)) {
						return true;
					}
					if (!seen.contains(next)) {
						newWorkList.add(next);
					}
				}
			}
			workList = newWorkList;
		}

		return false;
	}

	public static Optional<Vertex> getDFGTarget(Vertex vertex) {
		Iterator<Edge> it = vertex.edges(Direction.OUT, DFG);
		if (it.hasNext()) {
			return Optional.of(it.next().inVertex());
		}
		return Optional.empty();
	}

	public static Set<Vertex> getDFGSources(Vertex vertex) {
		Set<Vertex> result = new HashSet<>();
		Iterator<Edge> it = vertex.edges(Direction.IN, DFG);
		while (it.hasNext()) {
			result.add(it.next().outVertex());
		}
		return result;
	}

	public static Optional<Vertex> refersTo(Vertex vertex) {
		Iterator<Edge> it = vertex.edges(Direction.IN, REFERS_TO);
		if (it.hasNext()) {
			return Optional.of(it.next().outVertex());
		}
		return Optional.empty();
	}

	public static Optional<Vertex> getInitializerFor(Vertex vertex) {
		// we first go back to the declaredreference (if any)
		Iterator<Edge> it = vertex.edges(Direction.IN, REFERS_TO);
		if (it.hasNext()) {
			vertex = it.next().outVertex();
		}

		// then we go forward to all referenced vars
		Iterator<Edge> refersTo = vertex.edges(Direction.OUT, REFERS_TO);
		while (refersTo.hasNext()) {
			Vertex referenced = refersTo.next().inVertex();
			Iterator<Edge> initializer = referenced.edges(Direction.OUT, INITIALIZER);
			if (initializer.hasNext()) {
				return Optional.of(initializer.next().inVertex());
			}
		}
		return Optional.empty();
	}

	public static Optional<Vertex> getField(String fqnClassName, String fieldName, CrymlinTraversalSource traveral) {

		Set<Vertex> vertices = traveral.fields(fieldName).toSet();
		for (Vertex v : vertices) {
			Iterator<Edge> it = v.edges(Direction.IN, FIELDS);
			if (it.hasNext()) {
				Vertex base = it.next().outVertex();
				if (base.value("name").equals(fqnClassName)) {
					return Optional.of(v);
				}
			}
		}
		return Optional.empty();
	}

	public static Optional<Object> getInitializerValue(Vertex vertex) {

		Iterator<Edge> dfg = vertex.edges(Direction.OUT, INITIALIZER);
		if (dfg.hasNext()) {
			Vertex vertex1 = dfg.next().inVertex();
			if (vertex1.label().equals(Literal.class.getSimpleName()) && vertex1.property("value").isPresent()) {
				return Optional.of(vertex1.property("value").orElse(null));
			}
		}
		return Optional.empty();
	}

	/**
	 * Returns an (unmodifiable) possibly empty list of all types this vertex might have.
	 *
	 * @param next
	 * @return
	 */
	@NonNull
	public static Set<Type> getPossibleSubTypes(@NonNull Database<Node> db, @NonNull Vertex next) {
		Set<Type> types = new HashSet<>();
		UnmodifiableIterator<Edge> it = Iterators.filter(next.edges(Direction.OUT, TYPE, POSSIBLE_SUB_TYPES), v -> Utils.hasLabel(v.inVertex(), Type.class));
		it.forEachRemaining(e -> types.add((Type) db.vertexToNode(e.inVertex())));

		return types;
	}
}
