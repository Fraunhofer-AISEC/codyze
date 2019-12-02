
package de.fraunhofer.aisec.analysis.markevaluation;

import de.fraunhofer.aisec.analysis.scp.ConstantValue;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.CPGVertexWithValue;
import de.fraunhofer.aisec.analysis.structures.MarkContext;
import de.fraunhofer.aisec.analysis.structures.MarkContextHolder;
import de.fraunhofer.aisec.analysis.structures.ResultWithContext;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.analysis.utils.Utils;
import de.fraunhofer.aisec.crymlin.CrymlinQueryWrapper;
import de.fraunhofer.aisec.crymlin.builtin.Builtin;
import de.fraunhofer.aisec.crymlin.builtin.BuiltinRegistry;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import de.fraunhofer.aisec.mark.markDsl.Argument;
import de.fraunhofer.aisec.mark.markDsl.BooleanLiteral;
import de.fraunhofer.aisec.mark.markDsl.ComparisonExpression;
import de.fraunhofer.aisec.mark.markDsl.Expression;
import de.fraunhofer.aisec.mark.markDsl.FunctionCallExpression;
import de.fraunhofer.aisec.mark.markDsl.IntegerLiteral;
import de.fraunhofer.aisec.mark.markDsl.Literal;
import de.fraunhofer.aisec.mark.markDsl.LiteralListExpression;
import de.fraunhofer.aisec.mark.markDsl.LogicalAndExpression;
import de.fraunhofer.aisec.mark.markDsl.LogicalOrExpression;
import de.fraunhofer.aisec.mark.markDsl.MultiplicationExpression;
import de.fraunhofer.aisec.mark.markDsl.Operand;
import de.fraunhofer.aisec.mark.markDsl.OrderExpression;
import de.fraunhofer.aisec.mark.markDsl.StringLiteral;
import de.fraunhofer.aisec.mark.markDsl.UnaryExpression;
import de.fraunhofer.aisec.markmodel.MRule;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.python.antlr.base.expr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public class ExpressionEvaluator {

	private static final Logger log = LoggerFactory.getLogger(ExpressionEvaluator.class);

	// the mark rule this evaluation is called in
	private final MRule markRule;
	// configuration for the evaluation. E.g., used for WPDS vs. NFA for order evaluation
	private final ServerConfiguration config;
	// connection into the DB
	private final CrymlinTraversalSource traversal;
	// the resulting analysis context
	private final AnalysisContext resultCtx;
	private final MarkContextHolder markContext;

	public ExpressionEvaluator(MRule rule, AnalysisContext resultCtx, ServerConfiguration config, CrymlinTraversalSource traversal, MarkContextHolder context) {
		this.markRule = rule;
		this.resultCtx = resultCtx;
		this.config = config;
		this.traversal = traversal;
		this.markContext = context;
	}

	public MarkContextHolder getMarkContext() {
		return markContext;
	}

	/**
	 * Checks MARK expression against the CPG using the given instance and markvar assignments
	 *
	 * the value of the result is true/false if the expression is true/false null if the expression could not be evaluated (i.e., an error in the mark rule or the
	 * evaluation)
	 *
	 * @param expr The MARK expression to evaluate.
	 * @return one result (value and context)
	 */

	public Map<Integer, Object> evaluate(Expression expr) {
		log.debug("Evaluating top level expression: {}", ExpressionHelper.exprToString(expr));

		Map<Integer, Object> result = evaluateExpression(expr);
		return result;
	}

	/**
	 * Evaluates one expression and returns the result
	 * 
	 * @return
	 */
	public Map<Integer, Object> evaluateExpression(Expression expr) {
		// from lowest to highest operator precedence
		log.debug("evaluating {}: {}", expr.getClass().getSimpleName(), ExpressionHelper.exprToString(expr));

		if (expr instanceof OrderExpression) {
			return evaluateOrderExpression((OrderExpression) expr);
		} else if (expr instanceof LogicalOrExpression) {
			return evaluateLogicalExpr(expr);
		} else if (expr instanceof LogicalAndExpression) {
			return evaluateLogicalExpr(expr);
		} else if (expr instanceof ComparisonExpression) {
			return evaluateLogicalExpr(expr);
		} else if (expr instanceof MultiplicationExpression) {
			return evaluateMultiplicationExpr((MultiplicationExpression) expr);
		} else if (expr instanceof UnaryExpression) {
			return evaluateUnaryExpr((UnaryExpression) expr);
		} else if (expr instanceof Literal) {
			return evaluateLiteral((Literal) expr);
		} else if (expr instanceof Operand) {
			return evaluateOperand((Operand) expr);
		} else if (expr instanceof FunctionCallExpression) {
			return evaluateBuiltin((FunctionCallExpression) expr);
		} else if (expr instanceof LiteralListExpression) {
			Map<Integer, Object> literalList = new HashMap<>();
			for (Literal l : ((LiteralListExpression) expr).getValues()) {
				Map<Integer, Object> res = evaluateLiteral(l);
				for (Map.Entry<Integer, Object> entry : res.entrySet()) {
					List<Object> objects = (List<Object>) literalList.computeIfAbsent(entry.getKey(), x -> new ArrayList<>());
					objects.add(entry.getValue());
				}
			}
			return literalList;
		}

		throw new ExpressionEvaluationException("unknown expression: " + ExpressionHelper.exprToString(expr));
	}

	private Map<Integer, Object> evaluateOrderExpression(OrderExpression orderExpression) {
		log.debug("Evaluating order expression: {}", ExpressionHelper.exprToString(orderExpression));
		Map<Integer, Object> result = new HashMap<>();
		for (Map.Entry<Integer, MarkContext> entry : markContext.getAllContexts().entrySet()) {
			OrderEvaluator orderEvaluator = new OrderEvaluator(this.markRule, this.config);
			ResultWithContext evaluate = orderEvaluator.evaluate(orderExpression, entry.getValue().getInstanceContext(), this.resultCtx, this.traversal);

			if (evaluate != null) {
				ConstantValue cv = ConstantValue.of(evaluate.get());
				cv.addResponsibleVertices(evaluate.getResponsibleVertices()); // todo move to evaluation?
				entry.getValue().setFindingAlreadyAdded(evaluate.isFindingAlreadyAdded());
				result.put(entry.getKey(), cv);
			} else {
				result.put(entry.getKey(), ConstantValue.NULL);
			}
		}
		return result;
	}

	private Map<Integer, Object> evaluateLogicalExpr(Expression expr) {
		log.debug("Evaluating logical expression: {}", ExpressionHelper.exprToString(expr));

		Expression leftExp;
		Expression rightExp;

		if (expr instanceof ComparisonExpression) {
			return evaluateComparisonExpr((ComparisonExpression) expr);
		} else if (expr instanceof LogicalAndExpression) {
			LogicalAndExpression lae = (LogicalAndExpression) expr;

			leftExp = lae.getLeft();
			rightExp = lae.getRight();
		} else if (expr instanceof LogicalOrExpression) {
			LogicalOrExpression loe = (LogicalOrExpression) expr;

			leftExp = loe.getLeft();
			rightExp = loe.getRight();
		} else {
			throw new ExpressionEvaluationException("Unknown logical expression " + expr.getClass().getSimpleName());
		}

		Map<Integer, Object> leftResult = evaluateExpression(leftExp);
		Map<Integer, Object> rightResult = evaluateExpression(rightExp);

		Map<Integer, Object> combinedResult = new HashMap<>();

		for (Integer key : rightResult.keySet()) {
			// we only need to look at the keys from the right side.
			// the right side of the evaluation can add new values, then we have more values on the right than on the left.
			// the right side currently cannot remove values!
			ConstantValue leftBoxed = (ConstantValue) getcorrespondingLeftResult(leftResult, key);
			ConstantValue rightBoxed = (ConstantValue) rightResult.get(key);

			Object left = leftBoxed.getValue();
			Object right = rightBoxed.getValue();

			boolean leftIsNull = leftBoxed.equals(ConstantValue.NULL);
			boolean rightIsNull = rightBoxed.equals(ConstantValue.NULL);

			if (leftIsNull && rightIsNull) {
				// null & null = null, null | null = null

				combinedResult.put(key, ConstantValue.NULL);

			} else if (!(leftIsNull || left.getClass().equals(Boolean.class))
					||
					!(rightIsNull || right.getClass().equals(Boolean.class))) {

				log.warn("At least one subexpression is not of type Boolean: {} vs {}",
					ExpressionHelper.exprToString(leftExp),
					ExpressionHelper.exprToString(rightExp));
				combinedResult.put(key, ConstantValue.NULL);

			} else if (expr instanceof LogicalAndExpression) {
				if (leftIsNull || rightIsNull) {
					// null & true = null
					// null & false = false
					if ((!rightIsNull && right.equals(false))
							||
							(!leftIsNull && left.equals(false))) {
						combinedResult.put(key, ConstantValue.of(false));
					} else {
						combinedResult.put(key, ConstantValue.NULL);
					}
				} else if (left.getClass().equals(Boolean.class)
						&&
						right.getClass().equals(Boolean.class)) {

					ConstantValue cv = ConstantValue.of(Boolean.logicalAnd((Boolean) left, (Boolean) right));
					cv.addResponsibleVerticesFrom(leftBoxed, rightBoxed);
					combinedResult.put(key, cv);
				}

			} else { // LogicalOrExpression
				if (leftIsNull || rightIsNull) {
					// null | true = true
					// null | false = null
					if ((!rightIsNull && right.equals(true))
							||
							(!leftIsNull && left.equals(true))) {
						combinedResult.put(key, ConstantValue.of(true));
					} else {
						combinedResult.put(key, ConstantValue.NULL);
					}
				} else if (left.getClass().equals(Boolean.class)
						&&
						right.getClass().equals(Boolean.class)) {

					ConstantValue cv = ConstantValue.of(Boolean.logicalOr((Boolean) left, (Boolean) right));
					cv.addResponsibleVerticesFrom(leftBoxed, rightBoxed);
					combinedResult.put(key, cv);
				}
			}
		}

		return combinedResult;
	}

	private Map<Integer, Object> evaluateComparisonExpr(ComparisonExpression expr) {
		String op = expr.getOp();
		Expression leftExpr = expr.getLeft();
		Expression rightExpr = expr.getRight();

		log.debug(
			"comparing expression {} with expression {}",
			ExpressionHelper.exprToString(leftExpr),
			ExpressionHelper.exprToString(rightExpr));

		Map<Integer, Object> leftResult = evaluateExpression(leftExpr);
		Map<Integer, Object> rightResult = evaluateExpression(rightExpr);

		Map<Integer, Object> combinedResult = new HashMap<>();

		for (Integer key : rightResult.keySet()) {
			ExpressionComparator<String> comp = new ExpressionComparator<>();

			// we only need to look at the keys from the right side.
			// the right side of the evaluation can add new values, then we have more values on the right than on the left.
			// the right side currently cannot remove values!
			ConstantValue leftBoxed = (ConstantValue) getcorrespondingLeftResult(leftResult, key);
			Object left = leftBoxed.getValue();

			if (rightResult.get(key) instanceof List) {

				if (op.equals("in")) {
					List l = (List) rightResult.get(key);
					ConstantValue cv = ConstantValue.of(false);

					for (Object o : l) {
						log.debug(
							"Comparing left expression with element of right expression: {} vs. {}",
							left,
							o);

						if (o != null) {
							String inner = ExpressionHelper.toComparableString(o);
							if (comp.compare(ExpressionHelper.toComparableString(left), inner) == 0) {
								cv = ConstantValue.of(true);
								cv.addResponsibleVerticesFrom((ConstantValue) o);
								break;
							}
						}
					}
					cv.addResponsibleVerticesFrom(leftBoxed);
					combinedResult.put(key, cv);
				} else {
					log.warn("Unknown op for List on the right side");
					combinedResult.put(key, ConstantValue.NULL);
				}

			} else {

				ConstantValue rightBoxed = (ConstantValue) rightResult.get(key);
				Object right = rightBoxed.getValue();

				if (leftBoxed.equals(ConstantValue.NULL)
						|| rightBoxed.equals(ConstantValue.NULL)) {

					// result of comparison is not known
					combinedResult.put(key, ConstantValue.NULL);
				} else {

					String leftComp = ExpressionHelper.toComparableString(left);
					String rightComp = ExpressionHelper.toComparableString(right);

					log.debug("left result={} right result={}", left, right);

					// TODO implement remaining operations -> @FW: which operations are supported?
					ConstantValue cv;
					switch (op) {
						case "==":
							cv = ConstantValue.of(comp.compare(leftComp, rightComp) == 0);
							cv.addResponsibleVerticesFrom(leftBoxed, rightBoxed);
							break;
						case "!=":
							cv = ConstantValue.of(comp.compare(leftComp, rightComp) != 0);
							cv.addResponsibleVerticesFrom(leftBoxed, rightBoxed);
							break;
						case "<":
							cv = ConstantValue.of(comp.compare(leftComp, rightComp) < 0);
							cv.addResponsibleVerticesFrom(leftBoxed, rightBoxed);
							break;
						case "<=":
							cv = ConstantValue.of(comp.compare(leftComp, rightComp) <= 0);
							cv.addResponsibleVerticesFrom(leftBoxed, rightBoxed);
							break;
						case ">":
							cv = ConstantValue.of(comp.compare(leftComp, rightComp) > 0);
							cv.addResponsibleVerticesFrom(leftBoxed, rightBoxed);
							break;
						case ">=":
							cv = ConstantValue.of(comp.compare(leftComp, rightComp) >= 0);
							cv.addResponsibleVerticesFrom(leftBoxed, rightBoxed);
							break;

						case "like":
							cv = ConstantValue.of(
								Pattern.matches(ExpressionHelper.toComparableString(right), ExpressionHelper.toComparableString(left)));
							cv.addResponsibleVerticesFrom(leftBoxed, rightBoxed);
							break;
						default:
							log.warn("Unsupported operand {}", op);
							cv = ConstantValue.NULL;
					}
					combinedResult.put(key, cv);
				}
			}
		}
		return combinedResult;
	}

	private Object getcorrespondingLeftResult(Map<Integer, Object> leftResult, Integer key) {
		if (leftResult == null || key == null) {
			return ConstantValue.NULL;
		}
		if (leftResult.containsKey(key)) {
			return leftResult.get(key);
		}
		List<Integer> copyStack = markContext.getCopyStack(key);
		if (copyStack != null && !copyStack.isEmpty()) {
			for (int i = copyStack.size() - 1; i >= 0; i--) {
				int newKey = copyStack.get(i);
				if (leftResult.containsKey(newKey)) {
					return leftResult.get(newKey);
				}
			}
		}

		return ConstantValue.NULL;
	}

	/**
	 * Returns evaluated argument values of a Builtin-call.
	 *
	 * A Builtin function "myFunction" may accept 3 arguments: "myFunction(a,b,c)". Each argument may be given in form of an Expression, e.g. "myFunction(0==1, cm.init(),
	 * 42)".
	 *
	 * This method evaluates the Expressions of all arguments and return them as a list contained in the ResultWithContext
	 *
	 * @param argList the list of arguments to evaluate
	 * @return one result with a list of Objects as results for each argument
	 */

	public Map<Integer, Object> evaluateArgs(List<Argument> argList) {
		Map<Integer, Object> result = new HashMap<>();
		for (Argument arg : argList) {
			Map<Integer, Object> r = evaluateExpression((Expression) arg);
			for (Map.Entry<Integer, Object> entry : r.entrySet()) {
				ArrayList<Object> o = (ArrayList<Object>) result.computeIfAbsent(entry.getKey(), x -> new ArrayList<Object>());
				o.add(entry.getValue());
			}
		}
		return result;
	}

	/**
	 * Evaluate built-in functions.
	 *
	 * @param expr the expression for the builtin
	 * @return the result of the built-in call
	 */
	private Map<Integer, Object> evaluateBuiltin(FunctionCallExpression expr) {
		String functionName = expr.getName();

		// Call built-in function (if available)
		Optional<Builtin> builtin = BuiltinRegistry.getInstance().getRegisteredBuiltins().stream().filter(b -> b.getName().equals(functionName)).findFirst();

		if (builtin.isPresent()) {
			Map<Integer, Object> arguments = evaluateArgs(expr.getArgs());
			return builtin.get().execute(arguments, this);
		}

		throw new ExpressionEvaluationException("Unsupported function " + functionName);
	}

	private Map<Integer, Object> evaluateLiteral(Literal literal) {
		String v = literal.getValue();

		Object value;

		// ordering based on Mark grammar
		if (literal instanceof IntegerLiteral) {
			log.debug("Literal is Integer: {}", v);

			try {
				if (v.startsWith("0x")) {
					value = ConstantValue.of(Integer.parseInt(v.substring(2), 16));
				}
				value = ConstantValue.of(Long.parseLong(v));
			}
			catch (NumberFormatException nfe) {
				log.warn("Unable to convert integer literal {}", v, nfe);
				value = ConstantValue.NULL;
			}
		} else if (literal instanceof BooleanLiteral) {
			log.debug("Literal is Boolean: {}", v);
			value = ConstantValue.of(Boolean.parseBoolean(v));
		} else if (literal instanceof StringLiteral) {
			log.debug("Literal is String: {}", v);
			value = ConstantValue.of(Utils.stripQuotedString(v));
		} else {
			log.warn("Unknown literal encountered: {}", v);
			value = ConstantValue.NULL;
		}

		Map<Integer, Object> ret = new HashMap<>();
		for (Integer key : markContext.getAllContexts().keySet()) {
			ret.put(key, value);
		}
		return ret;
	}

	private Map<Integer, Object> evaluateMultiplicationExpr(MultiplicationExpression expr) {
		log.debug("Evaluating multiplication expression: {}", ExpressionHelper.exprToString(expr));

		String op = expr.getOp();

		Map<Integer, Object> leftResult = evaluateExpression(expr.getLeft());
		Map<Integer, Object> rightResult = evaluateExpression(expr.getRight());

		Map<Integer, Object> combinedResult = new HashMap<>();

		for (Integer key : rightResult.keySet()) {
			// we only need to look at the keys from the right side.
			// the right side of the evaluation can add new values, then we have more values on the right than on the left.
			// the right side currently cannot remove values!
			ConstantValue leftBoxed = (ConstantValue) getcorrespondingLeftResult(leftResult, key);
			ConstantValue rightBoxed = (ConstantValue) rightResult.get(key);

			Object left = leftBoxed.getValue();
			Object right = rightBoxed.getValue();

			if (leftBoxed.equals(ConstantValue.NULL)
					|| rightBoxed.equals(ConstantValue.NULL)) {

				// result of expr is not known
				combinedResult.put(key, ConstantValue.NULL);
			} else {

				Class leftResultType = left.getClass();
				Class rightResultType = right.getClass();

				if (!leftResultType.equals(rightResultType)) {
					log.warn("Type of left expression does not match type of right expression: {} vs {}",
						leftResultType.getSimpleName(),
						rightResultType.getSimpleName());
					combinedResult.put(key, ConstantValue.NULL);
				}

				Object unboxedResult;

				switch (op) {
					case "*":
						// FIXME check if an overflow occurs (Math.multiplyExact). But what to do if this overflows?
						if (leftResultType.equals(Integer.class)) {
							unboxedResult = ((Integer) left * (Integer) right);
						} else if (leftResultType.equals(Float.class)) {
							unboxedResult = ((Float) left * (Float) right);
						} else {
							log.warn("Multiplication operator multiplication ('*') not supported for type: {}", leftResultType.getSimpleName());
							unboxedResult = ConstantValue.NULL;
						}
						break;
					case "/":
						if (leftResultType.equals(Integer.class)) {
							unboxedResult = ((Integer) left / (Integer) right);
						} else if (leftResultType.equals(Float.class)) {
							unboxedResult = ((Float) left / (Float) right);
						} else {
							log.warn("Multiplication operator division ('/') not supported for type: {}", leftResultType.getSimpleName());
							unboxedResult = ConstantValue.NULL;
						}
						break;
					case "%":
						if (leftResultType.equals(Integer.class)) {
							unboxedResult = ((Integer) left % (Integer) right);
						} else {
							log.warn("Multiplication operator remainder ('%') not supported for type: {}", leftResultType.getSimpleName());
							unboxedResult = ConstantValue.NULL;
						}
						break;
					case "<<":
						if (leftResultType.equals(Integer.class)) {
							if (((Integer) right) >= 0) {
								unboxedResult = ((Integer) left << (Integer) right);
							} else {
								log.warn("Left shift operator supports only non-negative integers as its right operand");
								unboxedResult = ConstantValue.NULL;
							}
						} else {
							log.warn("Multiplication operator left shift ('<<') not supported for type: {}", leftResultType.getSimpleName());
							unboxedResult = ConstantValue.NULL;
						}
						break;
					case ">>":
						if (leftResultType.equals(Integer.class)) {
							if (((Integer) right) >= 0) {
								unboxedResult = ((Integer) left >> (Integer) right);
							} else {
								log.warn("Right shift operator supports only non-negative integers as its right operand");
								unboxedResult = ConstantValue.NULL;
							}
						} else {
							log.warn("Multiplication operator right shift ('>>') not supported for type: {}", leftResultType.getSimpleName());
							unboxedResult = ConstantValue.NULL;
						}
						break;
					case "&":
						if (leftResultType.equals(Integer.class)) {
							unboxedResult = ((Integer) left & (Integer) right);
						} else {
							log.warn("Addition operator bitwise and ('&') not supported for type: {}", leftResultType.getSimpleName());
							unboxedResult = ConstantValue.NULL;
						}
						break;
					case "&^":
						if (leftResultType.equals(Integer.class)) {
							unboxedResult = ((Integer) left & ~(Integer) right);
						} else {
							log.warn("Addition operator bitwise or ('|') not supported for type: {}", leftResultType.getSimpleName());
							unboxedResult = ConstantValue.NULL;
						}
						break;
					default:
						log.error("Unsupported expression {}", op);
						unboxedResult = ConstantValue.NULL;
				}
				ConstantValue cv = ConstantValue.of(unboxedResult);
				cv.addResponsibleVerticesFrom(leftBoxed, rightBoxed);
				combinedResult.put(key, cv);
			}
		}
		return combinedResult;
	}

	private Map<Integer, Object> evaluateUnaryExpr(UnaryExpression expr) {
		log.debug("Evaluating unary expression: {}", ExpressionHelper.exprToString(expr));

		String op = expr.getOp();

		Map<Integer, Object> subExprResult = evaluateExpression(expr.getExp()); // evaluate the subexpression

		for (Map.Entry<Integer, Object> entry : subExprResult.entrySet()) {

			ConstantValue valueBoxed = (ConstantValue) entry.getValue();
			Object value = valueBoxed.getValue();
			Class subExprResultType = value.getClass();

			Object unboxedResult;

			switch (op) {
				case "+":
					if (subExprResultType.equals(Integer.class) || subExprResultType.equals(Float.class)) {
						continue; // do not change anything
					} else {
						log.warn("Unary operator plus sign ('+') not supported for type: {}", subExprResultType.getSimpleName());
						unboxedResult = ConstantValue.NULL;
					}
					break;
				case "-":
					if (subExprResultType.equals(Integer.class)) {
						unboxedResult = -((Integer) value);
					} else if (subExprResultType.equals(Float.class)) {
						unboxedResult = -((Float) value);
					} else {
						log.warn("Unary operator minus sign ('-') not supported for type: {}", subExprResultType.getSimpleName());
						unboxedResult = ConstantValue.NULL;
					}
					break;
				case "!":
					if (subExprResultType.equals(Boolean.class)) {
						unboxedResult = !((Boolean) value);
					} else {
						log.warn("Unary operator logical not ('!') not supported for type: {}", subExprResultType.getSimpleName());
						unboxedResult = ConstantValue.NULL;
					}
					break;
				case "^":
					if (subExprResultType.equals(Integer.class)) {
						unboxedResult = ~((Integer) value);
					} else {
						log.warn("Unary operator bitwise complement ('~') not supported for type: {}", subExprResultType.getSimpleName());
						unboxedResult = ConstantValue.NULL;
					}
					break;
				default:
					log.warn("Trying to evaluate unknown unary expression: " + ExpressionHelper.exprToString(expr));
					unboxedResult = ConstantValue.NULL;
			}
			ConstantValue cv = ConstantValue.of(unboxedResult);
			cv.addResponsibleVerticesFrom(valueBoxed);
			subExprResult.put(entry.getKey(), cv);
		}
		return subExprResult;
	}

	private Map<Integer, Object> evaluateOperand(Operand operand) {

		Map<Integer, Object> resolvedOperand = markContext.getResolvedOperand(operand.getOperand());

		if (resolvedOperand == null) {
			Map<Integer, List<CPGVertexWithValue>> operandVertices = CrymlinQueryWrapper.resolveOperand(markContext, operand.getOperand(), markRule, traversal);
			if (operandVertices.size() == 0) {
				log.warn("Did not find any vertices for {}, following evaluation will be imprecise", operand.getOperand());
			}
			markContext.addResolvedOperands(operand.getOperand(), operandVertices);
			resolvedOperand = markContext.getResolvedOperand(operand.getOperand());
		}

		return resolvedOperand;
	}
}
