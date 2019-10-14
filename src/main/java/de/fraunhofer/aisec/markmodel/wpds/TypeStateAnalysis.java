package de.fraunhofer.aisec.markmodel.wpds;

import de.breakpoint.pushdown.IllegalTransitionException;
import de.breakpoint.pushdown.de.breakpoint.pushdown.fsm.Transition;
import de.breakpoint.pushdown.de.breakpoint.pushdown.fsm.WeightedAutomaton;
import de.breakpoint.pushdown.rules.NormalRule;
import de.breakpoint.pushdown.rules.Rule;
import de.fraunhofer.aisec.cpg.graph.DeclarationStatement;
import de.fraunhofer.aisec.cpg.graph.MemberCallExpression;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import de.fraunhofer.aisec.crymlin.server.AnalysisContext;
import de.fraunhofer.aisec.crymlin.utils.Pair;
import de.fraunhofer.aisec.mark.markDsl.OrderExpression;
import de.fraunhofer.aisec.markmodel.MEntity;
import de.fraunhofer.aisec.markmodel.MOp;
import de.fraunhofer.aisec.markmodel.MRule;
import de.fraunhofer.aisec.markmodel.fsm.FSM;
import de.fraunhofer.aisec.markmodel.fsm.Node;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

public class TypeStateAnalysis {

  public void analyze(AnalysisContext ctx, CrymlinTraversalSource crymlinTraversal, MRule rule) {
    System.out.println("Analysing " + ctx + " and " + crymlinTraversal);

    // For debugging only:
    for (Map.Entry<String, Pair<String, MEntity>> entry : rule.getEntityReferences().entrySet()) {
      MEntity ent = entry.getValue().getValue1();
      for (MOp op : ent.getOps()) {
        HashSet<Vertex> opVertices = op.getAllVertices();
        if (!opVertices.isEmpty()) {
          System.out.println("Found sth for rule " + rule.toString() + " " + opVertices.size() + " " + opVertices.iterator().next().toString());
          for (Vertex v : opVertices) {
            System.out.println("  " + v.property("code").value());
          }
        }
      }
    }

    HashMap<MOp, Vertex> verticeMap = getVerticesOfRule(rule);

    for (Map.Entry<MOp, Vertex> vertices : verticeMap.entrySet()) {
      System.out.println("Vertix " + vertices.getValue().property("code").value() + " for " + vertices.getKey().getName());
    }

    // Creating FSM (not needed at the moment)
    OrderExpression inner = (OrderExpression) rule.getStatement().getEnsure().getExp();
    FSM fsm = new FSM();
    fsm.sequenceToFSM(inner.getExp());

    // Creating a WPDS from CPG, starting at seeds. Note that this will neglect alias which have been defined before the seed.
    HashSet<Node> seedExpression = null; // TODO Seeds must be vertices with calls which MAY be followed by a typestate violation
    try {
      CpgWpds wpds = createWpds(seedExpression, verticeMap, crymlinTraversal, fsm);
    } catch (IllegalTransitionException e) {
      e.printStackTrace();
    }


  }


