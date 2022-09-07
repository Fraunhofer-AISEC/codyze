@file:Import("model.codyze.kts")

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Import
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression

class PythonLogging : Model_codyze.Logging {
    override fun log(message: String, vararg args: Any): List<CallExpression> {
        // logging.info("*")
        var interestingCalls = callFqn("logging.info", message)

        // TODO: Put proper filter on args here. Won't work as it is now.
        // We don't care about the order of the arguments. Just make sure that all objects in "args" somehow flow into the log message/args.
        interestingCalls =
            interestingCalls.filter { ic ->
                args.all { arg -> ic.arguments.any { matchValues(arg, it) } }
            }

        return interestingCalls
    }
}

class Sqlite3 : Model_codyze.ObjectRelationalMapper {
    override fun insert(`object`: Any): List<CallExpression> {
        // sqlite3.Cursor.execute("INSERT.*")
        var interestingCalls = callFqn("sqlite3.Cursor.execute", "INSERT.*")

        // TODO: Put filter on args here. Won't work as it is now.
        interestingCalls = interestingCalls.filter { matchValues(`object`, it.arguments[0]) }

        return interestingCalls
    }
}

class FlaskJWTUserContext : Model_codyze.UserContext {
    override val user = variable("current_identity")
}
