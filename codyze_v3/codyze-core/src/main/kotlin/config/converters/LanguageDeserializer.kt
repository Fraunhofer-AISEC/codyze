package de.fraunhofer.aisec.codyze_core.config.converters

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import de.fraunhofer.aisec.codyze.config.Configuration
import de.fraunhofer.aisec.codyze.config.Language
import java.io.IOException
import java.util.*
import org.slf4j.LoggerFactory

/**
 * Custom deserializer to populate a list of languages derived from strings from the Jackson parser
 */
class LanguageDeserializer : StdDeserializer<EnumSet<Language>?>(null as JavaType?) {

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
                        val instantiationError =
                            ctxt.instantiationException(
                                Language::class.java,
                                IllegalArgumentException(
                                    "No support for language with the name \"$s\"",
                                    e
                                )
                            )
                        log.warn(
                            "An error occurred while parsing configuration file{}: {}. Continue with parsing rest of configuration file.",
                            Configuration.getLocation(jp.tokenLocation),
                            instantiationError
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
