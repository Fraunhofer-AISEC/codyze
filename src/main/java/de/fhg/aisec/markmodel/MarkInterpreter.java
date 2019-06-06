package de.fhg.aisec.markmodel;

import de.fhg.aisec.mark.markDsl.*;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversal;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import de.fraunhofer.aisec.crymlin.server.AnalysisContext;
import de.fraunhofer.aisec.crymlin.server.AnalysisServer;
import de.fraunhofer.aisec.crymlin.utils.Utils;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.emf.common.util.EList;
import org.python.antlr.base.expr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarkInterpreter {
  private static final Logger log = LoggerFactory.getLogger(AnalysisServer.class);
  private final CrymlinTraversalSource crymlinTraversal;
  @NonNull private final Mark markModel;

  public MarkInterpreter(@NonNull Mark markModel, CrymlinTraversalSource crymlinTraversal) {
    this.markModel = markModel;
    this.crymlinTraversal = crymlinTraversal;
  }

  public static String exprToString(Expression expr) {
    if (expr == null) {
      return " null ";
    }

    if (expr instanceof LogicalOrExpression) {
      return exprToString(((LogicalOrExpression) expr).getLeft())
          + " || "
          + exprToString(((LogicalOrExpression) expr).getRight());
    } else if (expr instanceof LogicalAndExpression) {
      return exprToString(((LogicalAndExpression) expr).getLeft())
          + " && "
          + exprToString(((LogicalAndExpression) expr).getRight());
    } else if (expr instanceof ComparisonExpression) {
      ComparisonExpression compExpr = (ComparisonExpression) expr;
      return exprToString(compExpr.getLeft())
          + " "
          + compExpr.getOp()
          + " "
          + exprToString(compExpr.getRight());
    } else if (expr instanceof FunctionCallExpression) {
      FunctionCallExpression fExpr = (FunctionCallExpression) expr;
      String name = fExpr.getName();
      return name
          + "("
          + fExpr.getArgs().stream().map(arg -> argToString(arg)).collect(Collectors.joining(", "))
          + ")";
    } else if (expr instanceof LiteralListExpression) {
      return "[ "
          + ((LiteralListExpression) expr)
              .getValues().stream().map(val -> val.getValue()).collect(Collectors.joining(", "))
          + " ]";
    } else if (expr instanceof RepetitionExpression) {
      RepetitionExpression inner = (RepetitionExpression) expr;
      // todo @FW do we want this optimization () can be omitted if inner is no sequence
      if (inner.getExpr() instanceof SequenceExpression) {
        return "(" + exprToString(inner.getExpr()) + ")" + inner.getOp();
      } else {
        return exprToString(inner.getExpr()) + inner.getOp();
      }
    } else if (expr instanceof Operand) {
      return ((Operand) expr).getOperand();
    } else if (expr instanceof Literal) {
      return ((Literal) expr).getValue();
    } else if (expr instanceof SequenceExpression) {
      SequenceExpression seq = ((SequenceExpression) expr);
      return exprToString(seq.getLeft()) + seq.getOp() + " " + exprToString(seq.getRight());
    } else if (expr instanceof Terminal) {
      Terminal inner = (Terminal) expr;
      return inner.getEntity() + "." + inner.getOp() + "()";
    } else if (expr instanceof OrderExpression) {
      OrderExpression order = (OrderExpression) expr;
      SequenceExpression seq = (SequenceExpression) order.getExp();
      return "order " + exprToString(seq);
    }
    return "UNKNOWN EXPRESSION TYPE: " + expr.getClass();
  }

  public static String argToString(Argument arg) {
    return exprToString((Expression) arg); // Every Argument is also an Expression
  }

  public void dumpCFG(Edge edge, String pref, HashSet<Vertex> seen) {

    Vertex outV = edge.inVertex();
    if (seen.contains(outV)) {
      System.out.println("Already seen: " + edge);
      return;
    }
    seen.add(outV);
    System.out.println(pref + outV.label() + " - " + outV.property("name"));
    Iterator<Edge> cfg = outV.edges(Direction.OUT, "CFG");
    while (cfg.hasNext()) {
      Edge next = cfg.next();
      dumpCFG(next, pref + "\t", seen);
    }
  }

  private HashSet<Vertex> getVerticesForFunctionDeclaration(
      FunctionDeclaration functionDeclaration, MEntity ent) {
    String functionName = Utils.extractMethodName(functionDeclaration.getName());
    String baseType = Utils.extractType(functionDeclaration.getName());

    EList<String> params = functionDeclaration.getParams();
    // resolve parameters which have a corresponding var part in the entity
    ArrayList<String> cloned = new ArrayList<>(params);
    for (int i = 0; i < cloned.size(); i++) {
      String typeForVar = ent.getTypeForVar(cloned.get(i));
      if (typeForVar != null) {
        cloned.set(i, typeForVar);
      }
    }

    return crymlinTraversal.calls(functionName, baseType, cloned);
  }

  /**
   * Evaluates the {@code markModel} against the currently analyzed program.
   *
   * <p>This is the core of the MARK evaluation.s
   *
   * @param result
   */
  public TranslationResult evaluate(TranslationResult result, AnalysisContext ctx) {

    /*
    iterate all entities and precalculate some things:
       -
    */
    for (MEntity ent : this.markModel.getEntities()) {
      ent.parseVars();
      for (MOp op : ent.getOps()) {
        // todo rewrite once the grammar changed
        log.info("{} Parsing Call Statements", op.getName());
        for (OpStatement a : op.getStatements()) {
          HashSet<Vertex> temp = getVerticesForFunctionDeclaration(a.getCall(), ent);
          System.out.println(a.getCall().getName() + ": " + temp.size());
          op.addVertex(a, temp);
        }
        op.setParsingFinished();
      }
    }

    evaluateForbiddenCalls(ctx);
    /*
     *
     *  // Iterate over all statements of the program, along the CFG (created by SimpleForwardCFG)
     *  for tu in translationunits:
     *    for func in tu.functions:
     *      for stmt in func.cfg:
     *
     *        // If an object of interest is created -> track it as an "abstract object"
     *        // "of interest" = mentioned as an Entity in at least one MARK "rule".
     *        if is_object_creation(stmt):
     *          a_obj = create_abstract_object(stmt)
     *          init_typestate(a_obj)
     *          object_table.add(a_obj)
     *
     *        // If an abstract object is "used" (= one of its fields is set or one of its methods is called) -> update its typestate.
     *        // "update its typestate" needs further detailing
     *        if uses_abstract_object(stmt):
     *          update_typestate(stmt)
     *
     */

    /*
     * A "typestate" item is an object that approximates the "states" of a real object instances at runtime.
     *
     * States are defined as the (approximated) values of the object's member fields.
     */

    // Maintain all method calls in a list
    CrymlinTraversal<Vertex, Vertex> calls =
        (CrymlinTraversal<Vertex, Vertex>) crymlinTraversal.calls().clone();
    List<Vertex> vertices = calls.toList();
    for (Vertex v : vertices) {
      v.property("dennis", "test"); // attach temporary property
      // System.out.println(v + " " + v.label() + " (" + v.property("name") + ")");
      //      v.edges(Direction.OUT)
      //          .forEachRemaining(
      //              x ->
      //                  System.out.println(
      //                      x.label()
      //                          + ": "
      //                          + x.inVertex().label()
      //                          + " ("
      //                          + x.inVertex().property("name")
      //                          + ")"
      //                          + " -> "
      //                          + x.outVertex().label()
      //                          + " ("
      //                          + x.inVertex().property("name")
      //                          + ")"));

      if (v.graph().tx().isOpen()) {
        v.graph()
            .tx()
            .readWrite(); // should not be called by a program according to docu, but this persists
        // our new property
      } else {
        System.out.println("cannot persist, tx is not open");
      }
    }

    // TEST
    /*
    List<Vertex> do_crypt = crymlinTraversal.functiondeclaration("do_crypt").clone().toList();
    if (do_crypt.size() != 1) {
      System.err.println("Multiple functions with name do_crypt found");
    }
    Edge current = do_crypt.get(0).edges(Direction.OUT, "BODY").next();
    */
    // dumpCFG(current, "", new HashSet<>());

    calls = (CrymlinTraversal<Vertex, Vertex>) crymlinTraversal.calls().clone();
    List<String> myCalls = calls.name().toList();

    // "Populate" MARK objects
    for (MEntity ent : this.markModel.getEntities()) {
      Set<String> collect =
          ent.getOps().stream()
              .map(
                  x ->
                      x.getStatements().stream()
                          .map(cs -> cs.getCall().getName())
                          .collect(Collectors.toSet()))
              .flatMap(Collection::stream)
              .collect(Collectors.toSet());

      Optional<String> any =
          myCalls.stream()
              .filter(
                  call -> collect.stream().anyMatch(x -> x.endsWith(Utils.extractMethodName(call))))
              .findAny();
      // TODO now, only the function name is checked. We also need to check the Type the
      // function is executed on.

      if (any.isPresent()) {
        System.out.println("MARK MATCHED - " + ent.getName());
        System.out.println("\t\t" + any.get());
        // TODO if myCalls.size()>0, we found a call that was specified in MARK. "Populate" the
        // object.
        // TODO Find out arguments of the call and try to resolve concrete values for them
        // TODO "Populate" the entity and assign the resolved values to the entity's variables
        this.markModel.getPopulatedEntities().put(ent.getName(), ent);
      }
    }

    // Evaluate rules against populated objects
    for (MRule r : this.markModel.getRules()) {
      // System.out.println("Processing rule " + r.getName());
      // TODO Result of rule evaluation will not be a boolean but "not triggered/triggered and
      // violated/triggered and satisfied".
      if (evaluateRule(r)) {
        ctx.getFindings().add("Rule " + r.getName() + " is satisfied");
      }
    }
    return result;
  }

  private void evaluateForbiddenCalls(AnalysisContext ctx) {
    /*
     * For a call to be forbidden, it needs to:
     * - matches any forbidden signature (as callstatment in an op)
     *    - with * for arbitrary parameters,
     *    - _ for ignoring one parameter type, or
     *    - a reference to a var in the entity to specify a concrete type (no type hierarchy is analyzed!)
     * - _and_ is not allowed by any other non-forbidden matching call statement (in _any_ op)
     */

    log.info("Looking for forbidden calls");
    for (MEntity ent : this.markModel.getEntities()) {

      for (MOp op : ent.getOps()) {
        for (Map.Entry<Vertex, HashSet<OpStatement>> entry :
            op.getVertexToCallStatementsMap().entrySet()) {
          if (entry.getValue().stream()
              .noneMatch(call -> "forbidden".equals(call.getForbidden()))) {
            // only allowed entries
            continue;
          }
          Vertex v = entry.getKey();
          boolean vertex_allowed = false;
          HashSet<String> violating = new HashSet<>();
          for (OpStatement call : entry.getValue()) {
            String callString =
                call.getCall().getName() + "(" + String.join(",", call.getCall().getParams()) + ")";

            if (!"forbidden".equals(call.getForbidden())) {
              // there is at least one CallStatement which explicitly allows this Vertex!
              log.info(
                  "Vertex |{}| is allowed, since it matches whitelist entry {}",
                  v.value("code"),
                  callString);
              vertex_allowed = true;
              break;
            } else {
              violating.add(callString);
            }
          }
          if (!vertex_allowed) {
            String finding =
                "Violation against forbidden call(s) "
                    + String.join(", ", violating)
                    + " in Entity "
                    + ent.getName()
                    + ". Call was "
                    + v.value("code").toString();
            ctx.getFindings().add(finding);
            log.info(finding);
          }
        }
      }
    }
  }

  /**
   * DUMMY. JUST FOR DEMO. REWRITE.
   *
   * <p>Evaluates a MARK rule against the results of the analysis.
   *
   * @param r
   * @return
   */
  public boolean evaluateRule(MRule r) {
    // TODO parse rule and do something with it
    Optional<String> matchingEntity =
        r.getStatement().getEntities().stream()
            .map(ent -> ent.getE().getName())
            .filter(entityName -> this.markModel.getPopulatedEntities().containsKey(entityName))
            .findAny();
    if (matchingEntity.isPresent()) {
      // System.out.println("Found matching entity " + matchingEntity.get());
      Expression ensureExpr = r.getStatement().getEnsure().getExp();
      // System.out.println(exprToString(ensureExpr));
      // TODO evaluate expression against populated mark entities
      if (evaluateExpr(ensureExpr)) {
        // System.out.println("Rule " + r.getName() + " is satisfied.");
        return true;
      } else {
        // System.out.println("Rule " + r.getName() + " is matched but violated.");
        return false;
      }
    }
    return false;
  }

  private boolean evaluateExpr(Expression expr) {
    if (expr instanceof SequenceExpression) {
      OrderExpression left = ((SequenceExpression) expr).getLeft();
      OrderExpression right = ((SequenceExpression) expr).getRight();
      return evaluateExpr(left) && evaluateExpr(right);
    } else if (expr instanceof Terminal) {
      return containedInModel((Terminal) expr);
    } else if (expr instanceof OrderExpression) {
      SequenceExpression seqxpr = (SequenceExpression) ((OrderExpression) expr).getExp();
      if (seqxpr != null) {
        return evaluateExpr(seqxpr);
      }
    } else {
      // System.out.println("Cannot evaluate " + expr.getClass());
    }
    return false;
  }

  /**
   * DUMMY FOR DEMO.
   *
   * <p>Method fakes that a statement is contained in the a MARK entity
   *
   * @return
   */
  private boolean containedInModel(Terminal expr) {
    return true;
  }
}
