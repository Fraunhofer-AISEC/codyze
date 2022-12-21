package de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl.ksp

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import java.io.OutputStream

class AnnotationProcessor(private val codeGenerator: CodeGenerator, val log: KSPLogger) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val (annotations, ret) = resolver
            .getSymbolsWithAnnotation("de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Ensures")
            // we will only work with `KSAnnotated` where `validate()` returns true
            // all `KSAnnotated` where `validate()` returns false are returned
            .partition { it.validate() }

        log.info("Number of @Ensures annotations in code: ${annotations.size}")

        if (annotations.isNotEmpty()) {
            val filesWithEnsures = annotations.map { it.containingFile }.toList().filterNotNull().toTypedArray()

            val file: OutputStream = codeGenerator.createNewFile(Dependencies(true, *filesWithEnsures), "", "Ensures")

            file.append("enum class Ensures {\n")

            val functions = annotations.filterIsInstance<KSFunctionDeclaration>()

            log.info("Number of functions with @Ensures annotations: ${functions.size}")

            functions.forEach { it.accept(Visitor(file), Unit) }

            file.append("}\n")

            file.close()
        }

        return ret
    }

    inner class Visitor(private val file: OutputStream) : KSVisitorVoid() {

        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
            log.info("In visitFunctionDeclaration for function ${function.simpleName}")

            log.info("Function ${function.simpleName} annotations: ${function.annotations.joinToString { it.shortName.asString() }}")

            // There must be an `@Ensures` annotation else we would not be in this visit function
            val annotation = function.annotations.first { it.shortName.asString() == "Ensures" }

            // all `ensure` arguments of `@Ensures` annotation
            val ensures =
                annotation.arguments.asSequence()
                    .filter { it.name?.asString() == "ensure" }
                    .map { it.value }
                    .filterIsInstance<List<*>>()
                    .flatten()
                    .filterNotNull()
                    .toList()

            log.info("found following args for @Ensures: $ensures")

            ensures.forEach { file.append("    $it,\n") }
        }
    }

    /** Helper function that takes a String and not a ByteArray */
    private fun OutputStream.append(s: String) = this.write(s.toByteArray())
}
