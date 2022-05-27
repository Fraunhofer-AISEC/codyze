package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.AnalysisServer
import de.fraunhofer.aisec.codyze.config.CodyzeConfiguration
import de.fraunhofer.aisec.codyze.config.Configuration
import de.fraunhofer.aisec.codyze.config.CpgConfiguration
import de.fraunhofer.aisec.cpg.passes.EdgeCachePass
import de.fraunhofer.aisec.cpg.passes.FilenameMapper
import de.fraunhofer.aisec.cpg.passes.IdentifierPass
import java.io.*
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.MessageActionItem
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.ShowMessageRequestParams
import org.eclipse.lsp4j.TextDocumentItem
import org.eclipse.lsp4j.services.LanguageClient
import org.junit.jupiter.api.*

/** Tests for the server component of the Language Server Protocol. */
internal class LSPTest {
    @Test
    fun testForbidden() {
        val lc: LanguageClient =
            object : LanguageClient {
                override fun telemetryEvent(o: Any) {}
                override fun publishDiagnostics(
                    publishDiagnosticsParams: PublishDiagnosticsParams
                ) {
                    for (d in publishDiagnosticsParams.diagnostics) {
                        println(d.message)
                    }
                    // one finding is disabled via comment
                    assertEquals(4, publishDiagnosticsParams.diagnostics.size)
                }

                override fun showMessage(messageParams: MessageParams) {}
                override fun showMessageRequest(
                    showMessageRequestParams: ShowMessageRequestParams
                ): CompletableFuture<MessageActionItem>? {
                    return null
                }

                override fun logMessage(messageParams: MessageParams) {}
            }
        server.lsp.connect(lc)
        val tdi = TextDocumentItem()
        tdi.languageId = "cpp"
        tdi.text = "dummy"
        tdi.version = -1
        tdi.uri = Paths.get(parentFolder, "forbidden.cpp").toUri().toString()
        val params = DidOpenTextDocumentParams(tdi)
        server.lsp.textDocumentService.didOpen(params)
    }

    @Test
    fun testOrder() {
        val lc: LanguageClient =
            object : LanguageClient {
                override fun telemetryEvent(o: Any) {}
                override fun publishDiagnostics(
                    publishDiagnosticsParams: PublishDiagnosticsParams
                ) {
                    assertEquals(7, publishDiagnosticsParams.diagnostics.size)
                }

                override fun showMessage(messageParams: MessageParams) {}
                override fun showMessageRequest(
                    showMessageRequestParams: ShowMessageRequestParams
                ): CompletableFuture<MessageActionItem>? {
                    return null
                }

                override fun logMessage(messageParams: MessageParams) {}
            }
        server.lsp.connect(lc)
        val tdi = TextDocumentItem()
        tdi.languageId = "cpp"
        tdi.text = "dummy"
        tdi.version = -1
        tdi.uri = Paths.get(parentFolder, "order.cpp").toUri().toString()
        val params = DidOpenTextDocumentParams(tdi)
        server.lsp.textDocumentService.didOpen(params)
    }

    @Test
    fun testShutdown() {
        val shutdownFuture = server.lsp.shutdown()
        assertTrue(shutdownFuture.isDone) // expect immediate result
    }

    companion object {
        private var parentFolder: String? = null
        private lateinit var server: AnalysisServer

        @BeforeAll
        @JvmStatic
        fun setup() {
            val classLoader = AnalysisServerBotanTest::class.java.classLoader
            val resource = classLoader.getResource("unittests/order.mark")
            assertNotNull(resource)

            val markPoC1 = File(resource.file)
            assertNotNull(markPoC1)
            parentFolder = markPoC1.parent + File.separator

            // Start an analysis server

            val codyze = CodyzeConfiguration()
            codyze.mark = arrayOf(File(parentFolder))

            val cpg = CpgConfiguration()
            cpg.defaultPasses = true
            cpg.passes = listOf(FilenameMapper(), IdentifierPass(), EdgeCachePass())

            val config = Configuration(codyze, cpg)
            config.executionMode.isCli = false
            config.executionMode.isLsp = true

            server = AnalysisServer(config)
            server.start()
        }
    }
}
