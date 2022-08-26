@file:Import("model.codyze.kts")

class PythonLogging : Logging {
    override fun log(message: String, varargs: Any) = call("logging.info(*)")
}

class Sqlite3 : ObjectRelationalMapper {
    override fun insert(`object`: Any) = call("sqlite3.Cursor.execute('INSERT')")
}

class FlaskJWTUserContext : UserContext {
    override val user = variable("current_identity")
}

@Rule("This is a dummy description.")
fun `DB actions are always logged`(db: ObjectRelationalMapper, log: Logging, ctx: UserContext) {
    call(db::insert) follows call(log::log, ctx::user)
}
