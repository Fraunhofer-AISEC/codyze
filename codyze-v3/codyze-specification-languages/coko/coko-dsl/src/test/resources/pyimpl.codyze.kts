@file:Import("model.codyze.kts")

import de.fraunhofer.aisec.cpg.graph.evaluate
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression

class PythonLogging : Model_codyze.Logging {
    override fun log(message: String, varargs: Any) =
        if (message == "*") {
            call("logging.info(*)")
        } else {
            call("logging.info(*)")
        }
}

class Sqlite3 : Model_codyze.ObjectRelationalMapper {
    override fun insert(`object`: Any): List<CallExpression> {
        // sqlite3.Cursor.execute("INSERT .*")
        var interestingCalls = callFqn("sqlite3.Cursor.execute") as List<CallExpression>
        interestingCalls =
            interestingCalls.filter { "INSERT " in it.arguments[0].evaluate() as String }

        if (`object`.javaClass != Any().javaClass) {
            // We care about the args
            // TODO: Put filter on args here. Won't work as it is now.
            interestingCalls = interestingCalls.filter { it.arguments[0] == `object` }
        }

        return interestingCalls
    }
}

class FlaskJWTUserContext : Model_codyze.UserContext {
    override val user = variable("current_identity")
}
