@file:Import("model.codyze.kts")

plugins { id("cpg") }

class JavaLogging : Logging {
    override fun log(message: String, vararg args: Any) = op {
        +definition("java.util.logging.Logger.info") {
            +signature {
                +group {
                    +message
                    args.forEach { +it }
                }
            }
        }
    }
}

class JDBC : ObjectRelationalMapper {
    override fun insert(obj: Any) = op {
        +definition("java.sql.Statement.executeUpdate") {
            +signature {
                +group {
                    +"INSERT.*"
                    +obj
                }
            }
        }
    }
}

class JavalinJWT : UserContext {
    override val user = callFqn("javalinjwt.JavalinJWT.getTokenFromHeader")
}
