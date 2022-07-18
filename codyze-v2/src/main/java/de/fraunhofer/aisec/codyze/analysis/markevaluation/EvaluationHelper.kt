package de.fraunhofer.aisec.codyze.analysis.markevaluation

import de.fraunhofer.aisec.codyze.analysis.ErrorValue
import de.fraunhofer.aisec.codyze.analysis.ListValue
import de.fraunhofer.aisec.codyze.analysis.MarkContextHolder
import de.fraunhofer.aisec.codyze.analysis.NodeWithValue
import de.fraunhofer.aisec.codyze.analysis.markevaluation.Evaluator.log
import de.fraunhofer.aisec.codyze.analysis.resolution.ConstantResolver
import de.fraunhofer.aisec.codyze.analysis.resolution.ConstantValue
import de.fraunhofer.aisec.codyze.analysis.resolution.SimpleConstantResolver
import de.fraunhofer.aisec.codyze.analysis.utils.Utils
import de.fraunhofer.aisec.codyze.markmodel.*
import de.fraunhofer.aisec.cpg.ExperimentalGraph
import de.fraunhofer.aisec.cpg.graph.Graph
import de.fraunhofer.aisec.cpg.graph.HasInitializer
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.FieldDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.RecordDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.CompoundStatement
import de.fraunhofer.aisec.cpg.graph.statements.ReturnStatement
import de.fraunhofer.aisec.cpg.graph.statements.Statement
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import de.fraunhofer.aisec.cpg.graph.types.Type
import de.fraunhofer.aisec.cpg.passes.astParent
import de.fraunhofer.aisec.cpg.passes.followNextEOG
import de.fraunhofer.aisec.mark.markDsl.OpStatement
import de.fraunhofer.aisec.mark.markDsl.Parameter
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors
import java.util.stream.IntStream
import org.apache.commons.lang3.StringUtils

@ExperimentalGraph
fun Graph.getNodesForFunctionReference(
    markFunctionReference: de.fraunhofer.aisec.mark.markDsl.FunctionDeclaration,
): Set<CallExpression> {
    // resolve parameters which have a corresponding var part in the entity
    val callsAndInitializers: MutableSet<CallExpression> =
        HashSet(this.getCalls(markFunctionReference.name, markFunctionReference.params))

    callsAndInitializers.addAll(
        this.getConstructs(markFunctionReference.name, markFunctionReference.params)
    )

    return callsAndInitializers
}

/**
 * Returns a set of Vertices representing the `CallExpression` to a target function
 *
 * @param fqnName fully qualified name
 * @param parameters list of parameter types; must appear in order (i.e. index 0 = type of first
 * parameter, etc.); currently, types must be precise (i.e. with qualifiers, pointer, reference)
 * @return
 */
@ExperimentalGraph
fun Graph.getCalls(fqnName: String, parameters: List<Parameter>): MutableSet<CallExpression> {
    val nodes =
        this.nodes
            .filter {
                it is CallExpression &&
                    it.fqn == Utils.unifyType(fqnName) &&
                    argumentsMatchParameters(parameters, it.arguments)
            }
            .filterIsInstance<CallExpression>()

    return HashSet(nodes)
}

/**
 * Returns a set of `ConstructExpression`s with a specified name and parameters
 *
 * @param fqnName fully qualified name
 * @param parameters list of parameter types; must appear in order (i.e. index 0 = type of first
 * parameter, etc.); currently, types must be precise (i.e. with qualifiers, pointer, reference)
 * @return
 */
@ExperimentalGraph
fun Graph.getConstructs(fqnName: String, parameters: List<Parameter>): Set<ConstructExpression> {
    val nodes =
        this.nodes
            .filter { it is ConstructExpression && it.type.name == Utils.unifyType(fqnName) }
            .filterIsInstance<ConstructExpression>()
    // val nodes = this.query("MATCH (c:ConstructExpression)-[:TYPE]->(t:Type) WHERE t.name =
    // \"${Utils.unifyType(fqnName)}\"")

    // In case of constructors, "functionName" holds the name of the constructed type.
    val ret: MutableSet<ConstructExpression> = HashSet(nodes)

    // now, ret contains possible candidates --> need to filter out calls where params don't match
    ret.removeIf {
        // ConstructExpression needs a special treatment because the argument of a
        // ConstructExpression is the CallExpression to the constructor and we are interested in its
        // arguments.
        val args = it.arguments
        !argumentsMatchParameters(parameters, args)
    }

    return ret
}

