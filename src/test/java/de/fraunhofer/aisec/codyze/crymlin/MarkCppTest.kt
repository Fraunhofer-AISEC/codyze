package de.fraunhofer.aisec.codyze.crymlin

import java.lang.Exception
import kotlin.Throws
import org.junit.jupiter.api.*

internal class MarkCppTest : AbstractMarkTest() {
    @Test
    @Throws(Exception::class)
    fun nested_markvars() {
        val findings = performTest("mark_cpp/nested_markvars.cpp", "mark_cpp/nested_markvars.mark")
        expected(findings, "line 27: Rule SomethingSomething verified")
    }

    @Test
    @Throws(Exception::class)
    fun functioncall() {
        val findings = performTest("mark_cpp/functioncall.cpp", "mark_cpp/functioncall.mark")
        expected(
            findings,
            "line 9: Rule HasBeenCalled violated",
            "line 7: Rule HasBeenCalled verified"
        )
    }

    @Test
    @Throws(Exception::class)
    fun testNewExpression() {
        val findings = performTest("mark_cpp/new.cpp", "mark_cpp/new.mark")
        expected(findings, "line 10: Rule MustBeOne violated")
    }

    @Test
    @Throws(Exception::class)
    fun functioncallComplex() {
        val findings =
            performTest("mark_cpp/functioncall_complex.cpp", "mark_cpp/functioncall_complex.mark")
        expected(
            findings,
            "line [11, 12]: Rule Local verified",
            "line [17, 18]: Rule Local violated",
            "line [11, 12]: Rule Global verified",
            "line [12, 17]: Rule Global violated",
            "line [11, 18]: Rule Global violated",
            "line [17, 18]: Rule Global violated"
        )
    }

    @Test
    @Disabled(
        "requires interprocedural context-insensitive dataflow analysis for constant resolution."
    )
    @Throws(Exception::class)
    fun _01_assign() {
        val findings = performTest("mark_cpp/01_assign.cpp", "mark_cpp/01_assign.mark")
        println("All findings:")
        for (f in findings) {
            println(f.toString())
        }
        Assertions.assertFalse(true) // new tests!
    }

    @Test
    @Throws(Exception::class)
    fun _02_arg() {
        val findings = performTest("mark_cpp/02_arg.cpp", "mark_cpp/02_arg.mark")
        expected(
            findings,
            "line 13: Rule NotThree violated",
            "line 13: Rule SomethingAboutFoo verified"
        )
    }

    @Test
    @Disabled(
        "requires interprocedural context-insensitive dataflow analysis of function argument for constant resolution."
    )
    @Throws(Exception::class)
    fun _03_arg_as_param() {
        val findings = performTest("mark_cpp/03_arg_as_param.cpp", "mark_cpp/03_arg_as_param.mark")
        println("All findings:")
        for (f in findings) {
            println(f.toString())
        }
        Assertions.assertFalse(true) // new tests!
    }

    @Test
    @Throws(Exception::class)
    fun arg_prevassign_int() {
        val findings = performTest("mark_cpp/arg_prevassign_int.cpp", "mark_cpp/int.mark")
        expected(findings, "line 14: Rule SomethingAboutFoo violated")
    }

    @Test
    @Throws(Exception::class)
    fun arg_prevassign_bool() {
        val findings = performTest("mark_cpp/arg_prevassign_bool.cpp", "mark_cpp/bool.mark")
        expected(findings, "line 14: Rule SomethingAboutFoo verified")
    }

    @Test
    @Throws(Exception::class)
    fun arg_prevassign_string() {
        val findings = performTest("mark_cpp/arg_prevassign_string.cpp", "mark_cpp/string.mark")
        expected(findings, "line 15: Rule SomethingAboutFoo verified")
    }

    @Test
    @Throws(Exception::class)
    fun arg_vardecl_int() {
        val findings = performTest("mark_cpp/arg_vardecl_int.cpp", "mark_cpp/int.mark")
        expected(findings, "line 12: Rule SomethingAboutFoo verified")
    }

    @Test
    @Throws(Exception::class)
    fun arg_vardecl_bool() {
        val findings = performTest("mark_cpp/arg_vardecl_bool.cpp", "mark_cpp/bool.mark")
        expected(findings, "line 12: Rule SomethingAboutFoo verified")
    }

    @Test
    @Throws(Exception::class)
    fun arg_vardecl_string() {
        val findings = performTest("mark_cpp/arg_vardecl_string.cpp", "mark_cpp/string.mark")
        expected(findings, "line 13: Rule SomethingAboutFoo verified")
    }

    @Test
    @Throws(Exception::class)
    fun arg_assignconstructor_int() {
        val findings = performTest("mark_cpp/arg_assignconstructor_int.cpp", "mark_cpp/int.mark")
        expected(findings, "line 16: Rule SomethingAboutFoo verified")
    }

    @Test
    @Throws(Exception::class)
    fun arg_assignparenthesisexpr_int() {
        val findings =
            performTest("mark_cpp/arg_assignparenthesisexpr_int.cpp", "mark_cpp/int.mark")
        println("All findings:")
        for (f in findings) {
            println(f.toString())
        }
        expected(
            findings,
            "line 19: Rule SomethingAboutFoo verified",
            "line 28: Rule SomethingAboutFoo verified"
        )
    }

    @Test
    @Throws(Exception::class)
    fun arg_initializerparenthesisexpr_int() {
        val findings =
            performTest("mark_cpp/arg_initializerparenthesisexpr_int.cpp", "mark_cpp/int.mark")
        expected(findings, "line 19: Rule SomethingAboutFoo verified")
    }

    @Test
    @Throws(Exception::class)
    fun arg_uniforminitializer_int() {
        val findings = performTest("mark_cpp/arg_uniforminitializer_int.cpp", "mark_cpp/int.mark")
        expected(findings, "line 16: Rule SomethingAboutFoo verified")
    }

    @Test
    @Throws(Exception::class)
    fun const_value() {
        val findings = performTest("mark_cpp/const.cpp", "mark_cpp/const.mark")

        // todo: missing: Enum is not handled yet
        expected(
            findings,
            "line [13, 32]: Rule Static verified",
            "line [13, 33]: Rule Static violated",
            "line [13, 31]: Rule Static verified"
        )
    }
}
