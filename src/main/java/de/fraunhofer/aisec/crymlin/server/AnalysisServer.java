package de.fraunhofer.aisec.crymlin.server;

import de.fhg.aisec.mark.XtextParser;
import de.fhg.aisec.mark.markDsl.MarkModel;
import de.fraunhofer.aisec.cpg.Database;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.cpg.helpers.Benchmark;
import de.fraunhofer.aisec.cpg.passes.Pass;
import de.fraunhofer.aisec.crymlin.JythonInterpreter;
import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection;
import de.fraunhofer.aisec.crymlin.connectors.lsp.CpgLanguageServer;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import de.fraunhofer.aisec.crymlin.passes.PassWithContext;
import de.fraunhofer.aisec.markmodel.Mark;
import de.fraunhofer.aisec.markmodel.MarkInterpreter;
import de.fraunhofer.aisec.markmodel.MarkModelLoader;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.script.ScriptException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main CPG analysis server.
 *
 * <p>It accepts input from either an console or from a language server (LSP). As both use
 * stdin/stdout, only one of them can be used at a time.
 *
 * <p>The {@code analyze} method kicks off the main work: parsing the program, constructing the CPG
 * and evaluating MARK rules against it, with the help of Gremlin queries.
 *
 * @author julian
 */
public class AnalysisServer {

  private static final Logger log = LoggerFactory.getLogger(AnalysisServer.class);

  private static AnalysisServer instance;

  private ServerConfiguration config;

  private JythonInterpreter interp;

  public CpgLanguageServer lsp;

  private Mark markModel = new Mark();

  private AnalysisServer(ServerConfiguration config) {
    this.config = config;
    AnalysisServer.instance = this;
  }

  /**
   * Singleton must be initialized with AnalysisServer.builder().build() first.
   *
   * @return
   */
  @Nullable
  public static AnalysisServer getInstance() {
    return instance;
  }

  /** Starts the server in a separate threat, returns as soon as the server is ready to operate. */
  public void start() {
    if (config.launchLsp) {
      launchLspServer();
    } else if (config.launchConsole) {
      launchConsole();
    } else {
      // only load rules
      loadMarkRulesFromConfig();
    }
  }

  private void launchConsole() {

    loadMarkRulesFromConfig();

    // Initialize JythonInterpreter
    log.info("Launching crymlin query interpreter ...");
    interp = new JythonInterpreter();
    interp.connect();

    // Spawn an interactive console for gremlin experiments/controlling the server.
    // Blocks forever.
    // May be replaced by custom JLine console later (not important for the moment)
    // Blocks forever:
    interp.spawnInteractiveConsole();
  }

  /** Launches the LSP server. */
  private void launchLspServer() {
    lsp = new CpgLanguageServer();

    /*
     *  pool = Executors.newCachedThreadPool();
     *
     *  port = 9000;
     *
     * try ( serverSocket = new ServerSocket(port)) {
     * System.out.println("The language server is running on port " + port);
     * pool.submit( () -> { while (true) {  clientSocket = serverSocket.accept();
     *
     *  launcher = LSPLauncher.createServerLauncher( lsp,
     * clientSocket.getInputStream(), clientSocket.getOutputStream());
     *
     * launcher.startListening();
     *
     *  client = launcher.getRemoteProxy(); lsp.connect(client); } });
     * System.in.read(); }
     */
    Launcher<LanguageClient> launcher =
        LSPLauncher.createServerLauncher(lsp, System.in, System.out);

    launcher.startListening();

    LanguageClient client = launcher.getRemoteProxy();
    lsp.connect(client);
    log.info("LSP server started");

    loadMarkRulesFromConfig();
  }

