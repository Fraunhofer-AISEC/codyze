
package de.fraunhofer.aisec.analysis.markevaluation;

import de.fraunhofer.aisec.analysis.scp.ConstantValue;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.CPGInstanceContext;
import de.fraunhofer.aisec.analysis.structures.CPGVariableContext;
import de.fraunhofer.aisec.analysis.structures.CPGVertexWithValue;
import de.fraunhofer.aisec.analysis.structures.ResultWithContext;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.crymlin.builtin.Builtin;
import de.fraunhofer.aisec.crymlin.builtin.BuiltinRegistry;
import de.fraunhofer.aisec.analysis.utils.Utils;
import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection;
import de.fraunhofer.aisec.mark.markDsl.*;
import de.fraunhofer.aisec.markmodel.MRule;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

public class ExpressionEvaluator {

	private static final Logger log = LoggerFactory.getLogger(ExpressionEvaluator.class);

	private final MRule markRule;
	private final ServerConfiguration config;
	private final TraversalConnection traversal;
	private final AnalysisContext resultCtx;
	private CPGVariableContext variableContext;
	private CPGInstanceContext instanceContext;

	public ExpressionEvaluator(MRule rule, AnalysisContext resultCtx, ServerConfiguration config, TraversalConnection traversal) {
		this.markRule = rule;
		this.resultCtx = resultCtx;
		this.config = config;
		this.traversal = traversal;
	}

	/**
	 * Checks a source file against a MARK expression.
	 *
	 * <p>
	 * This method may return three results:
	 *
	 * <p>
	 * - empty: The expression could not be evaluated. - false: Expression was evaluated but does not match the source file. - true: Expression was evaluated and matches
	 * the source file.
	 *
	 * @param expr The MARK expression to evaluate.
	 * @return
	 */
	public ResultWithContext evaluate(Expression expr) {
		log.debug("Evaluating top level expression: {}", ExpressionHelper.exprToString(expr));

		ResultWithContext result = evaluateExpression(expr);
		if (result != null) {
			result.setVariableContext(variableContext);
			result.setInstanceContext(instanceContext);
		}
		return result;
	}

	private ResultWithContext evaluateOrderExpression(OrderExpression orderExpression) {
		OrderEvaluator orderEvaluator = new OrderEvaluator(this.markRule, this.config);
		return orderEvaluator.evaluate(this.instanceContext, this.resultCtx, this.traversal.getCrymlinTraversal());
	}

	private ResultWithContext evaluateLogicalExpr(Expression expr) {
		log.debug("Evaluating logical expression: {}", ExpressionHelper.exprToString(expr));

		if (expr instanceof ComparisonExpression) {
			return evaluateComparisonExpr((ComparisonExpression) expr);
		} else if (expr instanceof LogicalAndExpression) {
			LogicalAndExpression lae = (LogicalAndExpression) expr;

			Expression left = lae.getLeft();
			Expression right = lae.getRight();

			ResultWithContext leftResult = evaluateExpression(left);
			ResultWithContext rightResult = evaluateExpression(right);

			if (leftResult == null || rightResult == null) {
				log.error("At least one subexpression could not be evaluated");
				return null;
			}

			if (leftResult.get().getClass().equals(Boolean.class)
					&& rightResult.get().getClass().equals(Boolean.class)) {
				return ResultWithContext.fromExisting(
					Boolean.logicalAnd((Boolean) leftResult.get(), (Boolean) rightResult.get()),
					leftResult, rightResult);
			}

			log.error(
				"At least one subexpression is not of type Boolean: {} vs. {}",
				ExpressionHelper.exprToString(left),
				ExpressionHelper.exprToString(right));

			return null;
		} else if (expr instanceof LogicalOrExpression) {
			LogicalOrExpression loe = (LogicalOrExpression) expr;

			Expression left = loe.getLeft();
			Expression right = loe.getRight();

			ResultWithContext leftResult = evaluateExpression(left);
			ResultWithContext rightResult = evaluateExpression(right);

			if (leftResult == null || rightResult == null) {
				log.error("At least one subexpression could not be evaluated");
				return null;
			}

			if (leftResult.get().getClass().equals(Boolean.class)
					&& rightResult.get().getClass().equals(Boolean.class)) {
				return ResultWithContext.fromExisting(
					Boolean.logicalAnd((Boolean) leftResult.get(), (Boolean) rightResult.get()),
					leftResult, rightResult);
			}

			log.error(
				"At least one subexpression is not of type Boolean: {} vs. {}",
				ExpressionHelper.exprToString(left),
				ExpressionHelper.exprToString(right));

			return null;
		}

		log.error(
			"Trying to evaluate unknown logical expression: {}", ExpressionHelper.exprToString(expr));

		assert false; // not a logical expression
		return null;
	}

