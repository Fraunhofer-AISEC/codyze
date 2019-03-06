package de.fraunhofer.aisec.crymlin.server;

import java.io.File;

/**
 * Connects the server to the outside, i.e. to IDEs, CIs or other clients.
 *
 * <p>Implementations of this interface may be REST, LSP, etc.
 *
 * @author julian
 */
public interface Connector {

  void analyzeFile(File file);

  void analyzeFiles(File file);
}
