interface Logging : Concept {
    fun log(message: String, varargs: Any)
}

interface ObjectRelationalMapper : Concept {
    fun insert(`object`: Any)
}

interface UserContext : Concept {
    val user: Any
}

@Rule
fun `DB actions are always logged`(db: ObjectRelationalMapper, log: Logging, ctx: UserContext) {
    call(db::insert) follows call(log::log, any(), ctx::user)
}
