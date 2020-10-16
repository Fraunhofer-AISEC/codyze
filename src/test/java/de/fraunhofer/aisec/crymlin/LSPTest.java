
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.server.AnalysisServer;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for the server component of the Language Server Protocol. */
class LSPTest {

	private static String parentFolder;
	private static AnalysisServer server;

	@BeforeAll
	static void setup() throws Exception {
		ClassLoader classLoader = AnalysisServerBotanTest.class.getClassLoader();

		URL resource = classLoader.getResource("unittests/order.mark");
		assertNotNull(resource);
		File markPoC1 = new File(resource.getFile());
		assertNotNull(markPoC1);
		parentFolder = markPoC1.getParent() + File.separator;

		// Start an analysis server
		server = AnalysisServer.builder()
				.config(
					ServerConfiguration.builder()
							.launchConsole(false)
							.launchLsp(true)
							.markFiles(parentFolder)
							.build())
				.build();
		server.start();
	}

	@Test
	void testForbidden() {

		LanguageClient lc = new LanguageClient() {

			@Override
			public void telemetryEvent(Object o) {
			}

			@Override
			public void publishDiagnostics(PublishDiagnosticsParams publishDiagnosticsParams) {
				for (Diagnostic d : publishDiagnosticsParams.getDiagnostics()) {
					System.out.println(d.getMessage());
				}
				// one finding is disabled via comment
				assertEquals(4, publishDiagnosticsParams.getDiagnostics().size());
			}

			@Override
			public void showMessage(MessageParams messageParams) {
			}

			@Override
			public CompletableFuture<MessageActionItem> showMessageRequest(
					ShowMessageRequestParams showMessageRequestParams) {
				return null;
			}

			@Override
			public void logMessage(MessageParams messageParams) {
			}
		};
		server.getLSP().connect(lc);

		TextDocumentItem tdi = new TextDocumentItem();
		tdi.setLanguageId("cpp");
		tdi.setText("dummy");
		tdi.setVersion(-1);
		tdi.setUri(Paths.get(parentFolder, "forbidden.cpp").toUri().toString());
		DidOpenTextDocumentParams params = new DidOpenTextDocumentParams(tdi);

		server.getLSP().getTextDocumentService().didOpen(params);
	}

	@Test
	void testOrder() {

		LanguageClient lc = new LanguageClient() {

			@Override
			public void telemetryEvent(Object o) {
			}

			@Override
			public void publishDiagnostics(PublishDiagnosticsParams publishDiagnosticsParams) {
				assertEquals(8, publishDiagnosticsParams.getDiagnostics().size());
			}

			@Override
			public void showMessage(MessageParams messageParams) {
			}

			@Override
			public CompletableFuture<MessageActionItem> showMessageRequest(
					ShowMessageRequestParams showMessageRequestParams) {
				return null;
			}

			@Override
			public void logMessage(MessageParams messageParams) {
			}
		};
		server.getLSP().connect(lc);

		TextDocumentItem tdi = new TextDocumentItem();
		tdi.setLanguageId("cpp");
		tdi.setText("dummy");
		tdi.setVersion(-1);
		tdi.setUri(Paths.get(parentFolder, "order.cpp").toUri().toString());
		DidOpenTextDocumentParams params = new DidOpenTextDocumentParams(tdi);

		server.getLSP().getTextDocumentService().didOpen(params);
	}

	@Test
	void testShutdown() {
		CompletableFuture<Object> shutdownFuture = server.getLSP().shutdown();
		assertTrue(shutdownFuture.isDone()); // expect immediate result
	}
}
