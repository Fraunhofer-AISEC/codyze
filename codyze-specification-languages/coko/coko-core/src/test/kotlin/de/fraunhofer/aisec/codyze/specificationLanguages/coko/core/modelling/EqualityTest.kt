package de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.FunctionOp
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.definition
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.op
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.signature
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class EqualityTest {

    @Test
    fun `test definition equality`() {
        with(mockk<FunctionOp>(relaxed = true)) {
            val def1 = definition("Test.test") {
                signature {
                    - "string"
                    - listOf(1,2)
                }
                signature(1,3,5)
                signature {
                    - "h"
                }
            }

            val def2 = definition("Test.test") {
                signature(1,3,5)
                signature {
                    - "h"
                }
                signature {
                    - "string"
                    - listOf(1,2)
                }
            }

            assertEquals(def1,def2)
        }
    }

    @Test
    fun `test FunctionOp equality`() {
        val op1 = op {
            definition("Test.test1") {}
            definition("Test.test2") {}
            definition("Test.test3") {}
        }

        val op2 = op {
            definition("Test.test2") {}
            definition("Test.test1") {}
            definition("Test.test3") {}
        }

        assertEquals(op1,op2)
    }


}