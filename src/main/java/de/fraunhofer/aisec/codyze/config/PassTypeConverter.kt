package de.fraunhofer.aisec.codyze.config

import de.fraunhofer.aisec.cpg.passes.Pass
import org.slf4j.LoggerFactory
import picocli.CommandLine

class PassTypeConverter : CommandLine.ITypeConverter<Pass> {
    private val log = LoggerFactory.getLogger(PassTypeConverter::class.java)

    @Throws(Exception::class)
    override fun convert(value: String): Pass? {
        var result: Pass? = null
        try {
            result = convertHelper(value)
        } catch (e: ClassNotFoundException) {
            log.warn(
                "CLI error: {} is not a known class. Continue with parsing rest of configuration file.",
                e.toString()
            )
        } catch (e: ReflectiveOperationException) {
            log.warn("CLI error: {}. Continue parsing arguments.", e.toString())
        }
        return result
    }

    @Throws(ReflectiveOperationException::class)
    fun convertHelper(className: String): Pass {
        val clazz = Class.forName(className)
        return if (clazz == Pass::class.java)
            throw InstantiationException("Pass is an abstract class and cannot be instantiated")
        else if (Pass::class.java.isAssignableFrom(clazz))
            clazz.getDeclaredConstructor().newInstance() as Pass
        else throw ReflectiveOperationException("$className is not a CPG Pass")
    }
}
