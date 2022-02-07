package de.fraunhofer.aisec.codyze.config

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import de.fraunhofer.aisec.cpg.passes.Pass
import java.io.IOException
import org.slf4j.LoggerFactory

class PassListDeserializer : StdDeserializer<List<Pass?>?> {
    private val log = LoggerFactory.getLogger(PassListDeserializer::class.java)
    private val passTypeConverter = PassTypeConverter()

    constructor() : super(MutableList::class.java) {}
    constructor(vc: Class<*>?) : super(vc) {}
    constructor(valueType: JavaType?) : super(valueType) {}
    constructor(src: StdDeserializer<*>?) : super(src) {}

    @Throws(IOException::class, JacksonException::class)
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): List<Pass> {
        val result = ArrayList<Pass>()
        var current = jp.currentToken
        if (current == JsonToken.START_ARRAY) {
            current = jp.nextToken()
            while (current != JsonToken.END_ARRAY) {
                if (current == JsonToken.VALUE_STRING) {
                    val s = jp.valueAsString
                    try {
                        result.add(passTypeConverter.convertHelper(s))
                    } catch (e: ClassNotFoundException) {
                        log.warn(
                            "ConfigFile error: {} is not a known class. Continue with parsing rest of configuration file.",
                            e.toString()
                        )
                    } catch (e: ReflectiveOperationException) {
                        e.printStackTrace()
                        log.warn(
                            "ConfigFile error: {}. Continue with parsing rest of configuration file.",
                            e.toString()
                        )
                    }
                }
                current = jp.nextToken()
            }
        }
        return result
    }
}
