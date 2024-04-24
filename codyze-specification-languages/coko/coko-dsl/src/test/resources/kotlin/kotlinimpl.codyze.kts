@file:Import("model.codyze.kts")

plugins { id("cpg") }

class KotlinKelvin: SetKelvin {
    override fun kelvin(
        temp: Int?
    ) = op {
        definition("Test.setKelvin") {
            signature(temp)
        }
    }
}

class KotlinCelsius: SetCelsius {
    override fun celsius() = op {
        definition("Test.calculateCelsius") {
            signature()
        }
    }
}

class BadCall: Call {
    override fun call() = op {
        definition("Test.badCall") {
            signature()
        }
    }
}

