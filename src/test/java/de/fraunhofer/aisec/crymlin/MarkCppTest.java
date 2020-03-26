
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.crymlin.connectors.db.Database;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
	public void functioncallComplex() throws Exception {
		Set<Finding> findings = performTest("mark_cpp/functioncall_complex.cpp", "mark_cpp/functioncall_complex.mark");
		expected(findings,
			"line [11, 12]: MarkRuleEvaluationFinding: Rule Local verified",
			"line [17, 18]: MarkRuleEvaluationFinding: Rule Local violated",

			"line [11, 12]: MarkRuleEvaluationFinding: Rule Global verified",

			"line [12, 17]: MarkRuleEvaluationFinding: Rule Global violated",
			"line [11, 18]: MarkRuleEvaluationFinding: Rule Global violated",
			"line [17, 18]: MarkRuleEvaluationFinding: Rule Global violated");
	}

	@Test
	@Disabled // requires interprocedural context-insensitive dataflow analysis for constant resolution.
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
	@Disabled // requires interprocedural context-insensitive dataflow analysis of function argument for constant resolution.
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
			"line 19: MarkRuleEvaluationFinding: Rule SomethingAboutFoo verified",
			"line 28: MarkRuleEvaluationFinding: Rule SomethingAboutFoo verified");
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

	// FIXME reactivate once https://github.com/Fraunhofer-AISEC/cpg/pull/85 is available in a new CPG-release
	//@Test
	public void const_value() throws Exception {
		Set<Finding> findings = performTest("mark_cpp/const.cpp", "mark_cpp/const.mark");

		// todo: missing: Enum is not handled yet
		expected(findings, "line [13, 32]: MarkRuleEvaluationFinding: Rule Static verified",
			"line [13, 33]: MarkRuleEvaluationFinding: Rule Static violated",
			"line [13, 31]: MarkRuleEvaluationFinding: Rule Static verified");

	}

}
