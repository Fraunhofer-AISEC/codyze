package de.fraunhofer.aisec.codyze_core.config

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
