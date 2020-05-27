
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.structures.ConstantValue;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConstantValueTest {

	@Test
	public void testTryOfInteger() {
		int test = 42;
		Optional<ConstantValue> res = ConstantValue.tryOf(test);
		assertEquals(res.get().getValue(), test);
	}

	@Test
	public void testTryOfFloat() {
		float test = 42f;
		Optional<ConstantValue> res = ConstantValue.tryOf(test);
		assertEquals(res.get().getValue(), test);
	}

	@Test
	public void testTryOfBoolean() {
		Boolean test = true;
		Optional<ConstantValue> res = ConstantValue.tryOf(test);
		assertEquals(res.get().getValue(), test);
	}

	@Test
	public void testTryOfString() {
		String test = "test";
		Optional<ConstantValue> res = ConstantValue.tryOf(test);
		assertEquals(res.get().getValue(), test);
	}
}
