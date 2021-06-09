
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.structures.ConstantValue;
import de.fraunhofer.aisec.analysis.structures.ErrorValue;
import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.analysis.structures.ListValue;
import de.fraunhofer.aisec.crymlin.builtin.BuiltinHelper;
import de.fraunhofer.aisec.crymlin.builtin.InvalidArgumentException;
import org.apache.tinkerpop.gremlin.structure.util.detached.DetachedVertex;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.fail;

class BuiltInTest extends AbstractMarkTest {

	@Test
	void split_1() throws Exception {
		Set<Finding> findings = performTest("mark_cpp/simplesplit_splitstring.cpp", "mark_cpp/splitstring.mark");

		expected(findings,
			"line 26: Rule SPLIT_FIRSTELEMENT_EQUALS_AES violated",
			"line 17: Rule SPLIT_FIRSTELEMENT_EQUALS_AES verified",
			"line 17: Rule SPLIT_SECONDELEMENT_EQUALS_FIRST violated",
			"line 26: Rule SPLIT_SECONDELEMENT_EQUALS_FIRST verified");
	}

	@Test
	void is_instance_1() throws Exception {
		Set<Finding> findings = performTest("mark_cpp/simple_instancestring.cpp", "mark_cpp/instancestring.mark");

		expected(findings,
			"line 17: Rule HasBeenCalled verified");
	}

	@Test
	void eog_connection_1() throws Exception {
		Set<Finding> findings = performTest("mark_cpp/simple_eog_connection.cpp", "mark_cpp/eog_connection.mark");

		expected(findings,
			"line [22, 24]: Rule ControlFlow violated",
			"line [33, 37]: Rule ControlFlow verified",
			"line [45, 46]: Rule ControlFlow verified");
	}

	@Test
	void direct_eog_connection_1() throws Exception {
		Set<Finding> findings = performTest("mark_cpp/simple_eog_connection.cpp", "mark_cpp/direct_eog_connection.mark");

		expected(findings,
			"line [22, 24]: Rule ControlFlow violated",
			"line [33, 37]: Rule ControlFlow violated",
			"line [45, 46]: Rule ControlFlow verified");
	}

	@Test
	void dimensionLengthJava() throws Exception {
		Set<Finding> findings = performTest("mark_java/length.java", "mark_java/length.mark");

		expected(findings,
			"line 13: Rule LENGHTRULE violated",
			"line 10: Rule LENGHTRULE verified");
	}

	@Test
	void isJava() throws Exception {
		Set<Finding> findings = performTest("mark_java/is.java", "mark_java/is.mark");

		expected(findings,
			"line 22: Rule FooBar violated",
			"line 17: Rule FooBar verified");
	}

	@Test
	void hasValueJava() throws Exception {
		Set<Finding> findings = performTest("mark_java/has_value.java", "mark_java/has_value.mark");

		expected(findings,
			"line 17: Rule Bar violated",
			"line 21: Rule Foo verified");
	}

	@Test
	void extractResponsibleVertices() {

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
	void verifyArgumentTypesOrThrow() throws InvalidArgumentException {
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

	@Test
	void testSplitMatch() throws Exception {
		var findings = performTest("builtins/split_match.c", "builtins/split_match.mark");

		expected(findings,
			"line 2: Rule split_match_1 verified",
			"line 2: Rule split_match_2 verified",
			"line 2: Rule split_match_3 violated",
			"line 2: Rule split_match_4 violated");
	}

}
