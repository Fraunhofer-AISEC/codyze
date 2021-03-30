
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RealBotanTest extends AbstractMarkTest {

	@Test
	void testSimple() throws Exception {
		// Just a very simple test to explore the graph
		Set<Finding> findings = performTest("real-examples/botan/streamciphers/bsex.cpp", "real-examples/botan/streamciphers/bsex.mark");
		GraphTraversalSource t = ctx.getDatabase()
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
	@Disabled("WIP, not working yet")
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
		assertTrue(correctKeySizes.stream().anyMatch(f -> f.getRegions().get(0).getStartLine() == 249));
		assertTrue(correctKeySizes.stream().anyMatch(f -> f.getRegions().get(0).getStartLine() == 354));
		assertEquals(2, correctKeySizes.size());

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
		assertTrue(blockCiphers.stream().anyMatch(f -> f.getRegions().get(0).getStartLine() == 15));
		assertTrue(blockCiphers.stream().anyMatch(f -> f.getRegions().get(0).getStartLine() == 21));
		assertEquals(2, blockCiphers.size());

		// We expect an incorrect key size at line 16 and 22 because it is not explicitly set.
		List<Finding> keyLengths = findings
				.stream()
				.filter(f -> f.getOnfailIdentifier().equals("BadKeyLength"))
				.filter(Finding::isProblem)
				.collect(Collectors.toList());
		assertEquals(2, keyLengths.size());

		assertTrue(keyLengths.stream().anyMatch(l -> l.getRegions().get(0).getStartLine() == 15));
		assertTrue(keyLengths.stream().anyMatch(l -> l.getRegions().get(0).getStartLine() == 21));
	}
}
