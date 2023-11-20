package de.fraunhofer.aisec.codyze.aggregator

import io.github.detekt.sarif4k.Run
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.test.Test

class AggregateTest {

    /**
     * Tests the aggregation of a simple SARIF run
     */
    @Test
    fun aggregateSimpleRun() {
        TODO()
    }

    /**
     * Tests the aggregation of a SARIF run containing extension tools
     */
    @Test
    fun aggregateExtensionsRun() {
        TODO()
    }

    /**
     * Tests the aggregation of a SARIF run containing optional objects
     */
    @Test
    fun aggregateComplexRun() {
        TODO()
    }

    companion object {
        private lateinit var runs: Set<Run>

        @BeforeAll
        @JvmStatic
        fun createRuns() {
            runs = ParserTest.exampleResults.map { Parser().extractLatestRun(it) }.toSet()
        }
    }
}