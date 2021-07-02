package de.fraunhofer.aisec.analysis.markevaluation

import de.fraunhofer.aisec.analysis.markevaluation.Evaluator.log
import de.fraunhofer.aisec.analysis.utils.Utils
import de.fraunhofer.aisec.cpg.ExperimentalGraph
import de.fraunhofer.aisec.cpg.graph.Graph
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.Declaration
import de.fraunhofer.aisec.cpg.graph.declarations.FieldDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import de.fraunhofer.aisec.cpg.graph.types.Type
import de.fraunhofer.aisec.mark.markDsl.FunctionDeclaration
import de.fraunhofer.aisec.mark.markDsl.Parameter
import de.fraunhofer.aisec.markmodel.Constants
import org.apache.tinkerpop.gremlin.structure.Direction
import org.apache.tinkerpop.gremlin.structure.Vertex
import java.util.*

class EvaluationHelper {
}

@ExperimentalGraph
fun Graph.getVerticesForFunctionDeclaration(
    markFunctionReference: FunctionDeclaration,
): Set<Node> {

    // resolve parameters which have a corresponding var part in the entity
    val callsAndInitializers: MutableSet<Node> = HashSet(
        this.getCalls(markFunctionReference.name, markFunctionReference.params)
    )

    callsAndInitializers.addAll(
        this.getCtors(markFunctionReference.name, markFunctionReference.params
        )
    )

    // fix for Java. In java, a ctor is always accompanied with a NewExpression
    callsAndInitializers.removeIf { it is NewExpression }

    return callsAndInitializers
}

/**
 * Returns a set of Vertices representing the `CallExpression` to a target function
 *
 * @param fqnName          fully qualified name
 * @param parameters       list of parameter types; must appear in order (i.e. index 0 = type of first parameter, etc.); currently, types must be precise (i.e. with
 * qualifiers, pointer, reference)
 * @return
 */
@ExperimentalGraph
fun Graph.getCalls(
    fqnName: String,
    parameters: List<Parameter>
): MutableSet<CallExpression> {
    val nodes = this.nodes.filter {
        it is CallExpression &&
                it.fqn == Utils.unifyType(fqnName) &&
                argumentsMatchParameters(parameters, it.arguments) }
            .filterIsInstance<CallExpression>()

    return HashSet(nodes)
}

/**
 * Returns a set of `ConstructExpression`s with a specified name and parameters
 *
 * @param fqnName          fully qualified name
 * @param parameters       list of parameter types; must appear in order (i.e. index 0 = type of first parameter, etc.); currently, types must be precise (i.e. with
 * qualifiers, pointer, reference)
 * @return
 */
@ExperimentalGraph
fun Graph.getCtors(
    fqnName: String,
    parameters: List<Parameter>
): Set<ConstructExpression> {
    val nodes = this.nodes
        .filter { it is ConstructExpression && it.type.name == Utils.unifyType(fqnName) }
        .filterIsInstance<ConstructExpression>()
    //val nodes = this.query("MATCH (c:ConstructExpression)-[:TYPE]->(t:Type) WHERE t.name = \"${Utils.unifyType(fqnName)}\"")

    // In case of constructors, "functionName" holds the name of the constructed type.
    val ret: MutableSet<ConstructExpression> = HashSet(nodes)

    // now, ret contains possible candidates --> need to filter out calls where params don't match
    ret.removeIf {
        // ConstructExpression needs a special treatment because the argument of a ConstructExpression is the CallExpression to the constructor and we are interested in its arguments.
            val args = it.arguments
            if (args.size == 1 &&
                    args[0] is CallExpression
                )
             {
                return@removeIf argumentsMatchParameters(
                    parameters,
                    (args[0] as CallExpression).arguments
                )
            }
        argumentsMatchParameters(parameters, args)
    }
    return ret
}

/**
 *
 * TODO if there are really more than one possible DFG targets we should return a list
 *
 * @param vertex
 * @return
 */
