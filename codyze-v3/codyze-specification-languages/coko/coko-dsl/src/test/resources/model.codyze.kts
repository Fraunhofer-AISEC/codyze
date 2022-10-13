interface Logging {
    fun log(message: String, vararg args: Any): Op
}

interface ObjectRelationalMapper {
    fun insert(obj: Any): Op
}

interface UserContext {
    val user: Any
}

// TODO: can we "assert" that the ctx.user is not null here?
@Rule("This is a dummy description.")
fun DBActionsAreAlwaysLogged(db: ObjectRelationalMapper, log: Logging, ctx: UserContext) =
    db.insert(Wildcard) follows log.log(".*", ctx.user)
