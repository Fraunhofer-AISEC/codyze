package de.fraunhofer.aisec.crymlin.server;

import de.fhg.aisec.mark.XtextParser;
import de.fhg.aisec.mark.markDsl.CallStatement;
import de.fhg.aisec.mark.markDsl.Expression;
import de.fhg.aisec.mark.markDsl.MarkModel;
import de.fhg.aisec.markmodel.MEntity;
import de.fhg.aisec.markmodel.MOp;
import de.fhg.aisec.markmodel.MRule;
import de.fhg.aisec.markmodel.Mark;
import de.fhg.aisec.markmodel.MarkInterpreter;
import de.fhg.aisec.markmodel.MarkModelLoader;
import de.fraunhofer.aisec.cpg.Database;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.cpg.passes.Pass;
import de.fraunhofer.aisec.crymlin.JythonInterpreter;
import de.fraunhofer.aisec.crymlin.connectors.lsp.CpgLanguageServer;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import de.fraunhofer.aisec.crymlin.passes.PassWithContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.script.ScriptException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.lsp4j.launch.LSPLauncher;
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

  private CpgLanguageServer lsp;

  private AnalysisContext ctx = new AnalysisContext();

  @NonNull private Mark markModel = new Mark();

  @Nullable Set<String> evidences;

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

  /**
   * Starts the server in a separate threat, returns as soon as the server is ready to operate.
   *
   * @throws Exception
   */
  public void start() throws Exception {
    // TODO Initialize CPG

    // TODO requires refactoring. Ctx must be global per analysis run, not for all
    // runs.

    // Launch LSP server
    if (config.launchLsp) {
      launchLspServer();
    }

    // Initialize JythonInterpreter
    log.info("Launching crymlin query interpreter ...");
    interp = new JythonInterpreter();
    interp.connect();

    // Spawn an interactive console for gremlin experiments/controlling the server.
    // Blocks forever.
    // May be replaced by custom JLine console later (not important for the moment)
    if (config.launchConsole) {
      if (!config.launchLsp) {
        // Blocks forever:
        interp.spawnInteractiveConsole();
      } else {
        log.warn(
            "Running in LSP mode. Refusing to start interactive console as stdin/stdout is occupied by LSP.");
      }
    }
  }

  /** Launches the LSP server. */
  private void launchLspServer() {
    lsp = new CpgLanguageServer();

    /*
     * var pool = Executors.newCachedThreadPool();
     *
     * var port = 9000;
     *
     * try (var serverSocket = new ServerSocket(port)) {
     * System.out.println("The language server is running on port " + port);
     * pool.submit( () -> { while (true) { var clientSocket = serverSocket.accept();
     *
     * var launcher = LSPLauncher.createServerLauncher( lsp,
     * clientSocket.getInputStream(), clientSocket.getOutputStream());
     *
     * launcher.startListening();
     *
     * var client = launcher.getRemoteProxy(); lsp.connect(client); } });
     * System.in.read(); }
     */
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
   * @return
   * @throws ExecutionException
   * @throws InterruptedException
   */
  public CompletableFuture<TranslationResult> analyze(TranslationManager analyzer)
      throws InterruptedException, ExecutionException {
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

    /*
     * Create analysis context (in-memory structures) and register at all passes
     * supporting contexts.
     */
    this.ctx = new AnalysisContext();
    for (Pass p : analyzer.getPasses()) {
      if (p instanceof PassWithContext) {
        ((PassWithContext) p).setContext(this.ctx);
      }
    }

    // Run all passes and persist the result
    return analyzer
        .analyze()
        .thenApply(
            (result) -> {
              // Attach analysis context to result
              result.getScratch().put("ctx", ctx);

              // Persist the result
              Database db = Database.getInstance();
              try {
                db.connect(config.dbUri, config.dbUser, config.dbPassword);
              } catch (InterruptedException e) {
                log.warn(e.getMessage(), e);
              }
              result.persist(db);
              return result;
            })
        .thenApply(
            (result) -> {
              // Evaluate all rules
              return evaluate(result);
            });
  }

  /**
   * Evaluates the {@code markModel} against the currently analyzed program.
   *
   * @param result
   */
  @SuppressWarnings("unchecked")
  private TranslationResult evaluate(TranslationResult result) {

    // "Populate" objects
    for (MEntity ent : this.markModel.getEntities()) {
      for (MOp op : ent.getOps()) {
        for (CallStatement call : op.getCallStatements()) {
          CrymlinTraversalSource g = interp.getCrymlinTraversal();

          List<String> myCalls = g.calls().has("name", call.getCall().getName()).name().toList();
          if (!myCalls.isEmpty()) {
            log.debug("my calls: " + myCalls);
            // TODO if myCalls.size()>0, we found a call that was specified in MARK. "Populate" the
            // object.

            // TODO Find out arguments of the call and try to resolve concrete values for them

            // TODO "Populate" the entity and assign the resolved values to the entity's variables
          }
          this.markModel.getPopulatedEntities().put(ent.getName(), ent);
        }
      }
    }

    // Evaluate rules against populated objects
    MarkInterpreter interp = new MarkInterpreter(this.markModel);
    for (MRule r : this.markModel.getRules()) {
      log.debug("Processing rule {}", r.getName());
      // TODO parse rule and do something with it
      Optional<String> matchingEntity =
          r.getStatement().getEntities().stream()
              .map(ent -> ent.getN())
              .filter(entityName -> this.markModel.getPopulatedEntities().containsKey(entityName))
              .findAny();
      if (matchingEntity.isPresent()) {
        Expression ensureExpr = r.getStatement().getEnsure().getExp();
        log.debug(interp.exprToString(ensureExpr));
        // TODO evaluate expression against populated mark entities
      }
    }
    return result;
  }

  /**
   * Loads all MARK rules from a file or a directory.
   *
   * @param markFile
   */
  public void loadMarkRules(@NonNull File markFile) {
    XtextParser parser = new XtextParser();

    if (!markFile.exists() || !markFile.canRead()) {
      log.warn("Cannot read MARK file(s) {}", markFile.getAbsolutePath());
    }

    if (markFile.isDirectory()) {
      log.debug("Loading MARK from directory {}", markFile.getAbsolutePath());
      try {
        DirectoryStream<Path> fileStream = Files.newDirectoryStream(markFile.toPath());
        Iterator<Path> it = fileStream.iterator();
        while (it.hasNext()) {
          Path f = it.next();
          if (f.getFileName().toString().endsWith(".mark")) {
            log.debug("  Loading MARK file {}", f.toFile().getAbsolutePath());
            parser.addMarkFile(f.toFile());
          }
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    } else {
      parser.addMarkFile(markFile);
    }

    List<MarkModel> markModels = parser.parse();

    // Extract "evidences" from MARK entities. Evidences are either method calls or declarations
    // that we want to use as a start for our analysis
    this.markModel = new MarkModelLoader().load(markModels);

    log.info(
        "Loaded {} entities and {} rules.",
        this.markModel.getEntities().size(),
        this.markModel.getRules().size());
  }

  /**
   * Returns a list with the names of the currently loaded MARK rules.
   *
   * @param markFile
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
  public Object query(String crymlin) throws ScriptException {
    return interp.query(crymlin);
  }

  /**
   * Returns the analysis context of the last analysis run.
   *
   * <p>This methods will return a different object after each call to {@code analyze()}.
   *
   * @return
   */
  @Nullable
  public AnalysisContext retrieveContext() {
    return this.ctx;
  }

  public void stop() throws Exception {
    if (interp != null) {
      interp.close();
    }
    if (lsp != null) {
      lsp.shutdown();
    }
  }

  /**
   * Returns a (possibly empty) list of findings, i.e. violations of MARK rules that were found
   * during analysis. Make sure to call {@code analyze()} before as otherwise this method will
   * return an empty list.
   *
   * @return
   */
  @NonNull
  public List<String> getFindings() {
    if (this.ctx != null) {
      return this.ctx.getFindings();
    } else {
      return new ArrayList<>();
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
