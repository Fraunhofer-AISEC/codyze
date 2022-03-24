
package de.fraunhofer.aisec.codyze.crymlin.connectors.lsp;

import de.fraunhofer.aisec.codyze.analysis.AnalysisServer;
import de.fraunhofer.aisec.codyze.analysis.AnalysisContext;
import de.fraunhofer.aisec.codyze.analysis.Finding;
import de.fraunhofer.aisec.codyze.analysis.FindingDescription;
import de.fraunhofer.aisec.cpg.helpers.Benchmark;
import de.fraunhofer.aisec.cpg.sarif.Region;
import kotlin.Pair;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
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

/**
 * Implementation of a {@link TextDocumentService}, which handles certain notifications from a
 * Language Client, such as opening or changing files.
 */
public class CpgDocumentService implements TextDocumentService {

	private static final Logger log = LoggerFactory.getLogger(CpgDocumentService.class);

	private static final String DISABLE_FINDING_PREFIX = "CODYZE-IGNORE-";
	private static final String DISABLE_FINDING_ALL = "CODYZE-IGNORE-ALL";

	private final HashMap<String, Pair<String, PublishDiagnosticsParams>> lastScan = new HashMap<>();

	private LanguageClient client;

	private void analyze(String uriString, String text) {
		log.debug("Starting analysis of file {}", uriString);

		String sanitizedText = null;

		if (text != null) {
			sanitizedText = text.replaceAll("[\\n ]", "");
			if (lastScan.get(uriString) != null
					&& lastScan.get(uriString).getFirst().equals(sanitizedText)) {
				log.info("Same file already scanned, ignoring");
				client.publishDiagnostics(lastScan.get(uriString).getSecond());
				return;
			}
		}

		log.debug("Really starting analysis of file {}", uriString);

		Benchmark bm = new Benchmark(CpgDocumentService.class, "Analysis finished");

		File file = new File(URI.create(uriString));
		AnalysisServer instance = AnalysisServer.getInstance();
		if (instance == null) {
			log.error("Server instance is null.");
			return;
		}

		CompletableFuture<AnalysisContext> analyze = instance.analyze(file.getAbsolutePath());

		try {
			AnalysisContext ctx = analyze.get(5, TimeUnit.MINUTES);

			log.info("Analysis for {} done. Returning {} findings.", uriString, ctx.getFindings().size());
			bm.stop();

			// check if there are disabled findings
			@NonNull
			Map<Integer, String> ignoredLines = instance.getServerConfiguration().pedantic ? Map.of() : getIgnoredLines(file);

			List<Diagnostic> allDiags = findingsToDiagnostics(ctx.getFindings(), ignoredLines);

			PublishDiagnosticsParams diagnostics = new PublishDiagnosticsParams();
			diagnostics.setDiagnostics(allDiags);
			diagnostics.setUri(uriString);

			if (sanitizedText != null) {
				lastScan.put(uriString, new Pair<>(sanitizedText, diagnostics));
			}

			// sending diagnostics
			client.publishDiagnostics(diagnostics);
		}
		catch (InterruptedException e) {
			log.error("Analysis error: ", e);
			analyze.cancel(true);
			Thread.currentThread().interrupt();
		}
		catch (ExecutionException | TimeoutException e) {
			analyze.cancel(true);
			log.error("Analysis error: ", e);
		}
	}

