package de.fraunhofer.aisec.crymlin.server;

import de.fraunhofer.aisec.cpg.AnalysisManager;
import de.fraunhofer.aisec.cpg.Database;
import de.fraunhofer.aisec.cpg.graph.Statement;
import de.fraunhofer.aisec.crymlin.JythonInterpreter;
import de.fraunhofer.aisec.crymlin.connectors.lsp.CpgLanguageServer;
import de.fraunhofer.aisec.crymlin.structures.Method;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main CPG analysis server.
 *
 * @author julian
 */
public class AnalysisServer {

  private static final Logger log = LoggerFactory.getLogger(AnalysisServer.class);

  private static AnalysisServer instance;

  private ServerConfiguration config;

  /** Connector(s) receive(s) the incoming requests from IDE/CI and returns results. */
  private Connector connector = new ImmediateConnector();

  private AnalysisServer(ServerConfiguration config) {
    this.config = config;
    AnalysisServer.instance = this;
  }

  /**
   * Singleton must be initialized with AnalysisServer.builder().build() first.
   *
   * @return
   */
  public static AnalysisServer getInstance() {
    return instance;
  }

  /**
   * Starts the server in a separate threat, returns as soon as the server is ready to operate.
   *
   * @throws Exception
   */
  public void start() throws Exception {
    // TODO Initialize CPG

    // TODO requires refactoring. Ctx must be global per analysis run, not for all runs.

    // Launch LSP server
    if (config.launchLsp) {
      launchLspServer();
    }

    // Initialize JythonInterpreter
    log.info("Launching crymlin query interpreter ...");
    JythonInterpreter interp = new JythonInterpreter();
    interp.connect();

    // Spawn an interactive console for gremlin experiments/controlling the server. Blocks forever.
    // May be replaced by custom JLine console later (not important for the moment)
    if (config.launchConsole) {
      if (!config.launchLsp) {
        interp.spawnInteractiveConsole();
      } else {
        log.warn(
            "Running in LSP mode. Refusing to start interactive console as stdin/stdout is occupied by LSP.");
      }
    }

    interp.close();
  }

  /** Launches the LSP server. */
  private void launchLspServer() {
    var lsp = new CpgLanguageServer();

    /*var pool = Executors.newCachedThreadPool();

    var port = 9000;

    try (var serverSocket = new ServerSocket(port)) {
      System.out.println("The language server is running on port " + port);
      pool.submit(
          () -> {
            while (true) {
              var clientSocket = serverSocket.accept();

              var launcher =
                  LSPLauncher.createServerLauncher(
                      lsp, clientSocket.getInputStream(), clientSocket.getOutputStream());

              launcher.startListening();

              var client = launcher.getRemoteProxy();
              lsp.connect(client);
            }
          });
      System.in.read();
    }*/
    var launcher = LSPLauncher.createServerLauncher(lsp, System.in, System.out);

    log.info("LSP server starting");
    launcher.startListening();

    var client = launcher.getRemoteProxy();
    lsp.connect(client);
  }

  /**
   * Runs an analysis and persists the result.
   *
   * @param analyzer
   */
  public void analyze(AnalysisManager analyzer) {
    AnalysisContext ctx = new AnalysisContext();

    // Run all passes and persist the result
    analyzer
        .analyze()
        .thenAccept(
            (result) -> {
              // Persist the result
              Database db = Database.getInstance();
              try {
                db.connect();
              } catch (InterruptedException e) {
                log.warn(e.getMessage(), e);
              }
              result.persist(db);
            });

    if (log.isDebugEnabled()) {
      for (Method m : ctx.methods.values()) {
        log.debug("    meth: " + m.getSignature());
        for (Statement stmt : m.getStatements()) {
          log.debug("       stmt: " + stmt.getCode());
        }
      }
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private ServerConfiguration config;

    private Builder() {}

    public Builder config(ServerConfiguration config) {
      this.config = config;
      return this;
    }

    public AnalysisServer build() {
      return new AnalysisServer(this.config);
    }
  }
}
