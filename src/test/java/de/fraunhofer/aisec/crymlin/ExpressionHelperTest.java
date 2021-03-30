
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.markevaluation.ExpressionHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExpressionHelperTest {

	@Test
	void testComparableInt() {
		assertEquals("0.0", ExpressionHelper.toComparableString(0));
		assertEquals("0.0", ExpressionHelper.toComparableString(0.0));
		assertEquals("1.0", ExpressionHelper.toComparableString(1));
		assertEquals("-1.0", ExpressionHelper.toComparableString(-1));
		assertEquals("-2147483648.0", ExpressionHelper.toComparableString(Integer.MIN_VALUE));
		assertEquals("2147483647.0", ExpressionHelper.toComparableString(Integer.MAX_VALUE));
	}

	@Test
	void testComparableDouble() {
		assertEquals("0.0", ExpressionHelper.toComparableString(0d));
		assertEquals("0.0", ExpressionHelper.toComparableString(0.0d));
		assertEquals("1.0", ExpressionHelper.toComparableString(1d));
		assertEquals("-1.0", ExpressionHelper.toComparableString(-1d));
		assertEquals("4.9E-324", ExpressionHelper.toComparableString(Double.MIN_VALUE));
		assertEquals("1.7976931348623157E308", ExpressionHelper.toComparableString(Double.MAX_VALUE));
	}

	@Test
	void testComparableString() {
		assertEquals("", ExpressionHelper.toComparableString(""));
		assertEquals("a", ExpressionHelper.toComparableString("a"));
		assertEquals("1.0", ExpressionHelper.toComparableString("1"));
		assertEquals("-1.0", ExpressionHelper.toComparableString("-1"));
	}
}
