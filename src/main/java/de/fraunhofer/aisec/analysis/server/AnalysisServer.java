
package de.fraunhofer.aisec.analysis.server;

import de.fraunhofer.aisec.analysis.JythonInterpreter;
import de.fraunhofer.aisec.analysis.cpgpasses.PassWithContext;
import de.fraunhofer.aisec.analysis.markevaluation.Evaluator;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.FindingDescription;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.cpg.graph.Node;
import de.fraunhofer.aisec.cpg.helpers.Benchmark;
import de.fraunhofer.aisec.cpg.passes.Pass;
import de.fraunhofer.aisec.crymlin.builtin.*;
import de.fraunhofer.aisec.crymlin.connectors.db.Neo4jDatabase;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection;
import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection.Type;
import de.fraunhofer.aisec.crymlin.connectors.lsp.CpgLanguageServer;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import de.fraunhofer.aisec.mark.XtextParser;
import de.fraunhofer.aisec.mark.markDsl.MarkModel;
import de.fraunhofer.aisec.markmodel.Mark;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;

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

	private static AnalysisServer instance;

	private ServerConfiguration config;

	private JythonInterpreter interp;

	private CpgLanguageServer lsp;

	private TranslationResult translationResult;

	private Mark markModel = new Mark();

	private AnalysisServer(ServerConfiguration config) {
		this.config = config;
		AnalysisServer.instance = this;

		// Register built-in functions
		BuiltinRegistry.getInstance().register(new SplitBuiltin());
		BuiltinRegistry.getInstance().register(new IsInstanceBuiltin());
		BuiltinRegistry.getInstance().register(new ReceivesValueFromBuiltin());
		BuiltinRegistry.getInstance().register(new ReceivesValueDirectlyFromBuiltin());
	}

	/**
	 * Singleton must be initialized with AnalysisServer.builder().build() first.
	 *
	 * @return the current Analysisserver instance
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
	 * @param analyzer the translationmanager to analyze
	 * @return the Future for this analysis
	 */
	public CompletableFuture<AnalysisContext> analyze(TranslationManager analyzer) {

		/*
		 * Create analysis context and register at all passes supporting contexts. An analysis context is an in-memory data structure that can be used to exchange data
		 * across passes outside of the actual CPG.
		 */

		AnalysisContext ctx = new AnalysisContext(analyzer.getConfig().getSourceLocations().get(0).toURI()); // NOTE: We currently operate on a single source file.
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
						translationResult = result;
						return persistToODB(result);
					})
				.thenApply(
					result -> {
						if (ServerConfiguration.EXPORT_GRAPHML_AND_IMPORT_TO_NEO4J) {
							exportToGraphML();
							// Optional, only if neo4j is available in classpath: re-import into Neo4J
							importIntoNeo4j();
						}
						return result;
					})
				.thenApply(
					result -> {
						log.info(
							"Evaluating mark: {} entities, {} rules",
							this.markModel.getEntities().size(),
							this.markModel.getRules().size());
						// Evaluate all MARK rules
						Evaluator mi = new Evaluator(this.markModel, this.config);
						mi.evaluate(result, ctx);
						return ctx;
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
	 * @param markFile load all mark entities/rules from this file
	 */
	public void loadMarkRules(@NonNull File markFile) {
		File markDescriptionFile;

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
	 * @return the markmodel for this analysisserver
	 */
	public @NonNull Mark getMarkModel() {
		return this.markModel;
	}

	public void stop() {
		if (interp != null) {
			interp.close();
		}
		if (lsp != null) {
			lsp.shutdown();
		}

		log.info("stop.");
	}

	public CpgLanguageServer getLSP() {
		return lsp;
	}

	public TranslationResult getTranslationResult() {
		return translationResult;
	}

	@Deprecated
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
	 */
	private void exportToGraphML() {
		// Export from OverflowDB to file
		OverflowDatabase.getInstance().connect();
		Graph graph = OverflowDatabase.getInstance().getGraph();

		log.info("Exporting {} nodes to GraphML", graph.traversal().V().count().next());
		try (FileOutputStream fos = new FileOutputStream("this-is-so-graphic.graphml")) {
			GraphMLWriter.Builder writer = graph.io(GraphMLIo.build()).writer();
			writer.vertexLabelKey("labels");
			writer.create().writeGraph(fos, graph);
			log.info("Exported GraphML to {}/this-is-so-graphic.graphml", System.getProperty("user.dir"));
		}
		catch (IOException e) {
			log.error("IOException", e);
		}
	}

	/**
	 * Note that this methods expects neo4j in classpath at runtime.
	 *
	 * It is used for debugging only.
	 */
	private void importIntoNeo4j() {
		// Import from file to Neo4J (for visualization only)
		log.info("Importing into Neo4j ...");
		try (FileInputStream fis = new FileInputStream("this-is-so-graphic.graphml");
				Neo4jGraph neo4jGraph = Neo4jGraph.open(Path.of(".data", "databases", "graph.db").toString())) {
			File neo4jDB = Path.of(".data", "databases", "graph.db").toFile();
			if (neo4jDB.exists()) {
				Path of = Path.of(
					System.getProperty("java.io.tmpdir"),
					"backup" + System.currentTimeMillis() + ".db");
				Files.move(
					neo4jDB.toPath(), of);
				log.info("Backed up old Neo4j-Database to {}", of.getFileName());
			}
			GraphMLReader.Builder reader = neo4jGraph.io(GraphMLIo.build()).reader();
			reader.strict(false);
			reader.vertexLabelKey("labels");
			reader.create().readGraph(fis, neo4jGraph);

		}
		catch (IOException e) {
			log.error("IOException", e);
		}
		catch (RuntimeException e) {
			if (e.getCause() instanceof ClassNotFoundException) {
				log.warn("Neo4j not found in path, export to neo4j failed");
			} else {
				throw e;
			}
		}
		catch (Exception e) {
			log.error("Exception", e);
		}
		log.info("Done importing");
	}

	public CompletableFuture<AnalysisContext> analyze(String url) {
		List<File> files = new ArrayList<>();
		File f = new File(url);
		if (f.isDirectory()) {
			File[] list = f.listFiles();
			if (list != null) {
				files.addAll(Arrays.asList(list));
			} else {
				log.error("Null file list");
			}
		} else {
			files.add(f);
		}

		OverflowDatabase.getInstance().connect(); // simply returns if already connected
		OverflowDatabase.getInstance().purgeDatabase();

		TranslationManager translationManager = TranslationManager.builder()
				.config(
					TranslationConfiguration.builder()
							.debugParser(true)
							.failOnError(false)
							.codeInNodes(true)
							.defaultPasses()
							.sourceFiles(
								files.toArray(new File[0]))
							.build())
				.build();
		return analyze(translationManager);
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
