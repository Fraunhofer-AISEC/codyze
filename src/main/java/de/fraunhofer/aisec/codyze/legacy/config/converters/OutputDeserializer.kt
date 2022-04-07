package de.fraunhofer.aisec.codyze.legacy.config.converters

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.io.File

/**
 * Custom deserializer to parse the output location derived from the string from the Jackson parser
 * making it relative to the configuration file
 *
 * findInjectableValue part taken from
 * https://stackoverflow.com/questions/26879286/jackson-pass-values-to-jsondeserializer
 */
class OutputDeserializer() : StdDeserializer<String?>(String::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): String {
        val configFileBasePath =
            ctxt.findInjectableValue("configFileBasePath", null, null).toString()
        val result = p.readValueAs(String::class.java)
        return if (result != null) {
            if (result == "-" || File(result).isAbsolute) result
            else File(configFileBasePath, result).absolutePath
        } else "findings.sarif"
    }
}
