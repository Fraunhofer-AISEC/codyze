package de.fraunhofer.aisec.crymlin.dsl;

import de.fraunhofer.aisec.cpg.graph.CallExpression;
import de.fraunhofer.aisec.cpg.graph.Declaration;
import de.fraunhofer.aisec.cpg.graph.FunctionDeclaration;
import de.fraunhofer.aisec.cpg.graph.MethodDeclaration;
import de.fraunhofer.aisec.cpg.graph.RecordDeclaration;
import de.fraunhofer.aisec.cpg.graph.TranslationUnitDeclaration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * The DSL definition must be a class that extends {@code GraphTraversalSource} and should be
 * referenced in the {@code GremlinDsl} annotation on the {@code GraphTraversal} extension - in this
 * example {@link CrymlinTraversalDsl}. The methods on this class will be exposed with the other
 * traversal start steps on {@code GraphTraversalSource}.
 */
public class CrymlinTraversalSourceDsl extends GraphTraversalSource {

  private static final String CALL_EXPRESSION = "CallExpressicon::Expression::Statement";
  public static final String TRANSLATION_UNIT_DECLARATION = "Expression::Literal::Statement";
  public static final String LITERAL = "Expression::Literal::Statement";
  public static final String VARIABLE_DECLARATION = "Declaration::VariableDeclaration";

  public static final String ARGUMENTS = "ARGUMENTS";

  public static final String ARGUMENT_INDEX = "argumentIndex";
  public static final String NAME = "name";
  public static final String VALUE = "value";

  public CrymlinTraversalSourceDsl(
      final Graph graph, final TraversalStrategies traversalStrategies) {
    super(graph, traversalStrategies);
  }

  public CrymlinTraversalSourceDsl(final Graph graph) {
    super(graph);
  }

  public GraphTraversal<Vertex, Vertex> variableDeclarations() {
    GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

    return traversal.hasLabel(VARIABLE_DECLARATION);
  }

  /**
   * Returns nodes with a label {@code CallExpression}.
   *
   * @return
   */
  public GraphTraversal<Vertex, Vertex> calls() {
    GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

    return traversal.has(T.label, LabelP.of(CallExpression.class.getSimpleName()));
  }

  /**
   * Returns the calls with a given name.
   *
   * @return
   */
  public GraphTraversal<Vertex, Vertex> calls(String callee_name) {
    GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

    return traversal
        .has(T.label, LabelP.of(CallExpression.class.getSimpleName()))
        .has("name", callee_name);
  }

  public GraphTraversal<Vertex, Vertex> calls(String callee_name, String base_type) {
    GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

    return traversal
        .has(T.label, LabelP.of(CallExpression.class.getSimpleName()))
        .has("name", callee_name)
        .has("type", base_type);
  }

  public HashSet<Vertex> calls(String callee_name, String base_type, List<String> parameter) {
    GraphTraversal<Vertex, Vertex> traversal = this.clone().V();
    HashSet<Vertex> ret = new HashSet<>();

    for (Vertex v :
        traversal
            .has(T.label, LabelP.of(CallExpression.class.getSimpleName()))
            .has("name", callee_name)
            .has("type", base_type)
            .toList()) {

      boolean parameters_match = true;
      if (parameter.size() > 0 && parameter.get(0).equals("*")) {
        // ALL FUNCTIONS WITH THIS BASE TYPE AND NAME MATCH, PARAMETERS ARE IGNORED
      } else {
        boolean[] checkedParameters = new boolean[parameter.size()]; // defaults to false

        Iterator<Edge> arguments = v.edges(Direction.OUT, "ARGUMENTS");
        while (arguments.hasNext()) {
          Vertex arg = arguments.next().inVertex();
          int argumentIndex = Integer.parseInt(arg.value("argumentIndex").toString());
          // argumentIndex starts at 0!
          if (argumentIndex >= parameter.size()) {
            // argumentlength mismatch
            parameters_match = false;
            break;
          }
          checkedParameters[argumentIndex] =
              true; // this parameter is now checked. If it does not match we bail out early

          if (parameter.get(argumentIndex).equals("_")) {
            // skip matching
          } else {
            // either the param in the mark file directly matches, or it has to have a
            // corresponding var which indicates the type
            if (!parameter.get(argumentIndex).equals(arg.value("type").toString())) {
              parameters_match = false;
              break;
            }
          }
        }
        if (parameters_match) {
          // now check if all parameters were validated
          for (int i = 0; i < parameter.size(); i++) {
            if (!checkedParameters[i]) {
              parameters_match = false;
              break;
            }
          }
        }
      }
      if (parameters_match) { // if all of them were checked
        ret.add(v);
      }
    }
    return ret;
  }

  /**
   * Returns nodes with a label {@code MethodDeclaration}.
   *
   * @return
   */
  public GraphTraversal<Vertex, Vertex> methods() {
    GraphTraversalSource traversal = this.clone();

    return traversal.V().has(T.label, LabelP.of(MethodDeclaration.class.getSimpleName()));
  }

  //  @Deprecated
  //  public GraphTraversal<Vertex, Vertex> cipherListSetterCalls() {
  //    GraphTraversalSource traversal = this.clone();
  //
  //    return this.calls().has(NAME, "SSL_CTX_set_cipher_list");
  //  }

  /**
   * Returns all TranslationUnitDeclaration nodes.
   *
   * @return
   */
  public GraphTraversal<Vertex, Vertex> translationunits() {
    GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

    return traversal.has(T.label, LabelP.of(TranslationUnitDeclaration.class.getSimpleName()));
  }

  /**
   * Returns all RecordDeclarations (e.g., Java classes).
   *
   * @return
   */
  public GraphTraversal<Vertex, Vertex> recorddeclarations() {
    GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

    return traversal.has(T.label, LabelP.of(RecordDeclaration.class.getSimpleName()));
  }

  /**
   * Returns the RecordDeclarations with a given name.
   *
   * @return
   */
  public GraphTraversal<Vertex, Vertex> recorddeclaration(String recordname) {
    GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

    return traversal
        .has(T.label, LabelP.of(RecordDeclaration.class.getSimpleName()))
        .has("name", recordname);
  }

  /**
   * Returns all RecordDeclarations (e.g., Java classes).
   *
   * @return
   */
  public GraphTraversal<Vertex, Vertex> functiondeclarations() {
    GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

    return traversal.has(T.label, LabelP.of(FunctionDeclaration.class.getSimpleName()));
  }

  /**
   * Returns the FunctionDeclarations with a given name.
   *
   * @return
   */
  public GraphTraversal<Vertex, Vertex> functiondeclaration(String functionname) {
    GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

    return traversal
        .has(T.label, LabelP.of(FunctionDeclaration.class.getSimpleName()))
        .has("name", functionname);
  }

  /**
   * Returns all Declarations (e.g., variables).
   *
   * @return
   */
  public GraphTraversal<Vertex, Vertex> declarations() {
    GraphTraversal<Vertex, Vertex> traversal = this.clone().V();

    return traversal.has(T.label, LabelP.of(Declaration.class.getSimpleName()));
  }
}
