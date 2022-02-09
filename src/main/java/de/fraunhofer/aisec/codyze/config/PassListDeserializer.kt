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
                        printLogWarning(
                            "${e.toString()} is not a know class ${ctxt.instantiationException(Pass::class.java, e).location.toString()}"
                        )
                    } catch (e: ReflectiveOperationException) {
                        printLogWarning(
                            "${e.toString()} ${ctxt.instantiationException(Pass::class.java, e).location.toString()}"
                        )
                    }
                }
                current = jp.nextToken()
            }
        }
        return result
    }

    private fun printLogWarning(msg: String) {
        log.warn(
            "An error occurred while parsing configuration file: {}. Continue with parsing rest of configuration file.",
            msg
        )
    }
}
