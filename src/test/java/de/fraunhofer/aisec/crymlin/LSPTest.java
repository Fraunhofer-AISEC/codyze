package de.fraunhofer.aisec.crymlin;

import static org.junit.jupiter.api.Assertions.*;

import de.fraunhofer.aisec.crymlin.server.AnalysisServer;
import de.fraunhofer.aisec.crymlin.server.ServerConfiguration;
import java.io.File;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.services.LanguageClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
    server =
        AnalysisServer.builder()
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

    LanguageClient lc =
        new LanguageClient() {
          boolean first = true;

          @Override
          public void telemetryEvent(Object o) {}

          @Override
          public void publishDiagnostics(PublishDiagnosticsParams publishDiagnosticsParams) {
            for (Diagnostic d : publishDiagnosticsParams.getDiagnostics()) {
              System.out.println(d.getMessage());
            }
            if (first) {
              // a generic message to mark the whole file
              first = false;
              assertEquals(1, publishDiagnosticsParams.getDiagnostics().size());
            } else {
              assertEquals(3, publishDiagnosticsParams.getDiagnostics().size());
            }
          }

          @Override
          public void showMessage(MessageParams messageParams) {}

          @Override
          public CompletableFuture<MessageActionItem> showMessageRequest(
              ShowMessageRequestParams showMessageRequestParams) {
            return null;
          }

          @Override
          public void logMessage(MessageParams messageParams) {}
        };
    server.getLSP().connect(lc);

    TextDocumentItem tdi = new TextDocumentItem();
    tdi.setLanguageId("cpp");
    tdi.setText("dummy");
    tdi.setVersion(-1);
    tdi.setUri("file://" + parentFolder + "forbidden.cpp");
    DidOpenTextDocumentParams params = new DidOpenTextDocumentParams(tdi);

    server.getLSP().getTextDocumentService().didOpen(params);
  }

  @Test
  void testOrder() {

    LanguageClient lc =
        new LanguageClient() {
          boolean first = true;

          @Override
          public void telemetryEvent(Object o) {}

          @Override
          public void publishDiagnostics(PublishDiagnosticsParams publishDiagnosticsParams) {
            if (first) {
              // a generic message to mark the whole file
              first = false;
              assertEquals(1, publishDiagnosticsParams.getDiagnostics().size());
            } else {
              assertEquals(6, publishDiagnosticsParams.getDiagnostics().size());
            }
          }

          @Override
          public void showMessage(MessageParams messageParams) {}

          @Override
          public CompletableFuture<MessageActionItem> showMessageRequest(
              ShowMessageRequestParams showMessageRequestParams) {
            return null;
          }

          @Override
          public void logMessage(MessageParams messageParams) {}
        };
    server.getLSP().connect(lc);

    TextDocumentItem tdi = new TextDocumentItem();
    tdi.setLanguageId("cpp");
    tdi.setText("dummy");
    tdi.setVersion(-1);
    tdi.setUri("file://" + parentFolder + "order.cpp");
    DidOpenTextDocumentParams params = new DidOpenTextDocumentParams(tdi);

    server.getLSP().getTextDocumentService().didOpen(params);
  }
}
