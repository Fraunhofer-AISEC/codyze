@file:Import("orderImplementations.codyze.kts")

@Rule("simple order evaluation")
fun `validate CokoOrderImpl usage order`(testObj: CokoOrderImpl) =
    order(testObj::constructor) {
        +testObj::start
        +testObj::finish
    }