@ExperimentalGraph
fun Graph.getField(fqnClassName: String, fieldName: String?): FieldDeclaration? {
    return this.nodes
        .filter { it is RecordDeclaration && it.name == fqnClassName }
        .map { (it as RecordDeclaration).getField(fieldName) }
        .firstOrNull()
}

/**
 *
 * @return
 */
fun Node.getSuitableDFGTarget(): Node? {
    // There could potentially be multiple DFG targets. this can lead to
    // inconsistent results. Therefore, we filter the DFG targets for "interesting" types
    // and also sort them by name to make this more consistent.
    val suitable =
        this.nextDFG
            .filter {
                it is DeclaredReferenceExpression ||
                    it is ReturnStatement || // for builder-style functions
                    it is ConstructExpression ||
                    it is VariableDeclaration
            }
            .sortedWith(Comparator.comparing(Node::name))

    return suitable.firstOrNull()
}

val Node.initializedNode: Node?
    get() {
        return if (this.astParent is HasInitializer) {
            this.astParent
        } else {
            null
        }
    }

/**
 * If the expression is either a declaration of a reference to a declaration, it returns the
 * initializer for the underlying declaration.
 */
fun Node.getInitializerFor(): Expression? {
    if (this is HasInitializer) {
        // directly go to the initializer
        return this.initializer
    } else if (this is DeclaredReferenceExpression) {
        // follow the reference back to the declaration
        return this.refersTo?.getInitializerFor()
    }

    // in all other cases, return null
    return null
}

fun FieldDeclaration.getInitializerValue(): Any? {
    this.initializer?.let {
        if (it is Literal<*>) {
            return it.value
        }
    }

    return null
}

val Node.nextStatement: Statement?
    get() {
        return this.followNextEOG { it.end.astParent is CompoundStatement }?.last()?.end
            as? Statement
    }

/**
 * Given a node, try to find the function or method in which the node is contained. The resulting
 * Vertex will be of type FunctionDeclaration or MethodDeclaration. If v is not contained in a
 * function, this method returns an empty Optional.
 *
 * @return
 */
val Node.containingFunction: FunctionDeclaration?
    get() {
        var parent = this.astParent

        while (parent != null && parent !is FunctionDeclaration) {
            parent = parent.astParent
        }

        return parent as? FunctionDeclaration
    }

/** Checks, whether a EOG connection from this node (source) to the sink exists. */
fun Node.hasEOGTo(sink: Node, branchesAllowed: Boolean): Boolean {
    if (this == sink) {
        return true
    }

    var workList = HashSet<Node>()
    val seen = HashSet<Node>()
    workList.add(this)

    while (!workList.isEmpty()) {
        val newWorkList = HashSet<Node>()

        for (v in workList) {
            seen.add(v)
            val eog = v.nextEOG.iterator()
            var numEdges = 0

            while (eog.hasNext()) {
                numEdges++
                if (numEdges > 1 && !branchesAllowed) {
                    return false
                }

                val next = eog.next()
                if (next == sink) {
                    return true
                }

                if (!seen.contains(next)) {
                    newWorkList.add(next)
                }
            }
        }
        workList = newWorkList
    }

    return false
}

fun ConstructExpression.getAssignee(): Node? {
    this.initializedNode?.let { initializedNode ->
        var node = initializedNode

        // TODO: this follows initializers twice, but initializedNode should follow initializedNode
        // until we have an assignee
        node.initializedNode?.let { node = it }

        // if this refers to a 'new' expression, we need to traverse one more step
        if (node is NewExpression) {
            // use the DFG node to find the reference expression
            val iter = node.nextDFG.iterator()
            if (iter.hasNext()) {
                node = iter.next()
            }
        }

        if (node is DeclaredReferenceExpression) {
            (node as DeclaredReferenceExpression).refersTo?.let { node = it }
        }

        if (node !is VariableDeclaration) {
            log.warn(
                "Unexpected: Source of INITIALIZER edge to ConstructExpression is not a VariableDeclaration. Trying to continue anyway"
            )
        }

        return node
    }

    return null
}

