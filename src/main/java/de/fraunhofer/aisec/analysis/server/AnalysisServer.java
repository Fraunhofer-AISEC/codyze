
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
import de.fraunhofer.aisec.crymlin.builtin.Builtin;
import de.fraunhofer.aisec.crymlin.builtin.BuiltinRegistry;
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
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This is the main CPG analysis server.
 *
 * <p>
 * It accepts input from either an console or from a language server (LSP). As both use stdin/stdout, only one of them
 * can be used at a time.
 *
 * <p>
 * The {@code analyze} method kicks off the main work: parsing the program, constructing the CPG and evaluating MARK
 * rules against it, with the help of Gremlin queries.
 *
 * @author julian
 */
public class AnalysisServer {

	private static final Logger log = LoggerFactory.getLogger(AnalysisServer.class);

	/**
	 * Name of file containing human-readable explanations of findings.
	 */
	private static final String FINDING_DESCRIPTION_FILE = "findingDescription.json";

	private static AnalysisServer instance;

	private ServerConfiguration config;

	private JythonInterpreter interp;

	private CpgLanguageServer lsp;

	private TranslationResult translationResult;

	private Mark markModel = new Mark();

	@SuppressWarnings("java:S3010")
	private AnalysisServer(ServerConfiguration config) {
		this.config = config;
		AnalysisServer.instance = this;

		// Register built-in functions
		Reflections reflections = new Reflections("de.fraunhofer.aisec.crymlin.builtin");
		int i = 0;
		for (Class<? extends Builtin> builtin : reflections.getSubTypesOf(Builtin.class)) {
			log.info("Registering builtin {}", builtin.getName());
			try {
				Builtin bi = builtin.getDeclaredConstructor().newInstance();
				BuiltinRegistry.getInstance().register(bi);
			}
			catch (Exception e) {
				log.error("Could not instantiate {}: ", builtin.getName(), e);
			}
			i++;
		}
		log.info("Registered {} builtins", i);
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

	/**
	 * Starts the server in a separate threat, returns as soon as the server is ready to operate.
	 */
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

	/**
	 * Launches the LSP server.
	 */
	@SuppressWarnings("java:S106")
	private void launchLspServer() {
		lsp = new CpgLanguageServer();

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
		File srcLocation = analyzer.getConfig()
				.getSourceLocations()
				.get(0);
		AnalysisContext ctx = new AnalysisContext(srcLocation); // NOTE: We currently operate on a single source file.
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
				log.warn("Cannot read MARK model from {} (does exist: {}) - (can read: {})",
					markModelLocation.getAbsolutePath(),
					markModelLocation.exists(),
					markModelLocation.canRead());
			} else {
				loadMarkRules(markModelLocation);
			}
		}
	}

	/**
	 * recursively list all mark files
	 * @param currentFile
	 * @param allFiles
	 * @throws IOException
	 */
	private void getMarkFileLocations(File currentFile, List<File> allFiles) throws IOException {
		try (Stream<Path> walk = Files.walk(currentFile.toPath(), Integer.MAX_VALUE);) {
			File[] files = walk.map(Path::toFile)
					.filter(File::isFile)
					.filter(f -> f.getName().endsWith(".mark"))
					.toArray(File[]::new);
			for (File f : files) {
				log.info("  Loading MARK file {}", f.getAbsolutePath());
				if (f.isDirectory()) {
					getMarkFileLocations(f, allFiles);
				} else if (f.getName().endsWith(".mark")) {
					allFiles.add(f);
				}
			}
		}
	}

	/**
	 * extracts all mark-files and the findingDescription.js from a zip or jar file to a temp-folder (which is cleared by the JVM upon exiting)
	 * @param zipFilePath
	 * @return
	 * @throws IOException
	 */
	private ArrayList<File> unzipMarkAndFindingDescription(String zipFilePath) throws IOException {
		ArrayList<File> ret = new ArrayList<>();
		Path tempDirWithPrefix = Files.createTempDirectory("mark_extracted_");
		try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
			ZipEntry entry = zipIn.getNextEntry();
			// iterates over entries in the zip file
			while (entry != null) {
				String filePath = tempDirWithPrefix.toString() + File.separator + entry.getName();
				if (!entry.isDirectory()
						&& (entry.getName().endsWith(".mark") || entry.getName().equals(FINDING_DESCRIPTION_FILE))) {
					try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
						byte[] bytesIn = new byte[4096];
						int read;
						while ((read = zipIn.read(bytesIn)) != -1) {
							bos.write(bytesIn, 0, read);
						}
					}
					ret.add(new File(filePath));
				} else {
					// if the entry is a directory, make the directory
					File dir = new File(filePath);
					if (!dir.mkdir()) {
						throw new IOException("could not create folder " + dir.getAbsolutePath());
					}
				}
				zipIn.closeEntry();
				entry = zipIn.getNextEntry();
			}
		}
		return ret;
	}

	/**
	 * Loads all MARK rules from a file or a directory.
	 *
	 * @param markFile load all mark entities/rules from this file
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
			ArrayList<File> allMarkFiles = new ArrayList<>();
			try {
				getMarkFileLocations(markFile, allMarkFiles);
				for (File f : allMarkFiles) {
					log.info("  Loading MARK file {}", f.getAbsolutePath());
					parser.addMarkFile(f);
				}
			}
			catch (IOException e) {
				log.error("Failed to load MARK file", e);
			}
			markDescriptionFile = new File(markFile.getAbsolutePath() + File.separator + FINDING_DESCRIPTION_FILE);
		} else if (markFile.getName().endsWith(".jar") || markFile.getName().endsWith(".zip")) {
			try {
				ArrayList<File> allMarkFiles = unzipMarkAndFindingDescription(markFile.getAbsolutePath());
				for (File f : allMarkFiles) {
					if (f.getName().endsWith(".mark")) {
						log.info("  Loading MARK file {}", f.getAbsolutePath());
						parser.addMarkFile(f);
					} else if (f.getName().equals(FINDING_DESCRIPTION_FILE)) {
						markDescriptionFile = f;
					}
				}
			}
			catch (IOException e) {
				log.error("Failed to load MARK file", e);
			}
		} else {
			parser.addMarkFile(markFile);
			markDescriptionFile = new File(markFile.getParent() + File.separator + FINDING_DESCRIPTION_FILE);
		}

		HashMap<String, MarkModel> markModels = parser.parse();
		log.info("Done parsing MARK files in {} ms", Duration.between(start, Instant.now()).toMillis());

		for (Map.Entry<URI, List<Resource.Diagnostic>> e : parser.getErrors().entrySet()) {
			if (e.getValue() != null && !e.getValue().isEmpty()) {
				for (Resource.Diagnostic d : e.getValue()) {
					log.warn("Error in {}: l{}: {}", e.getKey().toFileString(), d.getLine(), d.getMessage());
				}
			}
		}

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

		if (markDescriptionFile != null && markDescriptionFile.exists()) {
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

		// Close in-memory graph and evict caches
		OverflowDatabase.getInstance().close();

		this.config = null;
		this.markModel = null;
		this.interp = null;
		this.lsp = null;
		this.translationResult = null;
		this.instance = null;
		log.info("stop.");
	}

	public CpgLanguageServer getLSP() {
		return lsp;
	}

	public TranslationResult getTranslationResult() {
		return translationResult;
	}

	/**
	 * @deprecated (Neo4J support will be phased out in the near future)
	 * @param result
	 * @return
	 */
	@Deprecated(forRemoval = true)
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
	 * We want to skip the OGM to make that what we see in the database is the actual graph from memory, and not the
	 * result of CPG -> OverflowDB-OGM -> OverflowDB -> Tinkerpop -> Neo4J-OGM -> Neo4J.
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
	 * <p>
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

		TranslationConfiguration.Builder tConfig = TranslationConfiguration.builder()
																		   .debugParser(true)
																		   .failOnError(false)
																		   .codeInNodes(true)
																		   .loadIncludes(config.analyzeIncludes)
																		   .defaultPasses()
																		   .sourceLocations(files.toArray(new File[0]));
		// TODO CPG only supports adding a single path as String per call. Must change to vararg of File.
		for (File includePath : config.includePath) {
			tConfig.includePath(includePath.getAbsolutePath());
		}
		TranslationManager translationManager = TranslationManager.builder()
				.config(tConfig.build())
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
