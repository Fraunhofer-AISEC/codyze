interface SetKelvin {
    fun kelvin(temp: Any?): Op
}

interface SetCelsius {
    fun celsius(): Op
}

interface Call {
    fun call(): Op
}


@Rule("Must not call kelvin with 0")
fun preventZeroKelvin(kelv: SetKelvin) {
    never(kelv.kelvin(0))
}

@Rule("Must call kelvin before celsius")
fun forceKelvinBeforeCelsius(kelv: SetKelvin, cels: SetCelsius, call: Call) {
    order(call.call()) {
        some(kelv::kelvin)
        maybe(cels::celsius)
    }
}