/**
 * Given a node that represents a `CallExpression`, return the base(s) that this call expression
 * uses.
 *
 * The result will be either an Optional.empty() in case of static method calls or function calls,
 * or contain a single element.
 *
 * @return
 */
fun CallExpression.getBaseDeclaration(): Node? {
    var base: Node? = null

    this.base.let {
        if (it is DeclaredReferenceExpression) {
            it.refersTo?.let { declaration -> base = declaration }
        } else {
            base = it
        }
    }

    return base
}

/**
 * This returns the "base" (MARK speech) object using the argument of a call expression. A common
 * use-case is a non-objected oriented programming language, where the to-be-tracked object is
 * passed as the first argument in a call expression.
 *
 * For example, for the code `call(a, b)` and using the [argumentIndex] `0`, this will return `a`.
 */
public fun CallExpression.getBaseOfCallExpressionUsingArgument(argumentIndex: Int): Node? {
    val list = this.arguments.filter { it.argumentIndex == argumentIndex }

    if (list.size == 1) {
        var node: Node = list.first()

        (node as? DeclaredReferenceExpression)?.refersTo?.let {
            // if the node refers to another node, return the node it refers to
            node = it
        }

        return node
    }

    return null
}

/**
 * This returns the "base" (MARK speech) object of an expression that is contained as an argument in
 * an initializer, e.g. in a construct or call expression.
 *
 * For example, for the code `Foo f = new Foo(b)` this would return `f` for the expression `b`.
 */
fun Expression.getBaseOfInitializerArgument(): Node? {
    var base: Node? = null
    var parent = this.astParent

    // check, if AST parent is a call expression and it contains this expression as an argument
    if (parent is CallExpression && parent.arguments.contains(this)) {
        // get the node, that this call expression initializes, i.e. the variable declaration
        // (either directly or indirectly)
        parent.initializedNode?.let {
            when (it) {
                is DeclaredReferenceExpression -> it.refersTo.let { base = it }
                is NewExpression -> {
                    // if the NewExpression is part of an initializer, return the initializer
                    parent = it.astParent
                    base =
                        if (parent is HasInitializer) {
                            parent
                        } else {
                            // otherwise, the NewExpression itself is the base
                            it
                        }
                }
                else -> base = it
            }
        }
    }

    return base
}

@ExperimentalGraph
fun MRule.resolveOperand(
    graph: Graph,
    context: MarkContextHolder,
    markVar: String,
    markModel: Mark,
): Map<Int, MutableList<NodeWithValue<Node>>> {
    val verticesPerContext = HashMap<Int, MutableList<NodeWithValue<Node>>>()

    // first get all nodes for the operand
    val matchingVertices = this.getMatchingReferences(graph, markVar, markModel)
    if (matchingVertices.isEmpty()) {
        log.warn("Did not find matching vertices for {}", markVar)
        return verticesPerContext
    }

    // Use Constant resolver to resolve assignments to arguments
    val vertices = resolveValuesForVertices(matchingVertices, markVar)

    // now split them up to belong to each instance (t) or markvar (t.foo)
    val instance = markVar.substring(0, markVar.lastIndexOf('.'))

    // precompute a list mapping
    // from a nodeID (representing the varabledecl for the instance)
    // to a List of contexts where this base is referenced
    val nodeIDToContextIDs = HashMap<Long, MutableList<Int>>()
    if (StringUtils.countMatches(instance, '.') >= 1) {
        // if the instance itself is a markvar,
        // precalculate the variabledecl where each of the corresponding instance points to
        // i.e., precalculate the variabledecl for t.foo for each instance
        for ((key, value) in context.allContexts) {
            val opInstance = value.getOperand(instance)
            when {
                opInstance == null -> {
                    log.warn("Instance not found in context")
                }
                opInstance.node == null -> {
                    log.warn("MARK variable {} does not correspond to a node", markVar)
                }
                else -> {
                    var vertex = opInstance.node
                    // if available, get the variabledeclaration, this declaredreference refers_to
                    if (vertex is DeclaredReferenceExpression) {
                        vertex.refersTo?.let { vertex = it }
                    }

                    val contextIDs = nodeIDToContextIDs.computeIfAbsent(vertex.id!!) { ArrayList() }
                    contextIDs.add(key)
                }
            }
        }
    } else {
        // if the instance is entity referenced in the op, precalculate the variabledecl for each
        // used op
        for ((key, value) in context.allContexts) {
            if (!value.instanceContext.containsInstance(instance)) {
                log.warn("Instance not found in context")
            } else {
                val opInstance = value.instanceContext.getNode(instance)
                var id = -1L
                if (opInstance != null) {
                    id = opInstance.id!!
                }
                val contextIDs = nodeIDToContextIDs.computeIfAbsent(id) { ArrayList() }
                contextIDs.add(key)
            }
        }
    }

    // now calculate a list of contextID to matching vertices which fill the base we are looking for
    for (vertexWithValue in vertices) {
        var id = -1L // -1 = null
        if (vertexWithValue.base != null) {
            val base = vertexWithValue.base
            id =
                if (base is DeclaredReferenceExpression && base.refersTo != null) {
                    val referencedBase = base.refersTo
                    referencedBase?.id!!
                } else {
                    base?.id!!
                }
        }
        val contextIDs: List<Int>? = nodeIDToContextIDs[id]
        if (contextIDs == null) {
            log.warn(
                "Base not found in any context. Following expressionevaluation will be incomplete"
            )
        } else {
            for (c in contextIDs) {
                val verts = verticesPerContext.computeIfAbsent(c) { ArrayList() }
                verts.add(vertexWithValue)
            }
        }
    }

    return verticesPerContext
}

