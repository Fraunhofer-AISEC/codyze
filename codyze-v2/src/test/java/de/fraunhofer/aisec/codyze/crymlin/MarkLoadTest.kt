package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.AnalysisServer
import de.fraunhofer.aisec.codyze.config.DisabledMarkRulesValue
import de.fraunhofer.aisec.codyze.markmodel.MRule
import java.io.File
import java.lang.Exception
import kotlin.Throws
import kotlin.test.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test

internal class MarkLoadTest {

    @Test
    @Throws(Exception::class)
    fun disabledRulesTest() {
        val disabledMarkRules = mutableMapOf<String, DisabledMarkRulesValue>()
        disabledMarkRules["java"] =
            DisabledMarkRulesValue(
                false,
                mutableSetOf("UseValidAlgorithm", "RandomInitializationVector", "MockWhen2")
            )
        disabledMarkRules[""] = DisabledMarkRulesValue(false, mutableSetOf("SignatureOrder"))

        analysisServer.loadMarkRules(
            disabledMarkRules,
            botanLocation,
            javaLocation,
            noPackageLocation
        )
        val actualMarkRules = analysisServer.markModel.rules
        assertNotNull(actualMarkRules)
        val actualMarkRuleNames = actualMarkRules.map { r -> r.name }

        assertFalse(actualMarkRuleNames.isEmpty(), "Expected to contain mark rules but was empty")

        val expectedSize =
            allMarkRules.size -
                disabledMarkRules
                    .getOrDefault("java", DisabledMarkRulesValue())
                    .disabledMarkRuleNames
                    .size -
                disabledMarkRules
                    .getOrDefault("", DisabledMarkRulesValue())
                    .disabledMarkRuleNames
                    .size
        assertEquals(
            expectedSize,
            actualMarkRuleNames.size,
            "Expected size to be $expectedSize, but was ${actualMarkRuleNames.size}"
        )

        for (s in
            disabledMarkRules
                .getOrDefault("java", DisabledMarkRulesValue())
                .disabledMarkRuleNames) assertFalse(
            actualMarkRuleNames.contains(s),
            "Expected to have filtered out $s but was included in mark rules"
        )

        for (s in
            disabledMarkRules
                .getOrDefault("", DisabledMarkRulesValue())
                .disabledMarkRuleNames) assertFalse(
            actualMarkRuleNames.contains(s),
            "Expected to have filtered out $s but was included in mark rules"
        )

        for (s in botanMarkRuleNames) assertTrue(
            actualMarkRuleNames.contains(s),
            "Expected to have loaded $s but was not in mark rules"
        )
    }

    @Test
    @Throws(Exception::class)
    fun disabledPackageTest() {
        val disabledMarkRules = mutableMapOf<String, DisabledMarkRulesValue>()
        disabledMarkRules["botan"] = DisabledMarkRulesValue(true, mutableSetOf())

        analysisServer.loadMarkRules(
            disabledMarkRules,
            botanLocation,
            javaLocation,
            noPackageLocation
        )
        val actualMarkRules = analysisServer.markModel.rules
        assertNotNull(actualMarkRules)
        val actualMarkRuleNames = actualMarkRules.map { r -> r.name }

        assertFalse(actualMarkRuleNames.isEmpty(), "Expected to contain mark rules but was empty")

        val expectedSize = allMarkRules.size - botanMarkRuleNames.size
        assertEquals(
            expectedSize,
            actualMarkRuleNames.size,
            "Expected size to be $expectedSize, but was ${actualMarkRuleNames.size}"
        )

        for (s in botanMarkRuleNames) assertFalse(
            actualMarkRuleNames.contains(s),
            "Expected to have filtered out $s but was included in mark rules"
        )

        for (s in javaMarkRuleNames) assertTrue(
            actualMarkRuleNames.contains(s),
            "Expected to have loaded $s but was not in mark rules"
        )

        for (s in noPackageRuleNames) assertTrue(
            actualMarkRuleNames.contains(s),
            "Expected to have loaded $s but was not in mark rules"
        )
    }

