import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Rule

interface Logging {
    fun log(message: String, varargs: Any)
}

interface ObjectRelationalMapper {
    fun insert(`object`: Any)
}

interface UserContext {
    val user: Any
}

@Rule("This is a dummy description.")
fun DBActionsAreAlwaysLogged(db: ObjectRelationalMapper, log: Logging, ctx: UserContext) {
    call(db::insert) follows call(log::log, ctx::user)
}