	private ResultWithContext evaluateComparisonExpr(ComparisonExpression expr) {
		String op = expr.getOp();
		Expression left = expr.getLeft();
		Expression right = expr.getRight();

		log.debug(
			"comparing expression {} with expression {}",
			ExpressionHelper.exprToString(left),
			ExpressionHelper.exprToString(right));

		ResultWithContext leftResult = evaluateExpression(left);
		ResultWithContext rightResult = evaluateExpression(right);

		if (leftResult == null || rightResult == null) {
			return null;
		}

		// fixme should the following 6 statements not be done only when really needed?
		String lString = ExpressionHelper.asString(leftResult);
		String rString = ExpressionHelper.asString(rightResult);

		Number lNumber = ExpressionHelper.asNumber(leftResult);
		Number rNumber = ExpressionHelper.asNumber(rightResult);

		Boolean lBoolean = ExpressionHelper.asBoolean(leftResult);
		Boolean rBoolean = ExpressionHelper.asBoolean(rightResult);

		Class leftType = leftResult.get().getClass();
		Class rightType = rightResult.get().getClass();

		log.debug("left result={} right result={}", leftResult.get(), rightResult.get());

		// TODO implement remaining operations
		switch (op) {
			case "==":
				if (lNumber != null && rNumber != null) {
					return ResultWithContext.fromExisting(lNumber.equals(rNumber), leftResult, rightResult);
				}
				if (lString != null && rString != null) {
					return ResultWithContext.fromExisting(lString.equals(rString), leftResult, rightResult);
				}
				if (lBoolean != null && rBoolean != null) {
					return ResultWithContext.fromExisting(lBoolean.equals(rBoolean), leftResult, rightResult);
				}

				log.error(
					"Type of left expression does not match type of right expression: {} vs. {}",
					leftType.getSimpleName(),
					rightType.getSimpleName());

				return null;
			case "!=":
				if (lNumber != null && rNumber != null) {
					return ResultWithContext.fromExisting(!lNumber.equals(rNumber), leftResult, rightResult);
				}
				if (lString != null && rString != null) {
					return ResultWithContext.fromExisting(!lString.equals(rString), leftResult, rightResult);
				}
				if (lBoolean != null && rBoolean != null) {
					return ResultWithContext.fromExisting(!lBoolean.equals(rBoolean), leftResult, rightResult);
				}

				log.error(
					"Type of left expression does not match type of right expression: {} vs. {}",
					leftType.getSimpleName(),
					rightType.getSimpleName());

				return null;
			case "<":
				if (lNumber != null && rNumber != null) {
					// Note that this is not a precise way to compare, as Number can also be BigDecimal. This will however not be the case here.
					return ResultWithContext.fromExisting(lNumber.floatValue() < rNumber.floatValue(), leftResult, rightResult);
				}

				log.error("Comparison operator less-than ('<') not supported for type: {}", leftType);

				return null;

			case "<=":
				if (lNumber != null && rNumber != null) {
					// Note that this is not a precise way to compare, as Number can also be BigDecimal. This will however not be the case here.
					return ResultWithContext.fromExisting(lNumber.floatValue() <= rNumber.floatValue(), leftResult, rightResult);
				}

				log.error("Comparison operator less-than-or-equal ('<=') not supported for type: {}", leftType);

				return null;

			case ">":
				if (lNumber != null && rNumber != null) {
					// Note that this is not a precise way to compare, as Number can also be BigDecimal. This will however not be the case here.
					return ResultWithContext.fromExisting(lNumber.floatValue() > rNumber.floatValue(), leftResult, rightResult);
				}

				log.error(
					"Type of left expression does not match type of right expression: {} vs. {}",
					leftType.getSimpleName(),
					rightType.getSimpleName());

				return null;
			case ">=":
				if (lNumber != null && rNumber != null) {
					// Note that this is not a precise way to compare, as Number can also be BigDecimal. This will however not be the case here.
					return ResultWithContext.fromExisting(lNumber.floatValue() >= rNumber.floatValue(), leftResult, rightResult);
				}

				log.error(
					"Comparison operator greater-than-or-equal ('>=') not supported for type: {}",
					leftType);

				return null;
			case "in":
				if (rightResult.get() instanceof List) {
					List l = (List) rightResult.get();

					boolean evalValue = false;
					for (Object o : l) {
						log.debug(
							"Comparing left expression with element of right expression: {} vs. {}",
							leftResult.get(),
							o);

						if (o != null) {
							if (lString != null) {
								evalValue |= lString.equals(o);
							} else if (lNumber != null) {
								evalValue |= lNumber.equals(o);
							} else if (lBoolean != null) {
								evalValue |= lBoolean.equals(o);
							}
						}
					}

					return ResultWithContext.fromExisting(evalValue, leftResult, rightResult);
				}

				log.error("Type of right expression must be List; given: {}", rightType);

				return null;
			case "like":
				if (lString != null) {
					return ResultWithContext.fromExisting(Pattern.matches(Pattern.quote((String) rightResult.get()), lString),
						leftResult, rightResult);
				}

				log.error("Comparison operator like ('like') not supported for type: {}", leftType);

				return null;

			default:
				log.error("Unsupported operand {}", op);
		}

		assert false;
		return null;
	}

