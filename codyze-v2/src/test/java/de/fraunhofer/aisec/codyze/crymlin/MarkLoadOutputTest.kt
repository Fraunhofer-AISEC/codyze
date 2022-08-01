package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.markmodel.Mark
import de.fraunhofer.aisec.codyze.markmodel.MarkModelLoader
import de.fraunhofer.aisec.mark.XtextParser
import java.io.*
import java.lang.Exception
import java.lang.StringBuilder
import java.nio.file.Files
import java.nio.file.Paths
import java.util.ArrayList
import java.util.HashMap
import kotlin.Throws
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.jupiter.api.*

internal class MarkLoadOutputTest {
    @Test
    @Throws(Exception::class)
    fun markModelLoaderTest() {
        for ((key, markModel) in allModels) {
            val reconstructed = StringBuilder()
            val entities = ArrayList(markModel.entities)
            if (entities.isNotEmpty()) {
                // they all have the same packed name in our context
                reconstructed.append("package ").append(entities[0].packageName).append("\n")
            }
            for (entity in entities) {
                reconstructed.append(entity.toString())
                reconstructed.append("\n")
            }
            for (rule in markModel.rules) {
                reconstructed.append(rule.toString())
                reconstructed.append("\n")
            }

            // remove identation and comments
            val sanitizedOriginal = StringBuilder()
            var full = String(Files.readAllBytes(Paths.get(key)))
            full = full.replace("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)".toRegex(), "")
            for (line in full.split("\n").toTypedArray()) {
                if (line.trim().isNotEmpty()) {
                    sanitizedOriginal.append(line.trim()).append("\n")
                }
            }
            val sanitizedReconstructed = StringBuilder()
            for (line in reconstructed.toString().split("\n").toTypedArray()) {
                if (line.trim().isNotEmpty()) {
                    sanitizedReconstructed.append(line.trim()).append("\n")
                }
            }
            assertEquals(sanitizedOriginal.toString(), sanitizedReconstructed.toString())
        }
    }

    companion object {
        private val allModels: MutableMap<String?, Mark> = HashMap()
        @BeforeAll
        fun startup() {
            val resource =
                MarkLoadOutputTest::class
                    .java
                    .classLoader
                    .getResource("mark/PoC_MS1/Botan_AutoSeededRNG.mark")
            assertNotNull(resource)
            val markPoC1 = File(resource.file)
            assertNotNull(markPoC1)
            val markModelFiles = markPoC1.parent
            val directories =
                File(markModelFiles).list { _: File?, name: String -> name.endsWith(".mark") }
            assertNotNull(directories)
            val parser = XtextParser()
            for (markFile in directories) {
                val fullName = markModelFiles + File.separator + markFile
                parser.addMarkFile(File(fullName))
            }
            val markModels = parser.parse()
            for (markFile in directories) {
                val fullName = markModelFiles + File.separator + markFile
                allModels[fullName] =
                    MarkModelLoader()
                        .load(markModels, fullName) // only load the model from this file
            }
        }
    }
}
