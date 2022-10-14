package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.modelling

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.Type
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.group
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.signature
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.unordered
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Literal
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertContentEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.random.Random
import kotlin.test.assertEquals

class OpComponentsTest {

    @ParameterizedTest(name = "[{index}] test with {0} parameters")
    @MethodSource("unaryPlusParamHelper")
    fun `test group`(expectedParams: ArrayList<Parameter>, description: String = "") {
        with(mockk<Signature>()) {
            val paramGroup = group {
                expectedParams.forEach { +it }
            }

            assertContentEquals(expectedParams, paramGroup.parameters)
        }

    }

    @ParameterizedTest(name = "[{index}] {1}")
    @MethodSource("unaryPlusParamHelper")
    fun `test unaryPlus in Signature`(expectedParams: ArrayList<Parameter>, description: String = "") {
        val sig = Signature()
        with(sig) {
            expectedParams.forEach { +it }
        }
        assertContentEquals(expectedParams, sig.parameters, "parameters in Signature were not as expected")
    }

    @Test
    fun `test simple signature`() {
        with(mockk<Definition>()) {
            val sig = signature { +singleParam }
            val sigShortcut = signature(singleParam)

            val expectedSig = Signature().apply { parameters.add(singleParam) }

            assertEquals(expectedSig, sig)
            assertEquals(expectedSig, sigShortcut)
        }
    }

    // TODO: test unordered

    @Test
    fun `test signature with group`() {
        with(mockk<Definition>()) {
            val sig = signature {
                +group {
                    multipleParams.forEach {+it}
                }
            }

            val groupShortcut = signature {
                +group(*multipleParams.toTypedArray())
            }

            val expectedSig = Signature().apply {
                parameters.add(
                    ParameterGroup().apply { parameters.addAll(multipleParams) }
                )
            }

            assertEquals(expectedSig, sig)
            assertEquals(expectedSig, groupShortcut)
        }
    }

    @Test
    fun `test complex signature`() {
        val (allOrdered, unordered) = multipleParams.partition { Random.nextBoolean() }
        val (ordered, grouped) = allOrdered.partition { Random.nextBoolean() }

        with(mockk<Definition>()) {
            val sig = signature {
                +singleParam
                +group {
                    grouped.forEach {+it}
                }
                ordered.forEach { +it }
            }.unordered {
                unordered.forEach { +it }
            }

            val expectedSig = Signature().apply {
                parameters.add(singleParam)
                parameters.add(ParameterGroup().apply { parameters.addAll(grouped) })
                parameters.addAll(ordered)

                unorderedParameters.addAll(unordered)
            }

            assertEquals(expectedSig, sig)
        }
    }



    companion object {
        val singleParam = arrayListOf<Parameter>("test")
        val multipleParams =  arrayListOf<Parameter>("test", emptyList<Parameter>(), Type("fqn"), arrayOf(1,2), Literal<Int>(), mockk<CallExpression>())

        @JvmStatic
        private fun unaryPlusParamHelper(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(singleParam, "Test with single parameter"),
                Arguments.of(multipleParams, "Test with multiple parameters")
            )
        }

    }


}