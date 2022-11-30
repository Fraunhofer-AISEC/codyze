/*
 * Copyright (c) 2022, Fraunhofer AISEC. All rights reserved.
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
package de.fraunhofer.aisec.codyze.core.config

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.parameter.parametersOf

inline fun <reified T> getKoinInstance(noinline params: ParametersDefinition): T {
    return object : KoinComponent { val value: T by inject(parameters = params) }.value
}

/**
 * Build a [Configuration] from the registered options/properties.
 *
 * @param normalize Whether to normalize the [Configuration]. Defaults to [true]
 */
fun buildConfiguration(
    codyzeConfigurationRegister: ConfigurationRegister,
    backendConfigurationRegister: ConfigurationRegister,
    normalize: Boolean = true
) =
    codyzeConfigurationRegister.configurationMap.let {
        val config = Configuration.from(
            map = it,
            backendConfiguration = getKoinInstance {
                parametersOf(
                    *backendConfigurationRegister.configurationMap.values.toTypedArray()
                )
            }
        )
        if (normalize) config.normalize()
        config
    }
