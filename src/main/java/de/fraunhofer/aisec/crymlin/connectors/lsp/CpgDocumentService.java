
package de.fraunhofer.aisec.crymlin.connectors.lsp;

import de.fraunhofer.aisec.analysis.server.AnalysisServer;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.analysis.structures.FindingDescription;
import de.fraunhofer.aisec.analysis.structures.Pair;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.helpers.Benchmark;
import de.fraunhofer.aisec.cpg.sarif.Region;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static de.fraunhofer.aisec.crymlin.dsl.__.filter;

/**
 * Implementation of a {@link TextDocumentService}, which handles certain notifications from a Language Client, such as opening or changing files.
 */
public class CpgDocumentService implements TextDocumentService {

	private static final Logger log = LoggerFactory.getLogger(CpgDocumentService.class);

	private static final String DISABLE_FINDING_PREFIX = "CODYZE-IGNORE-";
	private static final String DISABLE_FINDING_ALL = "CODYZE-IGNORE-ALL";

	private HashMap<String, Pair<String, PublishDiagnosticsParams>> lastScan = new HashMap<>();

	private LanguageClient client;

	// mark the whole file with _Information_ to indicate that the file is being scanned
	private void markWholeFile(String text, String uriString) {
		ArrayList<Diagnostic> allDiags = new ArrayList<>();
		Diagnostic diagnostic = new Diagnostic();
		diagnostic.setSeverity(DiagnosticSeverity.Information);
		diagnostic.setMessage("File is being scanned");
		String[] split = text.split("\n");
		diagnostic.setRange(
			new Range(
				new Position(0, 0),
				new Position(split.length - 1, split[split.length - 1].length())));
		allDiags.add(diagnostic);
		PublishDiagnosticsParams diagnostics = new PublishDiagnosticsParams();
		diagnostics.setDiagnostics(allDiags);
		diagnostics.setUri(uriString);
		client.publishDiagnostics(diagnostics);
	}

	private void analyze(String uriString, String text) {
		String sanitizedText = text.replaceAll("[\\n ]", "");
		if (lastScan.get(uriString) != null
				&& lastScan.get(uriString).getValue0().equals(sanitizedText)) {
			log.info("Same file already scanned, ignoring");
			client.publishDiagnostics(lastScan.get(uriString).getValue1());
			return;
		}

		markWholeFile(text, uriString);

		Benchmark bm = new Benchmark(CpgDocumentService.class, "Analysis finished");

		OverflowDatabase.getInstance().connect();
		OverflowDatabase.getInstance().purgeDatabase();

		File file = new File(URI.create(uriString));
		AnalysisServer instance = AnalysisServer.getInstance();
		if (instance == null) {
			log.error("Server instance is null.");
			return;
		}
		TranslationManager tm = TranslationManager.builder()
				.config(
					TranslationConfiguration.builder().debugParser(true).failOnError(false).codeInNodes(true).defaultPasses().sourceLocations(file).build())
				.build();

		CompletableFuture<AnalysisContext> analyze = instance.analyze(tm);

		try {
			AnalysisContext ctx = analyze.get(5, TimeUnit.MINUTES);

			log.info(
				"Analysis for {} done. Returning {} findings.",
				uriString,
				ctx.getFindings().size());
			bm.stop();

			// check if there are disabled findings
			@NonNull
			Map<Integer, String> ignoredLines = getIgnoredLines(file);

			List<Diagnostic> allDiags = findingsToDiagnostics(ctx.getFindings(), ignoredLines);

			PublishDiagnosticsParams diagnostics = new PublishDiagnosticsParams();
			diagnostics.setDiagnostics(allDiags);
			diagnostics.setUri(uriString);

			lastScan.put(uriString, new Pair<>(sanitizedText, diagnostics));

			// sending diagnostics
			client.publishDiagnostics(diagnostics);
		}
		catch (InterruptedException e) {
			log.error("Analysis error: ", e);
			Thread.currentThread().interrupt();
		}
		catch (ExecutionException | TimeoutException e) {
			analyze.cancel(true);
			log.error("Analysis error: ", e);
		}
	}

	/**
	 * Returns a map of line number to actual line content for all source
	 * code lines that contain a <code>DISABLE_FINDING</code> comment.
	 *
	 * @param file The file to read.
	 * @return A non-null but possibly empty map. Keys indicate line numbers, starting at 0.
	 */
	@NonNull
	private Map<Integer, String> getIgnoredLines(@NonNull File file) {
		Map<Integer, String> ignoredLines = new HashMap<>();
		try {
			List<String> allLines = Files.readAllLines(Paths.get(file.getAbsolutePath()));
			for (int i = 0; i < allLines.size(); i++) {
				String line = allLines.get(i);
				if (line.contains(DISABLE_FINDING_ALL) || line.contains(DISABLE_FINDING_PREFIX)) {
					ignoredLines.put(i, line);
				}
			}
		}
		catch (IOException e) {
			log.error("Error reading source file for message disabling", e);
		}

		return ignoredLines;
	}

	@NonNull
	private List<Diagnostic> findingsToDiagnostics(@NonNull Set<Finding> findings, @NonNull Map<Integer, String> ignoredLines) {
		List<Diagnostic> allDiags = new ArrayList<>();
		for (Finding f : findings) {
			for (Region reg : f.getRegions()) {
				Diagnostic diagnostic = new Diagnostic();
				diagnostic.setSeverity(f.isProblem() ? DiagnosticSeverity.Error : DiagnosticSeverity.Hint);

				// Get human readable description, if available
				String msg = FindingDescription.getInstance().getDescriptionShort(f.getOnfailIdentifier());
				if (msg == null) {
					msg = f.getLogMsg();
				}

				diagnostic.setCode(f.getOnfailIdentifier());
				diagnostic.setMessage(msg);
				Range r = new Range(new Position(reg.getStartLine(), reg.getStartColumn()), new Position(reg.getEndLine(), reg.getEndColumn()));
				diagnostic.setRange(r);

				String line = ignoredLines.get(r.getStart().getLine());
				if (line == null || (!line.contains(DISABLE_FINDING_ALL) && !line.contains(DISABLE_FINDING_PREFIX + f.getOnfailIdentifier()))) {
					allDiags.add(diagnostic);
				} else {
					log.warn("Skipping finding {}, disabled via comment", f);
				}
			}
		}

		return allDiags;
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		log.info("Handling didOpen for file: {}", params.getTextDocument().getUri());
		analyze(params.getTextDocument().getUri(), params.getTextDocument().getText());
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		// Nothing to do.
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		// Nothing to do.
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {
		log.info("Handling didSave for file: {}", params.getTextDocument().getUri());
		analyze(params.getTextDocument().getUri(), params.getText());
	}

	void setClient(LanguageClient client) {
		this.client = client;
	}
}
