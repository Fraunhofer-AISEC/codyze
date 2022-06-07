package de.fraunhofer.aisec.codyze_core.config.converters

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.io.File

/**
 * Custom deserializer to parse a File derived from the string from the Jackson parser making it
 * relative to the configuration file
 *
 * Taken from
 * https://stackoverflow.com/questions/18313323/how-do-i-call-the-default-deserializer-from-a-custom-deserializer-in-jackson
 */
class FileDeserializer(
    private val configFile: File,
    private val defaultDeserializer: JsonDeserializer<*>
) : StdDeserializer<File?>(File::class.java) {

    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): File? {
        val result = defaultDeserializer.deserialize(p, ctxt)
        if (result is File)
            return if (result.isAbsolute) result
            else File(configFile.absoluteFile.parentFile.absolutePath, result.path)
        return null
    }
}
