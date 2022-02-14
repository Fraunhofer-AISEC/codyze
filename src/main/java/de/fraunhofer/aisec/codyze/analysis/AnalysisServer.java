
package de.fraunhofer.aisec.codyze.analysis;

import de.fraunhofer.aisec.codyze.JythonInterpreter;
import de.fraunhofer.aisec.codyze.analysis.markevaluation.Evaluator;
import de.fraunhofer.aisec.codyze.config.Configuration;
import de.fraunhofer.aisec.codyze.crymlin.builtin.Builtin;
import de.fraunhofer.aisec.codyze.crymlin.builtin.BuiltinRegistry;
import de.fraunhofer.aisec.codyze.crymlin.connectors.lsp.CpgLanguageServer;
import de.fraunhofer.aisec.codyze.markmodel.Mark;
import de.fraunhofer.aisec.codyze.markmodel.MarkModelLoader;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.cpg.helpers.Benchmark;
import de.fraunhofer.aisec.cpg.passes.EdgeCachePass;
import de.fraunhofer.aisec.cpg.passes.IdentifierPass;
import de.fraunhofer.aisec.mark.XtextParser;
import de.fraunhofer.aisec.mark.markDsl.MarkModel;

import kotlin.Pair;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static de.fraunhofer.aisec.cpg.graph.GraphKt.getGraph;

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

	private Configuration config;

	private ServerConfiguration serverConfig;

	private JythonInterpreter interp;

	private CpgLanguageServer lsp;

	private TranslationResult translationResult;

	private Mark markModel = new Mark();

	private AnalysisServer(ServerConfiguration serverConfig, Configuration config) {
		this.serverConfig = serverConfig;
		this.config = config;
		AnalysisServer.instance = this;

		// Register built-in functions
		Benchmark bench = new Benchmark(AnalysisServer.class, "Registration of built-ins");
		Reflections reflections = new Reflections(Builtin.class.getPackageName());

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
		log.info("Registered {} built-ins", i);

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
		if (serverConfig.launchLsp) {
			launchLspServer();
		} else if (serverConfig.launchConsole) {
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

		// Run all passes and persist the result
		final Benchmark benchParsing = new Benchmark(AnalysisServer.class, "  Parsing source and creating CPG for " + srcLocation.getName());
		return analyzer.analyze() // Run analysis
				.thenApply(
					result -> {
						var ctx = new AnalysisContext(srcLocation, getGraph(result)); // NOTE: We currently operate on a single source file.

						benchParsing.stop();

						// Attach analysis context to result
						result.getScratch().put("ctx", ctx);
						translationResult = result;

						return new Pair<>(result, ctx);
					})
				.thenApply(
					pair -> {
						Benchmark bench = new Benchmark(AnalysisServer.class, "  Evaluation of MARK");
						log.info(
							"Evaluating mark: {} entities, {} rules",
							this.markModel.getEntities().size(),
							this.markModel.getRules().size());

						// Evaluate all MARK rules
						var evaluator = new Evaluator(this.markModel, this.serverConfig);

						var result = pair.getFirst();
						var ctx = pair.getSecond();

						evaluator.evaluate(result, ctx);

						bench.stop();
						return ctx;
					})
				.thenApply(
					ctx -> {
						Benchmark bench = new Benchmark(AnalysisServer.class, "  Filtering results");
						if (serverConfig.disableGoodFindings) {
							// Filter out "positive" results
							ctx.getFindings().removeIf(finding -> !finding.isProblem());
						}
						bench.stop();
						return ctx;
					});
	}

	public void loadMarkRulesFromConfig() {
		/*
		 * Load MARK model as given in configuration, if it has not been set manually before.
		 */
		File[] markModelLocations = Arrays.stream(serverConfig.markModelFiles).map(File::new).toArray(File[]::new);
		loadMarkRules(markModelLocations);
	}

	/**
	 * recursively list all mark files
	 *
	 * @param currentFile
	 * @param allFiles
	 * @throws IOException
	 */
	private void getMarkFileLocations(File currentFile, List<File> allFiles) throws IOException {
		try (Stream<Path> walk = Files.walk(currentFile.toPath(), Integer.MAX_VALUE)) {
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
	 * @param markFiles load all mark entities/rules from these files
	 */
	public void loadMarkRules(@NonNull File... markFiles) {
		File markDescriptionFile = null;

		XtextParser parser = new XtextParser();

		Instant start = Instant.now();

		for (File markFile : markFiles) {
			log.info("Parsing MARK files in {}", markFile.getAbsolutePath());

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

		this.serverConfig = null;
		this.markModel = null;
		this.interp = null;
		this.lsp = null;
		this.translationResult = null;
		instance = null;

		log.info("stop.");
	}

	public CpgLanguageServer getLSP() {
		return lsp;
	}

	public TranslationResult getTranslationResult() {
		return translationResult;
	}

	public CompletableFuture<AnalysisContext> analyze(String url) {
		List<File> files = new ArrayList<>();
		files.add(new File(url));

		var translationManager = TranslationManager.builder()
				.config(config.buildTranslationConfiguration(files))
				.build();

		return analyze(translationManager);
	}

	public static class Builder {
		private ServerConfiguration serverConfig;
		private Configuration config;

		private Builder() {
		}

		public Builder config(Configuration config) {
			this.serverConfig = config.buildServerConfiguration();
			this.config = config;
			return this;
		}

		public AnalysisServer build() {
			return new AnalysisServer(this.serverConfig, this.config);
		}
	}
}