/**
 * Returns a list of
 * [de.fraunhofer.aisec.cpg.graph.statements.expressions.DeclaredReferenceExpression]s and values
 * that correspond to a given MARK variable in a given rule.
 *
 * TODO: It seems that it can also contain declarations and others, not sure why
 *
 * @param markVar The MARK variable.
 * @param rule The MARK rule using the MARK variable.
 * @param markModel The current MARK model.
 * @param graph The Graph.
 *
 * @return List of reference nodes in a list of
 * [de.fraunhofer.aisec.analysis.structures.NodeWithValue]
 */
@ExperimentalGraph
fun MRule.getMatchingReferences(
    graph: Graph,
    markVar: String,
    markModel: Mark,
): MutableList<NodeWithValue<Node>> {
    val matchingVertices = mutableListOf<NodeWithValue<Node>>()

    // Split MARK variable "myInstance.attribute" into "myInstance" and "attribute".
    val markVarParts = markVar.split("\\.".toRegex()).toTypedArray()
    var instance = markVarParts[0]
    var attribute = markVarParts[1]

    // Get the MARK entity corresponding to the MARK instance variable.
    val ref = this.entityReferences[instance]
    if (ref?.second == null) {
        log.warn(
            "Unexpected: rule {} without referenced entity for instance {}",
            this.name,
            instance
        )
        return matchingVertices
    }

    var referencedEntity = ref.second

    if (StringUtils.countMatches(markVar, ".") > 1) {
        log.info("{} References an entity inside an entity", markVar)
        for (i in 1 until markVarParts.size - 1) {
            instance += "." + markVarParts[i]
            attribute = markVarParts[i + 1]

            // sanity-checking the references entity
            var match: MVar? = null
            for (`var` in referencedEntity!!.vars) {
                if (`var`.name == markVarParts[i]) {
                    match = `var`
                    break
                }
            }

            if (match == null) {
                log.warn("Entity does not contain variable {}", markVarParts[i])

                return matchingVertices
            }

            referencedEntity = markModel.getEntity(match.type)
            if (referencedEntity == null) {
                log.warn("No Entity with name {} found", match.type)

                return matchingVertices
            }
        }
    }

    val finalAttribute = attribute
    val usesAsVar: MutableList<Pair<MOp, Set<OpStatement>?>> = ArrayList()
    val usesAsFunctionArgs: MutableList<Pair<MOp, Set<OpStatement>?>> = ArrayList()

    // Collect *variables* assigned in Ops of this entity and *arguments* used in Ops.
    for (operation in referencedEntity!!.ops) {
        val vars: MutableSet<OpStatement> = HashSet()
        val args: MutableSet<OpStatement> = HashSet()

        // Iterate over all statements of that op
        for (opStmt in operation.statements) {
            // simple assignment, i.e. "var = something()"
            if (attribute == opStmt.getVar()) {
                vars.add(opStmt)
            }
            // Function parameter, i.e. "something(..., var, ...)"
            if (
                opStmt.call.params.stream().anyMatch { p: Parameter ->
                    p.getVar() == finalAttribute
                }
            ) {
                args.add(opStmt)
            }
        }
        if (vars.isNotEmpty()) {
            usesAsVar.add(Pair(operation, vars))
        }
        if (args.isNotEmpty()) {
            usesAsFunctionArgs.add(Pair(operation, args))
        }
    }

    // get nodes for all usesAsVar (i.e., simple assignment, i.e. "var = something()")
    for (p in usesAsVar) {
        if (p.second == null) {
            log.warn("Unexpected: Null value for usesAsFunctionArg {}", p.first)
            continue
        }

        for (opstmt in p.second!!) {
            val fqFunctionName = opstmt.call.name
            val vertices = graph.getCalls(fqFunctionName, opstmt.call.params)
            vertices.addAll(graph.getConstructs(fqFunctionName, opstmt.call.params))

            for (v in vertices) {
                // precalculate base
                val baseOfCallExpression = v.getBaseDeclaration()
                var foundTargetVertex = false

                // check if there was an assignment (i.e., i = call(foo);)
                val references = v.lhsReferenceOfAssignment()
                if (references.isNotEmpty()) {
                    foundTargetVertex = true
                    log.info("found assignment: {}", references)

                    references.forEach(
                        Consumer {
                            // create a pair of nodes and their values (uninitialized yet)
                            val cpgVertexWithValue =
                                NodeWithValue<Node>(it, ConstantValue.newUninitialized())
                            cpgVertexWithValue.base = baseOfCallExpression
                            matchingVertices.add(cpgVertexWithValue)
                        }
                    )
                }

                // check if there was a direct initialization (i.e., int i = call(foo);)
                val varDeclaration = v.initializedNode as? VariableDeclaration
                if (varDeclaration != null) {
                    foundTargetVertex = true
                    log.info("found direct initialization: {}", varDeclaration)

                    val cpgVertexWithValue =
                        NodeWithValue<Node>(varDeclaration, ConstantValue.newUninitialized())
                    cpgVertexWithValue.base = baseOfCallExpression
                    matchingVertices.add(cpgVertexWithValue)
                }

                if (!foundTargetVertex) { // this can be a directly used return value from a call
                    val cpgVertexWithValue =
                        NodeWithValue<Node>(v, ConstantValue.newUninitialized())
                    cpgVertexWithValue.base = baseOfCallExpression
                    matchingVertices.add(cpgVertexWithValue)
                }
            }
        }
    }

    // get vertices for all usesAsFunctionArgs (i.e., Function parameter, i.e. "something(..., var,
    // ...)")
    for (p in usesAsFunctionArgs) {
        if (p.second == null) {
            log.warn("Unexpected: Null value for usesAsFunctionArg {}", p.first)
            continue
        }

        for (opstmt in p.second!!) { // opstatement is one possible method call/ctor inside an op
            val fqFunctionName = opstmt.call.name
            val params = opstmt.call.params
            val paramPositions =
                IntStream.range(0, params.size)
                    .filter { i: Int -> finalAttribute == params[i].getVar() }
                    .toArray()
            if (paramPositions.size > 1) {
                log.warn(
                    "Invalid op signature: MarkVar is referenced more than once. Only the first one will be used."
                )
            }

            if (paramPositions.isEmpty()) {
                log.error("argument not found in parameters. This should not happen")
                continue
            }

            val argumentIndex = paramPositions[0]
            log.debug(
                "Checking for call/ctor. fqname: {} - markParams: {}",
                fqFunctionName,
                java.lang.String.join(", ", MOp.paramsToString(params))
            )
            for (v in graph.getCalls(fqFunctionName, params)) {
                val argumentVertices = v.arguments.filter { it.argumentIndex == argumentIndex }

                if (argumentVertices.size == 1) {
                    // handle the case, where we have a 'this' variable, explicitly specifying the
                    // base, this is usually used in scenarios, where the object is used as an
                    // argument
                    // rather than a member call

                    // get base of call expression
                    var baseOfCallExpression: Node?
                    val thisPositions =
                        IntStream.range(0, params.size)
                            .filter { i: Int -> "this" == params[i].getVar() }
                            .toArray()
                    if (thisPositions.size == 1) {
                        baseOfCallExpression =
                            v.getBaseOfCallExpressionUsingArgument(thisPositions[0])
                    } else {
                        if (v is StaticCallExpression) {
                            baseOfCallExpression = v.getSuitableDFGTarget()
                        } else {
                            baseOfCallExpression = v.getBaseDeclaration()
                            if (
                                baseOfCallExpression == null
                            ) { // if we did not find a base the "easy way", try to find a base
                                // using the simple-DFG
                                baseOfCallExpression = v.getSuitableDFGTarget()
                            }
                        }
                    }
                    val cpgVertexWithValue =
                        NodeWithValue<Node>(argumentVertices[0], ConstantValue.newUninitialized())
                    cpgVertexWithValue.base = baseOfCallExpression
                    matchingVertices.add(cpgVertexWithValue)
                } else {
                    log.warn(
                        "multiple arguments for function {} have the same argument_id. Invalid cpg.",
                        fqFunctionName
                    )
                }
            }
            for (v in graph.getConstructs(fqFunctionName, params)) {
                val argumentVertices = v.arguments.filter { it.argumentIndex == argumentIndex }
                if (argumentVertices.size == 1) {
                    // get base of initializer for ctor
                    val baseOfCallExpression = argumentVertices[0].getBaseOfInitializerArgument()
                    val cpgVertexWithValue =
                        NodeWithValue<Node>(argumentVertices[0], ConstantValue.newUninitialized())
                    cpgVertexWithValue.base = baseOfCallExpression
                    matchingVertices.add(cpgVertexWithValue)
                } else {
                    log.warn(
                        "multiple arguments for function {} have the same argument_id. Invalid cpg.",
                        fqFunctionName
                    )
                }
            }
        }
    }

    log.debug(
        "GETMATCHINGVERTICES for {} returns {}",
        markVar,
        matchingVertices
            .stream()
            .map { it.node.javaClass.simpleName + ": " + it.node.code }
            .collect(Collectors.joining(", "))
    )

    return matchingVertices
}