@ExperimentalGraph
fun Node.getSuitableDFGTarget(): Node? {
    val it = this.nextDFG.iterator()
    var target: Node? = null

    // there are cases where there is more than one outgoing DFG edge
    while (it.hasNext()) {
        val v = it.next()
        log.info("DFG target: {}", v)

        // set the first find
        if (target == null) {
            target = v
        } else {
            // otherwise, look for something more useful
            // like a declared reference expression or a variable declaration
            if (v is DeclaredReferenceExpression || v is VariableDeclaration) {
                target = v
            }
        }
    }

    return target
}

@ExperimentalGraph
fun Node.getInitializedNodes(graph: Graph): List<Node> {
    // TODO: we need the graph as a helper here, since we cannot go inwards in all nodes yet

    return graph.nodes.filter {
        // TODO: It would be nice, if the CPG would have a HasInitializer interface
        (it is VariableDeclaration && it.initializer == this) ||
                (it is FieldDeclaration && it.initializer == this) ||
                (it is NewExpression && it.initializer == this)
    }
}

@ExperimentalGraph
fun ConstructExpression.getAssignee(graph: Graph): Node? {
    var it = this.getInitializedNodes(graph).iterator()
    if (it.hasNext()) {
        var node = it.next()

        // TODO: this follows initializers twice, but it should follow it until we have an assignee
        it = node.getInitializedNodes(graph).iterator()
        if (it.hasNext()) {
            node = it.next()
        }

        // if this refers to a 'new' expression, we need to traverse one more step
        if (node is NewExpression) {
            // use the DFG node to find the reference expression
            it = node.nextDFG.iterator()
            if (it.hasNext()) {
                node = it.next()
            }
        }

        if(node is DeclaredReferenceExpression) {
            node.refersTo?.let {
                node = it
            }
        }

        if (node !is VariableDeclaration) {
            log.warn("Unexpected: Source of INITIALIZER edge to ConstructExpression is not a VariableDeclaration. Trying to continue anyway")
        }

        return node
    }

    return null
}

/**
 * Given a node that represents a `CallExpression`, return the base(s) that this call expression uses.
 *
 * The result will be either an Optional.empty() in case of static method calls or function calls, or contain a single element.
 *
 * @return
 */
fun CallExpression.getBaseDeclaration(): Node? {
    var base: Node? = null

    this.base?.let {
        if(it is DeclaredReferenceExpression) {
            it.refersTo?.let { ref: Declaration ->
                base = ref
            }
        } else {
            base = it
        }
    }

    return base
}

/**
 * Returns true if the given list of arguments (of a function or method or constructor call)
 * matches the given list of parameters in MARK.
 *
 * @param markParameters  List of types.
 * @param sourceArguments List of Vertices. Each Vertex is expected to have an "argumentIndex" property.
 * @return
 */
@ExperimentalGraph
private fun argumentsMatchParameters(
    markParameters: List<Parameter>,
    sourceArguments: List<Expression>
): Boolean {
    var i = 0
    while (i < markParameters.size && i < sourceArguments.size) {
        val markParam = markParameters[i]
        val sourceArgs: MutableSet<Type> = HashSet()

        // We cannot assume that the position in sourceArgument corresponds with the actual order.
         // Must rather check "argumentIndex" property.
        for (vArg in sourceArguments) {
            val sourceArgPos = vArg.argumentIndex

            if (sourceArgPos == i) {
                sourceArgs.addAll(vArg.possibleSubTypes)
            }
        }

        if (sourceArgs.isEmpty()) {
            /*logger.error(
                "Cannot compare function arguments to MARK parameters. Unexpectedly null element or no argument types: {}",
                java.lang.String.join(", ", MOp.paramsToString(markParameters))
            )*/
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
        val sublist: List<Parameter> = markParameters.subList(i, markParameters.size)
        endsWithEllipsis = sublist.stream().allMatch { markParm: Parameter -> Constants.ELLIPSIS == markParm.getVar() }
    }
    return (i == markParameters.size || endsWithEllipsis) && i == sourceArguments.size
}