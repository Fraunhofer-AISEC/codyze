package de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl

import de.fraunhofer.aisec.codyze.backends.cpg.CPGConfiguration
import de.fraunhofer.aisec.codyze.backends.cpg.coko.CokoCpgBackend
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl.host.CokoExecutor
import de.fraunhofer.aisec.cpg.passes.EdgeCachePass
import de.fraunhofer.aisec.cpg.passes.UnreachableEOGPass
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.reflect.full.isSubtypeOf
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ConceptTranslationTest {

    private val sourceFiles = listOfNotNull(
        CokoCpgIntegrationTest::class.java.classLoader
            .getResource("IntegrationTests/CokoCpg/Main.java"),
        CokoCpgIntegrationTest::class.java.classLoader
            .getResource("IntegrationTests/CokoCpg/SimpleOrder.java")
    ).map { Path(it.path) }.also { assertEquals(2, it.size) }

    val cpgConfiguration =
        CPGConfiguration(
            source = sourceFiles,
            useUnityBuild = false,
            typeSystemActiveInFrontend = true,
            debugParser = false,
            disableCleanup = false,
            codeInNodes = true,
            matchCommentsToNodes = false,
            processAnnotations = false,
            failOnError = false,
            useParallelFrontends = false,
            defaultPasses = true,
            additionalLanguages = setOf(),
            symbols = mapOf(),
            includeBlocklist = listOf(),
            includePaths = listOf(),
            includeAllowlist = listOf(),
            loadIncludes = false,
            passes = listOf(UnreachableEOGPass::class, EdgeCachePass::class),
        )

    @Test
    fun `test simple concept translation`() {
        val specFiles = listOfNotNull(
            CokoCpgIntegrationTest::class.java.classLoader
                .getResource("concept/bsi-tr.concepts"),
        ).map { Path(it.path) }

        val cokoConfiguration =
            CokoConfiguration(
                goodFindings = true,
                pedantic = false,
                spec = specFiles,
                disabledSpecRules = emptyList(),
            )

        val backend = CokoCpgBackend(cpgConfiguration)
        val specEvaluator = CokoExecutor.compileScriptsIntoSpecEvaluator(backend, specFiles)

        val expectedInterfaceToExpectedMembers = mapOf(
            "Cypher" to listOf("algo", "mode", "keySize", "tagSize"),
            "InitializationVector" to listOf("size"),
            "Encryption" to listOf("cypher", "iv", "encrypt", "decrypt")
        )

        assertEquals(expectedInterfaceToExpectedMembers.size, specEvaluator.types.size)

        for((expectedInterface, members) in expectedInterfaceToExpectedMembers) {
            val actualInterfaces =  specEvaluator.types.filter {it.simpleName?.contains(expectedInterface) ?: false}
            assertEquals(1, actualInterfaces.size, "Found none or more than one actual interface representing the concept \"$expectedInterface\"")
            val actualInterface = actualInterfaces.first()

            assertTrue(actualInterface.members.map { it.name }.containsAll(members))
        }
    }

    @Test
    fun `test concept translation with op pointer`() {
        val specFiles = listOfNotNull(
            CokoCpgIntegrationTest::class.java.classLoader
                .getResource("concept/some.concepts"),
        ).map { Path(it.path) }

        val cokoConfiguration =
            CokoConfiguration(
                goodFindings = true,
                pedantic = false,
                spec = specFiles,
                disabledSpecRules = emptyList(),
            )

        val backend = CokoCpgBackend(cpgConfiguration)
        val specEvaluator = CokoExecutor.compileScriptsIntoSpecEvaluator(backend, specFiles)

        val expectedInterfaceToExpectedMembers = mapOf(
            "Logging" to listOf("log", "info", "warn", "error"),
            "Database" to listOf("init", "insert"),
            "UserContext" to listOf("user")
        )

        assertEquals(3, specEvaluator.types.size)
        for((expectedInterface, members) in expectedInterfaceToExpectedMembers) {
            val actualInterfaces =  specEvaluator.types.filter {it.simpleName?.contains(expectedInterface) ?: false}
            assertEquals(1, actualInterfaces.size, "Found none or more than one actual interface representing the concept \"$expectedInterface\"")
            val actualInterface = actualInterfaces.first()
            assertTrue(actualInterface.members.map { it.name }.containsAll(members))

            if(expectedInterface == "Logging") {
                val log = actualInterface.members.firstOrNull { it.name == "log" }
                assertNotNull(log)
                assertFalse(log.isAbstract)
            }
        }
    }

    @Test
    fun `test concept in combination with coko scripts`() {
        val specFiles = listOfNotNull(
            CokoCpgIntegrationTest::class.java.classLoader
                .getResource("concept/followedBy.concepts"),
            CokoCpgIntegrationTest::class.java.classLoader
                .getResource("concept/followedByImplementations.codyze.kts"),
            CokoCpgIntegrationTest::class.java.classLoader
                .getResource("concept/followedByRule.codyze.kts"),
        ).map { Path(it.path) }

        val cokoConfiguration =
            CokoConfiguration(
                goodFindings = true,
                pedantic = false,
                spec = specFiles,
                disabledSpecRules = emptyList(),
            )

        val backend = CokoCpgBackend(cpgConfiguration)
        val executor = CokoExecutor(cokoConfiguration, backend)

        val run = executor.evaluate()
        assertEquals(1, run.results?.size)
    }
}
