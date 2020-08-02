
package de.fraunhofer.aisec.crymlin.connectors.lsp;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.ExecuteCommandOptions;
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
 * Implementation of a {@link LanguageServer} according to the Language Server Protocol (LSP). It synchronizes with the IDE and translates source code into our graph upon
 * triggering certain events.
 */
public class CpgLanguageServer implements LanguageServer, LanguageClientAware {

	private static final Logger log = LoggerFactory.getLogger(CpgLanguageServer.class);

	private CpgDocumentService textDocumentService = new CpgDocumentService();

	private CpgWorkspaceService workspaceService = new CpgWorkspaceService();

	private Instant start = Instant.now();
	private boolean shutdownRequested = false;

	@Override
	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		start = Instant.now();

		ClientCapabilities clientCaps = params.getCapabilities();
		List<String> hoverFormats = clientCaps.getTextDocument().getHover().getContentFormat();
		for (String hoverFormat : hoverFormats) {
			log.info("Hover format: " + hoverFormat);
		}
		InitializeResult result = new InitializeResult();

		ServerCapabilities capabilities = new ServerCapabilities();
		capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
		List<String> commands = Arrays
				.asList(new String[] { "textDocument/documentHighlight", "textDocument/documentSymbol", "textDocument/hover", "textDocument/codeAction", "resolve/wf",
						"resolve/fp" });
		ExecuteCommandOptions exep = new ExecuteCommandOptions(commands);
		capabilities.setExecuteCommandProvider(exep);
		capabilities.setHoverProvider(false);
		capabilities.setCodeActionProvider(true);

		result.setCapabilities(capabilities);

		return CompletableFuture.completedFuture(result);
	}

	@Override
	public CompletableFuture<Object> shutdown() {
		log.info("shutdown after  {} ms.", Duration.between(start, Instant.now()).toMillis());
		shutdownRequested = true;
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public void exit() {
		if (shutdownRequested) {
			System.exit(0);
		}
		System.exit(1); // acc. to LSP specification
	}

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
