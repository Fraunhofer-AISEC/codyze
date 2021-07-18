
package de.fraunhofer.aisec.codyze.crymlin;

import de.fraunhofer.aisec.codyze.analysis.structures.ConstantValue;
import de.fraunhofer.aisec.codyze.analysis.structures.ErrorValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StructuresTest {

	@Test
	void testConstantValueEquals() {
		ConstantValue nullCV = ErrorValue.newErrorValue("narf");
		ConstantValue otherNullCV = ErrorValue.newErrorValue("other");
		ConstantValue oneCV = ConstantValue.of(1);
		ConstantValue otheroneCV = ConstantValue.of(1);
		ConstantValue twoCV = ConstantValue.of(2);

		assertNotEquals(nullCV, oneCV);
		assertNotEquals(nullCV, otherNullCV);

		assertNotEquals(oneCV, twoCV);
		assertEquals(oneCV, otheroneCV);

		assertNotEquals(oneCV, new Object());
	}

}
