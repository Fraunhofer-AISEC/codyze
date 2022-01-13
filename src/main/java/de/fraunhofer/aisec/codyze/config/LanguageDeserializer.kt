package de.fraunhofer.aisec.codyze.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.io.IOException
import java.util.*
import org.slf4j.LoggerFactory

/** Custom deserializer for languages to turn them directly into enums */
class LanguageDeserializer : StdDeserializer<EnumSet<Language>?> {
    constructor() : super(null as JavaType?) {}

    @Throws(IOException::class)
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): EnumSet<Language>? {
        val result = EnumSet.noneOf(Language::class.java)
        var current = jp.currentToken
        if (current == JsonToken.START_ARRAY) {
            current = jp.nextToken()
            while (current != JsonToken.END_ARRAY) {
                if (current == JsonToken.VALUE_STRING) {
                    val s = jp.valueAsString
                    try {
                        result.add(Language.valueOf(s.uppercase(Locale.getDefault())))
                    } catch (e: IllegalArgumentException) {
                        log.warn(
                            "Could not parse configuration file correctly because " +
                                "\"{}\" is not a supported programming language. " +
                                "Continue with parsing rest of configuration file.",
                            s
                        )
                    }
                }
                current = jp.nextToken()
            }
        }
        return result
    }

    companion object {
        private val log = LoggerFactory.getLogger(LanguageDeserializer::class.java)
    }
}
