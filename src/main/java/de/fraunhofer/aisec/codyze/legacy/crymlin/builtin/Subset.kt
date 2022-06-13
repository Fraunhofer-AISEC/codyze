package de.fraunhofer.aisec.codyze.legacy.crymlin.builtin

import de.fraunhofer.aisec.codyze.legacy.analysis.*
import de.fraunhofer.aisec.codyze.legacy.analysis.markevaluation.ExpressionEvaluator
import de.fraunhofer.aisec.codyze.legacy.analysis.resolution.ConstantValue
import org.slf4j.LoggerFactory

/**
 * Method signature: _subset(List subset, List superset)
 *
 * This Builtin determines if the first list is a subset of the second list. In case of an error,
 * this Builtin returns an ErrorValue.
 */
class Subset : Builtin {

    /** Name of this builtin as used in MARK. */
    override fun getName(): String {
        return "_subset"
    }

    /**
     * Executes the builtin Subset.
     *
     * @param ctx The analysis context
     * @param argResultList Resolved argumentsList for one context of the Builtin function call
     * @param contextID ID of current MARK context
     * @param markContextHolder Container for contexts of MARK
     * @param expressionEvaluator the ExpressionEvaluator, this builtin is called from
     * @return
     */
    override fun execute(
        ctx: AnalysisContext,
        argResultList: ListValue,
        contextID: Int,
        markContextHolder: MarkContextHolder,
        expressionEvaluator: ExpressionEvaluator
    ): MarkIntermediateResult {
        return try {
            // verify number of arguments and argument type
            BuiltinHelper.verifyArgumentTypesOrThrow(
                argResultList,
                ListValue::class.java,
                ListValue::class.java
            )

            // get arguments
            val subsetArgument = argResultList[0] as ListValue
            val supersetArgument = argResultList[1] as ListValue

            // create proper set from superset list
            val superset = HashSet(supersetArgument.all)
            // superset must contain all elements of subset
            val isSubset = superset.containsAll(subsetArgument.all)

            // create result value of evaluation
            val cv = ConstantValue.of(isSubset)
            // add responsible nodes from both arguments
            cv.addResponsibleNodes(subsetArgument.responsibleNodes)
            cv.addResponsibleNodes(supersetArgument.responsibleNodes)
            // return result
            cv
        } catch (e: InvalidArgumentException) {
            // handle mismatch of argument count and type
            log.error("arguments must be lists")
            // on error return ErrorValue
            ErrorValue.newErrorValue("arguments must be lists", argResultList.all)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(Subset::class.java)
    }
}
