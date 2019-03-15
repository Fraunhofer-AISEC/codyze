package de.fraunhofer.aisec.crymlin.connectors.lsp;

import de.fraunhofer.aisec.cpg.AnalysisConfiguration;
import de.fraunhofer.aisec.cpg.AnalysisManager;
import de.fraunhofer.aisec.cpg.Database;
import de.fraunhofer.aisec.cpg.passes.CallResolver;
import de.fraunhofer.aisec.cpg.passes.ControlFlowGenerator;
import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
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

    var diagnostics = new PublishDiagnosticsParams();
    var diagnostic = new Diagnostic();
    diagnostic.setCode("SomeCode");
    diagnostic.setMessage("Bad bad crypto");
    diagnostic.setSeverity(DiagnosticSeverity.Warning);
    diagnostic.setRange(new Range(new Position(1, 2), new Position(2, 4)));

    diagnostics.setDiagnostics(List.of(diagnostic));
    diagnostics.setUri(params.getTextDocument().getUri());

    // sending diagnostics
    client.publishDiagnostics(diagnostics);

    Database.getInstance().purgeDatabase();

    var uri = URI.create(params.getTextDocument().getUri());

    var file = new File(uri);

    AnalysisManager analyzer =
        AnalysisManager.builder()
            .config(
                AnalysisConfiguration.builder()
                    .sourceFiles(file)
                    .registerPass(new ControlFlowGenerator()) // creates CFG
                    .registerPass(new CallResolver()) // creates CG
                    .debugParser(true)
                    .build())
            .build();

    try {
      var result = analyzer.analyze().get();

      var declaration = result.getTranslationUnits().get(0);

      Database.getInstance().save(declaration);
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void didChange(DidChangeTextDocumentParams params) {}

  @Override
  public void didClose(DidCloseTextDocumentParams params) {}

  @Override
  public void didSave(DidSaveTextDocumentParams params) {}

  void setClient(LanguageClient client) {
    this.client = client;
  }
}
