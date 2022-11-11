package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.modelling.Definition
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.modelling.Parameter
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.modelling.ParameterGroup
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.modelling.Signature
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.reflect.KFunction
import kotlin.test.assertEquals

class OpDslTest {
    @Test
    fun `test simple op`() {
        val mockDefinition = mockk<Definition>()
        val actualOp = op {
            +mockDefinition
        }
        val expectedOp = FunctionOp().apply { definitions.add(mockDefinition) }

        assertEquals(expectedOp, actualOp)
    }

    @Test
    fun `test complete op`() {
        val stringParam: Parameter = "test string"
        val numberParam: Parameter = 123
        val typeParam: Parameter = Type("fqn")
        val arrayParam = arrayOf<Any>()
        val callTestParam = mockk<KFunction<*>>()
        val collectionParam = mockk<Collection<Any>>()

        val actualOp = op {
            +definition("fqn1") {
                +signature {
                    +stringParam
                }
                +signature(stringParam, callTestParam)
                +signature {
                    +stringParam
                    +callTestParam
                }.unordered(numberParam)
                +signature(numberParam, collectionParam, stringParam)
            }
            +definition("fqn2") {
                +signature {
                    +typeParam
                    +group {
                        +stringParam
                        +arrayParam
                    }
                }
            }
        }

        val def1 = Definition("fqn1")
        def1.signatures.add(Signature().apply {
            parameters.add(stringParam)
        })
        def1.signatures.add(Signature().apply {
            parameters.add(stringParam)
            parameters.add(callTestParam)
        })
        def1.signatures.add(Signature().apply {
            parameters.add(stringParam)
            parameters.add(callTestParam)
            unorderedParameters.add(numberParam)
        })
        def1.signatures.add(Signature().apply {
            parameters.add(numberParam)
            parameters.add(collectionParam)
            parameters.add(stringParam)
        })

        val def2 = Definition("fqn2")
        def2.signatures.add(Signature().apply {
            parameters.add(typeParam)
            parameters.add(
                ParameterGroup().apply {
                    parameters.add(stringParam)
                    parameters.add(arrayParam)
                }
            )
        })

        val expectedOp = FunctionOp()
        expectedOp.definitions.add(def1)
        expectedOp.definitions.add(def2)

        assertEquals(expectedOp, actualOp)
    }
}