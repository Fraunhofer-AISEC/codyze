
package de.fraunhofer.aisec.crymlin;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.crymlin.connectors.db.Database;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import de.fraunhofer.aisec.crymlin.server.AnalysisContext;
import de.fraunhofer.aisec.crymlin.server.AnalysisServer;
import de.fraunhofer.aisec.crymlin.server.ServerConfiguration;
import de.fraunhofer.aisec.crymlin.structures.Finding;
import de.fraunhofer.aisec.mark.XtextParser;
import de.fraunhofer.aisec.mark.markDsl.MarkModel;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MarkCppTest {

	private static HashMap<String, MarkModel> markModels;

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
		AnalysisServer server = AnalysisServer.builder().config(
			ServerConfiguration.builder().launchConsole(false).launchLsp(false).markFiles(markFile.getAbsolutePath()).build()).build();
		server.start();

		// Start the analysis
		TranslationManager translationManager = TranslationManager.builder().config(
			TranslationConfiguration.builder().debugParser(true).failOnError(false).codeInNodes(true).defaultPasses().sourceFiles(cppFile).build()).build();
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
		AnalysisServer server = AnalysisServer.builder().config(
			ServerConfiguration.builder().launchConsole(false).launchLsp(false).markFiles(markFile.getAbsolutePath()).build()).build();
		server.start();

		// Start the analysis
		TranslationManager translationManager = TranslationManager.builder().config(
			TranslationConfiguration.builder().debugParser(true).failOnError(false).codeInNodes(true).defaultPasses().sourceFiles(cppFile).build()).build();
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
		AnalysisServer server = AnalysisServer.builder().config(
			ServerConfiguration.builder().launchConsole(false).launchLsp(false).markFiles(markFile.getAbsolutePath()).build()).build();
		server.start();

		// Start the analysis
		TranslationManager translationManager = TranslationManager.builder().config(
			TranslationConfiguration.builder().debugParser(true).failOnError(false).codeInNodes(true).defaultPasses().sourceFiles(cppFile).build()).build();
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
	public void arg_prevassign_int() throws Exception {
		runTest("arg_prevassign_int");
	}

	@Test
	public void arg_prevassign_bool() throws Exception {
		runTest("arg_prevassign_bool");
	}

	@Test
	public void arg_prevassign_string() throws Exception {
		runTest("arg_prevassign_string");
	}

	@Test
	public void arg_vardecl_int() throws Exception {
		runTest("arg_vardecl_int");
	}

	@Test
	public void arg_vardecl_bool() throws Exception {
		runTest("arg_vardecl_bool");
	}

	@Test
	public void arg_vardecl_string() throws Exception {
		runTest("arg_vardecl_string");
	}

	@Test
	public void split_1() throws Exception {
		runTest("simplesplit_splitstring");
	}

	@Test
	public void is_instance_1() throws Exception {
		runTest("simple_instancestring");
	}

	private void runTest(@NonNull String fileNamePart) throws ExecutionException, InterruptedException, TimeoutException {
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
		AnalysisServer server = AnalysisServer.builder().config(
			ServerConfiguration.builder().launchConsole(false).launchLsp(false).markFiles(markFile.getAbsolutePath()).build()).build();
		server.start();

		// Start the analysis
		TranslationManager translationManager = TranslationManager.builder().config(
			TranslationConfiguration.builder().debugParser(true).failOnError(false).codeInNodes(true).defaultPasses().sourceFiles(cppFile).build()).build();
		CompletableFuture<TranslationResult> analyze = server.analyze(translationManager);
		try {
			TranslationResult result = analyze.get(5, TimeUnit.MINUTES);

			AnalysisContext ctx = (AnalysisContext) result.getScratch().get("ctx");
			assertNotNull(ctx.getFindings());
			Set<Finding> findings = ctx.getFindings();

			assertEquals(1, findings.size());
			findings.forEach((f) -> assertTrue(f.getName().endsWith("ensure condition satisfied")));
		}
		catch (TimeoutException t) {
			analyze.cancel(true);
			throw t;
		}
	}
}
