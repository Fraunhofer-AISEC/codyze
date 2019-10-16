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
import de.fraunhofer.aisec.markmodel.fsm.Node;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of a WPDS-based typestate analysis using the code property graph.
 *
 * Legal typestates are given by a regular expression as of the MARK "order" construct. This class will convert this regular expression into a "typestate NFA".
 * The transitions of the typestate NFA are then represented as "weights" for a weighted pushdown system (WPDS). The WPDS is an abstraction of the data flows
 * in the program (currently there is one WPDS per function, but we can easily extend this to inter-procedural WPDS'es).
 *
 * Given an "initial configuration" in form of a "weighted automaton" P, the "post-*" algorithm [1] will then "saturate" this weighted automaton P into Ps. The saturated
 * weighted automaton Ps is a representation of all type states reachable from the initial configuration, given the underlying program abstraction in form of the WPDS.
 *
 * Thus when inspecting the weights (which are effectively type state transitions) of the saturated weighted automaton Ps, we can check if all operations on an object
 * (and its aliases) refer to legal type state transitions, if there is an execution path in the program which reaches the end of the typestate, or if any operation leads
 * to an illegal typestate (= empty weight in a transition of Ps).
 *
 *
 * [1] Reps T., Lal A., Kidd N. (2007) Program Analysis Using Weighted Pushdown Systems. In: Arvind V., Prasad S. (eds) FSTTCS 2007: Foundations of Software Technology
 * and Theoretical Computer Science. FSTTCS 2007. Lecture Notes in Computer Science, vol 4855. Springer, Berlin, Heidelberg
 */
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

    // Creating a WPDS from CPG, starting at seeds. Note that this will neglect alias which have been defined before the seed.
    HashSet<Node> seedExpression = null; // TODO Seeds must be vertices with calls which MAY be followed by a typestate violation
    try {
      CpgWpds wpds = createWpds(seedExpression, verticeMap, crymlinTraversal, inner.getExp());
    } catch (IllegalTransitionException e) {
      e.printStackTrace();
    }


  }


  /**
   * Creates a weighted pushdown system (WPDS), linked to a typestate NFA.
   *
   * When populating the WPDS using post-* algorithm, the result will be an automaton capturing the reachable type states.
   *
   * @param seedExpressions
   * @param verticeMap
   * @param crymlinTraversal
   * @return
   * @throws IllegalTransitionException
   */
  private CpgWpds createWpds(@Nullable HashSet<Node> seedExpressions, HashMap<MOp, Vertex> verticeMap, CrymlinTraversalSource crymlinTraversal, OrderExpression orderExpr) throws IllegalTransitionException {
    System.out.println("-----  Creating WPDS ----------");

    /* Create empty WPDS */
    CpgWpds wpds = new CpgWpds();

    /* Create typestate NFA.
    *  The typestate NFA is a non-deterministic finite automaton representing the regular expression of
    * a MARK typestate rule. */
    NFA nfa = NFA.of(orderExpr);
    System.out.println("Initial typestate NFA: ");
    System.out.println(nfa.toString());

    /* Create weight domain, linkeded to the NFA */
    Weight w = new Weight(nfa);

    // TODO WPDS should be "seeded" for a relevant statements. Currently we transform whole functions into a WPDS

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

      // TODO for testing only: skip all other methods except for ok2()
      if (!currentFunctionName.equals("ok2")) {
        continue;
      }
      ArrayDeque<Vertex> worklist = new ArrayDeque<>();
      worklist.add(functionDeclaration);
      Stmt previousStmt = new Stmt((String) functionDeclaration.property("name").value());

      while (!worklist.isEmpty()) {
        Vertex v = worklist.pop();
        System.out.println("Current v: " + v.property("code").value());

        // We consider only "Statements" in the EOG
        if (v.edges(Direction.IN, "STATEMENTS").hasNext()) {

          /* "MemberCallExpressions" result in a normal rule. TODO Later they should also result in push/pop rule pair for interprocedural analysis */
          if (v.label().equals(MemberCallExpression.class.getSimpleName())) {
            System.out.println("Found method call " + v.property("code").value());
            Stmt currentStmt = new Stmt(v.property("code").value().toString());

            // TODO Base should refer to the set of a current aliases, not only object instance used in this current MemberCall
            Vertex base = v.edges(Direction.OUT, "BASE").next().inVertex();
            Val baseVal = new Val((String) base.property("name").value(), currentFunctionName);

            // Create normal rule. Flow remains at Base.
            Set<NFATransition> relevantNFATransitions = nfa.getTransitions().stream().filter(tran -> tran.getSource().getOp().equals(v.property("name").value())).collect(Collectors.toSet());
            Weight weight = relevantNFATransitions.isEmpty() ? Weight.one() : new Weight(relevantNFATransitions);
            Rule<Stmt, Val, Weight> normalRule = new NormalRule<>(baseVal, previousStmt, baseVal, currentStmt, weight);
            wpds.addRule(normalRule);
            System.out.println("Adding normal rule " + normalRule.toString());
            previousStmt = currentStmt;

            /* "DeclarationStatements" result in a normal rule, assigning rhs to lhs. */
           } else if (v.label().equals(DeclarationStatement.class.getSimpleName())) {
            System.out.println("Found variable declaration " + v.property("code").value());

            Vertex decl = v.edges(Direction.OUT, "DECLARATIONS").next().inVertex();
            Val declVal = new Val((String) decl.property("name").value(), currentFunctionName);

            Vertex rhsVar = decl.edges(Direction.OUT, "INITIALIZER").next().inVertex(); // TODO Do not simply assume that the target of an INITIALIZER edge is a variable
            if (rhsVar.property("name").isPresent()) {
              System.out.println("  Has name on right hand side " + rhsVar.property("name").value());
              Val rhsVal = new Val((String) rhsVar.property("name").value(), currentFunctionName);
              Stmt currentStmt = new Stmt(v.property("code").value().toString());
              // We add all transitions of the typestate NFA that may be triggered by the current op

              Set<NFATransition> relevantNFATransitions =
                  nfa.getTransitions().stream()
                      .filter(tran -> tran.getSource().getOp().equals(rhsVal.getVariable()))
                      .collect(Collectors.toSet());
              Weight weight = relevantNFATransitions.isEmpty() ? Weight.one() : new Weight(relevantNFATransitions);

              Rule<Stmt, Val, Weight> normalRuleCopy = new NormalRule<>(rhsVal, previousStmt, declVal, currentStmt, weight);
              System.out.println("Adding normal rule " + normalRuleCopy.toString());
              wpds.addRule(normalRuleCopy);

              Rule<Stmt, Val, Weight> normalRuleSelf = new NormalRule<>(rhsVal, previousStmt, rhsVal, currentStmt, weight);
              System.out.println("Adding normal rule " + normalRuleSelf.toString());
              wpds.addRule(normalRuleSelf);

              previousStmt = currentStmt;
            } else {
              System.out.println("  Has no name on right hand side");
              // TODO handle new instantiations here
              Stmt currentStmt = new Stmt(v.property("code").value().toString());
              rhsVar = v.edges(Direction.OUT, "EOG").next().inVertex(); // TODO Do not simply assume that the target of an EOG edge is a variable
              Val rhsVal = new Val((String) rhsVar.property("name").value(), currentFunctionName);

              Rule<Stmt, Val, Weight> normalRule = new NormalRule<>(rhsVal, previousStmt, declVal, currentStmt, new Weight(nfa.getInitialTransitions()));
              System.out.println("Adding normal rule " + normalRule.toString());
              wpds.addRule(normalRule);
              previousStmt = currentStmt;
            }

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
      // TODO Initial configuration is still hardcoded
      String stmt = "Botan2 p2 = new Botan2(1);";
      String variable = "p2";
      String method = "ok2";
      WeightedAutomaton<Stmt, Val, Weight> wnfa = createInitialConfiguration(stmt, variable, method, nfa);

      // For debugging only:
      for (Rule r : wpds.getAllRules()) {
        System.out.println(r.toString());
      }

      // Print the non-saturated NFA
      System.out.println("Non saturated NFA");
      System.out.println(wnfa.toString());
      System.out.println(wnfa.toDotString());

      // Saturate the NFA from the WPDS, using the post-* algorithm.
      wpds.poststar(wnfa);

      // Print the post-*-saturated NFA
      System.out.println(wnfa.toString());
      System.out.println(wnfa.toDotString());

      /* Typestate analysis is finished. The results are as follows:

         1) Transitions in WNFA with *empty weights* or weights into an ERROR type state indicate an error. Type state requirements are violated at this point.
         2) If there is a path through the automaton leading to the END state, the type state specification is completely covered by this path
         3) If all transitions have proper type state weights but none of them leads to END, the type state is correct but incomplete.
       */
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

  /**
   * Creates an initial configuration of a WPDS from where post* runs.
   *
   * The initial configuration comprises the set of states (i.e. statements and variables on the stack) which are relevant for following typestate analysis and is given in form of a weighted automaton P.
   *
   * Typically, the initial configuration will refer to a single "trigger" statement from where typestate analysis should start. This statement i
   * @param variable
   * @param method
   * @return
   */
  private WeightedAutomaton createInitialConfiguration(String stmt, String variable, String method, NFA nfa) {
    Val initialState = new Val(variable, method);
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
    Val ACCEPTING = new Val("ACCEPT", "ACCEPT");
    // Create an automaton for the initial configuration from where post* will start.
    wnfa.addTransition(new Transition<>(initialState, new Stmt(stmt), ACCEPTING), new Weight(nfa.getInitialTransitions()));
    // Add final ("accepting") states to NFA.
    wnfa.addFinalState(ACCEPTING);

    return wnfa;
  }
}
