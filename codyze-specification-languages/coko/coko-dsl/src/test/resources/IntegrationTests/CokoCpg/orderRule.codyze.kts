class CokoOrderImpl {
    fun constructor(value: Int?) = constructor("Botan") { signature(value) }
    fun init() = op { definition("Botan.set_key") { signature(Wildcard) } }
    fun start() = op { definition("Botan.start") { signature(Wildcard) } }
    fun finish() = op { definition("Botan.finish") { signature(Wildcard) } }
}

@Rule("simple order evaluation")
fun `validate CokoOrderImpl usage order`(testObj: CokoOrderImpl) =
    order(testObj::constructor) {
        +testObj::start
        +testObj::finish
    }
