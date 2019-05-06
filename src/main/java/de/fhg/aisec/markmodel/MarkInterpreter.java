package de.fhg.aisec.markmodel;

import de.fhg.aisec.mark.markDsl.*;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversal;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import de.fraunhofer.aisec.crymlin.server.AnalysisContext;
import de.fraunhofer.aisec.crymlin.server.AnalysisServer;
import de.fraunhofer.aisec.crymlin.utils.Utils;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
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

  /**
   * Evaluates the {@code markModel} against the currently analyzed program.
   *
   * <p>This is the core of the MARK evaluation.s
   *
   * @param result
   */
  public TranslationResult evaluate(TranslationResult result, AnalysisContext ctx) {
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
      v.property("dennis", "test"); // attach temporary property?!
      System.out.println(v + " " + v.label() + " (" + v.property("name") + ")");
      v.edges(Direction.OUT)
          .forEachRemaining(
              x ->
                  System.out.println(
                      x.label()
                          + ": "
                          + x.inVertex().label()
                          + " ("
                          + x.inVertex().property("name")
                          + ")"
                          + " -> "
                          + x.outVertex().label()
                          + " ("
                          + x.inVertex().property("name")
                          + ")"));

      if (v.graph().tx().isOpen()) {
        v.graph()
            .tx()
            .readWrite(); // should not be called by a program according to docu, but this persists
        // our new property
      } else {
        System.out.println("cannot persist, tx is not open");
      }
    }

    //    calls = (CrymlinTraversal<Vertex, Vertex>) crymlinTraversal.calls().clone();
    //    for (Vertex v: calls.toList()) {
    //      System.out.println("TESTEST" + v.property("dennis"));
    //    }

    calls = (CrymlinTraversal<Vertex, Vertex>) crymlinTraversal.calls().clone();
    List<String> myCalls = calls.name().toList();

    // "Populate" MARK objects
    for (MEntity ent : this.markModel.getEntities()) {
      for (MOp op : ent.getOps()) {
        for (CallStatement opCall : op.getCallStatements()) {

          // Extract only the method Botan::Cipher_Mode::start())  -> start()
          final String methodName = Utils.extractMethodName(opCall.getCall().getName());
          Optional<String> call =
              myCalls.stream()
                  .filter(sourceCodeCall -> sourceCodeCall.endsWith(methodName))
                  .findAny();

          // TODO now, only the function name is checked. We also need to check the Type the
          // function is executed on.

          if (call.isPresent()) {
            System.out.println("my calls: " + call.get());
            // TODO if myCalls.size()>0, we found a call that was specified in MARK. "Populate" the
            // object.

            // TODO Find out arguments of the call and try to resolve concrete values for them

            // TODO "Populate" the entity and assign the resolved values to the entity's variables

            // one should be enough?
            break;
          } else {
            System.out.println("no call to " + methodName + " found.");
          }
          this.markModel.getPopulatedEntities().put(ent.getName(), ent);
        }
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
