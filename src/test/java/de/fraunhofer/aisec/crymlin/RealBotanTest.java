
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.cpg.graph.CallExpression;
import de.fraunhofer.aisec.cpg.graph.VariableDeclaration;
import de.fraunhofer.aisec.crymlin.connectors.db.Database;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.server.AnalysisServer;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.analysis.structures.TYPESTATE_ANALYSIS;
import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversal;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class RealBotanTest extends AbstractMarkTest {

	@Test
	public void testSimple() throws Exception {
		// Just a very simple test to explore the graph
		Set<Finding> findings = performTest("real-examples/botan/streamciphers/bsex.cpp", "real-examples/botan/streamciphers/bsex.mark");
		GraphTraversalSource t = OverflowDatabase.getInstance()
				.getGraph()
				.traversal();

		List<Vertex> variables = t.V().hasLabel(VariableDeclaration.class.getSimpleName()).toList();

		assertTrue(variables.size() > 0);
	}

	@Test
	void testCorrecKeySize() throws Exception {
		@NonNull
		Set<Finding> findings = performTest("real-examples/botan/streamciphers/bsex.cpp", "real-examples/botan/streamciphers/bsex.mark");

		// Note that line numbers of the "range" are the actual line numbers -1. This is required for proper LSP->editor mapping
		Optional<Finding> correctKeyLength = findings
				.stream()
				.filter(f -> f.getOnfailIdentifier().equals("CorrectPrivateKeyLength"))
				.findFirst();
		assertTrue(correctKeyLength.isPresent());
		assertFalse(correctKeyLength.get().isProblem());
	}

	@Test
	void testWrongKeySize() throws Exception {
		@NonNull
		Set<Finding> findings = performTest("real-examples/botan/streamciphers/bsex.cpp", "real-examples/botan/streamciphers/bsex.mark");

		// Note that line numbers of the "range" are the actual line numbers -1. This is required for proper LSP->editor mapping
		Optional<Finding> wrongKeyLength = findings
				.stream()
				.filter(f -> f.getOnfailIdentifier().equals("WrongPrivateKeyLength"))
				.findFirst();
		assertTrue(wrongKeyLength.isPresent());
		assertTrue(wrongKeyLength.get().isProblem());
	}

	@Test
	@Disabled //WIP, not working yet
	void testPlaybookCreator() throws Exception {
		@NonNull
		Set<Finding> findings = performTest("real-examples/botan/blockciphers/obraunsdorf.playbook-creator/pbcStorage.cpp",
			"real-examples/botan/MARK");

		assertTrue(findings.isEmpty());
	}

	@Test
	void testArsenic() throws Exception {
		@NonNull
		Set<Finding> findings = performTest("real-examples/botan/blockciphers/Antidote1911.Arsenic/Crypto.cpp",
			"real-examples/botan/MARK");

		// We expect a correct key size in line 250 and 355
		List<Finding> correctKeySizes = findings
				.stream()
				.filter(f -> !f.isProblem())
				.collect(Collectors.toList());
		assertEquals(2, correctKeySizes.size());
		assertTrue(correctKeySizes.stream().anyMatch(f -> f.getRanges().get(0).getStart().getLine() == 249));
		assertTrue(correctKeySizes.stream().anyMatch(f -> f.getRanges().get(0).getStart().getLine() == 354));

	}

	@Test
	void testQt_LockBox() throws Exception {
		@NonNull
		Set<Finding> findings = performTest("real-examples/botan/blockciphers/Prudkovskiy.Qt_LockBox/crypto.cpp",
			"real-examples/botan/MARK");

		// We expect two correct block ciphers AES/CBC (isProblem == false) at line 16 and 22
		List<Finding> blockCiphers = findings
				.stream()
				.filter(f -> f.getOnfailIdentifier().equals("WrongBlockCipher"))
				.filter(f -> !f.isProblem())
				.collect(Collectors.toList());
		assertEquals(2, blockCiphers.size());
		assertTrue(blockCiphers.stream().anyMatch(f -> f.getRanges().get(0).getStart().getLine() == 15));
		assertTrue(blockCiphers.stream().anyMatch(f -> f.getRanges().get(0).getStart().getLine() == 21));

		// We expect an incorrect key size at line 16 and 22 because it is not explicitly set.
		List<Finding> keyLengths = findings
				.stream()
				.filter(f -> f.getOnfailIdentifier().equals("BadKeyLength"))
				.filter(f -> f.isProblem())
				.collect(Collectors.toList());
		assertEquals(2, keyLengths.size());

		assertTrue(keyLengths.get(0).getRanges().stream().anyMatch(r -> r.getStart().getLine() == 15));
		assertTrue(keyLengths.get(1).getRanges().stream().anyMatch(r -> r.getStart().getLine() == 21));
	}
}
