concept Logging {
    operation log(message: String, varargs: any)
}

concept ObjectRelationalMapper {
    operation insert(object: any)
}

concept UserContext {
    var user
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