@file:Import("model.codyze.kts")

class GolangLogging : Logging {
    override fun log(message: String, varargs: Any) = call("log.Printf(*)")
}

class Gorm : ObjectRelationalMapper {
    override fun insert(`object`: Any) = call("db.Create($`object`)")
}

class GoJWTUserContext : UserContext {
    override val user = call("http.Request.Context().Value()")
}
