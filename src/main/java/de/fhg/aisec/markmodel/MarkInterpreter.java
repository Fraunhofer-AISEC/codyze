package de.fhg.aisec.markmodel;

import java.util.Optional;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhg.aisec.mark.markDsl.Argument;
import de.fhg.aisec.mark.markDsl.ComparisonExpression;
import de.fhg.aisec.mark.markDsl.Expression;
import de.fhg.aisec.mark.markDsl.FunctionCallExpression;
import de.fhg.aisec.mark.markDsl.Literal;
import de.fhg.aisec.mark.markDsl.LiteralListExpression;
import de.fhg.aisec.mark.markDsl.LogicalAndExpression;
import de.fhg.aisec.mark.markDsl.LogicalOrExpression;
import de.fhg.aisec.mark.markDsl.Operand;
import de.fhg.aisec.mark.markDsl.OrderExpression;
import de.fhg.aisec.mark.markDsl.SequenceExpression;
import de.fhg.aisec.mark.markDsl.Terminal;
import de.fraunhofer.aisec.crymlin.server.AnalysisServer;

public class MarkInterpreter {
  private static final Logger log = LoggerFactory.getLogger(AnalysisServer.class);
  @NonNull private Mark markModel;

  public MarkInterpreter(@NonNull Mark markModel) {
    this.markModel = markModel;
  }

  public String exprToString(Expression expr) {
    if (expr == null) {
      return "";
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
          + String.join(
              ",",
              fExpr.getArgs().stream()
                  .map(arg -> argToString(arg))
                  .collect(Collectors.<String>toList()))
          + ")";
    } else if (expr instanceof LiteralListExpression) {
      return "["
          + String.join(
              ",",
              ((LiteralListExpression) expr)
                  .getValues().stream().map(val -> val.getValue()).collect(Collectors.toList()))
          + "]";
    } else if (expr instanceof Operand) {
      return ((Operand) expr).getOperand();
    } else if (expr instanceof Literal) {
      return ((Literal) expr).getValue();
    } else if (expr instanceof SequenceExpression) {
      SequenceExpression seq = ((SequenceExpression) expr);
      return exprToString(seq.getLeft()) + " => " + exprToString(seq.getRight());
    } else if (expr instanceof Terminal) {
      return ((Terminal) expr).getOp();
    } else if (expr instanceof OrderExpression) {
      OrderExpression order = (OrderExpression) expr;
      SequenceExpression seq = (SequenceExpression) order.getExp();
      if (seq != null) {
        return exprToString(seq);
      } else {
        return order.toString();
      }
    }
    return expr.toString();
  }

  private String argToString(Argument arg) {
    return exprToString((Expression) arg); // Every Argument is also an Expression
  }

  /**
   * DUMMY. JUST FOR DEMO. REWRITE.
   * 
   * Evaluates a MARK rule against the results of the analysis.
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
   * Method fakes that a statement is contained in the a MARK entity
   * 
   * @param orderExpression
   * @param orderExpression2
   * @return
   */
  private boolean containedInModel(Terminal expr) {
    System.out.println(exprToString(expr));
    
    return true;
  }
}
