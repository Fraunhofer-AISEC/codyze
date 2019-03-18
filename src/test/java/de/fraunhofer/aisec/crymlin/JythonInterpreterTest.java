package de.fraunhofer.aisec.crymlin;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.script.ScriptException;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.BulkSet;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.jupiter.api.Test;

/** Testing the Gremlin-over-Jython interface of the analysis server. */
public class JythonInterpreterTest {

  @Test
  public void jythonClosingTest() throws Exception {
    JythonInterpreter interp = new JythonInterpreter();

    // Expecting an unconnected engine, throwing exceptions.
    assertNotNull(interp.engine);
    assertThrows(
        ScriptException.class,
        () -> {
          interp.query("graph");
        });
    assertThrows(
        ScriptException.class,
        () -> {
          interp.query("crymlin");
        });

    // Connect engine to DB
    interp.connect();

    // Expect a connected engine w/o exceptions.
    assertNotNull(interp.engine);
    assertDoesNotThrow(
        () -> {
          interp.query("graph");
        });
    assertDoesNotThrow(
        () -> {
          interp.query("crymlin");
        });

    interp.close();

    // Expecting an empty engine
    assertNotNull(interp.engine);
    assertThrows(
        ScriptException.class,
        () -> {
          interp.query("graph");
        });
    assertThrows(
        ScriptException.class,
        () -> {
          interp.query("crymlin");
        });
  }

  @Test
  public void simpleJythonTest() throws Exception {
    try (JythonInterpreter interp = new JythonInterpreter()) {

      /*
      This warning is known to the maintainers of Jython and is currently a non-bug:
      "An illegal reflective access operation has occurred"

      See https://bugs.jython.org/issue2705
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
    try (JythonInterpreter interp = new JythonInterpreter()) {
      interp.connect();

      // Run crymlin queries directly in Java
      CrymlinTraversalSource crymlin = interp.getCrymlinTraversal();
      List<Vertex> stmts = crymlin.recorddeclarations().toList();
      assertNotNull(stmts);

      crymlin.cipherListSetterCalls().literals().toList();
      crymlin.V().literals().toList();
      crymlin.translationunits().literals().toList();
      crymlin.recorddeclarations().variables().name().toList();
      crymlin.methods().sourcecode().toList();
    }
  }

  @Test
  public void crymlinDslTest() throws Exception {
    try (JythonInterpreter interp = new JythonInterpreter()) {
      interp.connect();

      // Run crymlin queries directly in Java
      CrymlinTraversalSource crymlin = interp.getCrymlinTraversal();
      Long count = (Long) crymlin.recorddeclarations().statements().count().next();
      System.out.println(count);
      assertNotNull(count);
    }
  }

  /**
   * Adding nodes to the graph. Note that <code>addV</code> will add nodes to the in-memory graph so
   * future queries will see them.
   *
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  @Test
  public void gremlinGraphMutationTest() throws Exception {
    try (JythonInterpreter interp = new JythonInterpreter()) {
      interp.connect();

      GraphTraversalSource g = interp.getGremlinTraversal();

      Long size = g.V().count().next();
      List<Object> t =
          g.addV()
              .property(T.label, "Test")
              .property("some_key", "some_value")
              .store("one")
              .addV()
              .property(T.label, "AnotherTest")
              .property("another_key", "another_value")
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
      assertEquals("Test", labels.get(0));
      assertEquals("AnotherTest", labels.get(1));
      Long sizeNew = g.V().count().next();

      // New graph is expected to be +2 nodes larger.
      assertSame(size + 2, sizeNew);

      // Even with a new traversal object, the graph will remain larger
      GraphTraversalSource g2 = interp.getGremlinTraversal();
      assertNotEquals(g, g2);
      Long sizeAgain = g2.V().count().next();
      assertSame(size + 2, sizeAgain);
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

      List<Vertex> literals =
          (List<Vertex>) interp.query("crymlin.translationunits().literals().toList()");
      assertNotNull(literals);
    }
  }
}
