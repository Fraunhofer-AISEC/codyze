@file:Import("model.codyze.kts")

class PythonLogging : Logging {
    override fun log(message: String, vararg args: Any) =
    // logging.info(message, args)
    // We don't care about the order of the arguments. Just make sure that all objects in "args"
    // somehow flow into the log message/args.
    op {
        +definition("logging.info") {
            +signature(args) {
                +message
            }
        }
    }
}

class Sqlite3 : ObjectRelationalMapper {
    override fun insert(`object`: Any) = op {
        +definition("sqlite3.Cursor.execute") {
            +signature {
                +group {
                    +"INSERT.*"
                    +`object`
                }
            }
        }
    }
}

class FlaskJWTUserContext : UserContext {
    override val user = variable("current_identity")
}
