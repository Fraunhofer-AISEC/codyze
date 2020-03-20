
package de.fraunhofer.aisec.crymlin;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.server.AnalysisServer;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.analysis.structures.Finding;
import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MarkJavaTest extends AbstractMarkTest {

	@BeforeEach
	public void clearDatabase() {
		// Make sure we start with a clean (and connected) db
		try {
			OverflowDatabase.getInstance().purgeDatabase();
		}
		catch (Throwable e) {
			e.printStackTrace();
			assumeFalse(true); // Assumption for this test not fulfilled. Do not fail but bail.
		}
	}

	@Test
	public void split_1() throws Exception {
		Set<Finding> findings = performTest("mark_java/simplesplit_splitstring.java", "mark_java/splitstring.mark");

		System.out.println("All findings:");
		for (Finding f : findings) {
			System.out.println(f.toString());
		}

		String[] expectedFindings = new String[] {
				"line 23: MarkRuleEvaluationFinding: Rule SPLIT_FIRSTELEMENT_EQUALS_AES violated",
				"line 14: MarkRuleEvaluationFinding: Rule SPLIT_FIRSTELEMENT_EQUALS_AES verified",
				"line 14: MarkRuleEvaluationFinding: Rule SPLIT_SECONDELEMENT_EQUALS_FIRST violated",
				"line 23: MarkRuleEvaluationFinding: Rule SPLIT_SECONDELEMENT_EQUALS_FIRST verified"
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
	public void is_instance_1() throws Exception {
		Set<Finding> findings = performTest("mark_java/simple_instancestring.java", "mark_java/instancestring.mark");

		System.out.println("All findings:");
		for (Finding f : findings) {
			System.out.println(f.toString());
		}

		String[] expectedFindings = new String[] {
				"line 12: MarkRuleEvaluationFinding: Rule HasBeenCalled verified",
				"line 15: MarkRuleEvaluationFinding: Rule HasBeenCalled verified",
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

}
