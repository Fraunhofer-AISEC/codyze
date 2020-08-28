
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.Commands;
import de.fraunhofer.aisec.analysis.CrymlinConsole;
import de.fraunhofer.aisec.analysis.JythonInterpreter;
import de.fraunhofer.aisec.analysis.server.AnalysisServer;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.analysis.structures.TypestateMode;
import de.fraunhofer.aisec.cpg.helpers.Benchmark;
import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.BulkSet;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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

		com.help();
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
