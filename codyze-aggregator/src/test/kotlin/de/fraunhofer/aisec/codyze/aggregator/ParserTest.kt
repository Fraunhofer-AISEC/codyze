package de.fraunhofer.aisec.codyze.aggregator

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile


class ParserTest {

    /**
     * Tests the parsing of a simple SARIF result file containing a single run
     */
    @Test
    fun parseOneRun() {
        TODO()
    }

    /**
     * Tests the parsing of a SARIF result file containing multiple runs of the same tool
     */
    @Test
    fun parseMultipleRuns() {
        TODO()
    }

    /**
     * Tests the parsing of a SARIF result file containing one run with optional objects
     */
    @Test
    fun parseOneRunComplex() {
        TODO()
    }


    companion object {
        lateinit var exampleResults: Set<File>

        @BeforeAll
        @JvmStatic
        fun loadResources() {
            val resultsDirectory: URL? = ParserTest::class.java.classLoader.getResource("results")

            assertNotNull(resultsDirectory)

            exampleResults = Files.walk(Path.of(resultsDirectory!!.toURI()))
                .filter { it.isRegularFile() }
                .map { it.toFile() }
                .toList().toSet()
        }
    }
}