	/**
	 * Returns evaluated argument values of a Builtin-call.
	 *
	 * <p>
	 * A Builtin function "myFunction" may accept 3 arguments: "myFunction(a,b,c)". Each argument may be given in form of an Expression, e.g. "myFunction(0==1, cm.init(),
	 * 42)".
	 *
	 * <p>
	 * This method evaluates the Expressions of all arguments and return them as a list.
	 *
	 * @param argList
	 * @return
	 */
	public ResultWithContext evaluateArgs(@NonNull List<Argument> argList) {
		List<Object> result = new ArrayList<>();
		List<ResultWithContext> resultWithContexts = new ArrayList<>();
		for (Argument arg : argList) {
			ResultWithContext r = evaluateExpression((Expression) arg);
			result.add(r.get());
			resultWithContexts.add(r);
		}

		return ResultWithContext.fromExisting(result, resultWithContexts.toArray(new ResultWithContext[0]));
	}

	/**
	 * Evaluate built-in functions.
	 *
	 * @param expr
	 * @return
	 */
	private ResultWithContext evaluateBuiltin(FunctionCallExpression expr) {
		String functionName = expr.getName();

		// Call built-in function (if available)
		for (Builtin b : BuiltinRegistry.getInstance().getRegisteredBuiltins()) {
			if (b.getName().equals(functionName)) {
				ResultWithContext arguments = evaluateArgs(expr.getArgs());
				if (arguments == null || !(arguments.get() instanceof List)) {
					return null;
				}
				return b.execute(arguments, this);
			}
		}

		log.error("Unsupported function {}", functionName);
		return null;
	}

	private ResultWithContext evaluateLiteral(Literal literal) {
		String v = literal.getValue();
		log.debug("Literal with value: {}", v);

		// ordering based on Mark grammar
		if (literal instanceof IntegerLiteral) {
			log.debug("Literal is Integer: {}", v);

			try {
				if (v.startsWith("0x")) {
					return ResultWithContext.fromLiteralOrOperand(Integer.parseInt(v.substring(2), 16));
				}
				return ResultWithContext.fromLiteralOrOperand(Integer.parseInt(v));
			}
			catch (NumberFormatException nfe) {
				log.error("Unable to convert integer literal to Integer: {}\n{}", v, nfe);
			}
			return null;
		} else if (literal instanceof BooleanLiteral) {
			log.debug("Literal is Boolean: {}", v);
			return ResultWithContext.fromLiteralOrOperand(Boolean.parseBoolean(v));
		} else if (literal instanceof StringLiteral) {
			log.debug("Literal is String: {}", v);
			return ResultWithContext.fromLiteralOrOperand(Utils.stripQuotedString(v));
		}

		log.error("Unknown literal encountered: {}", v);
		return null;
	}

	/**
	 * Evaluates a non-order MARK expression.
	 *
	 * @param expr
	 * @return
	 */
	public ResultWithContext evaluateExpression(Expression expr) {
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
			List<ResultWithContext> literalResultList = new ArrayList<>();
			List<Object> literalObjectList = new ArrayList<>();
			for (Literal l : ((LiteralListExpression) expr).getValues()) {
				ResultWithContext resultWithContext = evaluateLiteral(l);
				if (resultWithContext != null) {
					literalResultList.add(resultWithContext);
					literalObjectList.add(resultWithContext.get());
				}
			}
			return ResultWithContext.fromExisting(literalObjectList, literalResultList.toArray(new ResultWithContext[0]));
		}

