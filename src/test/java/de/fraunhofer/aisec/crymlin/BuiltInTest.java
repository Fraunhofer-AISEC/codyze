
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.structures.ConstantValue;
import de.fraunhofer.aisec.analysis.structures.ErrorValue;
import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.analysis.structures.ListValue;
import de.fraunhofer.aisec.crymlin.builtin.BuiltinHelper;
import de.fraunhofer.aisec.crymlin.builtin.InvalidArgumentException;
import de.fraunhofer.aisec.crymlin.connectors.db.Database;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import org.apache.tinkerpop.gremlin.structure.util.detached.DetachedVertex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.fail;
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
	public void eog_connection_1() throws Exception {
		Set<Finding> findings = performTest("mark_cpp/simple_eog_connection.cpp", "mark_cpp/eog_connection.mark");

		expected(findings,
			"line [22, 24]: MarkRuleEvaluationFinding: Rule ControlFlow violated",
			"line [33, 37]: MarkRuleEvaluationFinding: Rule ControlFlow verified",
			"line [45, 46]: MarkRuleEvaluationFinding: Rule ControlFlow verified");
	}

	@Test
	public void direct_eog_connection_1() throws Exception {
		Set<Finding> findings = performTest("mark_cpp/simple_eog_connection.cpp", "mark_cpp/direct_eog_connection.mark");

		expected(findings,
			"line [22, 24]: MarkRuleEvaluationFinding: Rule ControlFlow violated",
			"line [33, 37]: MarkRuleEvaluationFinding: Rule ControlFlow violated",
			"line [45, 46]: MarkRuleEvaluationFinding: Rule ControlFlow verified");
	}

	@Test
	public void dimensionLengthJava() throws Exception {
		Set<Finding> findings = performTest("mark_java/length.java", "mark_java/length.mark");

		expected(findings,
			"line 13: MarkRuleEvaluationFinding: Rule LENGHTRULE violated",
			"line 10: MarkRuleEvaluationFinding: Rule LENGHTRULE verified");
	}

	@Test
	public void isJava() throws Exception {
		Set<Finding> findings = performTest("mark_java/is.java", "mark_java/is.mark");

		expected(findings,
			"line 22: MarkRuleEvaluationFinding: Rule FooBar violated",
			"line 17: MarkRuleEvaluationFinding: Rule FooBar verified");
	}

	@Test
	public void hasValueJava() throws Exception {
		Set<Finding> findings = performTest("mark_java/has_value.java", "mark_java/has_value.mark");

		expected(findings,
			"line 17: MarkRuleEvaluationFinding: Rule Bar violated",
			"line 21: MarkRuleEvaluationFinding: Rule Foo verified");
	}

	@Test
	public void extractResponsibleVertices() {

		ListValue lv = new ListValue();
		ConstantValue cv1 = ConstantValue.of(2);
		cv1.addResponsibleVertices(new DetachedVertex(new Object(), "", null));
		lv.add(cv1);

		ConstantValue cv2 = ConstantValue.of(2);
		cv2.addResponsibleVertices(new DetachedVertex(new Object(), "", null));
		lv.add(cv2);

		try {
			// we expect this does not throw
			BuiltinHelper.extractResponsibleVertices(lv, 2);
		}
		catch (InvalidArgumentException e) {
			fail();
		}

		try {
			// we expect this throws as we would expect one more argument
			BuiltinHelper.extractResponsibleVertices(lv, 3);
			fail();
		}
		catch (InvalidArgumentException e) {
			// ok
		}

		try {
			// we expect this throws as the second ConstantValue has 2 responsiblevertices
			cv2.addResponsibleVertices(new DetachedVertex(new Object(), "", null));
			BuiltinHelper.extractResponsibleVertices(lv, 2);
			fail();
		}
		catch (InvalidArgumentException e) {
			// ok
		}

		lv = new ListValue();
		cv1 = ConstantValue.of(2);
		lv.add(cv1);

		try {
			// we expect this throws as the responsiblevertex is not availabe
			BuiltinHelper.extractResponsibleVertices(lv, 1);
			fail();
		}
		catch (InvalidArgumentException e) {
			// ok
		}

		try {
			// we expect this throws as the second argument is missing
			BuiltinHelper.extractResponsibleVertices(lv, 2);
			fail();
		}
		catch (InvalidArgumentException e) {
			// ok
		}

		lv = new ListValue();
		cv1 = ErrorValue.newErrorValue("test");
		lv.add(cv1);

		try {
			// we expect this throws as first argument is an ErrorValue
			BuiltinHelper.extractResponsibleVertices(lv, 1);
			fail();
		}
		catch (InvalidArgumentException e) {
			// ok
		}
	}

	@Test
	public void verifyArgumentTypesOrThrow() throws InvalidArgumentException {
		ListValue lv = new ListValue();
		lv.add(ConstantValue.of(2));
		lv.add(ConstantValue.of(3));

		try {
			// we expect this does not throw
			BuiltinHelper.verifyArgumentTypesOrThrow(lv, ConstantValue.class, ConstantValue.class);
		}
		catch (InvalidArgumentException e) {
			fail();
		}

		lv.add(ErrorValue.newErrorValue("error"));
		try {
			// we expect this to throw as there is one argument too many
			BuiltinHelper.verifyArgumentTypesOrThrow(lv, ConstantValue.class, ConstantValue.class);
			fail();
		}
		catch (InvalidArgumentException e) {
			// ok
		}

		// This is actually okay. We are expecting 3 arguments and receive two Constants and an Error.
		BuiltinHelper.verifyArgumentTypesOrThrow(lv, ConstantValue.class, ConstantValue.class, ConstantValue.class);

		// we expect this to be ok
		BuiltinHelper.verifyArgumentTypesOrThrow(lv, ConstantValue.class, ConstantValue.class, ErrorValue.class);

	}

}
