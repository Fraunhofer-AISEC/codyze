package de.fraunhofer.aisec.markmodel;

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.has;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.hasLabel;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.in;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.is;

import com.steelbridgelabs.oss.neo4j.structure.Neo4JVertex;
import de.fraunhofer.aisec.cpg.graph.BinaryOperator;
import de.fraunhofer.aisec.cpg.graph.VariableDeclaration;
import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversal;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import de.fraunhofer.aisec.crymlin.utils.Builtins;
import de.fraunhofer.aisec.crymlin.utils.CrymlinQueryWrapper;
import de.fraunhofer.aisec.crymlin.utils.Pair;
import de.fraunhofer.aisec.crymlin.utils.Utils;
import de.fraunhofer.aisec.mark.markDsl.Argument;
import de.fraunhofer.aisec.mark.markDsl.BooleanLiteral;
import de.fraunhofer.aisec.mark.markDsl.ComparisonExpression;
import de.fraunhofer.aisec.mark.markDsl.Expression;
import de.fraunhofer.aisec.mark.markDsl.FunctionCallExpression;
import de.fraunhofer.aisec.mark.markDsl.FunctionDeclaration;
import de.fraunhofer.aisec.mark.markDsl.IntegerLiteral;
import de.fraunhofer.aisec.mark.markDsl.Literal;
import de.fraunhofer.aisec.mark.markDsl.LiteralListExpression;
import de.fraunhofer.aisec.mark.markDsl.LogicalAndExpression;
import de.fraunhofer.aisec.mark.markDsl.LogicalOrExpression;
import de.fraunhofer.aisec.mark.markDsl.MultiplicationExpression;
import de.fraunhofer.aisec.mark.markDsl.OpStatement;
import de.fraunhofer.aisec.mark.markDsl.Operand;
import de.fraunhofer.aisec.mark.markDsl.OrderExpression;
import de.fraunhofer.aisec.mark.markDsl.StringLiteral;
import de.fraunhofer.aisec.mark.markDsl.UnaryExpression;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.eclipse.emf.common.util.EList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpressionEvaluator {

  private static final Logger log = LoggerFactory.getLogger(ExpressionEvaluator.class);

  private final EvaluationContext context;

  public ExpressionEvaluator(EvaluationContext ec) {
    this.context = ec;
  }

  public Optional<Boolean> evaluate(Expression expr) {
    if (expr instanceof OrderExpression) {
      return evaluateOrderExpression((OrderExpression) expr);
    }

    Optional result = evaluateExpression(expr);

    if (result.isEmpty()) {
      log.error("Expression could not be evaluated: {}", MarkInterpreter.exprToString(expr));
      return Optional.empty();
    }

    log.debug("Top level expression was evaluated: {}", result.get());

    if (!result.get().getClass().equals(Boolean.class)) {
      log.error("Expression result is not Boolean");
      return Optional.empty();
    }

    return result;
  }

  private Optional<Boolean> evaluateOrderExpression(OrderExpression orderExpression) {
    // TODO integrate Dennis' evaluateOrder()-function
    return Optional.empty();
  }

  private Optional<Boolean> evaluateLogicalExpr(Expression expr) {
    log.debug("Evaluating logical expression: {}", MarkInterpreter.exprToString(expr));

    if (expr instanceof ComparisonExpression) {
      return evaluateComparisonExpr((ComparisonExpression) expr);
    } else if (expr instanceof LogicalAndExpression) {
      LogicalAndExpression lae = (LogicalAndExpression) expr;

      Expression left = lae.getLeft();
      Expression right = lae.getRight();

      Optional leftResult = evaluateExpression(left);
      Optional rightResult = evaluateExpression(right);

      if (leftResult.isEmpty() || rightResult.isEmpty()) {
        log.error("At least one subexpression could not be evaluated");
        return Optional.empty();
      }

      if (leftResult.get().getClass().equals(Boolean.class)
          && rightResult.get().getClass().equals(Boolean.class)) {
        return Optional.of(
            Boolean.logicalAnd((Boolean) leftResult.get(), (Boolean) rightResult.get()));
      }

      // TODO #8
      log.error(
          "At least one subexpression is not of type Boolean: {} vs. {}",
          MarkInterpreter.exprToString(left),
          MarkInterpreter.exprToString(right));

      return Optional.empty();
    } else if (expr instanceof LogicalOrExpression) {
      LogicalOrExpression loe = (LogicalOrExpression) expr;

      Expression left = loe.getLeft();
      Expression right = loe.getRight();

      Optional leftResult = evaluateExpression(left);
      Optional rightResult = evaluateExpression(right);

      if (leftResult.isEmpty() || rightResult.isEmpty()) {
        log.error("At least one subexpression could not be evaluated");
        return Optional.empty();
      }

      if (leftResult.get().getClass().equals(Boolean.class)
          && rightResult.get().getClass().equals(Boolean.class)) {
        return Optional.of(
            Boolean.logicalAnd((Boolean) leftResult.get(), (Boolean) rightResult.get()));
      }

      // TODO #8
      log.error(
          "At least one subexpression is not of type Boolean: {} vs. {}",
          MarkInterpreter.exprToString(left),
          MarkInterpreter.exprToString(right));

      return Optional.empty();
    }

    log.error(
        "Trying to evaluate unknown logical expression: {}", MarkInterpreter.exprToString(expr));

    assert false; // not a logical expression
    return Optional.empty();
  }

  private Optional<Boolean> evaluateComparisonExpr(ComparisonExpression expr) {
    String op = expr.getOp();
    Expression left = expr.getLeft();
    Expression right = expr.getRight();

    log.debug(
        "comparing expression "
            + MarkInterpreter.exprToString(left)
            + " with expression "
            + MarkInterpreter.exprToString(right));

    Optional leftResult = evaluateExpression(left);
    Optional rightResult = evaluateExpression(right);

    if (leftResult.isEmpty() || rightResult.isEmpty()) {
      return Optional.empty();
    }

    Class leftType = leftResult.get().getClass();
    Class rightType = rightResult.get().getClass();

    log.debug("left result= " + leftResult.get() + " right result= " + rightResult.get());

    // TODO implement remaining operations
    switch (op) {
      case "==":
        if (leftType.equals(rightType)) {
          return Optional.of(leftResult.get().equals(rightResult.get()));
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftType.getSimpleName(),
            rightType.getSimpleName());

        return Optional.empty();
      case "!=":
        if (leftType.equals(rightType)) {
          return Optional.of(!leftResult.get().equals(rightResult.get()));
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftType.getSimpleName(),
            rightType.getSimpleName());

        return Optional.empty();
      case "<":
        if (leftType.equals(rightType)) {
          if (leftType.equals(Integer.class)) {
            return Optional.of(((Integer) leftResult.get()) < ((Integer) rightResult.get()));
          } else if (leftType.equals(Float.class)) {
            return Optional.of(((Float) leftResult.get()) < ((Float) rightResult.get()));
          }

          log.error("Comparison operator less-than ('<') not supported for type: {}", leftType);

          return Optional.empty();
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftType.getSimpleName(),
            rightType.getSimpleName());

        return Optional.empty();
      case "<=":
        if (leftType.equals(rightType)) {
          if (leftType.equals(Integer.class)) {
            return Optional.of(((Integer) leftResult.get()) <= ((Integer) rightResult.get()));
          } else if (leftType.equals(Float.class)) {
            return Optional.of(((Float) leftResult.get()) <= ((Float) rightResult.get()));
          }

          log.error(
              "Comparison operator less-than-or-equal ('<=') not supported for type: {}", leftType);

          return Optional.empty();
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftType.getSimpleName(),
            rightType.getSimpleName());

        return Optional.empty();
      case ">":
        if (leftType.equals(rightType)) {
          if (leftType.equals(Integer.class)) {
            return Optional.of(((Integer) leftResult.get()) > ((Integer) rightResult.get()));
          } else if (leftType.equals(Float.class)) {
            return Optional.of(((Float) leftResult.get()) > ((Float) rightResult.get()));
          }

          log.error("Comparison operator greater-than ('>') not supported for type: {}", leftType);

          return Optional.empty();
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftType.getSimpleName(),
            rightType.getSimpleName());

        return Optional.empty();
      case ">=":
        if (leftType.equals(rightType)) {
          if (leftType.equals(Integer.class)) {
            return Optional.of(((Integer) leftResult.get()) >= ((Integer) rightResult.get()));
          } else if (leftType.equals(Float.class)) {
            return Optional.of(((Float) leftResult.get()) >= ((Float) rightResult.get()));
          }

          log.error(
              "Comparison operator greater-than-or-equal ('>=') not supported for type: {}",
              leftType);

          return Optional.empty();
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftType.getSimpleName(),
            rightType.getSimpleName());

        return Optional.empty();
      case "in":
        if (rightResult.get() instanceof List) {
          List l = (List) rightResult.get();

          boolean evalValue = false;
          for (Object o : l) {
            log.debug(
                "Comparing left expression with element of right expression: {} vs. {}",
                leftResult.get(),
                o);

            if (o != null && leftType.equals(o.getClass())) {
              evalValue |= leftResult.get().equals(o);
            }
          }

          return Optional.of(evalValue);
        }

        // TODO #8
        log.error("Type of right expression must be List; given: {}", rightType);

        return Optional.empty();
      case "like":
        if (leftType.equals(rightType)) {
          if (leftType.equals(String.class)) {
            return Optional.of(
                Pattern.matches(
                    Pattern.quote((String) rightResult.get()), (String) leftResult.get()));
          }

          log.error("Comparison operator like ('like') not supported for type: {}", leftType);

          return Optional.empty();
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftType.getSimpleName(),
            rightType.getSimpleName());

        return Optional.empty();
    }

    assert false;
    return Optional.empty();
  }

  private List<Optional> evaluateArgs(EList<Argument> argList, int n) {
    List<Optional> result = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      Expression arg = (Expression) argList.get(i);
      result.add(evaluateExpression(arg));
    }
    return result;
  }

  private Optional evaluateFunctionCallExpr(FunctionCallExpression expr) {
    String functionName = expr.getName();
    if (!functionName.startsWith("_")) {
      log.error("Invalid function {}", functionName);
    }
    /*
     * currently we allow:
     *
     * boolean = _is_instance(var, String)
     * int     = _length(var)
     * boolean = _receives_value_from(var, var)
     * String  = _split(String, String, int)
     */
    switch (functionName) {
      case "_split":
        // arguments: String, String, int
        // example:
        // _split("ASD/EFG/JKL", "/", 1) returns "EFG
        final List<Class> paramTypes = Arrays.asList(String.class, String.class, Integer.class);
        List<Optional> argOptionals = evaluateArgs(expr.getArgs(), 3);

        if (paramTypes.size() != argOptionals.size()) {
          return Optional.<String>empty();
        }

        for (int i = 0; i < paramTypes.size(); i++) {
          Optional arg = argOptionals.get(i);

          if (arg.isEmpty()) {
            return Optional.<String>empty();
          }
          if (!arg.get().getClass().equals(paramTypes.get(i))) {
            return Optional.<String>empty();
          }
        }

        // todo validate that the arguments have the correct type
        String s = (String) argOptionals.get(0).get();
        String regex = (String) argOptionals.get(1).get();
        int index = (Integer) argOptionals.get(2).get();
        log.debug("args are: " + s + "; " + regex + "; " + index);
        // returns String
        return Optional.of(Builtins._split(s, regex, index));

      case "_receives_value_from":
        // arguments: var, var
        // example:

        // todo needs to be discussed, I am not clear what this should achieve
        // the example is:
        /*
        rule UseRandomIV {
          using Botan::Cipher_Mode as cm,
                Botan::AutoSeededRNG as rng
          when
            _split(cm.algorithm, "/", 1) == "CBC" && cm.direction == Botan::Cipher_Dir::ENCRYPTION
          ensure
            _receives_value_from(cm.iv, rng.myValue)
          onfail NoRandomIV
        }
        */
        return Optional.of(Builtins._receives_value_from());

      case "_is_instance":
        // arguments: var, String
        // example:
        // _is_instance(cm.rand, java.security.SecureRandom)
        /* algo:
        start at the DeclaredReferenceExpression of var
        check that the type of the node equals the second argument
         */
        // returns boolean
        break;
      case "_length":
        // todo what should this exactly to? check the array length?

        // arguments: var
        // example:
        // _length(cm.rand)

        /* algo:
        start at the DeclaredReferenceExpression
        go to VariableDeclaration via REFERS_TO edge
        go to <<?>> via INITIALIZER edge
        check array initialization length
         */

        // returns int
        break;
    }

    return Optional.empty();
  }

  private Optional evaluateLiteral(Literal literal) {
    String v = literal.getValue();
    log.debug("Literal with value: {}", v);

    // ordering based on Mark grammar
    if (literal instanceof IntegerLiteral) {
      log.debug("Literal is Integer: {}", v);

      try {
        if (v.startsWith("0x")) {
          return Optional.of(Integer.parseInt(v.substring(2), 16));
        }
        return Optional.of(Integer.parseInt(v));
      } catch (NumberFormatException nfe) {
        log.error("Unable to convert integer literal to Integer: {}\n{}", v, nfe);
      }
      return Optional.empty();
    } else if (literal instanceof BooleanLiteral) {
      log.debug("Literal is Boolean: {}", v);
      return Optional.of(Boolean.parseBoolean(v));
    } else if (literal instanceof StringLiteral) {
      log.debug("Literal is String: {}", v);
      return Optional.of(Utils.stripQuotedString(v));
    }

    log.error("Unknown literal encountered: {}", v);
    return Optional.empty();
  }

  private Optional evaluateExpression(Expression expr) {
    // from lowest to highest operator precedence

    if (expr instanceof LogicalOrExpression) {
      log.debug("evaluating LogicalOrExpression: " + MarkInterpreter.exprToString(expr));
      return evaluateLogicalExpr(expr);
    } else if (expr instanceof LogicalAndExpression) {
      log.debug("evaluating LogicalAndExpression: " + MarkInterpreter.exprToString(expr));
      return evaluateLogicalExpr(expr);
    } else if (expr instanceof ComparisonExpression) {
      log.debug("evaluating ComparisonExpression: " + MarkInterpreter.exprToString(expr));
      return evaluateLogicalExpr(expr);
    } else if (expr instanceof MultiplicationExpression) {
      log.debug("evaluating MultiplicationExpression: " + MarkInterpreter.exprToString(expr));
      return evaluateMultiplicationExpr((MultiplicationExpression) expr);
    } else if (expr instanceof UnaryExpression) {
      log.debug("evaluating UnaryExpression: " + MarkInterpreter.exprToString(expr));
      return evaluateUnaryExpr((UnaryExpression) expr);
    } else if (expr instanceof Literal) {
      log.debug("evaluating Literal expression: " + MarkInterpreter.exprToString(expr));
      return evaluateLiteral((Literal) expr);
    } else if (expr instanceof Operand) {
      log.debug("evaluating Operand expression: " + MarkInterpreter.exprToString(expr));
      return evaluateOperand((Operand) expr);
    } else if (expr instanceof FunctionCallExpression) {
      log.debug("evaluating FunctionCallExpression: " + MarkInterpreter.exprToString(expr));
      return evaluateFunctionCallExpr((FunctionCallExpression) expr);
    } else if (expr instanceof LiteralListExpression) {
      log.debug("evaluating LiteralListExpression: " + MarkInterpreter.exprToString(expr));

      List literalList = new ArrayList<>();

      for (Literal l : ((LiteralListExpression) expr).getValues()) {
        Optional v = evaluateLiteral(l);

        literalList.add(v.orElse(null));
      }
      return Optional.of(literalList);
    }

    log.error("unknown expression: " + MarkInterpreter.exprToString(expr));
    assert false; // all expression types must be handled
    return Optional.empty();
  }

  private Optional evaluateMultiplicationExpr(MultiplicationExpression expr) {
    log.debug("Evaluating multiplication expression: {}", MarkInterpreter.exprToString(expr));

    String op = expr.getOp();
    Expression left = expr.getLeft();
    Expression right = expr.getRight();

    Optional leftResult = evaluateExpression(left);
    Optional rightResult = evaluateExpression(right);

    if (leftResult.isEmpty() || rightResult.isEmpty()) {
      log.error("Unable to evaluate at least one subexpression");
      return Optional.empty();
    }

    Class leftResultType = leftResult.get().getClass();
    Class rightResultType = rightResult.get().getClass();

    switch (op) {
      case "*":
        if (leftResultType.equals(rightResultType)) {
          // TODO check if an overflow occurs
          if (leftResultType.equals(Integer.class)) {
            return Optional.of(((Integer) leftResult.get()) * ((Integer) rightResult.get()));
          } else if (leftResultType.equals(Float.class)) {
            return Optional.of(((Float) leftResult.get()) * ((Float) rightResult.get()));
          }

          // TODO #8
          log.error(
              "Multiplication operator multiplication ('*') not supported for type: {}",
              leftResultType.getSimpleName());
          return Optional.empty();
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftResultType.getSimpleName(),
            rightResultType.getSimpleName());

        return Optional.empty();
      case "/":
        if (leftResultType.equals(rightResultType)) {
          if (leftResultType.equals(Integer.class)) {
            return Optional.of(((Integer) leftResult.get()) / ((Integer) rightResult.get()));
          } else if (leftResultType.equals(Float.class)) {
            return Optional.of(((Float) leftResult.get()) / ((Float) rightResult.get()));
          }

          // TODO #8
          log.error(
              "Multiplication operator division ('/') not supported for type: {}",
              leftResultType.getSimpleName());
          return Optional.empty();
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftResultType.getSimpleName(),
            rightResultType.getSimpleName());

        return Optional.empty();
      case "%":
        if (leftResultType.equals(rightResultType)) {
          if (leftResultType.equals(Integer.class)) {
            return Optional.of(((Integer) leftResult.get()) % ((Integer) rightResult.get()));
          }

          // TODO #8
          log.error(
              "Multiplication operator remainder ('%') not supported for type: {}",
              leftResultType.getSimpleName());
          return Optional.empty();
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftResultType.getSimpleName(),
            rightResultType.getSimpleName());

        return Optional.empty();
      case "<<":
        if (leftResultType.equals(rightResultType)) {
          if (leftResultType.equals(Integer.class)) {
            if (((Integer) rightResult.get()) >= 0) {
              Optional.of(((Integer) leftResult.get()) << ((Integer) rightResult.get()));
            }

            // TODO #8
            log.error(
                "Left shift operator supports only non-negative integers as its right operand");
            return Optional.empty();
          }

          // TODO #8
          log.error(
              "Multiplication operator left shift ('<<') not supported for type: {}",
              leftResultType.getSimpleName());
          return Optional.empty();
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftResultType.getSimpleName(),
            rightResultType.getSimpleName());

        return Optional.empty();
      case ">>":
        if (leftResultType.equals(rightResultType)) {
          if (leftResultType.equals(Integer.class)) {
            if (((Integer) rightResult.get()) >= 0) {
              Optional.of(((Integer) leftResult.get()) >> ((Integer) rightResult.get()));
            }

            // TODO #8
            log.error(
                "Right shift operator supports only non-negative integers as its right operand");
            return Optional.empty();
          }

          // TODO #8
          log.error(
              "Multiplication operator right shift ('>>') not supported for type: {}",
              leftResultType.getSimpleName());
          return Optional.empty();
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftResultType.getSimpleName(),
            rightResultType.getSimpleName());

        return Optional.empty();
      case "&":
        if (leftResultType.equals(rightResultType)) {
          if (leftResultType.equals(Integer.class)) {
            return Optional.of(((Integer) leftResult.get()) & ((Integer) rightResult.get()));
          }

          // TODO #8
          log.error(
              "Addition operator bitwise and ('&') not supported for type: {}",
              leftResultType.getSimpleName());
          return Optional.empty();
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftResultType.getSimpleName(),
            rightResultType.getSimpleName());

        return Optional.empty();
      case "&^":
        if (leftResultType.equals(rightResultType)) {
          if (leftResultType.equals(Integer.class)) {
            return Optional.of(((Integer) leftResult.get()) & ~((Integer) rightResult.get()));
          }

          // TODO #8
          log.error(
              "Addition operator bitwise or ('|') not supported for type: {}",
              leftResultType.getSimpleName());
          return Optional.empty();
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftResultType.getSimpleName(),
            rightResultType.getSimpleName());

        return Optional.empty();
    }

    // TODO #8
    log.error(
        "Trying to evaluate unknown multiplication expression: {}",
        MarkInterpreter.exprToString(expr));

    assert false; // not an addition expression
    return Optional.empty();
  }

  private Optional evaluateUnaryExpr(UnaryExpression expr) {
    log.debug("Evaluating unary expression: {}", MarkInterpreter.exprToString(expr));

    String op = expr.getOp();
    Expression subExpr = expr.getExp();

    Optional subExprResult = evaluateExpression(subExpr);

    if (subExprResult.isEmpty()) {
      log.error("Unable to evaluate subexpression");
      return Optional.empty();
    }

    Class subExprResultType = subExprResult.get().getClass();

    switch (op) {
      case "+":
        if (subExprResultType.equals(Integer.class) || subExprResultType.equals(Float.class)) {
          return subExprResult;
        }

        // TODO #8
        log.error(
            "Unary operator plus sign ('+') not supported for type: {}",
            subExprResultType.getSimpleName());

        return Optional.empty();
      case "-":
        if (subExprResultType.equals(Integer.class)) {
          return Optional.of(-((Integer) subExprResult.get()));
        } else if (subExprResultType.equals(Float.class)) {
          return Optional.of(-((Float) subExprResult.get()));
        }

        // TODO #8
        log.error(
            "Unary operator minus sign ('-') not supported for type: {}",
            subExprResultType.getSimpleName());

        return Optional.empty();
      case "!":
        if (subExprResultType.equals(Boolean.class)) {
          return Optional.of(!((Boolean) subExprResult.get()));
        }

        // TODO #8
        log.error(
            "Unary operator logical not ('!') not supported for type: {}",
            subExprResultType.getSimpleName());

        return Optional.empty();
      case "^":
        if (subExprResultType.equals(Integer.class)) {
          return Optional.of(~((Integer) subExprResult.get()));
        }

        // TODO #8
        log.error(
            "Unary operator bitwise complement ('~') not supported for type: {}",
            subExprResultType.getSimpleName());

        return Optional.empty();
    }

    // TODO #8
    log.error(
        "Trying to evaluate unknown unary expression: {}", MarkInterpreter.exprToString(expr));

    assert false; // not an addition expression
    return Optional.empty();
  }

  private Optional evaluateOperand(Operand operand) {
    log.warn("Operand: {}", operand.getOperand());

    String operandString = operand.getOperand();

    // TODO instead of doing the hard lifting here, I'd rather call out to something that can
    // "resolve" text
    // what heavy lifting?
    // 1. resolve the meaning of text within the context of MARK
    // 2. link MARK definitions to graph components in CPG
    // 3. resolve information to perform evaluation (e.g. retrieve assigned values)

    if (context.hasContextType(EvaluationContext.Type.RULE)) {
      final MRule rule = context.getRule();

      if (operandString.contains(".")) {
        String[] operandParts = operandString.split("\\.");

        if (operandParts.length == 2) {
          String instance = operandParts[0];
          String attribute = operandParts[1];

          Pair<String, MEntity> ref = rule.getEntityReferences().get(instance);
          String entityName = ref.getValue0();
          MEntity referencedEntity = ref.getValue1();

          String attributeType = referencedEntity.getTypeForVar(attribute);

          List<Pair<MOp, Set<OpStatement>>> usesAsVar = new ArrayList<>();
          List<Pair<MOp, Set<OpStatement>>> usesAsFunctionArgs = new ArrayList<>();

          for (MOp operation : referencedEntity.getOps()) {
            Set<OpStatement> vars = new HashSet<>();
            Set<OpStatement> args = new HashSet<>();

            for (OpStatement opStmt : operation.getStatements()) {
              // simple assignment, i.e. var = something()
              if (attribute.equals(opStmt.getVar())) {
                vars.add(opStmt);
              }

              // ...or it's used as a function parameter, i.e. something(..., var, ...)
              FunctionDeclaration fd = opStmt.getCall();
              for (String param : fd.getParams()) {
                if (attribute.equals(param)) {
                  args.add(opStmt);
                }
              }
            }

            if (vars.size() > 0) {
              usesAsVar.add(new Pair<>(operation, vars));
            }

            if (args.size() > 0) {
              usesAsFunctionArgs.add(new Pair<>(operation, args));
            }
          }

          dump(usesAsVar, usesAsFunctionArgs);

          // know which ops and opstatements use operand
          // resolve vars
          try (TraversalConnection conn = new TraversalConnection()) {
            CrymlinTraversalSource crymlin = conn.getCrymlinTraversal();

            for (Pair<MOp, Set<OpStatement>> p : usesAsVar) {
              for (OpStatement opstmt : p.getValue1()) {
                /*
                 * TODO how would we do this? wouldn't we need to evaluate the calling function to
                 *  determine the value?
                 *
                 * I think the intention was to signal that a variable was set by a specific function
                 * call. It's basically a Boolean decision can I find an assignment to the variable where
                 * on the rhs the specified function is called --> YES or NO.
                 */
                FunctionDeclaration fd = opstmt.getCall();
                String fqFunctionName = fd.getName();
                List<String> functionArguments = fd.getParams();

                String functionName = Utils.extractMethodName(fqFunctionName);
                String fqNamePart = Utils.extractType(fqFunctionName);
                if (fqNamePart.equals(functionName)) {
                  fqNamePart = "";
                }

                List<String> functionArgumentTypes = new ArrayList<>();
                for (int i = 0; i < functionArguments.size(); i++) {
                  functionArgumentTypes.add(
                      i, referencedEntity.getTypeForVar(functionArguments.get(i)));
                }

                // 1. find callexpression with opstatment signature
                Set<Vertex> vertices =
                    CrymlinQueryWrapper.getCalls(
                        crymlin, fqNamePart, functionName, entityName, functionArgumentTypes);

                for (Vertex v : vertices) {
                  Traversal<Vertex, Vertex> nextTraversalStep =
                      crymlin
                          .byID((long) v.id())
                          .in("RHS")
                          .where(
                              has(T.label, LabelP.of(BinaryOperator.class.getSimpleName()))
                                  .and()
                                  .has("operatorCode", "="))
                          .out("LHS")
                          .out("REFERS_TO")
                          .has(T.label, LabelP.of(VariableDeclaration.class.getSimpleName()));

                  List<Vertex> nextVertices = nextTraversalStep.toList();
                  log.warn("found traversals: {}", nextVertices);
                }
                // TODO why am I doing all this work? what are we hoping to get from this
                // opstatement?
                // 2. see if EOG leads to Expression with BinaryOperator {operatorCode: '='}
              }
            }

            for (Pair<MOp, Set<OpStatement>> p : usesAsFunctionArgs) {
              for (OpStatement opstmt : p.getValue1()) {
                FunctionDeclaration fd = opstmt.getCall();

                String fqFunctionName = fd.getName();
                String functionName = Utils.extractMethodName(fqFunctionName);
                String fqName =
                    fqFunctionName.substring(0, fqFunctionName.lastIndexOf(functionName));

                if (fqName.endsWith("::")) {
                  fqName = fqName.substring(0, fqName.length() - 2);
                }

                List<String> argumentTypes = fd.getParams();
                int argumentIndex = -1;
                for (int i = 0; i < argumentTypes.size(); i++) {
                  String argVar = argumentTypes.get(i);

                  if (attribute.equals(argVar)) {
                    argumentIndex = i;
                  }

                  if ("_".equals(argumentTypes) || "*".equals(argumentTypes)) {
                    continue;
                  }

                  argumentTypes.set(i, referencedEntity.getTypeForVar(argVar));
                }

                // vertices in CPG where we call a function from a MARK OpStatement and we're
                // looking for one of the arguments
                Set<Vertex> vertices =
                    CrymlinQueryWrapper.getCalls(
                        crymlin, fqName, functionName, entityName, argumentTypes);

                // further investigate each function call
                for (Vertex v : vertices) {
                  // vertices (SHOULD ONLY BE ONE) representing a variable declaration for the
                  // argument we're using in the function call
                  CrymlinTraversal<Vertex, Vertex> variableDeclarationTraversal =
                      crymlin
                          .byID((long) v.id())
                          .out("ARGUMENTS")
                          .has("argumentIndex", argumentIndex)
                          .out("REFERS_TO");

                  // TODO potential NullPointerException
                  Vertex variableDeclarationVertex = variableDeclarationTraversal.toList().get(0);

                  // FIXME and now this code doesn't work as expected, especially path()

                  /* TODO general idea
                   *
                   *  1.  from CPG vertex representing the function argument
                   *      ('crymlin.byID((long) v.id()).out("ARGUMENTS").has("argumentIndex", argumentIndex)')
                   *      create all paths to vertex with variable declaration ('variableDeclarationVertex')
                   *      in theory 'crymlin.byID((long) v.id()).repeat(in("EOG").simplePath())
                   *          .until(hasId(variableDeclarationVertex.id())).path()'
                   *  2.  traverse this path from 'v' ---> 'variableDeclarationVertex'
                   *  3.  for each assignment, i.e. BinaryOperator{operatorCode: "="}
                   *  4.    check if -{"LHS"}-> v -{"REFERS_TO"}-> variableDeclarationVertex
                   *  5.    then determine value RHS
                   *  6.    done
                   *  7.  {no interjacent assignment} determine value of variableDeclarationVertex (e.g. from its initializer)
                   *  8.  {no intializer with value e.g. function argument} continue traversing the graph
                   */

                  //                  CrymlinTraversal<Vertex, Path> eogPathTraversal =
                  //                      crymlin
                  //                          .byID((long) variableDeclarationVertex.id())
                  //                          .repeat(out("EOG").simplePath())
                  //                          .until(hasId(v.id()))
                  //                          .path();

                  log.warn("Starting vertex: {}", v.id());
                  log.warn(
                      "Previous + 0 EOG vertex: {}",
                      crymlin.byID((long) v.id()).in("EOG").toList());
                  log.warn(
                      "Previous + 1 EOG vertex: {}",
                      crymlin.byID((long) v.id()).in("EOG").in("EOG").toList());
                  log.warn(
                      "Previous + 2 EOG vertex: {}",
                      crymlin.byID((long) v.id()).in("EOG").in("EOG").in("EOG").toList());
                  log.warn(
                      "Previous + 3 EOG vertex: {}",
                      crymlin.byID((long) v.id()).in("EOG").in("EOG").in("EOG").in("EOG").toList());
                  log.warn(
                      "Previous + 4 EOG vertex: {}",
                      crymlin
                          .byID((long) v.id())
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .toList());
                  log.warn(
                      "Previous + 5 EOG vertex: {}",
                      crymlin
                          .byID((long) v.id())
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .toList());
                  log.warn(
                      "Previous + 6 EOG vertex: {}",
                      crymlin
                          .byID((long) v.id())
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .toList());
                  log.warn(
                      "Previous + 7 EOG vertex: {}",
                      crymlin
                          .byID((long) v.id())
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .toList());
                  log.warn(
                      "Previous + 8 EOG vertex: {}",
                      crymlin
                          .byID((long) v.id())
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .toList());
                  log.warn(
                      "Previous + 9 EOG vertex: {}",
                      crymlin
                          .byID((long) v.id())
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .in("EOG")
                          .toList());

                  CrymlinTraversal<Vertex, Vertex> pathTraversal =
                      crymlin
                          .byID((long) v.id())
                          .emit()
                          .repeat(in("EOG"))
                          .until(hasLabel("FunctionDeclaration"));
                  CrymlinTraversal<Vertex, Vertex> ptClone =
                      (CrymlinTraversal<Vertex, Vertex>) pathTraversal.clone();
                  while (pathTraversal.hasNext()) {
                    log.warn("traversed vertex : {}", pathTraversal.next());
                  }

                  dumpPaths(ptClone.path().toList());

                  CrymlinTraversal<Vertex, Path> ptc1 =
                      crymlin
                          .byID((long) v.id())
                          .repeat(in("EOG"))
                          .until(hasLabel("FunctionDeclaration"))
                          .emit()
                          .path();
                  dumpPaths(ptc1.toList());

                  CrymlinTraversal<Vertex, Path> ptc2 =
                      crymlin
                          .byID((long) v.id())
                          .repeat(in("EOG"))
                          .until(hasLabel("FunctionDeclaration"))
                          .path();
                  dumpPaths(ptc2.toList());

                  CrymlinTraversal<Vertex, Path> ptc3 =
                      crymlin
                          .byID((long) v.id())
                          .emit()
                          .repeat(in("EOG").unfold())
                          .until(hasLabel("FunctionDeclaration"))
                          .path();
                  dumpPaths(ptc3.toList());

                  log.warn("Workaround?");

                  CrymlinTraversal<Vertex, Vertex> ptw =
                      crymlin
                          .byID((long) v.id())
                          .emit()
                          .repeat(in("EOG"))
                          .until(hasLabel("FunctionDeclaration"));
                  CrymlinTraversal<Vertex, Path> ptw_cp =
                      ((CrymlinTraversal<Vertex, Vertex>) ptw.clone()).path();

                  dumpVertices(ptw.toList());
                  dumpPaths(ptw_cp.toList());

                  log.warn("All in one?");
                  dumpPaths(
                      crymlin
                          .byID((long) v.id())
                          .emit()
                          .repeat(in("EOG"))
                          .until(hasLabel("FunctionDeclaration"))
                          .path()
                          .toList());

                  //                  Path path =
                  //                      crymlin
                  //                          .byID((long) v.id())
                  //                          .emit()
                  //                          .repeat(in("EOG").simplePath())
                  //                          .path()
                  //                          .next();
                  //                  log.warn("Path {}", path);
                  //                  List<Path> eogPaths = eogPathTraversal.toList();
                  //                  dumpPaths(eogPaths);

                  /*
                   * real code
                   */
                  log.warn("Vertex for function call: {}", v);
                  log.warn("Vertex of variable declaration: {}", variableDeclarationVertex);

                  // traverse in reverse along EOG edges from v until variableDeclarationVertex -->
                  // one of them must have more information on the value of the operand
                  CrymlinTraversal<Vertex, Vertex> traversal =
                      crymlin
                          .byID((long) v.id())
                          .repeat(in("EOG"))
                          .until(is(variableDeclarationVertex))
                          .emit();
                  dumpVertices(traversal.clone().toList());

                  while (traversal.hasNext()) {
                    Vertex tVertex = traversal.next();

                    boolean isBinaryOperatorVertex =
                        Arrays.stream(tVertex.label().split(Neo4JVertex.LabelDelimiter))
                            .anyMatch("BinaryOperator"::equals);

                    if (isBinaryOperatorVertex
                        && "=".equals(tVertex.property("operatorCode").value())) {
                      // this is an assignment that may set the value of our operand
                      Vertex lhs = tVertex.vertices(Direction.OUT, "LHS").next();

                      if (lhs.vertices(Direction.OUT, "REFERS_TO")
                          .next()
                          .equals(variableDeclarationVertex)) {
                        Vertex rhs = tVertex.vertices(Direction.OUT, "RHS").next();

                        boolean isRhsLiteral =
                            Arrays.stream(rhs.label().split(Neo4JVertex.LabelDelimiter))
                                .anyMatch("Literal"::equals);

                        if (isRhsLiteral) {
                          Object literalValue = rhs.property("value").value();
                          Class literalValueClass = literalValue.getClass();

                          if (literalValueClass.equals(Long.class)
                              || literalValueClass.equals(Integer.class)) {
                            return Optional.of(((Number) literalValue).intValue());
                          }

                          if (literalValueClass.equals(Double.class)
                              || literalValueClass.equals(Float.class)) {
                            return Optional.of(((Number) literalValue).floatValue());
                          }

                          if (literalValueClass.equals(Boolean.class)) {
                            return Optional.of((Boolean) literalValue);
                          }

                          if (literalValueClass.equals(String.class)) {
                            // character and string literals both have value of type String
                            String valueString = (String) literalValue;

                            // FIXME incomplete hack; only works for primitive char type; is that
                            // enough?
                            if ("char".equals(attributeType)
                                || "char"
                                    .equals(variableDeclarationVertex.property("type").value())) {
                              // FIXME this will likely break on an empty string
                              return Optional.of(valueString.charAt(0));
                            }
                            return Optional.of(valueString);
                          }
                          log.error(
                              "Unknown literal type encountered: {} (value: {})",
                              literalValue.getClass(),
                              literalValue);
                        }

                        // TODO properly resolve rhs expression

                        log.warn("Value of operand set in assignment expression");
                        break;
                      }
                    }
                  }

                  // we arrived at the declaration of the variable used as an argument
                  log.warn("Checking declaration for a literal initializer");

                  // check if we have an initializer with a literal
                  Iterator<Vertex> itInitializerVertex =
                      variableDeclarationVertex.vertices(Direction.OUT, "INITIALIZER");
                  while (itInitializerVertex.hasNext()) {
                    // there should be at most one
                    Vertex initializerVertex = itInitializerVertex.next();

                    if (Arrays.asList(initializerVertex.label().split(Neo4JVertex.LabelDelimiter))
                        .contains("Literal")) {
                      Object literalValue = initializerVertex.property("value").value();
                      Class literalValueClass = literalValue.getClass();

                      if (literalValueClass.equals(Long.class)
                          || literalValueClass.equals(Integer.class)) {
                        return Optional.of(((Number) literalValue).intValue());
                      }

                      if (literalValueClass.equals(Double.class)
                          || literalValueClass.equals(Float.class)) {
                        return Optional.of(((Number) literalValue).floatValue());
                      }

                      if (literalValueClass.equals(Boolean.class)) {
                        return Optional.of((Boolean) literalValue);
                      }

                      if (literalValueClass.equals(String.class)) {
                        // character and string literals both have value of type String
                        String valueString = (String) literalValue;

                        // FIXME incomplete hack; only works for primitive char type; is that
                        // enough?
                        if ("char".equals(attributeType)
                            || "char".equals(variableDeclarationVertex.property("type").value())) {
                          // FIXME this will likely break on an empty string
                          return Optional.of(valueString.charAt(0));
                        }
                        return Optional.of(valueString);
                      }
                      log.error(
                          "Unknown literal type encountered: {} (value: {})",
                          literalValue.getClass(),
                          literalValue);
                    }
                  }
                }
              }
            }
          }

          // TODO at this point I know what I need to look for: either assignments or function calls

        }
      }
    }

    /* FIXME need to differentiate between entity reference and actual code references to Botan/BouncyCastle
     * Currently, I just get some text that can be anything -- entity references, classes/variables/
     * types from Java or C++
     */

    return Optional.empty();
  }

  private void dump(
      List<Pair<MOp, Set<OpStatement>>> usesAsVar,
      List<Pair<MOp, Set<OpStatement>>> usesAsFunctionArg) {
    Set<Pair<MOp, Set<OpStatement>>> uses = new HashSet<>();
    uses.addAll(usesAsVar);
    uses.addAll(usesAsFunctionArg);

    for (Pair<MOp, Set<OpStatement>> p : uses) {
      log.warn("Number of uses in {}: {}", p.getValue0(), p.getValue1().size());
      for (OpStatement os : p.getValue1()) {
        log.warn("OpStatment: {}", os);
      }
    }

    for (Pair<MOp, Set<OpStatement>> pair : uses) {
      MOp mop = pair.getValue0();
      Set<OpStatement> opstmts = pair.getValue1();

      for (OpStatement stmt : opstmts) {
        Set<Vertex> vertices = mop.getVertices(stmt);

        vertices.forEach((v) -> log.warn("Operand used in OpStatement with vertex: {}", v));
      }
    }
  }

  private void dumpVertices(Collection<Vertex> vertices) {
    log.warn("Dumping vertices: {}", vertices.size());

    int i = 0;
    for (Vertex v : vertices) {
      log.warn("Vertex {}: {}", i++, v);
    }
  }

  private void dumpPaths(Collection<Path> paths) {
    log.warn("Number of paths: {}", paths.size());

    for (Path p : paths) {
      log.warn("Path of length: {}", p.size());
      for (Object o : p) {
        log.warn("Path step: {}", o);
      }
    }
  }
}
