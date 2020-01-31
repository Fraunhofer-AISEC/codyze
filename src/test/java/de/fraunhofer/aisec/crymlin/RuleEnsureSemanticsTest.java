
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.ConstantValue;
import de.fraunhofer.aisec.analysis.structures.MarkContextHolder;
import de.fraunhofer.aisec.analysis.structures.MarkIntermediateResult;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.analysis.structures.TYPESTATE_ANALYSIS;
import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection;
import de.fraunhofer.aisec.mark.XtextParser;
import de.fraunhofer.aisec.mark.markDsl.Expression;
import de.fraunhofer.aisec.mark.markDsl.MarkModel;
import de.fraunhofer.aisec.markmodel.*;
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

	private void test(String markFileEnding) throws Exception {
		List<String> markFilePaths = markModels.keySet().stream().filter(n -> n.endsWith(markFileEnding)).collect(Collectors.toList());
		assertEquals(1, markFilePaths.size());

		Mark mark = new MarkModelLoader().load(markModels, markFilePaths.get(0));
		ServerConfiguration config = ServerConfiguration.builder().markFiles(markFilePaths.get(0)).typestateAnalysis(TYPESTATE_ANALYSIS.NFA).build();
		AnalysisContext ctx = new AnalysisContext(new File(markFilePaths.get(0)));

		Map<String, Map<Integer, MarkIntermediateResult>> allResults = new TreeMap<>();
		try (TraversalConnection t = new TraversalConnection(TraversalConnection.Type.OVERFLOWDB)) { // connects to the DB
			for (MRule r : mark.getRules()) {

				MarkContextHolder markContextHolder = new MarkContextHolder();
				markContextHolder.getAllContexts().put(0, null); // add a dummy, so that we get exactly one result back for this context

				ExpressionEvaluator ee = new ExpressionEvaluator(mark, r, ctx, config, t.getCrymlinTraversal(), markContextHolder);

				Expression ensureExpr = r.getStatement().getEnsure().getExp();
				Map<Integer, MarkIntermediateResult> result = ee.evaluateExpression(ensureExpr);

				assertEquals(1, result.size());

				allResults.put(r.getName(), result);
			}
		}

		allResults.forEach((key, value) -> {
			assertTrue(value.get(0) instanceof ConstantValue);

			MarkIntermediateResult inner = value.get(0);

			if (key.endsWith("true")) {
				assertEquals(true, ((ConstantValue) inner).getValue(), key);
			} else if (key.endsWith("false")) {
				assertEquals(false, ((ConstantValue) inner).getValue(), key);
			} else if (key.endsWith("fail")) {
				assertTrue(ConstantValue.isError(inner));
			} else {
				fail("Unexpected: Rule should have failed, but is " + inner + ": " + key);
			}
		});
	}

	@Test
	public void testEquals() throws Exception {
		test("equals.mark");
	}

	@Test
	public void testLessThan() throws Exception {
		test("lt.mark");
	}

	@Test
	public void testtGreaterThan() throws Exception {
		test("gt.mark");
	}
}
