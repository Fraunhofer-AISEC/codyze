
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.Commands;
import de.fraunhofer.aisec.analysis.JythonInterpreter;
import de.fraunhofer.aisec.analysis.server.AnalysisServer;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.analysis.structures.TypestateMode;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/** Testing console commands. */
class CommandsTest {

	@Test
	void commandConsoleTest() {
		AnalysisServer server = AnalysisServer.builder()
				.config(ServerConfiguration.builder()
						.launchLsp(false)
						.launchConsole(true)
						.typestateAnalysis(TypestateMode.NFA)
						.markFiles(new File("src/test/resources/mark_java").getAbsolutePath())
						.useLegacyEvaluator(true)
						.build())
				.build();

		server.start();
		PrintStream oldOut = System.out;
		PrintStream oldErr = System.err;
		ByteArrayOutputStream bosOut = new ByteArrayOutputStream();
		PrintStream o = new PrintStream(bosOut);
		ByteArrayOutputStream bosErr = new ByteArrayOutputStream();
		PrintStream e = new PrintStream(bosErr);

		System.setOut(o);
		System.setErr(e);

		// Init console
		JythonInterpreter interp = new JythonInterpreter();
		Commands com = new Commands(interp);

		Commands.help();
		assertTrue(bosOut.toString().contains("help"));

		com.load_rules("src/test/resources/mark_java");

		com.list_rules();
		assertTrue(bosOut.toString().contains("Foo"));

		com.analyze("src/test/resources/good/Bouncycastle.java");
		com.show_findings();

		System.setOut(oldOut);
		System.setErr(oldErr);
	}
}
