@file:Import("followedBy.concepts")

plugins { id("cpg") }

class JavaLoggingTest : LoggingForTest {
    override fun log(message: Any?, vararg args: Any?) = op {
        definition("java.util.logging.Logger.info") {
            signature {
                group {
                    - message
                    args.forEach { - it }
                }
            }
        }
    }
}

class JDBCTest : ObjectRelationalMapperForTest {
    override fun insert(obj: Any?) = op {
        definition("java.sql.Statement.executeUpdate") {
            signature {
                group {
                    - "INSERT.*"
                    - obj
                }
            }
        }
    }
}

class JavalinJWTTest : UserContextForTest {
    override val user = cpgCallFqn("javalinjwt.JavalinJWT.getTokenFromHeader")
}
