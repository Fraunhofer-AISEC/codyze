
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.JythonInterpreter;
import de.fraunhofer.aisec.cpg.helpers.Benchmark;
import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.BulkSet;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Testing the Gremlin-over-Jython interface of the analysis server. */
public class JythonInterpreterTest {

	@Test
	public void jythonClosingTest() throws Exception {
		Benchmark bench = new Benchmark(this.getClass(), "Opening new Jython interpreter");
		JythonInterpreter interp = new JythonInterpreter();
		bench.stop();

		// Expecting an unconnected engine, throwing exceptions.
		assertNotNull(interp.getEngine());
		assertThrows(ScriptException.class, () -> interp.query("graph"));
		assertThrows(ScriptException.class, () -> interp.query("crymlin"));

		// Connect engine to DB
		bench = new Benchmark(this.getClass(), "Connecting to DB");
		interp.connect();
		bench.stop();

		// Expect a connected engine w/o exceptions.
		assertNotNull(interp.getEngine());
		bench = new Benchmark(this.getClass(), "Sending 2 queries to graph");
		assertDoesNotThrow(() -> interp.query("graph"));
		assertDoesNotThrow(() -> interp.query("crymlin"));
		bench.stop();

		bench = new Benchmark(this.getClass(), "Closing");
		interp.close();
		bench.stop();

		// Expecting an empty engine
		assertNotNull(interp.getEngine());
		assertThrows(ScriptException.class, () -> interp.query("graph"));
		assertThrows(ScriptException.class, () -> interp.query("crymlin"));
	}

	@Test
	public void simpleJythonTest() throws Exception {
		try (JythonInterpreter interp = new JythonInterpreter()) {

			/*
			 * This warning is known to the maintainers of Jython and is currently a non-bug: "An illegal reflective access operation has occurred" See
			 * https://bugs.jython.org/issue2705
			 */
			interp.connect();

			// Just for testing: We can now run normal python code and have access to the Tinkerpop graph
			interp.query("g = graph.traversal()");
			interp.query("print(g.V([]).toSet())");

			// Just for testing: We can run normal Gremlin queries like so:
			Object result = interp.query("g.V([]).toSet()"); // Get all (!) nodes
			assertEquals(HashSet.class, result.getClass());
		}
	}

	@Test
	public void crymlinTest() throws Exception {
		try (TraversalConnection traversalConnection = new TraversalConnection(TraversalConnection.Type.OVERFLOWDB)) {

			// Run crymlin queries directly in Java
			CrymlinTraversalSource crymlin = traversalConnection.getCrymlinTraversal();
			List<Vertex> stmts = crymlin.recorddeclarations().toList();
			assertNotNull(stmts);

			crymlin.V().literals().toList();
			crymlin.translationunits().literals().toList();
			crymlin.recorddeclarations().variables().name().toList();
			crymlin.methods().code().toList();
			crymlin.methods().comment().toList();
		}
	}

	@Test
	public void crymlinDslTest() throws Exception {
		try (TraversalConnection traversalConnection = new TraversalConnection(TraversalConnection.Type.OVERFLOWDB)) {

			// Run crymlin queries directly in Java
			CrymlinTraversalSource crymlin = traversalConnection.getCrymlinTraversal();
			Long count = crymlin.recorddeclarations().count().next();
			System.out.println(count);
			assertNotNull(count);
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

			Long size = g.V().count().next();
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
			Long sizeNew = g.V().count().next();

			// New graph is expected to be +2 nodes larger.
			assertEquals(2, sizeNew - size);

			// Even with a new traversalConnection object, the graph will remain larger
			GraphTraversalSource g2 = traversalConnection.getGremlinTraversal();
			assertNotEquals(g, g2);
			Long sizeAgain = g2.V().count().next();
			assertEquals(2, sizeAgain - size);
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void crymlinOverJythonTest() throws Exception {
		try (JythonInterpreter interp = new JythonInterpreter()) {
			interp.connect();

			// Run crymlin queries as strings and get back the results as Java objects:
			List<Vertex> classes = (List<Vertex>) interp.query("crymlin.recorddeclarations().toList()");
			assertNotNull(classes);

			List<Vertex> literals = (List<Vertex>) interp.query("crymlin.translationunits().literals().toList()");
			assertNotNull(literals);
		}
	}
}
