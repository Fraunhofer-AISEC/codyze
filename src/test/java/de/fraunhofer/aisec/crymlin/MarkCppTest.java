
package de.fraunhofer.aisec.crymlin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.crymlin.connectors.db.Database;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.server.AnalysisServer;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.mark.XtextParser;
import de.fraunhofer.aisec.mark.markDsl.MarkModel;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.*;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assumptions.assumeFalse;

public class MarkCppTest {
	private static HashMap<String, MarkModel> markModels;
	private final static boolean INCLUDE_KNOWN_NONWORKING_TESTS = false;

	@BeforeAll
	public static void startup() throws Exception {
		URL resource = MarkCppTest.class.getClassLoader().getResource("mark_cpp");
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
	}

	@BeforeEach
	public void clearDatabase() {
		// Make sure we start with a clean (and connected) db
		try {
			Database db = OverflowDatabase.getInstance();
			db.connect();
			db.purgeDatabase();
		}
		catch (Throwable e) {
			e.printStackTrace();
			assumeFalse(true); // Assumption for this test not fulfilled. Do not fail but bail.
		}
	}

	@Test
	public void _01_assign() throws Exception {
		ClassLoader classLoader = MarkCppTest.class.getClassLoader();

		URL resource = classLoader.getResource("mark_cpp/01_assign.cpp");
		assertNotNull(resource);
		File cppFile = new File(resource.getFile());
		assertNotNull(cppFile);

		resource = classLoader.getResource("mark_cpp/01_assign.mark");
		assertNotNull(resource);
		File markFile = new File(resource.getFile());
		assertNotNull(markFile);

		// Start an analysis server
		AnalysisServer server = AnalysisServer.builder()
				.config(
					ServerConfiguration.builder().launchConsole(false).launchLsp(false).markFiles(markFile.getAbsolutePath()).build())
				.build();
		server.start();

		// Start the analysis
		TranslationManager translationManager = TranslationManager.builder()
				.config(
					TranslationConfiguration.builder().debugParser(true).failOnError(false).codeInNodes(true).defaultPasses().sourceFiles(cppFile).build())
				.build();
		CompletableFuture<TranslationResult> analyze = server.analyze(translationManager);
		try {
			TranslationResult result = analyze.get(5, TimeUnit.MINUTES);
		}
		catch (TimeoutException t) {
			analyze.cancel(true);
			throw t;
		}
	}

	@Test
	public void _02_arg() throws Exception {
		ClassLoader classLoader = MarkCppTest.class.getClassLoader();

		URL resource = classLoader.getResource("mark_cpp/02_arg.cpp");
		assertNotNull(resource);
		File cppFile = new File(resource.getFile());
		assertNotNull(cppFile);

		resource = classLoader.getResource("mark_cpp/02_arg.mark");
		assertNotNull(resource);
		File markFile = new File(resource.getFile());
		assertNotNull(markFile);

		// Start an analysis server
		AnalysisServer server = AnalysisServer.builder()
				.config(
					ServerConfiguration.builder().launchConsole(false).launchLsp(false).markFiles(markFile.getAbsolutePath()).build())
				.build();
		server.start();

		// Start the analysis
		TranslationManager translationManager = TranslationManager.builder()
				.config(
					TranslationConfiguration.builder().debugParser(true).failOnError(false).codeInNodes(true).defaultPasses().sourceFiles(cppFile).build())
				.build();
		CompletableFuture<TranslationResult> analyze = server.analyze(translationManager);
		try {
			TranslationResult result = analyze.get(5, TimeUnit.MINUTES);
		}
		catch (TimeoutException t) {
			analyze.cancel(true);
			throw t;
		}
	}

	@Test
	public void _03_arg_as_param() throws Exception {
		ClassLoader classLoader = MarkCppTest.class.getClassLoader();

		URL resource = classLoader.getResource("mark_cpp/03_arg_as_param.cpp");
		assertNotNull(resource);
		File cppFile = new File(resource.getFile());
		assertNotNull(cppFile);

		resource = classLoader.getResource("mark_cpp/03_arg_as_param.mark");
		assertNotNull(resource);
		File markFile = new File(resource.getFile());
		assertNotNull(markFile);

		// Start an analysis server
		AnalysisServer server = AnalysisServer.builder()
				.config(
					ServerConfiguration.builder().launchConsole(false).launchLsp(false).markFiles(markFile.getAbsolutePath()).build())
				.build();
		server.start();

		// Start the analysis
		TranslationManager translationManager = TranslationManager.builder()
				.config(
					TranslationConfiguration.builder().debugParser(true).failOnError(false).codeInNodes(true).defaultPasses().sourceFiles(cppFile).build())
				.build();
		CompletableFuture<TranslationResult> analyze = server.analyze(translationManager);
		try {
			TranslationResult result = analyze.get(5, TimeUnit.MINUTES);
		}
		catch (TimeoutException t) {
			analyze.cancel(true);
			throw t;
		}
	}

	private void expected(Set<Finding> findings, String... expectedFindings) {
		System.out.println("All findings:");
		for (Finding f : findings) {
			System.out.println(f.toString());
		}

		for (String expected : expectedFindings) {
			assertEquals(1, findings.stream().filter(f -> f.toString().equals(expected)).count(), "not found: \"" + expected + "\"");
			Optional<Finding> first = findings.stream().filter(f -> f.toString().equals(expected)).findFirst();
			findings.remove(first.get());
		}
		if (findings.size() > 0) {
			System.out.println("Additional Findings:");
			for (Finding f : findings) {
				System.out.println(f.toString());
			}
		}

		assertEquals(0, findings.size());
	}