/**
 * Given a MARK variable and a list of vertices, attempts to find constant values that would be
 * assigned to these variables at runtime.
 *
 * The precision of this resolution depends on the implementation of the ConstantResolver.
 *
 * @param vertices
 * @param markVar
 * @return
 */
private fun resolveValuesForVertices(
    vertices: List<NodeWithValue<Node>>,
    markVar: String
): MutableList<NodeWithValue<Node>> {
    val ret = mutableListOf<NodeWithValue<Node>>()
    for (v in vertices) {
        if (v.node is Literal<*> && v.node.value != null) {
            // The vertices may already be constants ("Literal"). In that case, immediately add the
            // value.
            val add = NodeWithValue.of(v)
            add.value = ConstantValue.of(v.node.value)
            ret.add(add)
        } else if (v.node is MemberExpression) {
            // When resolving to a member ("javax.crypto.Cipher.ENCRYPT_MODE") we resolve to the
            // member's name.
            val cResolver: ConstantResolver = SimpleConstantResolver()
            val constantValue = cResolver.resolveConstantValues(v.node)
            if (constantValue.isNotEmpty()) {
                constantValue.forEach(
                    Consumer { cv: ConstantValue ->
                        val add = NodeWithValue.of(v)
                        add.value = cv
                        ret.add(add)
                    }
                )
            } else {
                val fqn: String = v.node.base.name + '.' + v.node.name
                val cv = ConstantValue.of(fqn)
                val add = NodeWithValue.of(v)
                add.value = cv
                ret.add(add)
            }
        } else if (v.node is DeclaredReferenceExpression) {
            // Otherwise we use ConstantResolver to find concrete values of a
            // DeclaredReferenceExpression.
            val cResolver: ConstantResolver = SimpleConstantResolver()
            val constantValue = cResolver.resolveConstantValues(v.node)
            if (constantValue.isNotEmpty()) {
                constantValue.forEach(
                    Consumer { cv: ConstantValue ->
                        val add = NodeWithValue.of(v)
                        add.value = cv
                        ret.add(add)
                    }
                )
            } else {
                val fqn = v.node.name
                val cv = ConstantValue.of(fqn)
                val add = NodeWithValue.of(v)
                add.value = cv
                v.value = ErrorValue.newErrorValue(String.format("could not resolve %s", markVar))
                ret.add(add)
            }
        } else if (v.node is ArrayCreationExpression) {
            if (v.node.initializer is InitializerListExpression) {
                val initializerList = v.node.initializer as InitializerListExpression

                val add = NodeWithValue.of(v)
                val lv = ListValue()
                for (e in initializerList.initializers) {
                    if (e is Literal<*>) {
                        lv.add(ConstantValue.of(e.value))
                    } else {
                        log.warn(
                            "Cannot resolve non-literal expressions in ArrayCreationExpression yet (not supported)."
                        )
                        lv.add(ErrorValue.newErrorValue("Unknown value"))
                    }
                }
                add.value = lv
                ret.add(add)
            }
        } else {
            log.info(
                "Cannot resolve concrete value of a node that is not a DeclaredReferenceExpression or a Literal: {} Returning NULL",
                v.node.javaClass.simpleName
            )
            val add = NodeWithValue.of(v)
            v.value = ErrorValue.newErrorValue(String.format("could not resolve %s", markVar))
            ret.add(add)
        }
    }

    return ret
}

