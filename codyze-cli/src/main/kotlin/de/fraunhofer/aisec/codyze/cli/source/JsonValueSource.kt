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
package de.fraunhofer.aisec.codyze.cli.source

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.InvalidFileFormat
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.sources.ValueSource
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import java.nio.file.Path
import kotlin.io.path.*

/**
 * A [ValueSource] that uses Kotlin serialization to parse JSON config files as context to Clikt
 * commands.
 */
class JsonValueSource(
    private val filePath: Path, // path to config file
    private val root: JsonObject,
) : ValueSource {
    override fun getValues(context: Context, option: Option): List<ValueSource.Invocation> {
        /**
         * Preprocess the values read from the config file depending on the metavar (the type) of
         * the option
         */
        // TODO: find better indicator of the type than the metavar
        fun preprocessValue(content: String) =
            when (option.metavar(context = context)) {
                // option.finalize(context=context, invocations =
                // listOf(OptionParser.Invocation(name = "source", values =
                // listOf(cursor[0].jsonPrimitive.content))))  // instead of relying on metavar, it
                // is also possible to parse the option and look at the return type
                "PATH" -> // make all paths given in the config file relative to the path of the
                    // config file
                    filePath
                        .toAbsolutePath() // needed for when the given config file path is a
                        // relative path
                        .parent
                        .div(content) // combine with the path in content
                        .normalize() // remove redundant pieces like '../'
                        .toString()
                else -> content
            }

        var cursor: JsonElement? = root
        val parts =
            option.valueSourceKey?.split(".")
                ?: (context.commandNameWithParents().drop(1) + ValueSource.name(option))
        for (part in parts) {
            if (cursor !is JsonObject) return emptyList()
            cursor = cursor[part]
        }
        if (cursor == null) return emptyList()

        // This implementation interprets a list as multiple invocations, but you could also
        // implement it as a single invocation with multiple values.
        if (cursor is JsonArray) {
            val preprocessedValues = cursor.map { preprocessValue(it.jsonPrimitive.content) }
            return preprocessedValues.map {
                ValueSource.Invocation.value(it)
            } // why is .jsonPrimitive.content needed here? strings are handled weirdly without...
        }
        val preprocessedValues = preprocessValue(cursor.jsonPrimitive.content)
        return ValueSource.Invocation.just(
            preprocessedValues
        ) // why is .jsonPrimitive.content needed here? strings are handled weirdly without...
    }

    companion object {
        fun from(file: Path, requireValid: Boolean = false): JsonValueSource {
            if (!file.isRegularFile()) return JsonValueSource(file, JsonObject(emptyMap()))

            val json =
                try {
                    Json.parseToJsonElement(file.readText()) as? JsonObject
                        ?: throw InvalidFileFormat(file.name, "object expected", 1)
                } catch (e: SerializationException) {
                    if (requireValid) {
                        throw InvalidFileFormat(file.name, e.message ?: "could not read file")
                    }
                    JsonObject(emptyMap())
                }
            return JsonValueSource(file, json)
        }

        fun from(file: String, requireValid: Boolean = false): JsonValueSource =
            from(Path(file), requireValid)
    }
}
