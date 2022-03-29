package de.fraunhofer.aisec.codyze.legacy.config

import de.fraunhofer.aisec.cpg.passes.Pass
import org.slf4j.LoggerFactory
import picocli.CommandLine

/** Custom type converter for fully qualified CPG pass names to their respective objects */
class PassTypeConverter : CommandLine.ITypeConverter<Pass> {
    private val log = LoggerFactory.getLogger(PassTypeConverter::class.java)

    @Throws(Exception::class)
    override fun convert(value: String): Pass? {
        var result: Pass? = null
        try {
            result = convertHelper(value)
        } catch (e: ReflectiveOperationException) {
            log.warn(
                "An error occurred while parsing arguments: {}. Continue with parsing arguments.",
                e.toString()
            )
        }
        return result
    }

    @Throws(ReflectiveOperationException::class)
    fun convertHelper(className: String): Pass {
        try {
            val clazz = Class.forName(className)
            if (Pass::class.java.isAssignableFrom(clazz))
                return clazz.getDeclaredConstructor().newInstance() as Pass
            else throw ReflectiveOperationException("$className is not a CPG Pass")
        } catch (e: InstantiationException) {
            throw InstantiationException("$className cannot be instantiated")
        } catch (e: ClassNotFoundException) {
            throw ClassNotFoundException("$className is not a known class", e)
        }
    }
}
