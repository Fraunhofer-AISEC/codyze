package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.AnalysisContext
import de.fraunhofer.aisec.codyze.analysis.AnalysisServer
import de.fraunhofer.aisec.codyze.analysis.Finding
import de.fraunhofer.aisec.codyze.analysis.ServerConfiguration
import de.fraunhofer.aisec.codyze.analysis.TypestateMode
import de.fraunhofer.aisec.codyze.analysis.passes.EdgeCachePass
import de.fraunhofer.aisec.codyze.analysis.passes.IdentifierPass
import de.fraunhofer.aisec.codyze.analysis.wpds.NFA
import de.fraunhofer.aisec.cpg.TranslationConfiguration
import de.fraunhofer.aisec.cpg.TranslationManager
import java.io.*
import java.lang.Exception
import java.nio.file.Path
import java.util.ArrayList
import java.util.HashSet
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.stream.Collectors
import kotlin.Throws
import org.junit.jupiter.api.Assertions

abstract class AbstractMarkTest {
    protected var translationManager: TranslationManager? = null
    protected lateinit var server: AnalysisServer
    protected var ctx: AnalysisContext? = null
    protected var tsMode = TypestateMode.NFA
    @Throws(Exception::class)
    protected fun performTest(sourceFileName: String): MutableSet<Finding> {
        return performTest(sourceFileName, null)
    }

    @Throws(Exception::class)
    protected fun performTest(sourceFileName: String, markFileName: String?): MutableSet<Finding> {
        return performTest(sourceFileName, null, markFileName)
    }

    @Throws(Exception::class)
    protected fun performTest(
        sourceFileName: String,
        additionalFiles: Array<String>?,
        markFileName: String?
    ): MutableSet<Finding> {
        val classLoader = AbstractMarkTest::class.java.classLoader
        var resource = classLoader.getResource(sourceFileName)
        Assertions.assertNotNull(resource, "Resource $sourceFileName not found")
        var javaFile = File(resource!!.file)
        Assertions.assertNotNull(javaFile, "File $sourceFileName not found")
        val toAnalyze = ArrayList<File>()
        toAnalyze.add(javaFile)
        if (additionalFiles != null) {
            for (s in additionalFiles) {
                resource = classLoader.getResource(s)
                Assertions.assertNotNull(resource, "Resource $s not found")
                javaFile = File(resource.file)
                Assertions.assertNotNull(javaFile, "File $s not found")
                toAnalyze.add(javaFile)
            }
        }
        var markDirPath = ""
        if (markFileName != null) {
            resource = classLoader.getResource(markFileName)
            if (resource == null) {
                // Assume `markFileName` is relative to project base `src` folder
                val p =
                    Path.of(classLoader.getResource(".").toURI())
                        .resolve(Path.of("..", "..", "..", "src"))
                        .resolve(markFileName)
                        .normalize()
                resource = p.toUri().toURL()
            }
            Assertions.assertNotNull(resource)
            val markDir = File(resource.file)
            Assertions.assertNotNull(markDir)
            markDirPath = markDir.absolutePath
        }

        // Start an analysis server
        server =
            AnalysisServer.builder()
                .config(
                    ServerConfiguration.builder()
                        .launchConsole(false)
                        .launchLsp(false)
                        .typestateAnalysis(tsMode)
                        .markFiles(markDirPath)
                        .useLegacyEvaluator()
                        .build()
                )
                .build()
        server.start()
        translationManager =
            TranslationManager.builder()
                .config(
                    TranslationConfiguration.builder()
                        .debugParser(true)
                        .failOnError(false)
                        .codeInNodes(true)
                        .defaultPasses()
                        .defaultLanguages()
                        .registerPass(IdentifierPass())
                        .registerPass(EdgeCachePass())
                        .loadIncludes(true)
                        .sourceLocations(*toAnalyze.toTypedArray())
                        .build()
                )
                .build()
        val analyze = server.analyze(translationManager)
        ctx =
            try {
                analyze[5, TimeUnit.MINUTES]
            } catch (t: TimeoutException) {
                analyze.cancel(true)
                throw t
            }

        Assertions.assertNotNull(ctx)

        val findings = ctx?.findings ?: mutableSetOf<Finding>()

        for (s in findings) {
            println(s)
        }

        return findings
    }

    protected fun expected(findings: MutableSet<Finding>, vararg expectedFindings: String) {
        println("All findings:")
        for (f in findings) {
            println(f.toString())
        }
        for (expected in expectedFindings) {
            Assertions.assertEquals(
                1,
                findings.stream().filter { f: Finding -> f.toString() == expected }.count(),
                "not found: \"$expected\""
            )
            val first =
                findings.stream().filter { f: Finding -> f.toString() == expected }.findFirst()
            findings.remove(first.get())
        }
        if (findings.size > 0) {
            println("Additional Findings:")
            for (f in findings) {
                println(f.toString())
            }
        }
        Assertions.assertEquals(
            0,
            findings.size,
            findings.stream().map { obj: Finding -> obj.toString() }.collect(Collectors.joining())
        )
    }

    /**
     * Verifies that a set of findings contains at least the given expected findings.
     * @param findings A set of findings to check.
     * @param expectedFindings A set of expected findings.
     */
    protected fun containsFindings(findings: Set<Finding>, vararg expectedFindings: String) {
        println("All findings:")
        for (f in findings) println(f.toString())
        val missingFindings: MutableSet<String> = HashSet()
        for (expected in expectedFindings) {
            var found = false
            for (finding in findings) {
                if (expected == finding.toString()) {
                    found = true
                    break
                }
            }
            if (!found) {
                missingFindings.add(expected)
            }
        }
        if (!missingFindings.isEmpty()) {
            println("Missing findings:")
            for (missing in missingFindings) {
                println(missing)
            }
        }
        Assertions.assertTrue(missingFindings.isEmpty())
    }
}
