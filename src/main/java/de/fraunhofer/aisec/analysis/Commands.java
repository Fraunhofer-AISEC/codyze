
package de.fraunhofer.aisec.analysis;

import de.fraunhofer.aisec.analysis.server.AnalysisServer;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.analysis.utils.Utils;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSourceDsl;
import de.fraunhofer.aisec.markmodel.MRule;
import de.fraunhofer.aisec.markmodel.Mark;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * These commands are only used by the Jython console.
 *
 * @author julian
 */
public class Commands {

	private static final Logger log = LoggerFactory.getLogger(Commands.class);

	// backref
	private final JythonInterpreter jythonInterpreter;

	public Commands(@NonNull JythonInterpreter jythonInterpreter) {
		this.jythonInterpreter = jythonInterpreter;
	}

	/**
	 * Starts the analysis of a single file or all files in a directory.
	 *
	 * @param url
	 */
	@ShellCommand("Starts analysis of a single file or all files in a given directory")
	public void analyze(String url) {
		AnalysisServer server = AnalysisServer.getInstance();
		if (server == null) {
			log.error("Analysis server not available");
			return;
		}
		CompletableFuture<AnalysisContext> analyze = server.analyze(url);

		try {
			AnalysisContext ctx = analyze.get(10, TimeUnit.MINUTES);
			jythonInterpreter.setFindings(ctx.getFindings());
			jythonInterpreter.connect();
		}
		catch (InterruptedException e) {
			log.error("Interrupted", e);
			Thread.currentThread().interrupt();
		}
		catch (ExecutionException e) {
			log.error("Exception", e);
		}
		catch (TimeoutException e) {
			analyze.cancel(true);
			log.warn("Analysis interrupted after timeout of 10 minutes.");
		}
	}

	/**
	 * Loads MARK rules into the server. Must be called before analyze, otherwise no rules will be evaluated.
	 *
	 * @param fileName
	 */
	@ShellCommand("Load MARK rules from given file or directory. Rules must be loaded before starting analysis.")
	public void load_rules(String fileName) {
		AnalysisServer server = AnalysisServer.getInstance();
		if (server == null) {
			log.error("Server not initialized");
			return;
		}

		server.loadMarkRules(new File(fileName));
	}

	@ShellCommand("Show active MARK rules")
	public void list_rules() {
		AnalysisServer server = AnalysisServer.getInstance();
		if (server == null) {
			log.error("Server not initialized");
			return;
		}

		Mark markModel = server.getMarkModel();
		for (MRule r : markModel.getRules()) {
			System.out.println(r.getName());
		}
	}

	@ShellCommand("Show findings after analysis")
	public void show_findings() {
		for (Finding fi : jythonInterpreter.getFindings()) {
			System.out.println(fi);
		}
	}

	/** Prints help to stdout. */
	@ShellCommand("Display this help")
	public static void help() {
		System.out.println(
			"Use the \"server\" object to control the analysis server.\n"
					+ "\n"
					+ "   server.load_rules(\"../mark-crymlin-eclipse-plugin/examples/PoC_MS1/Botan_CipherMode.mark\")\n"
					+ "          Load MARK rules.\n"
					+ "\n"
					+ "   server.list_rules()\n"
					+ "          List active MARK rules.\n"
					+ "\n"
					+ "   server.show_findings()\n"
					+ "          Show results of MARK evaluation.\n"
					+ "\n"
					+ "   server.analyze(\"src/test/resources/good/Bouncycastle.java\")\n"
					+ "   server.analyze(\"src/test/resources/symm_block_cipher.cpp\")\n"
					+ "          Analyze a single source file. Remember to load MARK rules before analyzing.\n"
					+ "\n"
					+ "   server.analyze(\"src/test/resources/good\")\n"
					+ "          Analyze all source files in a directory. Remember to load MARK rules before analyzing.\n"
					+ "\n"
					+ "\n"
					+ "You may then start writing crymlin queries using the \"crymlin\" object.\n"
					+ "\n"
					+ "Examples: \n"
					+ "   crymlin.recorddeclarations().toList()\n"
					+ "          Returns array of vertices representing RecordDeclarations.\n"
					+ "\n"
					+ "   crymlin.recorddeclaration(\"good.Bouncycastle\").next()\n"
					+ "          Returns vertex representing the RecordDeclarations of \"good.Bouncycastle\".\n"
					+ "\n"
					+ "   crymlin.recorddeclaration(\"good.Bouncycastle\").sourcecode().next()\n"
					+ "          Returns source code of \"good.Bouncycastle\".\n"
					+ "\n"
					+ "   crymlin.translationunits().name().toList()\n"
					+ "          Returns array of strings representing the names of TranslationUnits.\n"
					+ "\n"
					+ "   crymlin.translationunits().next()\n"
					+ "          Returns the first TranslationUnit vertex (or null if none exists).\n"
					+ "\n"
					+ "   dir(crymlin.translationunits())\n"
					+ "          Good ol' Python dir() to find out what properties/methods are available.\n");
		List<Method> annotatedMethods = Utils.getMethodsAnnotatedWith(Commands.class, ShellCommand.class);
		List<Method> queryMethods = (Utils.getMethodsAnnotatedWith(CrymlinTraversalSourceDsl.class, ShellCommand.class));
		print(annotatedMethods);
		System.out.println("\n ---------------- Graph traversals ------------- \n");
		print(queryMethods);
	}

	/**
	 * Prints methods annotated with @ShellCommand to stdout.
	 *
	 * @param methods
	 */
	@SuppressWarnings("java:S106")
	private static void print(List<Method> methods) {
		for (Method m : methods) {
			StringBuilder sbEntry = new StringBuilder(m.getName());
			sbEntry.append("(");
			for (int i = 0; i < m.getParameterTypes().length; i++) {
				sbEntry.append(m.getParameterTypes()[i].getSimpleName());
				if (i < m.getParameterTypes().length - 1) {
					sbEntry.append(", ");
				}
			}
			sbEntry.append(")");
			sbEntry = new StringBuilder(String.format("%-30s", sbEntry.toString()));
			sbEntry.append("-\t" + m.getAnnotation(ShellCommand.class).value());
			System.out.println(sbEntry.toString());
		}
	}
}
