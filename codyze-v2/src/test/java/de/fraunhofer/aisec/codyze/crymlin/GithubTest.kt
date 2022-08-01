package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.AnalysisServer
import de.fraunhofer.aisec.codyze.config.CodyzeConfiguration
import de.fraunhofer.aisec.codyze.config.Configuration
import de.fraunhofer.aisec.codyze.config.CpgConfiguration
import java.io.*
import java.lang.Exception
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.Throws
import kotlin.test.assertNotNull
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.LogEvent
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.slf4j.LoggerFactory

// TODO Remove before release or at least remove hardcoded paths
@Disabled
internal class GithubTest : AbstractTest() {
    companion object {
        private const val FILES_OFFSET = 0
        private const val MAX_FILES_TO_SCAN = -1 // -1: all
        private const val OUTFOLDERNAME = "/home/user/temp/eval_151020/"
        private var baseFolder: String? = null
        private val log = LoggerFactory.getLogger(GithubTest::class.java)
        private const val RESCAN = true
        private lateinit var server: AnalysisServer
        private var logCopy: TestAppender? = null
        private fun listFiles(): List<String?> {
            // File folder = new File("/tmp/random_sources");
            val folder = File(baseFolder)
            assertNotNull(folder)
            val files = folder.listFiles()
            assertNotNull(files)
            var ret: MutableList<String> = ArrayList()
            for (f in files) {
                ret.add(f.absolutePath.substring(baseFolder!!.length + 1)) // only use the file name
            }
            ret.sort()
            // random!
            ret.shuffle()
            if (MAX_FILES_TO_SCAN != -1) {
                ret = ret.subList(FILES_OFFSET, FILES_OFFSET + MAX_FILES_TO_SCAN)
            }
            return ret
        }

        @BeforeAll
        fun setup() {
            val classLoader = GithubTest::class.java.classLoader
            val resource = classLoader.getResource("unittests/order2.mark")
            assertNotNull(resource)

            val markPoC1 = File(resource.file)
            assertNotNull(markPoC1)

            val codyze = CodyzeConfiguration()
            codyze.mark = arrayOf(markPoC1)

            val config = Configuration(codyze, CpgConfiguration())
            config.executionMode.isCli = false
            config.executionMode.isLsp = false

            server = AnalysisServer(config)
            server.start()
            logCopy = TestAppender("logCopy", null)
            logCopy!!.injectIntoLogger()
        }

        @AfterAll
        fun shutdown() {
            server.stop()
        }

        init {
            //    ClassLoader classLoader = GithubTest.class.getClassLoader();
            //    URL resource = classLoader.getResource("random_github");
            //    assertNotNull(resource);
            // baseFolder = resource.getFile();
            baseFolder = "/home/ubuntu/github"
        }
    }

    @ParameterizedTest
    @MethodSource("listFiles")
    @Throws(Exception::class)
    fun performTest(sourceFileName: String) {
        var sourceFileName = sourceFileName
        val dir = File(OUTFOLDERNAME)
        val tmpString = sourceFileName
        val matchingFiles =
            dir.listFiles { pathname: File -> pathname.name.endsWith("$tmpString.out") }
        if (!RESCAN && matchingFiles != null && matchingFiles.isNotEmpty()) {
            println("File already scanned")
            return
        }
        val tempDir = Files.createTempDirectory("githubtest_")
        val tempFile = File(tempDir.toString() + File.separator + sourceFileName)
        Files.copy(
            File(baseFolder + File.separator + sourceFileName).toPath(),
            tempFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )
        logCopy!!.reset()

        // prepend base folder. we call this function only with the file name to make the tests
        // nicely readable
        sourceFileName = tempFile.absolutePath
        val cppFile = File(sourceFileName)
        assertNotNull(cppFile)
        log.info("File size: {} kB", cppFile.length() / 1024)

        //    try (Stream<String> lines = Files.lines(cppFile.toPath(), StandardCharsets.UTF_8)) {
        //      log.info("Benchmark: {} contains {} lines", sourceFileName, lines.count());
        //    } catch (Exception e) {
        //      try (Stream<String> lines = Files.lines(cppFile.toPath(),
        // StandardCharsets.ISO_8859_1))
        // {
        //        log.info("Benchmark: {} contains {} lines", sourceFileName, lines.count());
        //      }
        //    }

        // Make sure we start with a clean (and connected) db
        // if this does not work, just throw
        val tm = newAnalysisRun()
        var hasError = false
        val analyze = server.analyze(tm)
        try {
            val result = analyze[30, TimeUnit.MINUTES]
            assertNotNull(result)
            //      AnalysisContext ctx = (AnalysisContext) result.getScratch().get("ctx");
            //      assertNotNull(ctx);
        } catch (e: Exception) {
            analyze.cancel(true)
            val sw = StringWriter()
            e.printStackTrace(PrintWriter(sw))
            log.error(sw.toString())
            hasError = true
        }
        tempFile.delete()
        tempDir.toFile().delete()
        val writer =
            PrintWriter(
                OUTFOLDERNAME + System.currentTimeMillis() + "_" + cppFile.name + ".out",
                StandardCharsets.UTF_8
            )
        for (e in logCopy!!.getLog()) {
            writer.println(
                e.timeMillis.toString() +
                    " " +
                    e.level.toString() +
                    " " +
                    e.loggerName.substring(e.loggerName.lastIndexOf(".") + 1) +
                    " " +
                    e.message.formattedMessage
            )
        }
        writer.flush()
        writer.close()
        System.gc()
        System.runFinalization()

        //    System.out.println("The following Errors/Warnings occured:");
        //    for(LogEvent e: logCopy.getLog(Level.ERROR, Level.WARN)) {
        //      System.out.println(e.toString());
        //    }
        if (hasError) {
            Assertions.fail<Any>()
        }
        val log = logCopy!!.getLog(Level.ERROR)
        val logFiltered: MutableList<LogEvent?> = ArrayList()
        var hasParseError = false
        for (x in log) {
            if (
                x.message.formattedMessage.contains(
                    "Parsing of type class org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTProblemStatement is not supported (yet)"
                ) || x.message.formattedMessage.contains("JavaParser could not parse file")
            ) {
                hasParseError = true
            }
            logFiltered.add(x)
        }
        if (!hasParseError && logFiltered.size > 0) {
            Assertions.fail<Any>()
        }
    }

    @Test
    @Throws(Exception::class)
    fun specificTest() {
        //		performTest("p059.java");

        // performTest("redis.c"); // very long persisting

        // performTest("rpcwallet.cpp");
        // performTest("indexed_data.cpp");
        // performTest("RSHooks.cpp");
        performTest("OSOption.cpp")

        // NPE
    }
}
