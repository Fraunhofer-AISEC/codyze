@file:Import("model.codyze.kts")

class JavaLogging : Logging {
    override fun log(message: String, vararg args: Any) =
        callFqn("java.util.logging.Logger.info") {
            message flowsTo arguments[0] && args.all { it flowsTo arguments[0] }
        }
}

class JDBC : ObjectRelationalMapper {
    override fun insert(obj: Any) =
        callFqn("java.sql.Statement.executeUpdate") {
            "INSERT.*" flowsTo arguments[0] && obj flowsTo arguments[0]
        }
}

class JavalinJWT : UserContext {
    override val user = callFqn("javalinjwt.JavalinJWT.getTokenFromHeader")
}
