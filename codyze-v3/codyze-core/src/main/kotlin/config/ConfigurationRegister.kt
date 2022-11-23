package de.fraunhofer.aisec.codyze_core.config

import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parameters.options.OptionDelegate
import kotlin.reflect.KProperty
import org.koin.core.component.KoinComponent

/**
 * Helper singleton responsible for registering CLI options that should be passed to the
 * [Configuration] class
 */
class ConfigurationRegister : KoinComponent {
    private val options = mutableMapOf<String, Any?>()
    val configurationMap by lazy {
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

    fun addOption(name: String, option: Option) {
        options[name] = option
    }

    private data class LazyPropertyInitializer(
        val lazyProperty: Lazy<*>,
        val thisRef: Any,
        val property: KProperty<*>
    )
    fun addLazyOption(name: String, lazyProperty: Lazy<*>, thisRef: Any, property: KProperty<*>) {
        options[name] = LazyPropertyInitializer(lazyProperty, thisRef, property)
    }
}
