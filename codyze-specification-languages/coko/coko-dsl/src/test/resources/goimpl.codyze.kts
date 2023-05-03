@file:Import("model.codyze.kts")

import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.*
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberExpression

plugins { id("cpg") }

class GolangLogging : Model_codyze.Logging {
    // TODO(oxisto): arguments flow into
    override fun log(message: String?, vararg args: Any?) = op {
        definition("log.Printf") { signature { unordered(*args) } }
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
            val subjects = cpgMemberExpr {
                this.base.name.localName == "RegisteredClaims" && name.localName == "Subject"
            }
            val claims = cpg.calls("Value").filter {
                it.arguments[0].type.name.localName == "ContextKey"
            }

            val user = subjects.filter {
                claims.cpgFlowsTo((it.base as MemberExpression).base)
            }
            return user
        }
}
