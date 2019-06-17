package de.fraunhofer.aisec.crymlin.connectors.lsp;

import de.fraunhofer.aisec.cpg.Database;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.cpg.passes.CallResolver;
import de.fraunhofer.aisec.cpg.passes.DataFlowPass;
import de.fraunhofer.aisec.cpg.passes.EvaluationOrderGraphPass;
import de.fraunhofer.aisec.cpg.passes.TypeHierarchyResolver;
import de.fraunhofer.aisec.cpg.passes.VariableUsageResolver;
import de.fraunhofer.aisec.crymlin.server.AnalysisContext;
import de.fraunhofer.aisec.crymlin.server.AnalysisServer;
import de.fraunhofer.aisec.crymlin.structures.Finding;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
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

  private LanguageClient client;

  @Override
  public void didOpen(DidOpenTextDocumentParams params) {
    log.info("Handling didOpen: {}", params);

    Database.getInstance().connect();
    Database.getInstance().purgeDatabase();

    URI uri = URI.create(params.getTextDocument().getUri());

    File file = new File(uri);
    try {
      AnalysisServer instance = AnalysisServer.getInstance();
      if (instance == null) {
        log.error("Server instance is null.");
        return;
      }
      TranslationResult result =
          instance
              .analyze(
                  TranslationManager.builder()
                      .config(
                          TranslationConfiguration.builder()
                              .debugParser(true)
                              .failOnError(false)
                              .codeInNodes(true)
                              .registerPass(new TypeHierarchyResolver())
                              .registerPass(new VariableUsageResolver())
                              .registerPass(new CallResolver()) // creates CG
                              .registerPass(new DataFlowPass())
                              .registerPass(new CallResolver()) // creates CG
                              .registerPass(new DataFlowPass())
                              .registerPass(new EvaluationOrderGraphPass()) // creates EOG
                              .sourceFiles(file)
                              .build())
                      .build())
              .get(5, TimeUnit.MINUTES);
      AnalysisContext ctx = (AnalysisContext) result.getScratch().get("ctx");

      if (ctx == null) {
        log.error("ctx is null. Did the analysis run without errors?");
        return;
      }

      ArrayList<Diagnostic> allDiags = new ArrayList<>();
      for (Finding f : ctx.getFindings()) {
        Diagnostic diagnostic = new Diagnostic();
        diagnostic.setSeverity(DiagnosticSeverity.Warning);
        diagnostic.setMessage(f.getFinding());
        diagnostic.setRange(f.getRange());
        allDiags.add(diagnostic);
      }

      PublishDiagnosticsParams diagnostics = new PublishDiagnosticsParams();
      diagnostics.setDiagnostics(allDiags);
      diagnostics.setUri(params.getTextDocument().getUri());

      // sending diagnostics
      client.publishDiagnostics(diagnostics);

    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void didChange(DidChangeTextDocumentParams params) {
    log.info("Handling didChange: {}", params);
  }

  @Override
  public void didClose(DidCloseTextDocumentParams params) {}

  @Override
  public void didSave(DidSaveTextDocumentParams params) {
    log.info("Handling didSave: {}", params);
  }

  void setClient(LanguageClient client) {
    this.client = client;
  }
}
