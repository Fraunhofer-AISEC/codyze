
package de.fraunhofer.aisec.crymlin.server;

import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.cpg.graph.Node;
import de.fraunhofer.aisec.cpg.helpers.Benchmark;
import de.fraunhofer.aisec.cpg.passes.Pass;
import de.fraunhofer.aisec.crymlin.JythonInterpreter;
import de.fraunhofer.aisec.crymlin.builtin.BuiltinRegistry;
import de.fraunhofer.aisec.crymlin.builtin.IsInstanceBuiltin;
import de.fraunhofer.aisec.crymlin.builtin.ReceivesValueFromBuiltin;
import de.fraunhofer.aisec.crymlin.builtin.SplitBuiltin;
import de.fraunhofer.aisec.crymlin.connectors.db.Neo4jDatabase;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection;
import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection.Type;
import de.fraunhofer.aisec.crymlin.connectors.lsp.CpgLanguageServer;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import de.fraunhofer.aisec.crymlin.passes.PassWithContext;
import de.fraunhofer.aisec.crymlin.utils.FindingDescription;
import de.fraunhofer.aisec.mark.XtextParser;
import de.fraunhofer.aisec.mark.markDsl.MarkModel;
import de.fraunhofer.aisec.markmodel.Mark;
import de.fraunhofer.aisec.markmodel.MarkInterpreter;
import de.fraunhofer.aisec.markmodel.MarkModelLoader;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.io.graphml.GraphMLIo;
import org.apache.tinkerpop.gremlin.structure.io.graphml.GraphMLReader;
import org.apache.tinkerpop.gremlin.structure.io.graphml.GraphMLWriter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * This is the main CPG analysis server.
 *
 * <p>
 * It accepts input from either an console or from a language server (LSP). As both use stdin/stdout, only one of them can be used at a time.
 *
 * <p>
 * The {@code analyze} method kicks off the main work: parsing the program, constructing the CPG and evaluating MARK rules against it, with the help of Gremlin queries.
 *
 * @author julian
 */
public class AnalysisServer {

	private static final Logger log = LoggerFactory.getLogger(AnalysisServer.class);
	private static final boolean EXPORT_TO_NEO4J = false;

	private static AnalysisServer instance;

	private ServerConfiguration config;

	private JythonInterpreter interp;

	private CpgLanguageServer lsp;

	private Mark markModel = new Mark();

