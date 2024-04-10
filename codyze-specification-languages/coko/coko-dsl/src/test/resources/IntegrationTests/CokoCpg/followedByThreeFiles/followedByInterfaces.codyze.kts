interface LoggingForTest {
    fun log(message: String?, vararg args: Any?): Op
}

interface ObjectRelationalMapperForTest {
    fun insert(obj: Any?): Op
}

interface UserContextForTest {
    val user: Any
}
