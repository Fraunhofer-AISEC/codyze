@file:Import("followedBy.concepts")

// TODO: can we "assert" that the ctx.user is not null here?
@Rule("This is a dummy description.")
fun DBActionsAreAlwaysLogged(db: ObjectRelationalMapperForTest, log: LoggingForTest, ctx: UserContextForTest) =
    db.insert(Wildcard) followedBy log.log(".*", ctx.user)