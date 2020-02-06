
package de.fraunhofer.aisec.crymlin.connectors.lsp;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.services.WorkspaceService;

public class CpgWorkspaceService implements WorkspaceService {

	@Override
	public void didChangeConfiguration(DidChangeConfigurationParams params) {
		// unclear what this would be needed for
	}

	@Override
	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
		// not needed, we react to documentService.onSave
	}
}
