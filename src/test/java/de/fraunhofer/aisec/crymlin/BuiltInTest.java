
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.crymlin.connectors.db.Database;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

public class BuiltInTest extends AbstractMarkTest {

	@BeforeEach
	public void clearDatabase() {
		// Make sure we start with a clean (and connected) db
		try {
			Database db = OverflowDatabase.getInstance();
			db.connect();
			db.purgeDatabase();
		}
		catch (Throwable e) {
			e.printStackTrace();
			assumeFalse(true); // Assumption for this test not fulfilled. Do not fail but bail.
		}
	}

	@Test
	public void split_1() throws Exception {
		Set<Finding> findings = performTest("mark_cpp/simplesplit_splitstring.cpp", "mark_cpp/splitstring.mark");

		expected(findings,
			"line 26: MarkRuleEvaluationFinding: Rule SPLIT_FIRSTELEMENT_EQUALS_AES violated",
			"line 17: MarkRuleEvaluationFinding: Rule SPLIT_FIRSTELEMENT_EQUALS_AES verified",
			"line 17: MarkRuleEvaluationFinding: Rule SPLIT_SECONDELEMENT_EQUALS_FIRST violated",
			"line 26: MarkRuleEvaluationFinding: Rule SPLIT_SECONDELEMENT_EQUALS_FIRST verified");
	}

	@Test
	public void is_instance_1() throws Exception {
		Set<Finding> findings = performTest("mark_cpp/simple_instancestring.cpp", "mark_cpp/instancestring.mark");

		expected(findings,
			"line 17: MarkRuleEvaluationFinding: Rule HasBeenCalled verified");
	}

	@Test
	public void receives_value_from_1() throws Exception {
		Set<Finding> findings = performTest("mark_cpp/simple_receivesvalue.cpp", "mark_cpp/receivesvalue.mark");

		assertEquals(0, findings.size());
	}

}
