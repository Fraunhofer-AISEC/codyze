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

import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parameters.options.OptionDelegate
import org.koin.core.component.KoinComponent
import kotlin.reflect.KProperty

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
                            propertyInitializer ->
                        propertyInitializer.lazyProperty.getValue(
                            propertyInitializer.thisRef,
                            propertyInitializer.property
                        )
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
