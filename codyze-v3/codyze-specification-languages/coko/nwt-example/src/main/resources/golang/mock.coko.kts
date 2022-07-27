concept Logging {
    operation log(message: String, varargs: any)
}

concept ObjectRelationalMapper {
    operation insert(object: any)
}

concept UserContext {
    var user
}

implementation GolangLogging {
    operation log(message, varargs...) by {
        call("log.Printf(message, varargs...)")
    }
}

implementation Gorm {
    operation insert(obj) by {
        call("db.Create(obj")
    }
}

implementation GoJWTUserContext {
    var user by {
        value = call("http.Request.Context().Value()")
    }
}

rule AuditLog {
    var db: ObjectRelationalMapper
    var log: Logging
    var ctx: UserContext

    match on db.insert(x) {
        followEOG {
            log.log(_, ctx.user)
        }
    }
}