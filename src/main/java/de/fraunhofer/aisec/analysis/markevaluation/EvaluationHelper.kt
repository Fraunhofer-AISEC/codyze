package de.fraunhofer.aisec.analysis.markevaluation

import de.fraunhofer.aisec.analysis.utils.Utils
import de.fraunhofer.aisec.cpg.ExperimentalGraph
import de.fraunhofer.aisec.cpg.graph.Graph
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.ConstructExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Expression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.NewExpression
import de.fraunhofer.aisec.cpg.graph.types.Type
import de.fraunhofer.aisec.crymlin.CrymlinQueryWrapper
import de.fraunhofer.aisec.crymlin.connectors.db.Database
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource
import de.fraunhofer.aisec.mark.markDsl.FunctionDeclaration
import de.fraunhofer.aisec.mark.markDsl.Parameter
import de.fraunhofer.aisec.markmodel.Constants
import org.apache.tinkerpop.gremlin.structure.Vertex
import org.eclipse.emf.common.util.EList
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
    val nodes = this.query("MATCH (c:CallExpression) WHERE c.fqn == ${Utils.unifyType(fqnName)}")

    val ret: MutableSet<CallExpression> = HashSet(nodes.filterIsInstance<CallExpression>())

    // now, ret contains possible candidates --> need to filter out calls where params don't match
    ret.removeIf {
        argumentsMatchParameters(parameters, it.arguments)
    }

    return ret
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
    val nodes = this.query("MATCH (c:ConstructExpression)-->(t:Type) WHERE t.name == ${Utils.unifyType(fqnName)}")

    // In case of constructors, "functionName" holds the name of the constructed type.
    val ret: MutableSet<ConstructExpression> = HashSet(nodes.filterIsInstance<ConstructExpression>())

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