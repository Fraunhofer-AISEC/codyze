@file:Import("model.codyze.kts")

class KotlinKelvin: SetKelvin {
    override fun kelvin(
        temp: Any?
    ) = op {
        definition("Test.setKelvin") {
            signature(temp?.withType("UInt") ?: temp)
        }
    }
}

class KotlinCelsius: SetCelsius {
    override fun celsius() = op {
        definition("Test.calculateCelsius") { }
    }
}

class BadCall: Call {
    override fun call() = op {
        definition("Test.badCall") { }
    }
}

