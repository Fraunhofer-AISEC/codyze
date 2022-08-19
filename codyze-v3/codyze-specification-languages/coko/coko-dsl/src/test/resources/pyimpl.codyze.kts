@file:Import("model.codyze.kts")

class PythonLogging: Logging {
    override fun log(message: String, varargs: Any) = call("logging.info(*)")
}

class Sqlite3: ObjectRelationalMapper {
    val msg = ".*INSERT.*"

    override fun insert(`object`: Any){
        call("sqlite3.Cursor.execute($msg)")
    }
}

class FlaskJWTUserContext: UserContext {
    var user = variable("current_identity")
}
