
package de.fraunhofer.aisec.crymlin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.crymlin.connectors.db.Database;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.server.AnalysisServer;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.mark.XtextParser;
import de.fraunhofer.aisec.mark.markDsl.MarkModel;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.*;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assumptions.assumeFalse;

public class MarkCppTest extends AbstractMarkTest {

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
	public void nested_markvars() throws Exception {
		Set<Finding> findings = performTest("mark_cpp/nested_markvars.cpp", "mark_cpp/nested_markvars.mark");
		expected(findings, "line 27: MarkRuleEvaluationFinding: Rule SomethingSomething verified");
	}

	@Test
	public void functioncall() throws Exception {
		Set<Finding> findings = performTest("mark_cpp/functioncall.cpp", "mark_cpp/functioncall.mark");
		expected(findings, "line 9: MarkRuleEvaluationFinding: Rule HasBeenCalled violated",
			"line 7: MarkRuleEvaluationFinding: Rule HasBeenCalled verified");
	}

	@Test
	@Disabled // requires Dataflow analysis
	public void _01_assign() throws Exception {
		Set<Finding> findings = performTest("mark_cpp/01_assign.cpp", "mark_cpp/01_assign.mark");
		System.out.println("All findings:");
		for (Finding f : findings) {
			System.out.println(f.toString());
		}

		assertFalse(true); // new tests!
	}

	@Test
	public void _02_arg() throws Exception {
		Set<Finding> findings = performTest("mark_cpp/02_arg.cpp", "mark_cpp/02_arg.mark");
		expected(findings,
			"line 13: MarkRuleEvaluationFinding: Rule NotThree violated",
			"line 13: MarkRuleEvaluationFinding: Rule SomethingAboutFoo verified");
	}

	@Test
	@Disabled // requires Dataflow analysis
	public void _03_arg_as_param() throws Exception {
		Set<Finding> findings = performTest("mark_cpp/03_arg_as_param.cpp", "mark_cpp/03_arg_as_param.mark");
		System.out.println("All findings:");
		for (Finding f : findings) {
			System.out.println(f.toString());
		}

		assertFalse(true); // new tests!
	}

	@Test
	public void arg_prevassign_int() throws Exception {
		Set<Finding> findings = performTest("mark_cpp/arg_prevassign_int.cpp", "mark_cpp/int.mark");

		expected(findings, "line 14: MarkRuleEvaluationFinding: Rule SomethingAboutFoo violated");
	}

	@Test
	public void arg_prevassign_bool() throws Exception {
		Set<Finding> findings = performTest("mark_cpp/arg_prevassign_bool.cpp", "mark_cpp/bool.mark");

		expected(findings, "line 14: MarkRuleEvaluationFinding: Rule SomethingAboutFoo verified");
	}

	@Test
	public void arg_prevassign_string() throws Exception {
		Set<Finding> findings = performTest("mark_cpp/arg_prevassign_string.cpp", "mark_cpp/string.mark");

		expected(findings, "line 15: MarkRuleEvaluationFinding: Rule SomethingAboutFoo verified");
	}

	@Test
	public void arg_vardecl_int() throws Exception {
		Set<Finding> findings = performTest("mark_cpp/arg_vardecl_int.cpp", "mark_cpp/int.mark");

		expected(findings, "line 12: MarkRuleEvaluationFinding: Rule SomethingAboutFoo verified");
	}

	@Test
	public void arg_vardecl_bool() throws Exception {
		Set<Finding> findings = performTest("mark_cpp/arg_vardecl_bool.cpp", "mark_cpp/bool.mark");

		expected(findings, "line 12: MarkRuleEvaluationFinding: Rule SomethingAboutFoo verified");
	}

	@Test
	public void arg_vardecl_string() throws Exception {
		Set<Finding> findings = performTest("mark_cpp/arg_vardecl_string.cpp", "mark_cpp/string.mark");

		expected(findings, "line 13: MarkRuleEvaluationFinding: Rule SomethingAboutFoo verified");

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

	@Disabled // TODO currently unsupported feature
	@Test
	public void arg_prevassignop_int() throws Exception {
		Set<Finding> findings = performTest("mark_cpp/arg_prevassignop_int.cpp", "mark_cpp/");

		System.out.println("All findings:");
		for (Finding f : findings) {
			System.out.println(f.toString());
		}

		assertFalse(true); // new tests!
	}

	@Test
	public void arg_assignconstructor_int() throws Exception {
		Set<Finding> findings = performTest("mark_cpp/arg_assignconstructor_int.cpp", "mark_cpp/int.mark");

		expected(findings,
			"line 16: MarkRuleEvaluationFinding: Rule SomethingAboutFoo verified");
	}

	@Test
	public void arg_assignparenthesisexpr_int() throws Exception {
		Set<Finding> findings = performTest("mark_cpp/arg_assignparenthesisexpr_int.cpp", "mark_cpp/int.mark");

		System.out.println("All findings:");
		for (Finding f : findings) {
			System.out.println(f.toString());
		}

		expected(findings,
			"line 20: MarkRuleEvaluationFinding: Rule SomethingAboutFoo verified");

		// TODO To be implemented https://***REMOVED***/***REMOVED***/issues/17
		//expected(findings,
		//		 "line 27: MarkRuleEvaluationFinding: Rule SomethingAboutFoo verified");
	}

	@Test
	public void arg_initializerparenthesisexpr_int() throws Exception {
		Set<Finding> findings = performTest("mark_cpp/arg_initializerparenthesisexpr_int.cpp", "mark_cpp/int.mark");

		expected(findings,
			"line 19: MarkRuleEvaluationFinding: Rule SomethingAboutFoo verified");
	}

	@Test
	public void arg_uniforminitializer_int() throws Exception {
		Set<Finding> findings = performTest("mark_cpp/arg_uniforminitializer_int.cpp", "mark_cpp/int.mark");

		expected(findings,
			"line 16: MarkRuleEvaluationFinding: Rule SomethingAboutFoo verified");
	}

	private void expected(Set<Finding> findings, String... expectedFindings) {
		System.out.println("All findings:");
		for (Finding f : findings) {
			System.out.println(f.toString());
		}

		for (String expected : expectedFindings) {
			assertEquals(1, findings.stream().filter(f -> f.toString().equals(expected)).count(), "not found: \"" + expected + "\"");
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
