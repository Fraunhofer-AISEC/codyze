package de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl.host

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Evaluator
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Rule
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.op
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.signature
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertContains

class SpecEvaluatorTest {

    @Test
    fun `test evaluate rule using class with private primary constructor`() {
        val specEvaluator = SpecEvaluator()
        specEvaluator.addSpec(PrivateConstructorSpec::class, PrivateConstructorSpec())
        val exception: Exception =
            Assertions.assertThrows(IllegalArgumentException::class.java) {
                specEvaluator.evaluate()
            }
        val expectedMessage = Regex(
            "Could not create an instance of .*PrivateConstructor to pass to rule " +
                "\"test rule\" because it's primary constructor is not public."
        )
        val actualMessage = exception.message.orEmpty()

        assertContains(actualMessage, expectedMessage)
    }

    @Test
    fun `test evaluate rule using class with parameter in constructor`() {
        val specEvaluator = SpecEvaluator()
        specEvaluator.addSpec(ConstructorWithParamSpec::class, ConstructorWithParamSpec())
        val exception: Exception =
            Assertions.assertThrows(IllegalArgumentException::class.java) {
                specEvaluator.evaluate()
            }
        val expectedMessage = Regex(
            "Could not create an instance of .*WithParam to pass to rule " +
                "\"test rule\" because it's primary constructor expects arguments."
        )
        val actualMessage = exception.message.orEmpty()

        assertContains(actualMessage, expectedMessage)
    }
}

class PrivateConstructorSpec {

    class PrivateConstructor private constructor() {
        fun myOp(i: Any) = op {
            "fqn" {
                signature(i)
            }
        }
    }

    @Rule
    fun `test rule`(privateConstructor: PrivateConstructor) = Evaluator { emptyList() }
}

class ConstructorWithParamSpec {

    class WithParam(val t: Int) {
        fun myOp(i: Any) = op {
            "fqn" {
                signature(i, t)
            }
        }
    }

    @Rule
    fun `test rule`(withParam: WithParam) = Evaluator { emptyList() }
}
