import de.fraunhofer.aisec.cpg.graph.Node

interface Logging {
    fun log(message: String, vararg args: Any): List<Node>
}

interface ObjectRelationalMapper {
    fun insert(`object`: Any): List<Node>
}

interface UserContext {
    val user: Any
}

@Rule("This is a dummy description.")
fun DBActionsAreAlwaysLogged(db: ObjectRelationalMapper, log: Logging, ctx: UserContext) {
    db.insert(Wildcard) follows log.log(".*", ctx::user)
}
