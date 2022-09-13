@file:Import("model.codyze.kts")

import de.fraunhofer.aisec.cpg.graph.Node

class GolangLogging : Logging {
    override fun log(message: String, vararg args: Any) = callFqn("log.Printf")
}

class Gorm : ObjectRelationalMapper {
    override fun insert(obj: Any): List<Node> {
        return callFqn("db.Create") { obj flowsTo arguments[0] }
    }
}

class GoJWTUserContext : UserContext {
    override val user = callFqn("http.Request.Context().Value()")
}