  /**
   * Runs an analysis and persists the result.
   *
   * @param analyzer
   * @return
   * @throws ExecutionException
   * @throws InterruptedException
   */
  public CompletableFuture<TranslationResult> analyze(TranslationManager analyzer) {
    /*
     * Create analysis context and register at all passes supporting contexts.
     *
     * An analysis context is an in-memory data structure that can be used to
     * exchange data across passes outside of the actual CPG.
     */

    AnalysisContext ctx = new AnalysisContext();
    for (Pass p : analyzer.getPasses()) {
      if (p instanceof PassWithContext) {
        ((PassWithContext) p).setContext(ctx);
      }
    }

    // Run all passes and persist the result
    return analyzer
        .analyze() // Run analysis
        .thenApply( // Persist to DB
            (result) -> {
              // Attach analysis context to result
              result.getScratch().put("ctx", ctx);

              Benchmark b = new Benchmark(this.getClass(), "Persisting to Database");
              // Persist the result
              Database.getInstance()
                  .connect(); // this does not connect again if we are already connected
              result.persist(Database.getInstance());
              long duration = b.stop();
              try (TraversalConnection t = new TraversalConnection()) { // connects to the DB
                CrymlinTraversalSource crymlinTraversal = t.getCrymlinTraversal();
                Long numEdges = crymlinTraversal.E().count().next();
                Long numVertices = crymlinTraversal.V().count().next();
                log.info(
                    "Nodes in graph: {} ({} ms/node), edges in graph: {} ({} ms/edge)",
                    numVertices,
                    String.format("%.2f", (double) duration / numVertices),
                    numEdges,
                    String.format("%.2f", (double) duration / numEdges));
              }
              log.info(
                  "Benchmark: Persisted approx {} nodes", Database.getInstance().getNumNodes());
              return result;
            })
        .thenApply(
            (result) -> {
              log.info(
                  "Evaluating mark: {} entities, {} rules",
                  this.markModel.getEntities().size(),
                  this.markModel.getRules().size());
              // Evaluate all MARK rules
              MarkInterpreter mi = new MarkInterpreter(this.markModel);
              return mi.evaluate(result, ctx);
            });
  }

  public void loadMarkRulesFromConfig() {
    /*
     * Load MARK model as given in configuration, if it has not been set manually before.
     */
    if (config.markModelFiles != null && !config.markModelFiles.isEmpty()) {
      File markModelLocation = new File(config.markModelFiles);
      if (!markModelLocation.exists() || !markModelLocation.canRead()) {
        log.warn("Cannot read MARK model from {}", markModelLocation.getAbsolutePath());
      } else {
        loadMarkRules(markModelLocation);
      }
    }
  }

  /**
   * Loads all MARK rules from a file or a directory.
   *
   * @param markFile
   */
  public void loadMarkRules(@NonNull File markFile) {
    log.info("Parsing MARK files");
    Instant start = Instant.now();

    XtextParser parser = new XtextParser();

    if (!markFile.exists() || !markFile.canRead()) {
      log.warn("Cannot read MARK file(s) {}", markFile.getAbsolutePath());
    }

    if (markFile.isDirectory()) {
      log.info("Loading MARK from directory {}", markFile.getAbsolutePath());
      try {
        DirectoryStream<Path> fileStream = Files.newDirectoryStream(markFile.toPath());
        for (Path f : fileStream) {
          if (f.getFileName().toString().endsWith(".mark")) {
            log.info("  Loading MARK file {}", f.toFile().getAbsolutePath());
            parser.addMarkFile(f.toFile());
          }
        }
      } catch (IOException e) {
        log.error("Failed to load MARK file", e);
      }
    } else {
      parser.addMarkFile(markFile);
    }

    HashMap<String, MarkModel> markModels = parser.parse();
    log.info("Done parsing MARK files in {} ms", Duration.between(start, Instant.now()).toMillis());

    start = Instant.now();
    log.info("Transforming MARK Xtext to internal format");
    this.markModel = new MarkModelLoader().load(markModels);
    log.info(
        "Done Transforming MARK Xtext to internal format in {} ms",
        Duration.between(start, Instant.now()).toMillis());

    log.info(
        "Loaded {} entities and {} rules.",
        this.markModel.getEntities().size(),
        this.markModel.getRules().size());
  }

  /**
   * Returns a list with the names of the currently loaded MARK rules.
   *
   * @return
   */
  public @NonNull Mark getMarkModel() {
    return this.markModel;
  }

  /**
   * Runs a Gremlin query against the currently analyzed program.
   *
   * <p>Make sure to call {@code analyze()} before.
   *
   * @param crymlin
   * @return
   * @throws ScriptException
   */
  // todo remove. Then it is sufficient to initialize Jython only for the console case!
  @Deprecated
  public Object query(String crymlin) throws ScriptException {
    if (interp == null) {
      interp = new JythonInterpreter();
      interp.connect();
    }
    return interp.query(crymlin);
  }

  public void stop() throws Exception {
    if (interp != null) {
      interp.close();
      interp = null;
    }
    if (lsp != null) {
      lsp.shutdown();
      lsp = null;
    }
    Database.getInstance().close();
    config = null;
    markModel = null;

    log.info("stop.");
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
