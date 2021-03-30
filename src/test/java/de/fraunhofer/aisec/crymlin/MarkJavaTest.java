
package de.fraunhofer.aisec.crymlin;

import static org.junit.jupiter.api.Assertions.*;

import de.fraunhofer.aisec.analysis.structures.Finding;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class MarkJavaTest extends AbstractMarkTest {

	@Test
	void split_1() throws Exception {
		Set<Finding> findings = performTest("mark_java/simplesplit_splitstring.java", "mark_java/splitstring.mark");

		System.out.println("All findings:");
		for (Finding f : findings) {
			System.out.println(f.toString());
		}

		String[] expectedFindings = new String[] {
				"line 23: Rule SPLIT_FIRSTELEMENT_EQUALS_AES violated",
				"line 14: Rule SPLIT_FIRSTELEMENT_EQUALS_AES verified",
				"line 14: Rule SPLIT_SECONDELEMENT_EQUALS_FIRST violated",
				"line 23: Rule SPLIT_SECONDELEMENT_EQUALS_FIRST verified"
		};

		for (String expected : expectedFindings) {
			assertTrue(1 == findings.stream().filter(f -> f.toString().equals(expected)).count(), "not found: \"" + expected + "\"");
			Optional<Finding> first = findings.stream().filter(f -> f.toString().equals(expected)).findFirst();
			findings.remove(first.get());
		}
		if (findings.size() > 0) {
			System.out.println("Additional Findings:");
			for (Finding f : findings) {
				System.out.println(f.toString());
			}
		}

		assertEquals(0, findings.size());
	}

	@Test
	void is_instance_1() throws Exception {
		Set<Finding> findings = performTest("mark_java/simple_instancestring.java", "mark_java/instancestring.mark");

		System.out.println("All findings:");
		for (Finding f : findings) {
			System.out.println(f.toString());
		}

		String[] expectedFindings = new String[] {
				"line 12: Rule HasBeenCalled verified",
				"line 15: Rule HasBeenCalled verified",
		};

		for (String expected : expectedFindings) {
			assertTrue(1 == findings.stream().filter(f -> f.toString().equals(expected)).count(), "not found: \"" + expected + "\"");
			Optional<Finding> first = findings.stream().filter(f -> f.toString().equals(expected)).findFirst();
			findings.remove(first.get());
		}
		if (findings.size() > 0) {
			System.out.println("Additional Findings:");
			for (Finding f : findings) {
				System.out.println(f.toString());
			}
		}

		assertEquals(0, findings.size());
	}

	@Test
	void const_value() throws Exception {
		Set<Finding> findings = performTest("mark_java/const.java", "mark_java/const.mark");

		// todo: missing: Enum is not handled yet
		expected(findings, "line [17, 3]: Rule Static verified",
			"line [15, 3]: Rule Static verified");

	}

}
