
package de.fraunhofer.aisec.analysis.server;

import de.fraunhofer.aisec.analysis.JythonInterpreter;
import de.fraunhofer.aisec.analysis.cpgpasses.PassWithContext;
import de.fraunhofer.aisec.analysis.markevaluation.Evaluator;
import de.fraunhofer.aisec.analysis.markevaluation.LegacyEvaluator;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.FindingDescription;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.cpg.graph.Node;
import de.fraunhofer.aisec.cpg.helpers.Benchmark;
import de.fraunhofer.aisec.cpg.passes.CallResolver;
import de.fraunhofer.aisec.cpg.passes.EvaluationOrderGraphPass;
import de.fraunhofer.aisec.cpg.passes.FilenameMapper;
import de.fraunhofer.aisec.cpg.passes.ImportResolver;
import de.fraunhofer.aisec.cpg.passes.JavaExternalTypeHierarchyResolver;
import de.fraunhofer.aisec.cpg.passes.Pass;
import de.fraunhofer.aisec.cpg.passes.TypeHierarchyResolver;
import de.fraunhofer.aisec.cpg.passes.TypeResolver;
import de.fraunhofer.aisec.cpg.passes.VariableUsageResolver;
import de.fraunhofer.aisec.crymlin.legacy_builtin.Builtin;
import de.fraunhofer.aisec.crymlin.legacy_builtin.BuiltinRegistry;
import de.fraunhofer.aisec.crymlin.connectors.db.Database;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection;
import de.fraunhofer.aisec.crymlin.connectors.lsp.CpgLanguageServer;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import de.fraunhofer.aisec.mark.XtextParser;
import de.fraunhofer.aisec.mark.markDsl.MarkModel;
import de.fraunhofer.aisec.markmodel.Mark;
import de.fraunhofer.aisec.markmodel.MarkModelLoader;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

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

	private Database<Node> db;

	private Mark markModel = new Mark();

	@SuppressWarnings("java:S3010")
	private AnalysisServer(ServerConfiguration config) {
		this.config = config;
		AnalysisServer.instance = this;

		// Register built-in functions
		Benchmark bench = new Benchmark(AnalysisServer.class, "Registration of builtins");
		Reflections reflections = new Reflections("de.fraunhofer.aisec.crymlin.legacy_builtin");
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
		bench.stop();
		log.info("Registered {} builtins", i);

		db = new OverflowDatabase(config);
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
		AnalysisContext ctx = new AnalysisContext(srcLocation, db); // NOTE: We currently operate on a single source file.
		for (Pass p : analyzer.getPasses()) {
			if (p instanceof PassWithContext) {
				((PassWithContext) p).setContext(ctx);
			}
		}
		// Run all passes and persist the result
		final Benchmark benchParsing = new Benchmark(AnalysisServer.class, "  Parsing source and creating CPG for " + srcLocation.getName());
		return analyzer.analyze() // Run analysis
				.thenApply(
					result -> {
						benchParsing.stop();
						// Attach analysis context to result
						result.getScratch().put("ctx", ctx);
						translationResult = result;
						return persistToODB(result);
					})
				.thenApply(
					result -> {
						Benchmark bench = new Benchmark(AnalysisServer.class, "  Evaluation of MARK");
						log.info(
							"Evaluating mark: {} entities, {} rules",
							this.markModel.getEntities().size(),
							this.markModel.getRules().size());
						if (config.legacyEvaluator) {
							// Evaluate all MARK rules
							LegacyEvaluator mi = new LegacyEvaluator(this.markModel, this.config);
							mi.evaluate(result, ctx);
						} else {
							// Evaluate all MARK rules
							var evaluator = new Evaluator(this.markModel, this.config);
							evaluator.evaluate(result, ctx);
						}
						bench.stop();
						return ctx;
					})
				.thenApply(
					analysisContext -> {
						Benchmark bench = new Benchmark(AnalysisServer.class, "  Filtering results");
						if (config.disableGoodFindings) {
							// Filter out "positive" results
							analysisContext.getFindings().removeIf(finding -> !finding.isProblem());
						}
						bench.stop();
						return analysisContext;
					});
	}

	public void loadMarkRulesFromConfig() {
		/*
		 * Load MARK model as given in configuration, if it has not been set manually before.
		 */
		if (config.markModelFiles != null && !config.markModelFiles.isEmpty()) {
			File markModelLocation = new File(config.markModelFiles);
			loadMarkRules(markModelLocation);
		}
	}

	/**
	 * recursively list all mark files
	 *
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
	 * Loads all MARK rules from a file or a directory.
	 *
	 * @param markFile load all mark entities/rules from this file
	 */
	public void loadMarkRules(@NonNull File markFile) {
		File markDescriptionFile = null;

		log.info("Parsing MARK files in {}", markFile.getAbsolutePath());
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
		} else {
			log.info("Loading MARK from file {}", markFile.getAbsolutePath());
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
		db.close();

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

	private TranslationResult persistToODB(TranslationResult result) {
		Benchmark bench = new Benchmark(this.getClass(), " Serializing into OverflowDB");

		// ensure, that the database is clear
		db.clearDatabase();

		// connect
		if (!db.isConnected()) {
			db.connect();
		}

		// Persist the result
		db.saveAll(result.getTranslationUnits());

		long duration = bench.stop();
		// connect to DB
		try (TraversalConnection t = new TraversalConnection(db)) {
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

		TranslationConfiguration.Builder tConfig = TranslationConfiguration.builder()
				.debugParser(true)
				.failOnError(false)
				.codeInNodes(true)
				.loadIncludes(config.analyzeIncludes)
				//.defaultPasses()
				.defaultLanguages()
				.registerPass(new TypeHierarchyResolver())
				.registerPass(new JavaExternalTypeHierarchyResolver())
				.registerPass(new ImportResolver())
				.registerPass(new VariableUsageResolver())
				.registerPass(new CallResolver()) // creates CG
				.registerPass(new EvaluationOrderGraphPass()) // creates EOG
				.registerPass(new TypeResolver())
				//.registerPass(new de.fraunhofer.aisec.cpg.passes.ControlFlowSensitiveDFGPass())
				.registerPass(new FilenameMapper())
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
