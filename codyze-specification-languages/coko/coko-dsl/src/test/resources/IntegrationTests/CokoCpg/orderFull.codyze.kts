class CokoOrderImpl {
    fun constructor(value: Int?) = constructor("Botan") { signature(value) }
    fun init() = op { "Botan.set_key" { signature(Wildcard) } }
    fun start() = op { "Botan.start" { signature(Wildcard) } }
    fun finish() = op { "Botan.finish" { signature(Wildcard) } }
}

@Rule("simple order evaluation")
fun `validate CokoOrderImpl usage order`(testObj: CokoOrderImpl) =
    order(testObj::constructor) {
        - testObj::start
        - testObj::finish
    }
