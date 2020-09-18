
package de.fraunhofer.aisec.crymlin.connectors.lsp;

import de.fraunhofer.aisec.analysis.server.AnalysisServer;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.analysis.structures.FindingDescription;
import de.fraunhofer.aisec.analysis.structures.Pair;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.helpers.Benchmark;
import de.fraunhofer.aisec.cpg.passes.CallResolver;
import de.fraunhofer.aisec.cpg.passes.EvaluationOrderGraphPass;
import de.fraunhofer.aisec.cpg.passes.FilenameMapper;
import de.fraunhofer.aisec.cpg.passes.ImportResolver;
import de.fraunhofer.aisec.cpg.passes.JavaExternalTypeHierarchyResolver;
import de.fraunhofer.aisec.cpg.passes.TypeHierarchyResolver;
import de.fraunhofer.aisec.cpg.passes.TypeResolver;
import de.fraunhofer.aisec.cpg.passes.VariableUsageResolver;
import de.fraunhofer.aisec.cpg.sarif.Region;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
				new Position(0, 0), new Position(split.length - 1, split[split.length - 1].length())));
		allDiags.add(diagnostic);
		PublishDiagnosticsParams diagnostics = new PublishDiagnosticsParams();
		diagnostics.setDiagnostics(allDiags);
		diagnostics.setUri(uriString);
		client.publishDiagnostics(diagnostics);
	}

	private void analyze(String uriString, String text) {
		log.debug("Starting analysis of file {}", uriString);

		String sanitizedText = null;

		if (text != null) {
			sanitizedText = text.replaceAll("[\\n ]", "");
			if (lastScan.get(uriString) != null
					&& lastScan.get(uriString).getValue0().equals(sanitizedText)) {
				log.info("Same file already scanned, ignoring");
				client.publishDiagnostics(lastScan.get(uriString).getValue1());
				return;
			}
		}

		log.debug("Really starting analysis of file {}", uriString);

		// markWholeFile(text, uriString);

		Benchmark bm = new Benchmark(CpgDocumentService.class, "Analysis finished");

		File file = new File(URI.create(uriString));
		AnalysisServer instance = AnalysisServer.getInstance();
		if (instance == null) {
			log.error("Server instance is null.");
			return;
		}
		TranslationManager tm = TranslationManager.builder()
				.config(
					TranslationConfiguration.builder()
							.debugParser(false)
							.failOnError(false)
							.codeInNodes(true)
							//.defaultPasses()
							.registerPass(new TypeHierarchyResolver())
							.registerPass(new JavaExternalTypeHierarchyResolver())
							.registerPass(new ImportResolver())
							.registerPass(new VariableUsageResolver())
							.registerPass(new CallResolver()) // creates CG
							.registerPass(new EvaluationOrderGraphPass()) // creates EOG
							.registerPass(new TypeResolver())
							//.registerPass(new de.fraunhofer.aisec.cpg.passes.ControlFlowSensitiveDFGPass())
							.registerPass(new FilenameMapper())
							.sourceLocations(file)
							.build())
				.build();

		CompletableFuture<AnalysisContext> analyze = instance.analyze(tm);

		try {
			AnalysisContext ctx = analyze.get(5, TimeUnit.MINUTES);

			log.info("Analysis for {} done. Returning {} findings.", uriString, ctx.getFindings().size());
			bm.stop();

			// check if there are disabled findings
			@NonNull
			Map<Integer, String> ignoredLines = getIgnoredLines(file);

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
				diagnostic.setSeverity(f.isProblem() ? DiagnosticSeverity.Error : DiagnosticSeverity.Information);

				// Get human readable description, if available
				String msg = FindingDescription.getInstance().getDescriptionShort(f.getOnfailIdentifier());
				if (msg == null) {
					msg = f.getLogMsg();
				}

				String longDesc = FindingDescription.getInstance().getDescriptionFull(f.getOnfailIdentifier());
				if (longDesc != null) {
					msg += ": " + longDesc;
				}

				diagnostic.setCode(Either.forLeft(f.getOnfailIdentifier()));
				diagnostic.setSource("Codyze");
				diagnostic.setMessage(msg);
				Range r = new Range(
					new Position(reg.getStartLine(), reg.getStartColumn()),
					new Position(reg.getEndLine(), reg.getEndColumn()));
				diagnostic.setRange(r);

				String line = ignoredLines.get(r.getStart().getLine());
				if (line == null
						|| (!line.contains(DISABLE_FINDING_ALL)
								&& !line.contains(DISABLE_FINDING_PREFIX + f.getOnfailIdentifier()))) {
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