    @Test
    @Throws(Exception::class)
    fun disabledRulesAndPackageTest() {
        val disabledMarkRules = mutableMapOf<String, DisabledMarkRulesValue>()
        disabledMarkRules["java"] =
            DisabledMarkRulesValue(
                false,
                mutableSetOf("UseValidAlgorithm", "RandomInitializationVector", "MockWhen2")
            )
        disabledMarkRules["botan"] = DisabledMarkRulesValue(true, mutableSetOf())

        analysisServer.loadMarkRules(
            disabledMarkRules,
            botanLocation,
            javaLocation,
            noPackageLocation
        )
        val actualMarkRules = analysisServer.markModel.rules
        assertNotNull(actualMarkRules)
        val actualMarkRuleNames = actualMarkRules.map { r -> r.name }

        assertFalse(actualMarkRuleNames.isEmpty(), "Expected to contain mark rules but was empty")

        val expectedSize =
            allMarkRules.size -
                disabledMarkRules
                    .getOrDefault("java", DisabledMarkRulesValue())
                    .disabledMarkRuleNames
                    .size -
                botanMarkRuleNames.size
        assertEquals(
            expectedSize,
            actualMarkRuleNames.size,
            "Expected size to be $expectedSize, but was ${actualMarkRuleNames.size}"
        )

        for (s in
            disabledMarkRules
                .getOrDefault("java", DisabledMarkRulesValue())
                .disabledMarkRuleNames) assertFalse(
            actualMarkRuleNames.contains(s),
            "Expected to have filtered out $s but was included in mark rules"
        )

        for (s in botanMarkRuleNames) assertFalse(
            actualMarkRuleNames.contains(s),
            "Expected to have filtered out $s but was included in mark rules"
        )

        for (s in noPackageRuleNames) assertTrue(
            actualMarkRuleNames.contains(s),
            "Expected to have loaded $s but was not in mark rules"
        )
    }

    companion object {
        private lateinit var analysisServer: AnalysisServer
        private lateinit var javaLocation: File
        private lateinit var botanLocation: File
        private lateinit var noPackageLocation: File
        private lateinit var allMarkRules: MutableList<MRule>
        private lateinit var botanMarkRuleNames: List<String>
        private lateinit var javaMarkRuleNames: List<String>
        private lateinit var noPackageRuleNames: List<String>

        @BeforeAll
        @JvmStatic
        fun startup() {
            analysisServer = AnalysisServer(null)
            assertNotNull(analysisServer)

            val botanMarkResource =
                MarkLoadTest::class.java.classLoader.getResource("real-examples/botan/MARK")
            assertNotNull(botanMarkResource)
            botanLocation = File(botanMarkResource.file)
            assertNotNull(botanLocation)

            val javaMarkResource =
                MarkLoadTest::class
                    .java
                    .classLoader
                    .getResource("real-examples/bc/rwedoff.Password-Manager")
            assertNotNull(javaMarkResource)
            javaLocation = File(javaMarkResource.file)
            assertNotNull(javaLocation)

            val noPackageResource =
                MarkLoadTest::class.java.classLoader.getResource("unittests/nfa-test.mark")
            assertNotNull(botanMarkResource)
            noPackageLocation = File(noPackageResource.file)
            assertNotNull(noPackageLocation)

            analysisServer.loadMarkRules(botanLocation, javaLocation, noPackageLocation)
            allMarkRules = analysisServer.markModel.rules
            assertNotNull(allMarkRules)

            analysisServer.loadMarkRules(botanLocation)
            val botanMarkRules = analysisServer.markModel.rules
            assertNotNull(botanMarkRules)
            botanMarkRuleNames = botanMarkRules.map { r -> r.name }

            analysisServer.loadMarkRules(javaLocation)
            val javaMarkRules = analysisServer.markModel.rules
            assertNotNull(javaMarkRules)
            javaMarkRuleNames = javaMarkRules.map { r -> r.name }

            analysisServer.loadMarkRules(noPackageLocation)
            val noPackageMarkRules = analysisServer.markModel.rules
            assertNotNull(noPackageMarkRules)
            noPackageRuleNames = noPackageMarkRules.map { r -> r.name }
        }
    }
}
