package de.fraunhofer.aisec.codyze.config

import de.fraunhofer.aisec.cpg.passes.Pass
import picocli.CommandLine

class PassTypeConverter : CommandLine.ITypeConverter<Pass> {
    @Throws(Exception::class)
    override fun convert(value: String): Pass {
        return convertHelper(value)
    }

    @Throws(ReflectiveOperationException::class)
    fun convertHelper(className: String): Pass {
        val clazz = Class.forName(className)
        return if (clazz == Pass::class.java)
            throw InstantiationException("Pass is an abstract class and cannot be instantiated")
        else if (Pass::class.java.isAssignableFrom(clazz))
            clazz.getDeclaredConstructor().newInstance() as Pass
        else throw ReflectiveOperationException("$className is not a CPG Pass.")
    }
}
