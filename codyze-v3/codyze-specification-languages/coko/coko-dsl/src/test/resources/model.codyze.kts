import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Rule
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.follows
import de.fraunhofer.aisec.cpg.graph.Node

interface Logging {
    fun log(message: String, varargs: Any): List<Node>
}

interface ObjectRelationalMapper {
    fun insert(`object`: Any): List<Node>
}

interface UserContext {
    val user: Any
}

@Rule("This is a dummy description.")
fun DBActionsAreAlwaysLogged(db: ObjectRelationalMapper, log: Logging, ctx: UserContext) {
    db.insert(Any()) follows log.log("*", ctx::user)
}
