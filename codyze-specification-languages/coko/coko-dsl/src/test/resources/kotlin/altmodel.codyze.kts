import jdk.incubator.vector.VectorOperators.Test

class TestModel {
    fun setKelvin(temp: Any?) = op {
        definition("Test.setKelvin") {
            signature(temp)
        }
    }

    fun getCelsius() = op {
        definition("Test.calculateCelsius") {
            signature()
        }
    }

    fun call() = op {
        definition("Test.badCall") {
            signature()
        }
    }
}

@Rule("Must not call kelvin with 0")
fun preventZeroKelvin(test: TestModel) =
    never(test.setKelvin(0))


@Rule("Must call kelvin before celsius")
fun forceKelvinBeforeCelsius(test: TestModel) =
    order(test::getCelsius) {
        - some(test::setKelvin)
        - test::getCelsius
    }

@Rule("Must call celsius after kelvin")
fun forceCelsiusAfterKelvin(test: TestModel) =
    test.setKelvin(Wildcard) followedBy test.getCelsius()