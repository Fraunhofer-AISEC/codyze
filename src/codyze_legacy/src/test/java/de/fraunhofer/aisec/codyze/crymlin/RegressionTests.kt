package de.fraunhofer.aisec.codyze.crymlin

import java.lang.Exception
import kotlin.Throws
import org.junit.jupiter.api.Test

internal class RegressionTests : AbstractMarkTest() {
    /**
     * Test for arguments of NestedConstructExpressions (Java).
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testNestedConstructExpressionsJava() {
        val findings =
            performTest(
                "legacy/unittests/regression/nested_constructors/NestedConstructor.java",
                "legacy/unittests/regression/nested_constructors/"
            )
        expected(findings, "line 8: Rule PublicKeyInstanceOfVerifier violated")
    }

    /**
     * Test for arguments of NestedConstructExpressions (C++).
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testNestedConstructExpressionsCpp() {
        val findings =
            performTest(
                "legacy/unittests/regression/nested_constructors/nested_constructor.cpp",
                "legacy/unittests/regression/nested_constructors/"
            )
        expected(findings, "line 31: Rule PublicKeyInstanceOfVerifier violated")
    }
}
