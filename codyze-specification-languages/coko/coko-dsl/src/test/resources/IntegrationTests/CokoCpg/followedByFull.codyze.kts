plugins { id("cpg") }

interface Logging {
    fun log(message: String?, vararg args: Any?): Op
}

interface ObjectRelationalMapper {
    fun insert(obj: Any?): Op
}

interface UserContext {
    val user: Any
}

// TODO: can we "assert" that the ctx.user is not null here?
@Rule("This is a dummy description.")
fun DBActionsAreAlwaysLogged(db: ObjectRelationalMapper, log: Logging, ctx: UserContext) =
    db.insert(Wildcard) followedBy log.log(".*", ctx.user)


class JavaLogging : Logging {
    override fun log(message: String?, vararg args: Any?) = op {
        "java.util.logging.Logger.info" {
            signature {
                group {
                    +message
                    args.forEach { +it }
                }
            }
        }
    }
}

class JDBC : ObjectRelationalMapper {
    override fun insert(obj: Any?) = op {
        "java.sql.Statement.executeUpdate" {
            signature {
                group {
                    +"INSERT.*"
                    +obj
                }
            }
        }
    }
}

class JavalinJWT : UserContext {
    override val user = cpgCallFqn("javalinjwt.JavalinJWT.getTokenFromHeader")
}