	@Test
	public void arg_prevassign_int() throws Exception {
		Set<Finding> findings = runTest("arg_prevassign_int");

		expected(findings, "line 14: MarkRuleEvaluationFinding: Rule SomethingAboutFoo violated");
	}

	@Test
	public void arg_prevassign_bool() throws Exception {
		Set<Finding> findings = runTest("arg_prevassign_bool");

		expected(findings, "line 14: MarkRuleEvaluationFinding: Rule SomethingAboutFoo verified");
	}

	@Test
	public void arg_prevassign_string() throws Exception {
		Set<Finding> findings = runTest("arg_prevassign_string");

		expected(findings, "line 15: MarkRuleEvaluationFinding: Rule SomethingAboutFoo verified");
	}

	@Test
	public void arg_vardecl_int() throws Exception {
		Set<Finding> findings = runTest("arg_vardecl_int");

		expected(findings, "line 12: MarkRuleEvaluationFinding: Rule SomethingAboutFoo verified");
	}

	@Test
	public void arg_vardecl_bool() throws Exception {
		Set<Finding> findings = runTest("arg_vardecl_bool");

		expected(findings, "line 12: MarkRuleEvaluationFinding: Rule SomethingAboutFoo verified");
	}

	@Test
	public void arg_vardecl_string() throws Exception {
		Set<Finding> findings = runTest("arg_vardecl_string");

		expected(findings, "line 13: MarkRuleEvaluationFinding: Rule SomethingAboutFoo verified");

	}

	@Test
	public void split_1() throws Exception {
		Set<Finding> findings = runTest("simplesplit_splitstring");

		expected(findings,
			"line 26: MarkRuleEvaluationFinding: Rule SPLIT_FIRSTELEMENT_EQUALS_AES violated",
			"line 17: MarkRuleEvaluationFinding: Rule SPLIT_FIRSTELEMENT_EQUALS_AES verified",
			"line 17: MarkRuleEvaluationFinding: Rule SPLIT_SECONDELEMENT_EQUALS_FIRST violated",
			"line 26: MarkRuleEvaluationFinding: Rule SPLIT_SECONDELEMENT_EQUALS_FIRST verified");
	}

	@Test
	public void is_instance_1() throws Exception {
		Set<Finding> findings = runTest("simple_instancestring");

		expected(findings,
			"line 17: MarkRuleEvaluationFinding: Rule HasBeenCalled verified");
	}

	@Disabled // TODO currently unsupported feature
	@Test
	public void arg_prevassignop_int() throws Exception {
		Set<Finding> findings = runTest("arg_prevassignop_int");

		System.out.println("All findings:");
		for (Finding f : findings) {
			System.out.println(f.toString());
		}

		assertFalse(true); // new tests!
	}

	@Test
	public void arg_assignconstructor_int() throws Exception {
		Set<Finding> findings = runTest("arg_assignconstructor_int");

		expected(findings,
			"line 16: MarkRuleEvaluationFinding: Rule SomethingAboutFoo verified");
	}

	@Disabled // TODO currently unsupported feature
	@Test
	public void arg_assignparenthesisexpr_int() throws Exception {
		Set<Finding> findings = runTest("arg_assignparenthesisexpr_int");

		System.out.println("All findings:");
		for (Finding f : findings) {
			System.out.println(f.toString());
		}

		assertFalse(true); // new tests!
	}

	@Test
	public void arg_uniforminitializer_int() throws Exception {
		Set<Finding> findings = runTest("arg_uniforminitializer_int");

		expected(findings,
			"line 16: MarkRuleEvaluationFinding: Rule SomethingAboutFoo verified");
	}

	private @NonNull Set<Finding> runTest(@NonNull String fileNamePart)
			throws ExecutionException, InterruptedException, TimeoutException {
		String type = fileNamePart.substring(fileNamePart.lastIndexOf('_') + 1);

		ClassLoader classLoader = MarkCppTest.class.getClassLoader();
		URL resource = classLoader.getResource("mark_cpp/" + fileNamePart + ".cpp");
		assertNotNull(resource);
		File cppFile = new File(resource.getFile());
		assertNotNull(cppFile);

		resource = classLoader.getResource("mark_cpp/" + type + ".mark");
		assertNotNull(resource);
		File markFile = new File(resource.getFile());
		assertNotNull(markFile);

		// Start an analysis server
		AnalysisServer server = AnalysisServer.builder()
				.config(
					ServerConfiguration.builder().launchConsole(false).launchLsp(false).markFiles(markFile.getAbsolutePath()).build())
				.build();
		server.start();

		// Start the analysis
		TranslationManager translationManager = TranslationManager.builder()
				.config(
					TranslationConfiguration.builder().debugParser(true).failOnError(false).codeInNodes(true).defaultPasses().sourceFiles(cppFile).build())
				.build();
		CompletableFuture<TranslationResult> analyze = server.analyze(translationManager);
		try {
			TranslationResult result = analyze.get(5, TimeUnit.MINUTES);

			AnalysisContext ctx = (AnalysisContext) result.getScratch().get("ctx");
			assertNotNull(ctx.getFindings());
			return ctx.getFindings();
		}
		catch (TimeoutException t) {
			analyze.cancel(true);
			throw t;
		}
	}
}
