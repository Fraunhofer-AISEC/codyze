
package de.fraunhofer.aisec.codyze.crymlin.connectors.lsp;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.concurrent.CompletableFuture;

public class CpgWorkspaceService implements WorkspaceService {

	private LanguageClient client;

	@Override
	public void didChangeConfiguration(DidChangeConfigurationParams params) {
		// unclear what this would be needed for
	}

	@Override
	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
		// not needed, we react to documentService.onSave
	}

	@Override
	public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
		return CompletableFuture.supplyAsync(
			() -> {
				MessageParams mp = new MessageParams();
				mp.setMessage("Received command " + params.getCommand());
				client.showMessage(mp);
				return true;
			});
	}

	void setClient(LanguageClient client) {
		this.client = client;
	}

}
