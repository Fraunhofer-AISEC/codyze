
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.ResultWithContext;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.analysis.structures.TYPESTATE_ANALYSIS;
import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import de.fraunhofer.aisec.mark.XtextParser;
import de.fraunhofer.aisec.mark.markDsl.Expression;
import de.fraunhofer.aisec.mark.markDsl.MarkModel;
import de.fraunhofer.aisec.markmodel.*;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class RuleEnsureSemanticsTest {

	private static HashMap<String, MarkModel> markModels;

	@BeforeAll
	public static void startup() throws Exception {
		URL resource = RuleEnsureSemanticsTest.class.getClassLoader().getResource("mark/rules/ensure/semantics/");
		assertNotNull(resource);

		File markFile = new File(resource.getFile());
		assertNotNull(markFile);

		File[] directoryContent = markFile.listFiles((current, name) -> name.endsWith(".mark"));

		if (directoryContent == null) {
			directoryContent = new File[] { markFile };
		}

		assertNotNull(directoryContent);
		assertTrue(directoryContent.length > 0);

		XtextParser parser = new XtextParser();
		for (File mf : directoryContent) {
			parser.addMarkFile(mf);
		}

		markModels = parser.parse();
		assertFalse(markModels.isEmpty());
	}

	@AfterAll
	public static void teardown() throws Exception {
		// noop
	}

	@Test
	public void equalsTest() throws Exception {
		// TODO convenient checking but pretty ugly and difficult to see, what went wrong
		List<String> markFilePaths = markModels.keySet().stream().filter(n -> n.endsWith("equals.mark")).collect(Collectors.toList());
		assertTrue(markFilePaths.size() == 1);

		Mark mark = new MarkModelLoader().load(markModels, markFilePaths.get(0));
		ServerConfiguration config = ServerConfiguration.builder().markFiles(markFilePaths.get(0)).typestateAnalysis(TYPESTATE_ANALYSIS.NFA).build();
		AnalysisContext ctx = new AnalysisContext(new File(markFilePaths.get(0)).toURI());

		Map<@NonNull String, ResultWithContext> ensureExprResults = new TreeMap<>();
		try (TraversalConnection t = new TraversalConnection(TraversalConnection.Type.OVERFLOWDB)) { // connects to the DB
			for (MRule r : mark.getRules()) {
				ExpressionEvaluator ee = new ExpressionEvaluator(r, ctx, config, t);

				Expression ensureExpr = r.getStatement().getEnsure().getExp();
				ResultWithContext result = ee.evaluate(ensureExpr);
				ensureExprResults.put(r.getName(), result);
			}
		}

		ensureExprResults.entrySet()
				.forEach(
					entry -> {
						if (entry.getKey().endsWith("true")) {
							assertTrue((Boolean) entry.getValue().get());
						} else if (entry.getKey().endsWith("false")) {
							assertFalse((Boolean) entry.getValue().get());
						} else {
							assertTrue(entry.getValue() == null);
						}
					});
	}
}
