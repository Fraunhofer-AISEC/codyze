package de.fraunhofer.aisec.crymlin.connectors.lsp;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CpgWorkspaceService implements WorkspaceService {
  private static final Logger log = LoggerFactory.getLogger(CpgWorkspaceService.class);

  @Override
  public void didChangeConfiguration(DidChangeConfigurationParams params) {
    // TODO implement me
    log.debug("Not implemented yet");
  }

  @Override
  public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
    // TODO implement me
    log.debug("Not implemented yet");
  }
}
