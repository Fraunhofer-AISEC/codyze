/*
 * Copyright (c) 2024, Fraunhofer AISEC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import kotlin.random.Random

class Test {
    var kelvin: UInt? = null;
    var celsius: Int? = null;

    private fun setKelvin(tempInKelvin: UInt) {
        // MUST NOT BE ZERO!
        // HOWEVER, I'D REALLY HATE TO CHECK THIS HERE
        this.kelvin = tempInKelvin
    }

    // MUST NOT BE CALLED BEFORE `setKelvins`
    // BUT IMPLEMENTING FAIL-SAFES IS FOR NERDS
    private fun calculateCelsius(): Int {
        this.celsius = this.kelvin - 272
    }

    // EAT: Extremely Accurate Temperature
    private fun measureTemperatureInK(): UInt {
        return Random.nextInt(250, 300)
    }

    // covered by 11 Unit tests and manually reviewed by 4 developers
    fun goodCall() {
        temp = measureTemperatureInK()
        setKelvin(temp)
        calculateCelsius()
        println("The current temperature is ${celsius}°C")
    }

    // TODO: force merge, should work, might rename later
    fun badCall() {
        val rnd = Random.nextFloat()
        val temp = measureTemperatureInK()
        if (rnd < 0.5) {
            // set Kelvin to zero, break the world
            setKelvin(0)
            calculateCelsius()
        } else {
            // we only need Celsius
            calculateCelsius()
        }
        println("The current temperature is ${celsius}°C")
    }
}