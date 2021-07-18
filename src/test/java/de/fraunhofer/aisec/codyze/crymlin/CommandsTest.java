
package de.fraunhofer.aisec.codyze.crymlin;

import de.fraunhofer.aisec.codyze.Commands;
import de.fraunhofer.aisec.codyze.JythonInterpreter;
import de.fraunhofer.aisec.codyze.analysis.AnalysisServer;
import de.fraunhofer.aisec.codyze.analysis.ServerConfiguration;
import de.fraunhofer.aisec.codyze.analysis.TypestateMode;
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
						.useLegacyEvaluator()
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
