@file:Import("model.codyze.kts")

class PythonLogging : Logging {
    override fun log(message: String, vararg args: Any) =
        // logging.info(message, args)
        // We don't care about the order of the arguments. Just make sure that all objects in "args"
        // somehow flow into the log message/args.
        callFqn("logging.info") {
            message flowsTo arguments[0] && args.all { it flowsTo arguments }
        }
}

class Sqlite3 : ObjectRelationalMapper {
    override fun insert(`object`: Any) =
        callFqn("sqlite3.Cursor.execute") {
            "INSERT.*" flowsTo arguments[0] && `object` flowsTo arguments[0]
        }
}

class FlaskJWTUserContext : UserContext {
    override val user = variable("current_identity")
}
