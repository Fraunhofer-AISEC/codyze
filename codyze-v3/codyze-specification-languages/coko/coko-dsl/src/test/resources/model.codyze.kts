import de.fraunhofer.aisec.cpg.graph.Node

interface Logging {
    fun log(message: String, vararg args: Any): List<Node>
}

interface ObjectRelationalMapper {
    fun insert(obj: Any): List<Node>
}

interface UserContext {
    val user: Any
}

// TODO: can we "assert" that the ctx.user is not null here?
@Rule("This is a dummy description.")
fun DBActionsAreAlwaysLogged(db: ObjectRelationalMapper, log: Logging, ctx: UserContext) =
    db.insert(Wildcard) follows log.log(".*", ctx.user)
