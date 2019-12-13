
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.server.AnalysisServer;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.analysis.structures.TYPESTATE_ANALYSIS;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.cpg.graph.VariableDeclaration;
import de.fraunhofer.aisec.crymlin.connectors.db.Database;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

public class RealBCTest extends AbstractMarkTest {

	@Test
	public void testSimple() throws Exception {
		// Just a very simple test to explore the graph
		Set<Finding> findings = performTest("real-examples/bc/rwedoff.Password-Manager/Main.java", "real-examples/bc/rwedoff.Password-Manager/");

		System.out.println("\n\n\n");
		findings.stream().filter(f -> f.isProblem()).forEach(System.out::println);
		System.out.println("\n");
		findings.stream().filter(f -> !f.isProblem()).forEach(System.out::println);
		System.out.println("\n\n\n");

		assertEquals(1, findings.stream().filter(Finding::isProblem).count()); // MockWhen1 results in a finding.
	}

}
// currently broken/need investigating

// the problem seems to be that the entity references `this` which is not correct. See github-issue #5
// line -1: Verified Order: Cipher_Order
