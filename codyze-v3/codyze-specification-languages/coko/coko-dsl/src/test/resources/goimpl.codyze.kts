@file:Import("model.codyze.kts")

import de.fraunhofer.aisec.cpg.graph.*
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberExpression

class GolangLogging : Logging {
    // TODO(oxisto): arguments flow into
    override fun log(message: String, vararg args: Any) =
        callFqn("log.Printf") { args.all { it flowsTo arguments } }
}

class Gorm : ObjectRelationalMapper {
    override fun insert(obj: Any) = callFqn("db.Create") { obj flowsTo arguments[0] }
}

class GoJWTUserContext : UserContext {
    // TODO(oxisto): FQNs are "slightly" broken in Go
    // TODO(oxisto): can we do this in a shorter syntax?
    // TODO(oxisto): Do we really need to select "all" nodes here? for example we might just be
    //  interested in the user context of the current function
    override val user: List<Node>
        get() {
            val subjects = memberExpr { base.name == "RegisteredClaims" && name == "Subject" }
            val claims =
                callFqn("Context.Value") { arguments[0].name == "jwtmiddleware.ContextKey" }

            return subjects.filter { claims flowsTo (it.base as MemberExpression).base }
        }
}
