package de.fraunhofer.aisec.crymlin;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import javax.script.ScriptException;
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
