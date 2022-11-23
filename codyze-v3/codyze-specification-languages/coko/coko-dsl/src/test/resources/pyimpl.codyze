implementation GolangLogging {
    operation log(message, varargs...) by {
        call("logging.info(message, varargs...)")
    }
}

// This follows PEP-249, so probably should work for others as well
implementation Sqlite3 {
    operation insert(obj) by {
        call("sqlite3.Cursor.execute(msg)") {
            msg contains "INSERT"
        }
    }
}

implementation FlaskJWTUserContext {
    var user by {
        value = variable("current_identity")
    }
}