	private AnalysisServer(ServerConfiguration config) {
		this.config = config;
		AnalysisServer.instance = this;

		// Register built-in functions
		BuiltinRegistry.getInstance().register(new SplitBuiltin());
		BuiltinRegistry.getInstance().register(new IsInstanceBuiltin());
		BuiltinRegistry.getInstance().register(new ReceivesValueFromBuiltin());
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

	public static Builder builder() {
		return new Builder();
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
		 * pool = Executors.newCachedThreadPool(); port = 9000; try ( serverSocket = new ServerSocket(port)) {
		 * System.out.println("The language server is running on port " + port); pool.submit( () -> { while (true) { clientSocket = serverSocket.accept(); launcher =
		 * LSPLauncher.createServerLauncher( lsp, clientSocket.getInputStream(), clientSocket.getOutputStream()); launcher.startListening(); client =
		 * launcher.getRemoteProxy(); lsp.connect(client); } }); System.in.read(); }
		 */
		Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(lsp, System.in, System.out);

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
		 * Create analysis context and register at all passes supporting contexts. An analysis context is an in-memory data structure that can be used to exchange data
		 * across passes outside of the actual CPG.
		 */

		AnalysisContext ctx = new AnalysisContext();
		for (Pass p : analyzer.getPasses()) {
			if (p instanceof PassWithContext) {
				((PassWithContext) p).setContext(ctx);
			}
		}

		// Run all passes and persist the result
		return analyzer.analyze() // Run analysis
				.thenApply(
					result -> {
						// Attach analysis context to result
						result.getScratch().put("ctx", ctx);
						return persistToODB(result);
					}).thenApplyAsync(
						result -> {
							if (EXPORT_TO_NEO4J) {
								// Optional, just for debugging: re-import into Neo4J
								exportToNeo4j(result);
							}
							return result;
						}).thenApply(
							result -> {
								log.info(
									"Evaluating mark: {} entities, {} rules",
									this.markModel.getEntities().size(),
									this.markModel.getRules().size());
								// Evaluate all MARK rules
								MarkInterpreter mi = new MarkInterpreter(this.markModel, this.config);
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
		File markDescriptionFile = null;

		log.info("Parsing MARK files");
		Instant start = Instant.now();

		XtextParser parser = new XtextParser();

		if (!markFile.exists() || !markFile.canRead()) {
			log.warn("Cannot read MARK file(s) {}", markFile.getAbsolutePath());
		}

		if (markFile.isDirectory()) {
			log.info("Loading MARK from directory {}", markFile.getAbsolutePath());
			try (DirectoryStream<Path> fileStream = Files.newDirectoryStream(markFile.toPath())) {
				for (Path f : fileStream) {
					if (f.getFileName().toString().endsWith(".mark")) {
						log.info("  Loading MARK file {}", f.toFile().getAbsolutePath());
						parser.addMarkFile(f.toFile());
					}
				}
			}
			catch (IOException e) {
				log.error("Failed to load MARK file", e);
			}
			markDescriptionFile = new File(markFile.getAbsolutePath() + File.separator + "findingDescription.json");
		} else {
			parser.addMarkFile(markFile);
			markDescriptionFile = new File(markFile.getParent() + File.separator + "findingDescription.json");
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

		if (markDescriptionFile.exists()) {

			FindingDescription.getInstance().init(markDescriptionFile);
		} else {
			log.info("MARK description file does not exist");
		}
	}

	/**
	 * Returns a list with the names of the currently loaded MARK rules.
	 *
	 * @return
	 */
	public @NonNull Mark getMarkModel() {
		return this.markModel;
	}

	public void stop() {
		if (interp != null) {
			interp.close();
			interp = null;
		}
		if (lsp != null) {
			lsp.shutdown();
			lsp = null;
		}
		Neo4jDatabase.getInstance().close();
		config = null;
		markModel = null;

		log.info("stop.");
	}

	public CpgLanguageServer getLSP() {
		return lsp;
	}

	private TranslationResult persistToNeo4J(TranslationResult result) {
		Benchmark b = new Benchmark(this.getClass(), "Persisting to Database");
		// Persist the result
		Neo4jDatabase.getInstance().connect(); // this does not connect again if we are already connected
		Neo4jDatabase.getInstance().purgeDatabase();
		Neo4jDatabase<Node> db = Neo4jDatabase.getInstance();
		db.saveAll(result.getTranslationUnits());
		long duration = b.stop();
		// connect to DB
		try (TraversalConnection t = new TraversalConnection(TraversalConnection.Type.NEO4J)) {
			CrymlinTraversalSource crymlinTraversal = t.getCrymlinTraversal();
			Long numEdges = crymlinTraversal.E().count().next();
			Long numVertices = crymlinTraversal.V().count().next();
			log.info(
				"Nodes in Neo4J graph: {} ({} ms/node), edges in graph: {} ({} ms/edge)",
				numVertices,
				String.format("%.2f", (double) duration / numVertices),
				numEdges,
				String.format("%.2f", (double) duration / numEdges));
		}
		log.info("Benchmark: Persisted approx {} nodes", Neo4jDatabase.getInstance().getNumNodes());
		return result;
	}

	private TranslationResult persistToODB(TranslationResult result) {
		Benchmark b = new Benchmark(this.getClass(), "Persisting to Database");
		// Persist the result
		OverflowDatabase.getInstance().purgeDatabase();
		OverflowDatabase<Node> db = OverflowDatabase.getInstance();
		db.saveAll(result.getTranslationUnits());
		long duration = b.stop();
		// connect to DB
		try (TraversalConnection t = new TraversalConnection(Type.OVERFLOWDB)) {
			CrymlinTraversalSource crymlinTraversal = t.getCrymlinTraversal();
			Long numEdges = crymlinTraversal.V().outE().count().next();
			Long numVertices = crymlinTraversal.V().count().next();
			log.info(
				"Nodes in OverflowDB graph: {} ({} ms/node), edges in graph: {} ({} ms/edge)",
				numVertices,
				String.format("%.2f", (double) duration / numVertices),
				numEdges,
				String.format("%.2f", (double) duration / numEdges));
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		log.info("Benchmark: Persisted approx {} nodes", db.getNumNodes());
		return result;
	}

	/**
	 * It's awful, but this is the only way to import raw data into Neo4J without relying on an OGM.
	 *
	 * <p>
	 * We want to skip the OGM to make that what we see in the database is the actual graph from memory, and not the result of CPG -> OverflowDB-OGM -> OverflowDB ->
	 * Tinkerpop -> Neo4J-OGM -> Neo4J.
	 *
	 * @param result
	 * @return
	 */
	private TranslationResult exportToNeo4j(TranslationResult result) {
		try {
			// Export from OverflowDB to file
			OverflowDatabase.getInstance().connect();
			Graph graph = OverflowDatabase.getInstance().getGraph();

			log.info("Exporting {} nodes to GraphML", graph.traversal().V().count().next());
			try (FileOutputStream fos = new FileOutputStream("this-is-so-graphic.graphml")) {
				GraphMLWriter.Builder writer = graph.io(GraphMLIo.build()).writer();
				writer.vertexLabelKey("labels");
				writer.create().writeGraph(fos, graph);
			}
			catch (IOException e) {
				log.error("IOException", e);
			}

			// Import from file to Neo4J (for visualization only)
			log.info("Importing into Neo4j ...");
			try (FileInputStream fis = new FileInputStream("this-is-so-graphic.graphml")) {
				File neo4jDB = Path.of(".data", "databases", "graph.db").toFile(); // new File("./.data/databases/graph.db");
				if (neo4jDB.exists()) {
					Files.move(
						neo4jDB.toPath(),
						Path.of(
							System.getProperty("java.io.tmpdir"),
							"backup" + System.currentTimeMillis() + ".db"));
				}
				try (Neo4jGraph neo4jGraph = Neo4jGraph.open(Path.of(".data", "databases", "graph.db").toString())) {
					GraphMLReader.Builder reader = neo4jGraph.io(GraphMLIo.build()).reader();
					reader.strict(false);
					reader.vertexLabelKey("labels");
					reader.create().readGraph(fis, neo4jGraph);
				}
			}
			catch (IOException e) {
				log.error("IOException", e);
			}
			log.info("Done importing");
		}
		catch (Throwable t) {
			log.error("Throwable", t);
		}
		return result;
	}

	public static class Builder {
		private ServerConfiguration config;

		private Builder() {
		}

		public Builder config(ServerConfiguration config) {
			this.config = config;
			return this;
		}

		public AnalysisServer build() {
			return new AnalysisServer(this.config);
		}
	}
}
