
package de.fraunhofer.aisec.crymlin.connectors.lsp;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.NavigableMap;
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
				System.out.println("Command " + params);
				System.out.println("   cmd: " + params.getCommand());
				for (Object arg : params.getArguments()) {
					System.out.println("  " + arg);
				}
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