/**
 * If this expression is the right-hand side of a
 * [de.fraunhofer.aisec.cpg.graph.statements.expressions.BinaryOperator], it will return the
 * left-hand side of the operation. Additionally, it will filter only for references, i.e.
 * [de.fraunhofer.aisec.cpg.graph.statements.expressions.DeclaredReferenceExpression].
 */
@ExperimentalGraph
private fun Expression.lhsReferenceOfAssignment(): List<DeclaredReferenceExpression> {
    val nodes = listOf(this.astParent)

    return nodes
        .filter {
            it is BinaryOperator && it.rhs == this && it.operatorCode == "="
        } // look for a binary operation where this expression is on the right-hand side
        .map { (it as BinaryOperator).lhs } // return the LHS
        .filterIsInstance<DeclaredReferenceExpression>() // we are only interested in references
}

/**
 * Returns true if the given list of arguments (of a function or method or constructor call) matches
 * the given list of parameters in MARK.
 *
 * @param markParameters List of types.
 * @param sourceArguments List of Vertices. Each Vertex is expected to have an "argumentIndex"
 * property.
 * @return
 */
@ExperimentalGraph
fun argumentsMatchParameters(
    markParameters: List<Parameter>,
    sourceArguments: List<Expression>
): Boolean {
    var i = 0

    while (i < markParameters.size && i < sourceArguments.size) {
        val markParam = markParameters[i]
        val sourceArgs: MutableSet<Type> = HashSet()

        for (arg in sourceArguments) {
            sourceArgs.add(arg.type)
        }

        if (sourceArgs.isEmpty()) {
            log.error(
                "Cannot compare function arguments to MARK parameters. Unexpectedly null element or no argument types: {}",
                java.lang.String.join(", ", MOp.paramsToString(markParameters))
            )
            return false
        }

        if (Constants.ELLIPSIS == markParam.getVar()) {
            return true
        }

        // UNDERSCORE means we do not care about this specific argument at all
        if (Constants.UNDERSCORE == markParam.getVar()) {
            i++
            continue
        }

        // ANY_TYPE means we have a MARK variable, but do not care about its type
        if (Constants.ANY_TYPE == markParam.getVar()) {
            i++
            continue
        }
        if (!Utils.isSubTypeOf(sourceArgs, markParam)) {
            return false
        }
        i++
    }

    // If parameter list ends with an ELLIPSIS, we ignore the remaining arguments
    var endsWithEllipsis = false

    if (i < markParameters.size) {
        val sublist = markParameters.subList(i, markParameters.size)
        endsWithEllipsis =
            sublist.stream().allMatch { markParam: Parameter ->
                Constants.ELLIPSIS == markParam.getVar()
            }
    }

    return (i == markParameters.size || endsWithEllipsis) && i == sourceArguments.size
}

fun Node.followNextDFG(predicate: (Node) -> Boolean): List<Node>? {
    val path = mutableListOf<Node>()

    for (to in this.nextDFG) {
        path.add(to)

        if (predicate(to)) {
            return path
        }

        val subPath = to.followNextDFG(predicate)
        if (subPath != null) {
            path.addAll(subPath)

            return path
        }
    }

    return null
}
