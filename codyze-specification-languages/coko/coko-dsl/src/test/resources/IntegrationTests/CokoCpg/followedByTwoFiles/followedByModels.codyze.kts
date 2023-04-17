interface LoggingForTest {
    fun log(message: String?, vararg args: Any?): Op
}

interface ObjectRelationalMapperForTest {
    fun insert(obj: Any?): Op
}

interface UserContextForTest {
    val user: Any
}

// TODO: can we "assert" that the ctx.user is not null here?
@Rule("This is a dummy description.")
fun DBActionsAreAlwaysLogged(db: ObjectRelationalMapperForTest, log: LoggingForTest, ctx: UserContextForTest) =
    db.insert(Wildcard) followedBy log.log(".*", ctx.user)
