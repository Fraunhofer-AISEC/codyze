package de.fraunhofer.aisec.crymlin.server;

import de.fraunhofer.aisec.cpg.AnalysisConfiguration;
import de.fraunhofer.aisec.cpg.AnalysisManager;
import de.fraunhofer.aisec.cpg.AnalysisResult;
import de.fraunhofer.aisec.cpg.Database;
import de.fraunhofer.aisec.cpg.graph.Statement;
import de.fraunhofer.aisec.crymlin.JythonInterpreter;
import de.fraunhofer.aisec.crymlin.passes.StatementsPerMethodPass;
import de.fraunhofer.aisec.crymlin.structures.Method;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main CPG analysis server.
 *
 * @author julian
 */
public class AnalysisServer {

  private static final Logger log = LoggerFactory.getLogger(AnalysisServer.class);

  private ServerConfiguration config;

  /** Connector(s) receive(s) the incoming requests from IDE/CI and returns results. */
  private Connector connector = new ImmediateConnector();

  private AnalysisServer(ServerConfiguration config) {
    this.config = config;
  }

  /**
   * Starts the server in a separate threat, returns as soon as the server is ready to operate.
   *
   * @throws Exception
   */
  public void start() throws Exception {
    // TODO Initialize CPG

    // TODO requires refactoring. Ctx must be global per analysis run, not for all runs.
    AnalysisContext ctx = new AnalysisContext();
    AnalysisManager aServer =
        AnalysisManager.builder()
            .config(
                AnalysisConfiguration.builder()
                    .sourceFiles(new File("src/test/resources/good/Bouncycastle.java"))
                    .registerPass(new StatementsPerMethodPass(ctx))
                    .build())
            .build();

    // Run all passes. This will *not* yet persist the result
    AnalysisResult result = aServer.analyze().get();

    // Persist the result
    Database db = Database.getInstance();
    db.connect();
    result.persist(db);

    for (Method m : ctx.methods.values()) {
      System.out.println("    -> " + m.getSignature());
      for (Statement stmt : m.getStatements()) {
        System.out.println("        - " + stmt.getCode());
      }
    }

    // Initialize JythonInterpreter
    System.out.println("Launching query interpreter ...");
    JythonInterpreter interp = new JythonInterpreter();
    interp.connect();

    // Spawn an interactive console for gremlin experiments/controlling the server. Blocks forever.
    // May be replaced by custom JLine console later (not important for the moment)
    if (config.launchConsole) {
      interp.spawnInteractiveConsole();
    }

    interp.close();
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
