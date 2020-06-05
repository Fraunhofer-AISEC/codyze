
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.BulkSet;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class CrymlinTest {

	@Test
	public void crymlinDslTest() throws Exception {
		try (TraversalConnection traversalConnection = new TraversalConnection(TraversalConnection.Type.OVERFLOWDB)) {
			// Run crymlin queries directly in Java
			CrymlinTraversalSource crymlin = traversalConnection.getCrymlinTraversal();
			Optional<Long> count = crymlin.recorddeclarations()
					.count()
					.tryNext();
			assertTrue(count.isPresent());
		}
	}

	/**
	 * Adding nodes to the graph. Note that <code>addV</code> will add nodes to the in-memory graph so future queries will see them.
	 *
	 * <p>
	 * Note that we need to use labels which actually exist in our CPG and we must provide them in Tinkerpop's multi-label syntax (<label 1>::<label 2>).
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void gremlinGraphMutationTest() throws Exception {
		try (TraversalConnection traversalConnection = new TraversalConnection(TraversalConnection.Type.OVERFLOWDB)) {
			GraphTraversalSource g = traversalConnection.getGremlinTraversal();

			Long size = g.V()
					.count()
					.next();
			List<Object> t = g.addV()
					.property(T.label, "TranslationUnitDeclaration")
					.property("name", "some_value")
					.store("one")
					.addV()
					.property(T.label,
						"Declaration")
					.property("name", "another_value")
					.store("one")
					.cap("one")
					.toList();

			assertNotNull(t);

			List<String> labels = new ArrayList<>();
			for (Object x : t) {
				BulkSet<Vertex> v = (BulkSet<Vertex>) x;
				for (Vertex a : v) {
					labels.add(a.label());
				}
			}
			assertEquals(2, labels.size());
			assertEquals("TranslationUnitDeclaration", labels.get(0));
			assertEquals("Declaration", labels.get(1));
			Long sizeNew = g.V()
					.count()
					.next();

			// New graph is expected to be +2 nodes larger.
			assertEquals(2, sizeNew - size);

			// Even with a new traversalConnection object, the graph will remain larger
			GraphTraversalSource g2 = traversalConnection.getGremlinTraversal();
			assertNotEquals(g, g2);
			Long sizeAgain = g2.V()
					.count()
					.next();
			assertEquals(2, sizeAgain - size);
		}
	}
}
