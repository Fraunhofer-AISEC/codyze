interface Logging: Concept {
    fun log(message: String, varargs: Any)
}

interface ObjectRelationalMapper: Concept {
    fun insert(`object`: Any)
}

interface UserContext: Concept {
    val user: Any
}

@rule
fun `DB actions are always logged`(db: ObjectRelationalMapper, log: Logging, ctx: UserContext) {
    occurance(db::insert) follows occurance(log::log, any(), ctx::user)
}