  /**
   * Creates a weighted pushdown system.
   *
   * @param seedExpressions
   * @param verticeMap
   * @param crymlinTraversal
   * @param fsm
   * @return
   * @throws IllegalTransitionException
   */
  private CpgWpds createWpds(@Nullable HashSet<Node> seedExpressions, HashMap<MOp, Vertex> verticeMap, CrymlinTraversalSource crymlinTraversal, FSM fsm) throws IllegalTransitionException {
    System.out.println("-----  Creating WPDS ----------");
    CpgWpds wpds = new CpgWpds();
    Weight w = new Weight(fsm);

    // TODO Here we simply use p2.create() as seed. Seeds must be created from MOp
    // --------------------- Start experimental -----------------------------
//    Vertex seed = null;
//    for (Map.Entry<MOp, Vertex> vertexEntry : verticeMap.entrySet()) {
//      MOp op = vertexEntry.getKey();
//      Vertex vertex = vertexEntry.getValue();
//
//      if (vertex.property("code").value().equals("p2.create();")) {
//        seed = vertex;
//      }
//    }
//
//    if (seed == null) {
//      return wpds;
//    }
    // --------------------- End experimental -----------------------------
//    assert seed != null;

    // Alias analysis for base of seed: Create set of objects which are aliases of seed
    // TODO Alias analysis before seed

    /** For each function, create a WPDS
     *
     * The (normal, push, pop) rules of the WPDS reflect the data flow, similar to a static taint analysis.
     *
     */
    for (Vertex functionDeclaration : crymlinTraversal.functiondeclarations().toList()) {
      // Work list of following EOG nodes. Not all EOG nodes will result in a WPDS rule, though.
      String currentFunctionName = (String) functionDeclaration.property("name").value();
      ArrayDeque<Vertex> worklist = new ArrayDeque<>();
      worklist.add(functionDeclaration);
      Stmt previousStmt = new Stmt((String) functionDeclaration.property("name").value());

      while (!worklist.isEmpty()) {
        Vertex v = worklist.pop();
        System.out.println("Current v: " + v.property("code").value());

        // We consider only "Statements" in the EOG
        if (v.edges(Direction.IN, "STATEMENTS").hasNext()) {

          /* "MemberCallExpressions" result in a normal rule.
          TODO Later they should also result in push/pop rule pair for interprocedural analysis */
          if (v.label().equals(MemberCallExpression.class.getSimpleName())) {
            System.out.println("Found method call " + v.property("code").value());
            Stmt currentStmt = new Stmt(v.property("code").value().toString());

            // TODO Base should refer to the set of a current aliases, not only object instance used in this current MemberCall
            Vertex base = v.edges(Direction.OUT, "BASE").next().inVertex();
            Val baseVal = new Val((String) base.property("name").value(), currentFunctionName);

            // Create normal rule. Flow remains at Base.
            Rule<Stmt, Val, Weight> normalRule = new NormalRule<>(baseVal, previousStmt, baseVal, currentStmt, Weight.one());
            wpds.addRule(normalRule);
            previousStmt = currentStmt;

            /* "DeclarationStatements" result in a normal rule, assigning rhs to lhs. */
           } else if (v.label().equals(DeclarationStatement.class.getSimpleName())) {
            System.out.println("Found variable declaration " + v.property("code").value());

            Vertex lhsVar = v.edges(Direction.OUT, "DECLARATIONS").next().inVertex();
            Val lhsVal = new Val((String) lhsVar.property("name").value(), currentFunctionName);
            Vertex rhsVar =
                v.edges(Direction.OUT, "EOG")
                    .next()
                    .inVertex(); // TODO Do not simply assume that the target of an EOG edge is a variable
            Val rhsVal = new Val((String) rhsVar.property("code").value(), currentFunctionName);
            Stmt currentStmt = new Stmt(v.property("code").value().toString());
            /* TODO Choose a correct weight function: identity, if current stmt does not change type state, otherwise respective type state change. The weight instantiated here should refer to all possible current states of the current Mark FSM. */
            Rule<Stmt, Val, Weight> normalRule = new NormalRule<>(rhsVal, previousStmt, lhsVal, currentStmt, Weight.one());
            wpds.addRule(normalRule);
            previousStmt = currentStmt;
          }
        }
        // Add successors to work list
        Iterator<Edge> successors = v.edges(Direction.OUT, "EOG");
        // TODO For the moment we ignore branches and follow only the first successor
        if (successors.hasNext()) {
          worklist.add(successors.next().inVertex());
        }
      }

      // Create a weighted automaton (= a weighted NFA) that describes the initial configurations
      Val initialState = new Val("p2", "ok2");
      WeightedAutomaton<Stmt, Val, Weight> wnfa = new WeightedAutomaton<Stmt, Val, Weight>(initialState) {
        @Override
        public Val createState(Val val, Stmt stmt) {
          return val;
        }

        @Override
        public boolean isGeneratedState(Val val) {
          return false;
        }

        @Override
        public Stmt epsilon() {
          return new Stmt("EPSILON");
        }

        @Override
        public Weight getZero() { return Weight.zero();
        }

        @Override
        public Weight getOne() { return Weight.one(); }
      };
      // Add an (artificial) initial transition to NFA. Otherwise, post* will not work.
      wnfa.addTransition(new Transition<>(initialState, new Stmt("ok2"), initialState));
      // Add final ("accepting") states to NFA.
      wnfa.addFinalState(new Val("p2", " Botan2 p2 = new Botan2(1);"));

      // For debugging only:
      for (Rule r : wpds.getAllRules()) {
        System.out.println(r.toString());
      }

      // Saturate the NFA from the WPDS, using the post-* algorithm.
      wpds.poststar(wnfa);

      // Print the post-*-saturated NFA
      System.out.println(wnfa.toString());
      System.out.println(wnfa.toDotString());
    }
    return wpds;
  }


  private HashMap<MOp, Vertex> getVerticesOfRule(MRule rule) {
    HashMap<MOp, Vertex> opToVertex = new HashMap<>();
    for (Map.Entry<String, Pair<String, MEntity>> entry : rule.getEntityReferences().entrySet()) {
      MEntity ent = entry.getValue().getValue1();
      if (ent == null) {
        continue;
      }
      for (MOp op : ent.getOps()) {
        op.getAllVertices().forEach(v -> opToVertex.put(op, v));
      }
    }
    return opToVertex;
  }
}
