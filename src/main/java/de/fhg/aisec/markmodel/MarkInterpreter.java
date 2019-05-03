package de.fhg.aisec.markmodel;

import de.fhg.aisec.mark.markDsl.*;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import de.fraunhofer.aisec.crymlin.server.AnalysisContext;
import de.fraunhofer.aisec.crymlin.server.AnalysisServer;
import de.fraunhofer.aisec.crymlin.utils.Utils;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
   * <p>TODO This is the core of the MARK evaluation. It needs a complete rewrite.
   *
   * <p>The pesudocode comment outlines a "proper" analysis (but it is still incomplete), while the
   * actual Java code below was just a quick'n dirty hack for the PoC demonstration.
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
    List<String> myCalls = crymlinTraversal.calls().name().toList();

    // "Populate" MARK objects
    for (MEntity ent : this.markModel.getEntities()) {
      for (MOp op : ent.getOps()) {
        for (CallStatement opCall : op.getCallStatements()) {

          Optional<String> call = containsCall(myCalls, opCall);

          if (call.isPresent()) {
            log.debug("my calls: " + call.get());
            // TODO if myCalls.size()>0, we found a call that was specified in MARK. "Populate" the
            // object.

            // TODO Find out arguments of the call and try to resolve concrete values for them

            // TODO "Populate" the entity and assign the resolved values to the entity's variables
          }
          this.markModel.getPopulatedEntities().put(ent.getName(), ent);
        }
      }
    }

    // Evaluate rules against populated objects

    for (MRule r : this.markModel.getRules()) {
      log.debug("Processing rule {}", r.getName());
      // TODO Result of rule evaluation will not be a boolean but "not triggered/triggered and
      // violated/triggered and satisfied".
      if (evaluateRule(r)) {
        ctx.getFindings().add("Rule " + r.getName() + " is satisfied");
      }
    }
    return result;
  }

  private Optional<String> containsCall(List<String> myCalls, CallStatement opCall) {
    // Extract only the method Botan::Cipher_Mode::start())  -> start()
    final String methodName = Utils.extractMethodName(opCall.getCall().getName());
    return myCalls.stream().filter(sourceCodeCall -> sourceCodeCall.endsWith(methodName)).findAny();
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
      System.out.println("Found matching entity " + matchingEntity.get());
      Expression ensureExpr = r.getStatement().getEnsure().getExp();
      log.debug(exprToString(ensureExpr));
      // TODO evaluate expression against populated mark entities
      if (evaluateExpr(ensureExpr)) {
        log.debug("Rule {} is satisfied.", r.getName());
        return true;
      } else {
        log.debug("Rule {} is matched but violated.", r.getName());
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
      System.out.println("Cannot evaluate " + expr.getClass());
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
    System.out.println(exprToString(expr));

    return true;
  }
}
