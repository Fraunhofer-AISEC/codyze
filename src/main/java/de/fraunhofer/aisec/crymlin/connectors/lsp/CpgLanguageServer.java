package de.fraunhofer.aisec.crymlin.connectors.lsp;

import de.fraunhofer.aisec.cpg.Database;
import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a {@link LanguageServer} according to the Language Server Protocol (LSP). It
 * synchronizes with the IDE and translates source code into our graph upon triggering certain
 * events.
 */
public class CpgLanguageServer implements LanguageServer, LanguageClientAware {

  private static final Logger log = LoggerFactory.getLogger(CpgLanguageServer.class);

  private CpgDocumentService textDocumentService = new CpgDocumentService();

  private CpgWorkspaceService workspaceService = new CpgWorkspaceService();

  @Override
  public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
    log.debug("Pre-connecting to DB");
    Database.getInstance().connect();

    InitializeResult result = new InitializeResult();

    ServerCapabilities capabilities = new ServerCapabilities();
    capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);

    result.setCapabilities(capabilities);

    return CompletableFuture.completedFuture(result);
  }

  @Override
  public CompletableFuture<Object> shutdown() {
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public void exit() {}

  @Override
  public TextDocumentService getTextDocumentService() {
    return this.textDocumentService;
  }

  @Override
  public WorkspaceService getWorkspaceService() {
    return this.workspaceService;
  }

  @Override
  public void connect(LanguageClient client) {
    this.textDocumentService.setClient(client);
  }
}
