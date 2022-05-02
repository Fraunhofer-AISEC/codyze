package de.fraunhofer.aisec.codyze.legacy.crymlin

import de.fraunhofer.aisec.codyze.legacy.analysis.*
import de.fraunhofer.aisec.codyze.legacy.analysis.resolution.ConstantValue
import de.fraunhofer.aisec.codyze.legacy.crymlin.builtin.BuiltinHelper
import de.fraunhofer.aisec.codyze.legacy.crymlin.builtin.InvalidArgumentException
import de.fraunhofer.aisec.cpg.graph.Node
import java.lang.Exception
import kotlin.Throws
import org.junit.jupiter.api.*

internal class BuiltInTest : AbstractMarkTest() {
    @Test
    @Throws(Exception::class)
    fun split_1() {
        val findings =
            performTest(
                "legacy/mark_cpp/simplesplit_splitstring.cpp",
                "legacy/mark_cpp/splitstring.mark"
            )
        expected(
            findings,
            "line 26: Rule SPLIT_FIRSTELEMENT_EQUALS_AES violated",
            "line 17: Rule SPLIT_FIRSTELEMENT_EQUALS_AES verified",
            "line 17: Rule SPLIT_SECONDELEMENT_EQUALS_FIRST violated",
            "line 26: Rule SPLIT_SECONDELEMENT_EQUALS_FIRST verified"
        )
    }

    @Test
    @Throws(Exception::class)
    fun is_instance_1() {
        val findings =
            performTest(
                "legacy/mark_cpp/simple_instancestring.cpp",
                "legacy/mark_cpp/instancestring.mark"
            )
        expected(findings, "line 17: Rule HasBeenCalled verified")
    }

    @Test
    @Throws(Exception::class)
    fun eog_connection_1() {
        val findings =
            performTest(
                "legacy/mark_cpp/simple_eog_connection.cpp",
                "legacy/mark_cpp/eog_connection.mark"
            )
        expected(
            findings,
            "line [22, 24]: Rule ControlFlow violated",
            "line [33, 37]: Rule ControlFlow verified",
            "line [45, 46]: Rule ControlFlow verified"
        )
    }

    @Test
    @Throws(Exception::class)
    fun direct_eog_connection_1() {
        val findings =
            performTest(
                "legacy/mark_cpp/simple_eog_connection.cpp",
                "legacy/mark_cpp/direct_eog_connection.mark"
            )
        expected(
            findings,
            "line [22, 24]: Rule ControlFlow violated",
            "line [33, 37]: Rule ControlFlow violated",
            "line [45, 46]: Rule ControlFlow verified"
        )
    }

    @Test
    @Throws(Exception::class)
    fun dimensionLengthJava() {
        val findings = performTest("legacy/mark_java/length.java", "legacy/mark_java/length.mark")
        expected(findings, "line 13: Rule LENGHTRULE violated", "line 10: Rule LENGHTRULE verified")
    }

    @get:Throws(Exception::class)
    @get:Test
    val isJava: Unit
        get() {
            val findings = performTest("legacy/mark_java/is.java", "legacy/mark_java/is.mark")
            expected(findings, "line 22: Rule FooBar violated", "line 17: Rule FooBar verified")
        }

    @Test
    @Throws(Exception::class)
    fun hasValueJava() {
        val findings =
            performTest("legacy/mark_java/has_value.java", "legacy/mark_java/has_value.mark")
        expected(findings, "line 17: Rule Bar violated", "line 21: Rule Foo verified")
    }

