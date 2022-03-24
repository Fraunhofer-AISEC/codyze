package de.fraunhofer.aisec.codyze.config.converters

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.io.File

/**
 * Custom deserializer to parse a File derived from the string from the Jackson parser making it
 * relative to the configuration file
 */
class FileDeserializer(private val configFile: File) : StdDeserializer<File?>(File::class.java) {

    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): File? {
        val result = super.deserialize(p, ctxt, File(""))
        if (result != null) {
            return if (result.isAbsolute) result else File(configFile.absolutePath + result.path)
        }
        return null
    }
}
