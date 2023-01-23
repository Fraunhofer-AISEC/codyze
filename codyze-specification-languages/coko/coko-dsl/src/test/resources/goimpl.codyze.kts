@file:Import("model.codyze.kts")

plugins { id("cpg") }

class GolangLogging : Logging {
    // TODO(oxisto): arguments flow into
    override fun log(message: String?, vararg args: Any?) = op {
        definition("log.Printf") { signature().unordered(*args) }
    }
}

class Gorm : ObjectRelationalMapper {
    override fun insert(obj: Any?) = op { definition("db.Create") { signature(obj) } }
}

class GoJWTUserContext : UserContext {
    // TODO(oxisto): FQNs are "slightly" broken in Go
    // TODO(oxisto): can we do this in a shorter syntax?
    // TODO(oxisto): Do we really need to select "all" nodes here? for example we might just be
    //  interested in the user context of the current function
    override val user: List<Node>
        get() {
            val subjects = cpgMemberExpr { base.name == "RegisteredClaims" && name == "Subject" }
            val claims =
                cpgCallFqn("Context.Value") { arguments[0].name == "jwtmiddleware.ContextKey" }

            return subjects.filter { claims cpgFlowsTo (it.base as MemberExpression).base }
        }
}
