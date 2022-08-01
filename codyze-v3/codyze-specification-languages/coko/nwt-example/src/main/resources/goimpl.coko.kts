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
