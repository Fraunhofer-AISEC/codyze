
package de.fraunhofer.aisec.codyze.crymlin;

import de.fraunhofer.aisec.codyze.analysis.structures.Finding;
import org.junit.jupiter.api.Test;

import java.util.Set;

class RegressionTests extends AbstractMarkTest {

	/**
	 * Test for arguments of NestedConstructExpressions (Java).
	 *
	 * @throws Exception
	 */
	@Test
	void testNestedConstructExpressionsJava() throws Exception {
		Set<Finding> findings = performTest("unittests/regression/nested_constructors/NestedConstructor.java", "unittests/regression/nested_constructors/");
		expected(findings,
			"line 8: Rule PublicKeyInstanceOfVerifier violated");
	}

	/**
	 * Test for arguments of NestedConstructExpressions (C++).
	 *
	 * @throws Exception
	 */
	@Test
	void testNestedConstructExpressionsCpp() throws Exception {
		Set<Finding> findings = performTest("unittests/regression/nested_constructors/nested_constructor.cpp", "unittests/regression/nested_constructors/");
		expected(findings,
			"line 31: Rule PublicKeyInstanceOfVerifier violated");
	}
}