    @Test
    fun extractResponsibleVertices() {
        var lv = ListValue()
        var cv1 = ConstantValue.of(2)
        cv1.addResponsibleNodes(Node())
        lv.add(cv1)
        val cv2 = ConstantValue.of(2)
        cv2.addResponsibleNodes(Node())
        lv.add(cv2)
        try {
            // we expect this does not throw
            BuiltinHelper.extractResponsibleNodes(lv, 2)
        } catch (e: InvalidArgumentException) {
            Assertions.fail<Any>()
        }
        try {
            // we expect this throws as we would expect one more argument
            BuiltinHelper.extractResponsibleNodes(lv, 3)
            Assertions.fail<Any>()
        } catch (e: InvalidArgumentException) {
            // ok
        }
        try {
            // we expect this throws as the second ConstantValue has 2 responsiblevertices
            cv2.addResponsibleNodes(Node())
            BuiltinHelper.extractResponsibleNodes(lv, 2)
            Assertions.fail<Any>()
        } catch (e: InvalidArgumentException) {
            // ok
        }
        lv = ListValue()
        cv1 = ConstantValue.of(2)
        lv.add(cv1)
        try {
            // we expect this throws as the responsiblevertex is not availabe
            BuiltinHelper.extractResponsibleNodes(lv, 1)
            Assertions.fail<Any>()
        } catch (e: InvalidArgumentException) {
            // ok
        }
        try {
            // we expect this throws as the second argument is missing
            BuiltinHelper.extractResponsibleNodes(lv, 2)
            Assertions.fail<Any>()
        } catch (e: InvalidArgumentException) {
            // ok
        }
        lv = ListValue()
        cv1 = ErrorValue.newErrorValue("test")
        lv.add(cv1)
        try {
            // we expect this throws as first argument is an ErrorValue
            BuiltinHelper.extractResponsibleNodes(lv, 1)
            Assertions.fail<Any>()
        } catch (e: InvalidArgumentException) {
            // ok
        }
    }

    @Test
    @Throws(InvalidArgumentException::class)
    fun verifyArgumentTypesOrThrow() {
        val lv = ListValue()
        lv.add(ConstantValue.of(2))
        lv.add(ConstantValue.of(3))
        try {
            // we expect this does not throw
            BuiltinHelper.verifyArgumentTypesOrThrow(
                lv,
                ConstantValue::class.java,
                ConstantValue::class.java
            )
        } catch (e: InvalidArgumentException) {
            Assertions.fail<Any>()
        }
        lv.add(ErrorValue.newErrorValue("error"))
        try {
            // we expect this to throw as there is one argument too many
            BuiltinHelper.verifyArgumentTypesOrThrow(
                lv,
                ConstantValue::class.java,
                ConstantValue::class.java
            )
            Assertions.fail<Any>()
        } catch (e: InvalidArgumentException) {
            // ok
        }

        // This is actually okay. We are expecting 3 arguments and receive two Constants and an
        // Error.
        BuiltinHelper.verifyArgumentTypesOrThrow(
            lv,
            ConstantValue::class.java,
            ConstantValue::class.java,
            ConstantValue::class.java
        )

        // we expect this to be ok
        BuiltinHelper.verifyArgumentTypesOrThrow(
            lv,
            ConstantValue::class.java,
            ConstantValue::class.java,
            ErrorValue::class.java
        )
    }

    @Test
    @Throws(Exception::class)
    fun testSplitMatch() {
        val findings =
            performTest(
                "legacy/builtins/split_match_unordered.c",
                "legacy/builtins/split_match_unordered.mark"
            )
        expected(
            findings,
            "line 2: Rule split_match_unordered_1 verified",
            "line 2: Rule split_match_unordered_2 verified",
            "line 2: Rule split_match_unordered_3 verified",
            "line 2: Rule split_match_unordered_4 violated",
            "line 2: Rule split_match_unordered_5 verified",
            "line 2: Rule split_match_unordered_6 violated"
        )
    }

    @Test
    @Throws(Exception::class)
    fun testSplitDisjoint() {
        val findings =
            performTest("legacy/builtins/split_disjoint.c", "legacy/builtins/split_disjoint.mark")
        expected(
            findings,
            "line 2: Rule split_disjoint_1 verified",
            "line 2: Rule split_disjoint_2 verified",
            "line 2: Rule split_disjoint_3 violated"
        )
    }

    @Test
    @Throws(Exception::class)
    fun testNow() {
        val findings = performTest("legacy/builtins/now.c", "legacy/builtins/now.mark")
        expected(findings, "line []: Rule Test verified")
    }

    @Test
    @Throws(Exception::class)
    fun testYear() {
        val findings = performTest("legacy/builtins/year.c", "legacy/builtins/year.mark")
        expected(findings, "line []: Rule Test verified")
    }
}
