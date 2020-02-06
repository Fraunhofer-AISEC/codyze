
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.server.AnalysisServer;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.analysis.utils.TestAppender;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

// TODO Remove before release or at least remove hardcoded paths
@Disabled
class GithubTest {

	private static final int FILES_OFFSET = 0;
	private static final int MAX_FILES_TO_SCAN = -1; // -1: all
	private static final String OUTFOLDERNAME = "/home/user/temp/eval_151020/";
	private static final String baseFolder;
	private static final Logger log = LoggerFactory.getLogger(GithubTest.class);
	private static final boolean RESCAN = true;
	private static AnalysisServer server;
	private static TestAppender logCopy;

	static {
		//    ClassLoader classLoader = GithubTest.class.getClassLoader();
		//    URL resource = classLoader.getResource("random_github");
		//    assertNotNull(resource);
		// baseFolder = resource.getFile();
		baseFolder = "/home/ubuntu/github";
	}

	private static List<String> listFiles() {
		// File folder = new File("/tmp/random_sources");

		File folder = new File(baseFolder);
		assertNotNull(folder);
		File[] files = folder.listFiles();
		assertNotNull(files);
		List<String> ret = new ArrayList<>();
		for (File f : files) {
			ret.add(f.getAbsolutePath().substring(baseFolder.length() + 1)); // only use the file name
		}
		Collections.sort(ret);
		// random!
		Collections.shuffle(ret);
		if (MAX_FILES_TO_SCAN != -1) {
			ret = ret.subList(FILES_OFFSET, FILES_OFFSET + MAX_FILES_TO_SCAN);
		}
		return ret;
	}

	@BeforeAll
	static void setup() {
		OverflowDatabase.getInstance().connect();
		OverflowDatabase.getInstance().close();

		ClassLoader classLoader = GithubTest.class.getClassLoader();
		URL resource = classLoader.getResource("unittests/order2.mark");
		assertNotNull(resource);
		File markPoC1 = new File(resource.getFile());
		assertNotNull(markPoC1);

		server = AnalysisServer.builder()
				.config(
					ServerConfiguration.builder().launchConsole(false).launchLsp(false).markFiles(markPoC1.getAbsolutePath()).build())
				.build();

		server.start();

		logCopy = new TestAppender("logCopy", null);
		logCopy.injectIntoLogger();
	}

	@AfterAll
	static void shutdown() {
		server.stop();
	}

	@ParameterizedTest
	@MethodSource("listFiles")
	void performTest(String sourceFileName) throws Exception {

		File dir = new File(OUTFOLDERNAME);
		final String tmpString = sourceFileName;
		File[] matchingFiles = dir.listFiles(pathname -> pathname.getName().endsWith(tmpString + ".out"));

		if (!RESCAN && matchingFiles != null && matchingFiles.length > 0) {
			System.out.println("File already scanned");
			return;
		}

		Path tempDir = Files.createTempDirectory("githubtest_");
		File tempFile = new File(tempDir.toString() + File.separator + sourceFileName);
		Files.copy(
			new File(baseFolder + File.separator + sourceFileName).toPath(),
			tempFile.toPath(),
			StandardCopyOption.REPLACE_EXISTING);
		logCopy.reset();

		// prepend base folder. we call this function only with the file name to make the tests
		// nicely readable
		sourceFileName = tempFile.getAbsolutePath();

		File cppFile = new File(sourceFileName);
		assertNotNull(cppFile);

		log.info("File size: {} kB", cppFile.length() / 1024);

		//    try (Stream<String> lines = Files.lines(cppFile.toPath(), StandardCharsets.UTF_8)) {
		//      log.info("Benchmark: {} contains {} lines", sourceFileName, lines.count());
		//    } catch (Exception e) {
		//      try (Stream<String> lines = Files.lines(cppFile.toPath(), StandardCharsets.ISO_8859_1))
		// {
		//        log.info("Benchmark: {} contains {} lines", sourceFileName, lines.count());
		//      }
		//    }

		// Make sure we start with a clean (and connected) db
		// if this does not work, just throw
		OverflowDatabase.getInstance().purgeDatabase();

		TranslationManager tm = TranslationManager.builder()
				.config(
					TranslationConfiguration.builder().debugParser(true).failOnError(false).defaultPasses().sourceLocations(cppFile).build())
				.build();

		boolean hasError = false;
		CompletableFuture<AnalysisContext> analyze = server.analyze(tm);
		try {
			AnalysisContext result = analyze.get(30, TimeUnit.MINUTES);

			assertNotNull(result);
			//      AnalysisContext ctx = (AnalysisContext) result.getScratch().get("ctx");
			//      assertNotNull(ctx);

		}
		catch (Exception e) {
			analyze.cancel(true);

			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			log.error(sw.toString());
			hasError = true;
		}
		tempFile.delete();
		tempDir.toFile().delete();

		OverflowDatabase.getInstance().close();

		PrintWriter writer = new PrintWriter(
			OUTFOLDERNAME + System.currentTimeMillis() + "_" + cppFile.getName() + ".out",
			StandardCharsets.UTF_8);
		for (LogEvent e : logCopy.getLog()) {
			writer.println(
				e.getTimeMillis()
						+ " "
						+ e.getLevel().toString()
						+ " "
						+ e.getLoggerName().substring(e.getLoggerName().lastIndexOf(".") + 1)
						+ " "
						+ e.getMessage().getFormattedMessage());
		}
		writer.flush();
		writer.close();

		System.gc();
		System.runFinalization();

		//    System.out.println("The following Errors/Warnings occured:");
		//    for(LogEvent e: logCopy.getLog(Level.ERROR, Level.WARN)) {
		//      System.out.println(e.toString());
		//    }
		if (hasError) {
			fail();
		}
		List<LogEvent> log = logCopy.getLog(Level.ERROR);
		List<LogEvent> logFiltered = new ArrayList<>();
		boolean hasParseError = false;
		for (LogEvent x : log) {
			if (x.getMessage()
					.getFormattedMessage()
					.contains(
						"Parsing of type class org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTProblemStatement is not supported (yet)")
					|| x.getMessage().getFormattedMessage().contains("JavaParser could not parse file")) {
				hasParseError = true;
			}
			logFiltered.add(x);
		}

		if (!hasParseError && logFiltered.size() > 0) {
			fail();
		}
	}

	@Test
	void specificTest() throws Exception {
		OverflowDatabase.getInstance().purgeDatabase();

		//		performTest("p059.java");

		// performTest("redis.c"); // very long persisting

		// performTest("rpcwallet.cpp");
		// performTest("indexed_data.cpp");
		// performTest("RSHooks.cpp");

		performTest("OSOption.cpp");

		// NPE
	}

}