		log.error("unknown expression: {}", ExpressionHelper.exprToString(expr));
		assert false; // all expression types must be handled
		return null;
	}

	private ResultWithContext evaluateMultiplicationExpr(MultiplicationExpression expr) {
		log.debug("Evaluating multiplication expression: {}", ExpressionHelper.exprToString(expr));

		String op = expr.getOp();
		Expression left = expr.getLeft();
		Expression right = expr.getRight();

		ResultWithContext leftResult = evaluateExpression(left);
		ResultWithContext rightResult = evaluateExpression(right);

		if (leftResult == null || rightResult == null) {
			log.error("Unable to evaluate at least one subexpression");
			return null;
		}

		Class leftResultType = leftResult.get().getClass();
		Class rightResultType = rightResult.get().getClass();

		// Consider ConstantValue here instead of native Java types.
		// TODO why is this needed??
		if (leftResultType.equals(ConstantValue.class)) {
			leftResultType = ((ConstantValue) leftResult.get()).getValue().getClass();
			leftResult.set(((ConstantValue) leftResult.get()).getValue());
		}

		if (rightResultType.equals(ConstantValue.class)) {
			rightResultType = ((ConstantValue) rightResult.get()).getValue().getClass();
			rightResult.set(((ConstantValue) rightResult.get()).getValue());
		}

		switch (op) {
			case "*":
				if (leftResultType.equals(rightResultType)) {
					// TODO check if an overflow occurs
					if (leftResultType.equals(Integer.class)) {
						return ResultWithContext.fromExisting(((Integer) leftResult.get()) * ((Integer) rightResult.get()), leftResult, rightResult);
					} else if (leftResultType.equals(Float.class)) {
						return ResultWithContext.fromExisting(((Float) leftResult.get()) * ((Float) rightResult.get()), leftResult, rightResult);
					}

					log.error(
						"Multiplication operator multiplication ('*') not supported for type: {}",
						leftResultType.getSimpleName());
					return null;
				}

				log.error(
					"Type of left expression does not match type of right expression: {} vs. {}",
					leftResultType.getSimpleName(),
					rightResultType.getSimpleName());

				return null;
			case "/":
				if (leftResultType.equals(rightResultType)) {
					if (leftResultType.equals(Integer.class)) {
						return ResultWithContext.fromExisting(((Integer) leftResult.get()) / ((Integer) rightResult.get()), leftResult, rightResult);
					} else if (leftResultType.equals(Float.class)) {
						return ResultWithContext.fromExisting(((Float) leftResult.get()) / ((Float) rightResult.get()), leftResult, rightResult);
					}

					log.error(
						"Multiplication operator division ('/') not supported for type: {}",
						leftResultType.getSimpleName());
					return null;
				}

				log.error(
					"Type of left expression does not match type of right expression: {} vs. {}",
					leftResultType.getSimpleName(),
					rightResultType.getSimpleName());

				return null;
			case "%":
				if (leftResultType.equals(rightResultType)) {
					if (leftResultType.equals(Integer.class)) {
						return ResultWithContext.fromExisting(((Integer) leftResult.get()) % ((Integer) rightResult.get()), leftResult, rightResult);
					}

					log.error(
						"Multiplication operator remainder ('%') not supported for type: {}",
						leftResultType.getSimpleName());
					return null;
				}

				log.error(
					"Type of left expression does not match type of right expression: {} vs. {}",
					leftResultType.getSimpleName(),
					rightResultType.getSimpleName());

				return null;
			case "<<":
				if (leftResultType.equals(rightResultType)) {
					if (leftResultType.equals(Integer.class)) {
						if (((Integer) rightResult.get()) >= 0) {
							return ResultWithContext.fromExisting(((Integer) leftResult.get()) << ((Integer) rightResult.get()), leftResult, rightResult);
						}

						log.error(
							"Left shift operator supports only non-negative integers as its right operand");
						return null;
					}

					log.error(
						"Multiplication operator left shift ('<<') not supported for type: {}",
						leftResultType.getSimpleName());
					return null;
				}

				log.error(
					"Type of left expression does not match type of right expression: {} vs. {}",
					leftResultType.getSimpleName(),
					rightResultType.getSimpleName());

				return null;
			case ">>":
				if (leftResultType.equals(rightResultType)) {
					if (leftResultType.equals(Integer.class)) {
						if (((Integer) rightResult.get()) >= 0) {
							return ResultWithContext.fromExisting(((Integer) leftResult.get()) >> ((Integer) rightResult.get()), leftResult, rightResult);
						}

						log.error(
							"Right shift operator supports only non-negative integers as its right operand");
						return null;
					}

					log.error(
						"Multiplication operator right shift ('>>') not supported for type: {}",
						leftResultType.getSimpleName());
					return null;
				}

				log.error(
					"Type of left expression does not match type of right expression: {} vs. {}",
					leftResultType.getSimpleName(),
					rightResultType.getSimpleName());

				return null;
			case "&":
				if (leftResultType.equals(rightResultType)) {
					if (leftResultType.equals(Integer.class)) {
						return ResultWithContext.fromExisting(((Integer) leftResult.get()) & ((Integer) rightResult.get()), leftResult, rightResult);
					}

					log.error(
						"Addition operator bitwise and ('&') not supported for type: {}",
						leftResultType.getSimpleName());
					return null;
				}

				log.error(
					"Type of left expression does not match type of right expression: {} vs. {}",
					leftResultType.getSimpleName(),
					rightResultType.getSimpleName());

				return null;
			case "&^":
				if (leftResultType.equals(rightResultType)) {
					if (leftResultType.equals(Integer.class)) {
						return ResultWithContext.fromExisting(((Integer) leftResult.get()) & ~((Integer) rightResult.get()), leftResult, rightResult);
					}

					log.error(
						"Addition operator bitwise or ('|') not supported for type: {}",
						leftResultType.getSimpleName());
					return null;
				}

				log.error(
					"Type of left expression does not match type of right expression: {} vs. {}",
					leftResultType.getSimpleName(),
					rightResultType.getSimpleName());

				return null;

			default:
				log.error("Unsupported expression {}", op);
		}

		log.error(
			"Trying to evaluate unknown multiplication expression: {}",
			ExpressionHelper.exprToString(expr));

		assert false; // not an addition expression
		return null;
	}

	// preserves the vertex of the inner expression
	private ResultWithContext evaluateUnaryExpr(UnaryExpression expr) {
		log.debug("Evaluating unary expression: {}", ExpressionHelper.exprToString(expr));

		String op = expr.getOp();
		Expression subExpr = expr.getExp();

		ResultWithContext subExprResult = evaluateExpression(subExpr);

		if (subExprResult == null) {
			log.error("Unable to evaluate subexpression");
			return null;
		}

		Class subExprResultType = subExprResult.get().getClass();

		switch (op) {
			case "+":
				if (subExprResultType.equals(Integer.class) || subExprResultType.equals(Float.class)) {
					return subExprResult;
				}

				log.error(
					"Unary operator plus sign ('+') not supported for type: {}",
					subExprResultType.getSimpleName());

				return null;
			case "-":
				if (subExprResultType.equals(Integer.class)) {
					return ResultWithContext.fromExisting(-((Integer) subExprResult.get()), subExprResult);
				} else if (subExprResultType.equals(Float.class)) {
					return ResultWithContext.fromExisting(-((Float) subExprResult.get()), subExprResult);
				}

				log.error(
					"Unary operator minus sign ('-') not supported for type: {}",
					subExprResultType.getSimpleName());

				return null;
			case "!":
				if (subExprResultType.equals(Boolean.class)) {
					return ResultWithContext.fromExisting(!((Boolean) subExprResult.get()), subExprResult);
				}

				log.error(
					"Unary operator logical not ('!') not supported for type: {}",
					subExprResultType.getSimpleName());

				return null;
			case "^":
				if (subExprResultType.equals(Integer.class)) {
					return ResultWithContext.fromExisting(~((Integer) subExprResult.get()), subExprResult);
				}

				log.error(
					"Unary operator bitwise complement ('~') not supported for type: {}",
					subExprResultType.getSimpleName());

				return null;

			default:
				log.error("Unsupported expresison {}", op);
		}

		log.error(
			"Trying to evaluate unknown unary expression: {}", ExpressionHelper.exprToString(expr));

		assert false; // not an addition expression
		return null;
	}

	private ResultWithContext evaluateOperand(Operand operand) {
		CPGVertexWithValue vertexWithValue = variableContext.get(operand.getOperand());
		if (vertexWithValue == null) {
			log.error("{} does not have a value", operand.getOperand());
			return null;
		}
		ResultWithContext result = ResultWithContext.fromLiteralOrOperand(vertexWithValue.value);
		result.setVertex(vertexWithValue.argumentVertex);
		return result;
	}

	public void setCPGVariableContext(CPGVariableContext varContext) {
		this.variableContext = varContext;
	}

	public void setCPGInstanceContext(CPGInstanceContext instanceContext) {
		this.instanceContext = instanceContext;
	}
}
