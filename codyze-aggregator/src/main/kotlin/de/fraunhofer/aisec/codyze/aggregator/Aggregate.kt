/*
 * Copyright (c) 2023, Fraunhofer AISEC. All rights reserved.
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
package de.fraunhofer.aisec.codyze.aggregator

import io.github.detekt.sarif4k.Run

/**
 * A class containing information about the aggregated SARIF run.
 * Each external Tool will be listed as an extension while Codyze functions as the driver.
 */
class Aggregate {
    /**
     * TODO: add internal properties
     */

    fun getAggregate(): Run {
        TODO()
    }

    fun addRun(run: Run): Boolean {
        TODO()
    }

    fun removeRun(run: Run): Boolean {
        TODO()
    }
}