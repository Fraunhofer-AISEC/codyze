
package de.fraunhofer.aisec.crymlin.connectors.lsp;

import de.fraunhofer.aisec.analysis.server.AnalysisServer;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.analysis.structures.Pair;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.sarif.Region;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import org.apache.tinkerpop.gremlin.process.traversal.P;
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
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

		Instant start = Instant.now();

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
				"Analysis for {} done. Returning {} findings. Took {} ms\n-------------------------------------------------------------------",
				uriString,
				ctx.getFindings().size(),
				Duration.between(start, Instant.now()).toMillis());

			// check if there are disabled findings
			List<String> allLines = null;
			try {
				allLines = Files.readAllLines(Paths.get(file.getAbsolutePath()));
			}
			catch (IOException e) {
				log.error("Error reading source file for messge disabling", e);
			}

			ArrayList<Diagnostic> allDiags = new ArrayList<>();
			for (Finding f : ctx.getFindings()) {
				for (Region reg : f.getRegions()) {
					boolean skipWarning = false;
					Diagnostic diagnostic = new Diagnostic();
					if (f.isProblem()) {
						// Negative finding: Bad code
						diagnostic.setSeverity(DiagnosticSeverity.Error);
					} else {
						// Positive finding: Good code
						diagnostic.setSeverity(DiagnosticSeverity.Hint);
					}
					diagnostic.setCode(f.getOnfailIdentifier());
					diagnostic.setMessage(f.getLogMsg());
					Range r = new Range(new Position(reg.getStartLine(), reg.getStartColumn()), new Position(reg.getEndLine(), reg.getEndColumn()));
					diagnostic.setRange(r);
					if (allLines != null) {
						String line = allLines.get(r.getStart().getLine());
						if (line.contains(DISABLE_FINDING_ALL) || line.contains(DISABLE_FINDING_PREFIX + f.getOnfailIdentifier())) {
							log.warn("Skipping finding {}, disabled via comment", f);
							skipWarning = true;
						}
					}
					if (!skipWarning) {
						allDiags.add(diagnostic);
					}
				}
			}

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

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		log.info("Handling didOpen for file: {}", params.getTextDocument().getUri());
		analyze(params.getTextDocument().getUri(), params.getTextDocument().getText());
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
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
