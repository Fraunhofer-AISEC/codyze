
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.structures.ConstantValue;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConstantValueTest {

	@Test
	void testTryOfInteger() {
		int test = 42;
		Optional<ConstantValue> res = ConstantValue.tryOf(test);
		assertEquals(res.get().getValue(), test);
	}

	@Test
	void testTryOfFloat() {
		float test = 42f;
		Optional<ConstantValue> res = ConstantValue.tryOf(test);
		assertEquals(res.get().getValue(), test);
	}

	@Test
	void testTryOfBoolean() {
		Boolean test = true;
		Optional<ConstantValue> res = ConstantValue.tryOf(test);
		assertEquals(res.get().getValue(), test);
	}

	@Test
	void testTryOfString() {
		String test = "test";
		Optional<ConstantValue> res = ConstantValue.tryOf(test);
		assertEquals(res.get().getValue(), test);
	}
}
