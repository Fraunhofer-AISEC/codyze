package de.fraunhofer.aisec.crymlin.connectors.lsp;

import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import de.fraunhofer.aisec.crymlin.server.AnalysisContext;
import de.fraunhofer.aisec.crymlin.server.AnalysisServer;
import de.fraunhofer.aisec.crymlin.structures.Finding;
import de.fraunhofer.aisec.crymlin.utils.Pair;
import java.io.File;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a {@link TextDocumentService}, which handles certain notifications from a
 * Language Client, such as opening or changing files.
 */
public class CpgDocumentService implements TextDocumentService {

  private static final Logger log = LoggerFactory.getLogger(CpgDocumentService.class);

  private HashMap<String, Pair<String, PublishDiagnosticsParams>> lastScan = new HashMap<>();

  private LanguageClient client;

  private void analyze(String uriString, String text) {
    String sanitizedText = text.replaceAll("[\\n ]", "");
    if (lastScan.get(uriString) != null
        && lastScan.get(uriString).getValue0().equals(sanitizedText)) {
      log.info("Same file already scanned, ignoring");
      client.publishDiagnostics(lastScan.get(uriString).getValue1());
      return;
    }

    { // mark the whole file with _Information_ to indicate that the file is being scanned
      ArrayList<Diagnostic> allDiags = new ArrayList<>();
      Diagnostic diagnostic = new Diagnostic();
      diagnostic.setSeverity(DiagnosticSeverity.Information);
      diagnostic.setMessage("File is being scanned");
      String[] split = text.split("\n");
      diagnostic.setRange(
          new Range(
              new Position(0, 0),
              new Position(split.length - 1, split[split.length - 1].length())));
      allDiags.add(diagnostic);
      PublishDiagnosticsParams diagnostics = new PublishDiagnosticsParams();
      diagnostics.setDiagnostics(allDiags);
      diagnostics.setUri(uriString);
      client.publishDiagnostics(diagnostics);
    }

    Instant start = Instant.now();

    OverflowDatabase.getInstance().connect();
    OverflowDatabase.getInstance().purgeDatabase();

    File file = new File(URI.create(uriString));
    AnalysisServer instance = AnalysisServer.getInstance();
    if (instance == null) {
      log.error("Server instance is null.");
      return;
    }
    TranslationManager tm =
        TranslationManager.builder()
            .config(
                TranslationConfiguration.builder()
                    .debugParser(true)
                    .failOnError(false)
                    .codeInNodes(true)
                    .defaultPasses()
                    .sourceFiles(file)
                    .build())
            .build();

    CompletableFuture<TranslationResult> analyze = instance.analyze(tm);

    try {
      TranslationResult result = analyze.get(5, TimeUnit.MINUTES);

      AnalysisContext ctx = (AnalysisContext) result.getScratch().get("ctx");

      if (ctx == null) {
        log.error("ctx is null. Did the analysis run without errors?");
        return;
      }
      log.info(
          "Analysis for {} done. Returning {} findings. Took {} ms\n-------------------------------------------------------------------",
          uriString,
          ctx.getFindings().size(),
          Duration.between(start, Instant.now()).toMillis());

      ArrayList<Diagnostic> allDiags = new ArrayList<>();
      for (Finding f : ctx.getFindings()) {
        Diagnostic diagnostic = new Diagnostic();
        diagnostic.setSeverity(DiagnosticSeverity.Error);
        diagnostic.setCode("test");
        diagnostic.setMessage(f.getName());
        diagnostic.setRange(f.getRange());
        allDiags.add(diagnostic);
      }

      PublishDiagnosticsParams diagnostics = new PublishDiagnosticsParams();
      diagnostics.setDiagnostics(allDiags);
      diagnostics.setUri(uriString);

      lastScan.put(uriString, new Pair<>(sanitizedText, diagnostics));

      // sending diagnostics
      client.publishDiagnostics(diagnostics);
    } catch (InterruptedException e) {
      log.error("Analysis error: ", e);
      Thread.currentThread().interrupt();
    } catch (ExecutionException | TimeoutException e) {
      analyze.cancel(true);
      tm.cancel(true);
      log.error("Analysis error: ", e);
    }
  }

  @Override
  public void didOpen(DidOpenTextDocumentParams params) {
    log.info("Handling didOpen for file: {}", params.getTextDocument().getUri());
    analyze(params.getTextDocument().getUri(), params.getTextDocument().getText());
  }

  @Override
  public void didChange(DidChangeTextDocumentParams params) {
    // log.info("Handling didChange: {}", params);
  }

  @Override
  public void didClose(DidCloseTextDocumentParams params) {}

  @Override
  public void didSave(DidSaveTextDocumentParams params) {
    log.info("Handling didSave for file: {}", params.getTextDocument().getUri());
    analyze(params.getTextDocument().getUri(), params.getText());
  }

  void setClient(LanguageClient client) {
    this.client = client;
  }
}
