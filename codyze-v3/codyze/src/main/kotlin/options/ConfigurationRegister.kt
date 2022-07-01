package de.fraunhofer.aisec.codyze.options

import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parameters.options.OptionDelegate
import de.fraunhofer.aisec.codyze_core.config.Configuration
import kotlin.reflect.KProperty

/**
 * Helper singleton responsible for registering CLI options that should be passed to the
 * [Configuration] class
 */
object ConfigurationRegister {
    private val options = mutableMapOf<String, Any?>()
    val configurationMap: Map<String, Any?> by lazy {
        options.mapValues {
            when (it.value) {
                is OptionDelegate<*> -> (it.value as OptionDelegate<*>).value
                is LazyPropertyInitializer ->
                    (it.value as LazyPropertyInitializer).let {
                        it.lazyProperty.getValue(it.thisRef, it.property)
                    }
                else -> it.value
            }
        }
    }

    /**
     * Build a [Configuration] from the registered options/properties.
     *
     * @param normalize Whether to normalize the [Configuration]. Defaults to [true]
     */
    fun toConfiguration(normalize: Boolean = true): Configuration =
        configurationMap.let {
            val config =
                Configuration.from(
                    map = it,
                    cpgConfiguration = Configuration.CPGConfiguration.from(it)
                )
            if (normalize) config.normalize()
            return config
        }

    fun addOption(name: String, option: Option) {
        options.put(name, option)
    }

    private data class LazyPropertyInitializer(
        val lazyProperty: Lazy<*>,
        val thisRef: Any,
        val property: KProperty<*>
    )
    fun addLazy(name: String, lazyProperty: Lazy<*>, thisRef: Any, property: KProperty<*>) {
        options.put(name, LazyPropertyInitializer(lazyProperty, thisRef, property))
    }
}
