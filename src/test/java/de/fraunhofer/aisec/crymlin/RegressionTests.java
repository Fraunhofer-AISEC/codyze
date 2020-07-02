
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.structures.Finding;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class RegressionTests extends AbstractMarkTest {

	/**
	 * Test for arguments of NestedConstructExpressions (Java).
	 *
	 * @throws Exception
	 */
	@Test
	public void testNestedConstructExpressionsJava() throws Exception {
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
	public void testNestedConstructExpressionsCpp() throws Exception {
		Set<Finding> findings = performTest("unittests/regression/nested_constructors/nested_constructor.cpp", "unittests/regression/nested_constructors/");
		expected(findings,
			"line 31: Rule PublicKeyInstanceOfVerifier violated");
	}
}