	/**
	 * Returns a map of line number to actual line content for all source code lines that contain a
	 * <code>DISABLE_FINDING</code> comment.
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
	private List<Diagnostic> findingsToDiagnostics(
			@NonNull Set<Finding> findings, @NonNull Map<Integer, String> ignoredLines) {
		List<Diagnostic> allDiags = new ArrayList<>();
		for (Finding f : findings) {
			for (Region reg : f.getRegions()) {
				Diagnostic diagnostic = new Diagnostic();
				// TODO Replace HINT for verified findings with Code Lens

				// Everything is informational until it becomes a problem
				DiagnosticSeverity severity = DiagnosticSeverity.Information;
				if (f.isProblem()) {
					switch (f.getAction()) {
						case INFO:
							severity = DiagnosticSeverity.Information;
							break;
						case WARN:
							severity = DiagnosticSeverity.Warning;
							break;
						case FAIL:
							// Error
						default:
							severity = DiagnosticSeverity.Error;
							break;
					}
				}
				diagnostic.setSeverity(severity);

				// Get human readable description, if available
				String msg = (f.isProblem() ? FindingDescription.getInstance().getDescriptionShort(f.getIdentifier())
						: FindingDescription.getInstance().getDescriptionPass(f.getIdentifier()));
				if (msg == null) {
					msg = f.getLogMsg();
				}

				if (f.isProblem()) {
					String longDesc = FindingDescription.getInstance().getDescriptionFull(f.getIdentifier());
					if (longDesc != null) {
						msg += ": " + longDesc;
					}
				}

				diagnostic.setCode(Either.forLeft(f.getIdentifier()));
				diagnostic.setSource("Codyze");
				diagnostic.setMessage(msg);
				Range r = new Range(
					new Position(reg.getStartLine(), reg.getStartColumn()),
					new Position(reg.getEndLine(), reg.getEndColumn()));
				diagnostic.setRange(r);

				String line = ignoredLines.get(r.getStart().getLine());
				if (line == null
						|| (!line.contains(DISABLE_FINDING_ALL)
								&& !line.contains(DISABLE_FINDING_PREFIX + f.getIdentifier()))) {
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

	@Override
	public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
		return CompletableFuture.supplyAsync(
			() -> {
				List<Either<Command, CodeAction>> list = new ArrayList<>();
				for (Diagnostic diag : params.getContext().getDiagnostics()) {
					if (!diag.getSource().equals("Codyze")
							|| diag.getSeverity().equals(DiagnosticSeverity.Hint)) {
						// Skip non-Codyze and "verified" findings
						continue;
					}

					CodeAction comWf = new CodeAction();
					comWf.setKind(CodeActionKind.QuickFix);
					comWf.setDiagnostics(List.of(diag));

					TextDocumentItem textDocItem = new TextDocumentItem();
					textDocItem.setUri(params.getTextDocument().getUri());
					textDocItem.setVersion(1);
					VersionedTextDocumentIdentifier textDoc = new VersionedTextDocumentIdentifier(textDocItem.getUri(), textDocItem.getVersion());
					TextDocumentEdit textDocChange = new TextDocumentEdit();
					textDocChange.setTextDocument(textDoc);

					Position pos = new Position(params.getRange().getStart().getLine() + 1, -1);
					if (diag.getCode() != null && diag.getCode().isLeft()) {
						comWf.setTitle("Resolve as ignored");
						comWf.setEdit(
							new WorkspaceEdit(
								List.of(
									Either.forLeft(
										new TextDocumentEdit(
											textDoc,
											List.of(
												new TextEdit(
													new Range(pos, pos),
													"  // "
															+ DISABLE_FINDING_PREFIX
															+ diag.getCode().getLeft())))))));
					} else {
						comWf.setTitle("Resolve all as ignored");
						comWf.setEdit(
							new WorkspaceEdit(
								List.of(
									Either.forLeft(
										new TextDocumentEdit(
											textDoc,
											List.of(
												new TextEdit(
													new Range(pos, pos), "  // " + DISABLE_FINDING_ALL)))))));
					}
					list.add(Either.forRight(comWf));

					CodeAction comFp = new CodeAction("Resolve as false positive");
					comFp.setKind(CodeActionKind.QuickFix);
					comFp.setDiagnostics(List.of(diag));
					comFp.setEdit(
						new WorkspaceEdit(
							List.of(
								Either.forLeft(
									new TextDocumentEdit(
										textDoc,
										List.of(
											new TextEdit(
												new Range(pos, pos),
												"  // "
														+ DISABLE_FINDING_PREFIX
														+ diag.getCode().getLeft()
														+ " (false positive)")))))));
					list.add(Either.forRight(comFp));
				}
				return list;
			});
	}

	void setClient(LanguageClient client) {
		this.client = client;
	}
}
