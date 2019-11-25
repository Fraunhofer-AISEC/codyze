
package de.fraunhofer.aisec.analysis.markevaluation;

import de.fraunhofer.aisec.analysis.scp.ConstantValue;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.CPGInstanceContext;
import de.fraunhofer.aisec.analysis.structures.CPGVariableContext;
import de.fraunhofer.aisec.analysis.structures.CPGVertexWithValue;
import de.fraunhofer.aisec.analysis.structures.ResultWithContext;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.analysis.utils.Utils;
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
import javassist.expr.Expr;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.python.antlr.base.expr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

	// the current variable context, e.g. the assignment of t.foo to a node/value, cm.rand to a node/value, etc.
	private CPGVariableContext variableContext;
	// the current instance context, e.g. the assignment of t to a node, cm to a node, etc.
	private CPGInstanceContext instanceContext;

	public ExpressionEvaluator(MRule rule, AnalysisContext resultCtx, ServerConfiguration config, CrymlinTraversalSource traversal) {
		this.markRule = rule;
		this.resultCtx = resultCtx;
		this.config = config;
		this.traversal = traversal;
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
	@NonNull
	public ResultWithContext evaluate(@NonNull Expression expr) throws ExpressionEvaluationException {
		log.debug("Evaluating top level expression: {}", ExpressionHelper.exprToString(expr));

		ResultWithContext result = evaluateExpression(expr);
		result.setVariableContext(variableContext);
		result.setInstanceContext(instanceContext);
		return result;
	}

	@NonNull
	private ResultWithContext evaluateOrderExpression(@NonNull OrderExpression orderExpression) {
		log.debug("Evaluating order expression: {}", ExpressionHelper.exprToString(orderExpression));
		OrderEvaluator orderEvaluator = new OrderEvaluator(this.markRule, this.config);
		return orderEvaluator.evaluate(orderExpression, this.instanceContext, this.resultCtx, this.traversal);
	}

	@NonNull
	private ResultWithContext evaluateLogicalExpr(@NonNull Expression expr) throws ExpressionEvaluationException {
		log.debug("Evaluating logical expression: {}", ExpressionHelper.exprToString(expr));

		Expression left;
		Expression right;

		if (expr instanceof ComparisonExpression) {
			return evaluateComparisonExpr((ComparisonExpression) expr);
		} else if (expr instanceof LogicalAndExpression) {
			LogicalAndExpression lae = (LogicalAndExpression) expr;

			left = lae.getLeft();
			right = lae.getRight();
		} else if (expr instanceof LogicalOrExpression) {
			LogicalOrExpression loe = (LogicalOrExpression) expr;

			left = loe.getLeft();
			right = loe.getRight();
		} else {
			throw new ExpressionEvaluationException("Unknown logical expression " + expr.getClass().getSimpleName());
		}

		ResultWithContext leftResult = evaluateExpression(left);
		ResultWithContext rightResult = evaluateExpression(right);

		if (!leftResult.get().getClass().equals(Boolean.class)
				|| !rightResult.get().getClass().equals(Boolean.class)) {

			throw new ExpressionEvaluationException(
				"At least one subexpression is not of type Boolean: " +
						ExpressionHelper.exprToString(left) + " vs " +
						ExpressionHelper.exprToString(right));
		}
		if (expr instanceof LogicalAndExpression) {
			return ResultWithContext.fromExisting(
				Boolean.logicalAnd((Boolean) leftResult.get(), (Boolean) rightResult.get()),
				leftResult, rightResult);
		} else { //LogicalOrExpression
			return ResultWithContext.fromExisting(
				Boolean.logicalOr((Boolean) leftResult.get(), (Boolean) rightResult.get()),
				leftResult, rightResult);
		}

	}

	@NonNull
	private ResultWithContext evaluateComparisonExpr(@NonNull ComparisonExpression expr) throws ExpressionEvaluationException {
		String op = expr.getOp();
		Expression left = expr.getLeft();
		Expression right = expr.getRight();

		log.debug(
			"comparing expression {} with expression {}",
			ExpressionHelper.exprToString(left),
			ExpressionHelper.exprToString(right));

		ResultWithContext leftResult = evaluateExpression(left);
		ResultWithContext rightResult = evaluateExpression(right);

		String lString = ExpressionHelper.asString(leftResult);
		String rString = ExpressionHelper.asString(rightResult);

		Number lNumber = ExpressionHelper.asNumber(leftResult);
		Number rNumber = ExpressionHelper.asNumber(rightResult);

		Boolean lBoolean = ExpressionHelper.asBoolean(leftResult);
		Boolean rBoolean = ExpressionHelper.asBoolean(rightResult);
		String leftComp = ExpressionHelper.toComparableString(leftResult.get());
		String rightComp = ExpressionHelper.toComparableString(rightResult.get());
		ExpressionComparator<String> comp = new ExpressionComparator<>();

		Class leftType = leftResult.get().getClass();
		Class rightType = rightResult.get().getClass();

		log.debug("left result={} right result={}", leftResult.get(), rightResult.get());

		// TODO implement remaining operations -> @FW: which operations are supported?
		switch (op) {
			case "==":
				try {
					return ResultWithContext.fromExisting(comp.compare(leftComp, rightComp) == 0, leftResult, rightResult);
				}
				catch (ExpressionEvaluationException e) {
					return ResultWithContext.fromExisting(false, leftResult, rightResult);
				}
			case "!=":
				try {
					return ResultWithContext.fromExisting(comp.compare(leftComp, rightComp) != 0, leftResult, rightResult);
				}
				catch (ExpressionEvaluationException e) {
					return ResultWithContext.fromExisting(false, leftResult, rightResult);
				}
			case "<":
				try {
					return ResultWithContext.fromExisting(comp.compare(leftComp, rightComp) < 0, leftResult, rightResult);
				}
				catch (ExpressionEvaluationException e) {
					return ResultWithContext.fromExisting(false, leftResult, rightResult);
				}
			case "<=":
				try {
					return ResultWithContext.fromExisting(comp.compare(leftComp, rightComp) <= 0, leftResult, rightResult);
				}
				catch (ExpressionEvaluationException e) {
					return ResultWithContext.fromExisting(false, leftResult, rightResult);
				}
			case ">":
				try {
					return ResultWithContext.fromExisting(comp.compare(leftComp, rightComp) > 0, leftResult, rightResult);
				}
				catch (ExpressionEvaluationException e) {
					return ResultWithContext.fromExisting(false, leftResult, rightResult);
				}
			case ">=":
				try {
					return ResultWithContext.fromExisting(comp.compare(leftComp, rightComp) >= 0, leftResult, rightResult);
				}
				catch (ExpressionEvaluationException e) {
					return ResultWithContext.fromExisting(false, leftResult, rightResult);
				}
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

				throw new ExpressionEvaluationException("Type of right expression must be List; given: " + rightType);
			case "like":
				try {
					return ResultWithContext.fromExisting(
						Pattern.matches(ExpressionHelper.toComparableString(rightResult.get()), ExpressionHelper.toComparableString(leftResult.get())), leftResult,
						rightResult);
				}
				catch (ExpressionEvaluationException e) {
					return ResultWithContext.fromExisting(false, leftResult, rightResult);
				}
				//				if (lString != null) {
				//					return ResultWithContext.fromExisting(Pattern.matches(Pattern.quote((String) rightResult.get()), lString),
				//						leftResult, rightResult);
				//				}
				//
				//				throw new ExpressionEvaluationException("Comparison operator like ('like') not supported for type: " + leftType);

			default:
				log.error("Unsupported operand {}", op);
		}

		throw new ExpressionEvaluationException("Unhandled expression with operand " + op);
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
	@NonNull
	public ResultWithContext evaluateArgs(@NonNull List<Argument> argList) throws ExpressionEvaluationException {
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
	 * @param expr the expression for the builtin
	 * @return the result of the built-in call
	 */
	@NonNull
	private ResultWithContext evaluateBuiltin(@NonNull FunctionCallExpression expr) throws ExpressionEvaluationException {
		String functionName = expr.getName();

		// Call built-in function (if available)
		Optional<Builtin> builtin = BuiltinRegistry.getInstance().getRegisteredBuiltins().stream().filter(b -> b.getName().equals(functionName)).findFirst();

		if (builtin.isPresent()) {
			ResultWithContext arguments = evaluateArgs(expr.getArgs());
			if (!(arguments.get() instanceof List)) {
				throw new ExpressionEvaluationException("Unexpected type. Was not list: " + arguments.get().getClass().getSimpleName());
			}
			return builtin.get().execute(arguments, this);
		}

		throw new ExpressionEvaluationException("Unsupported function " + functionName);
	}

	@NonNull
	private ResultWithContext evaluateLiteral(@NonNull Literal literal) throws ExpressionEvaluationException {
		String v = literal.getValue();

		// ordering based on Mark grammar
		if (literal instanceof IntegerLiteral) {
			log.debug("Literal is Integer: {}", v);

			try {
				if (v.startsWith("0x")) {
					return ResultWithContext.fromLiteralOrOperand(Integer.parseInt(v.substring(2), 16));
				}
				return ResultWithContext.fromLiteralOrOperand(Long.parseLong(v));
			}
			catch (NumberFormatException nfe) {
				throw new ExpressionEvaluationException("Unable to convert integer literal " + v + " to Integer", nfe);
			}
		} else if (literal instanceof BooleanLiteral) {
			log.debug("Literal is Boolean: {}", v);
			return ResultWithContext.fromLiteralOrOperand(Boolean.parseBoolean(v));
		} else if (literal instanceof StringLiteral) {
			log.debug("Literal is String: {}", v);
			return ResultWithContext.fromLiteralOrOperand(Utils.stripQuotedString(v));
		}

		throw new ExpressionEvaluationException("Unknown literal encountered: " + v);
	}

	/**
	 * Evaluates one expression and returns the result
	 */
	@NonNull
	public ResultWithContext evaluateExpression(@Nullable Expression expr) throws ExpressionEvaluationException {
		if (expr == null) {
			throw new ExpressionEvaluationException("null expression");
		}
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
				literalResultList.add(resultWithContext);
				literalObjectList.add(resultWithContext.get());
			}
			return ResultWithContext.fromExisting(literalObjectList, literalResultList.toArray(new ResultWithContext[0]));
		}

		throw new ExpressionEvaluationException("unknown expression: " + ExpressionHelper.exprToString(expr));
	}

	@NonNull
	private ResultWithContext evaluateMultiplicationExpr(@NonNull MultiplicationExpression expr) throws ExpressionEvaluationException {
		log.debug("Evaluating multiplication expression: {}", ExpressionHelper.exprToString(expr));

		String op = expr.getOp();
		Expression left = expr.getLeft();
		Expression right = expr.getRight();

		ResultWithContext leftResult = evaluateExpression(left);
		ResultWithContext rightResult = evaluateExpression(right);

		Class leftResultType = leftResult.get().getClass();
		Class rightResultType = rightResult.get().getClass();

		// Unbox ConstantValues
		if (leftResultType.equals(ConstantValue.class)) {
			leftResultType = ((ConstantValue) leftResult.get()).getValue().getClass();
			leftResult.set(((ConstantValue) leftResult.get()).getValue());
		}

		if (rightResultType.equals(ConstantValue.class)) {
			rightResultType = ((ConstantValue) rightResult.get()).getValue().getClass();
			rightResult.set(((ConstantValue) rightResult.get()).getValue());
		}

		if (!leftResultType.equals(rightResultType)) {
			throw new ExpressionEvaluationException(
				"Type of left expression does not match type of right expression: " +
						leftResultType.getSimpleName() + " vs " +
						rightResultType.getSimpleName());

		}

		switch (op) {
			case "*":
				// FIXME check if an overflow occurs (Math.multiplyExact). But what to do if this overflows?
				if (leftResultType.equals(Integer.class)) {
					return ResultWithContext.fromExisting((Integer) leftResult.get() * (Integer) rightResult.get(), leftResult, rightResult);
				} else if (leftResultType.equals(Float.class)) {
					return ResultWithContext.fromExisting((Float) leftResult.get() * (Float) rightResult.get(), leftResult, rightResult);
				}

				throw new ExpressionEvaluationException(
					"Multiplication operator multiplication ('*') not supported for type: " +
							leftResultType.getSimpleName());
			case "/":
				if (leftResultType.equals(Integer.class)) {
					return ResultWithContext.fromExisting((Integer) leftResult.get() / (Integer) rightResult.get(), leftResult, rightResult);
				} else if (leftResultType.equals(Float.class)) {
					return ResultWithContext.fromExisting((Float) leftResult.get() / (Float) rightResult.get(), leftResult, rightResult);
				}

				throw new ExpressionEvaluationException(
					"Multiplication operator division ('/') not supported for type: {}" +
							leftResultType.getSimpleName());
			case "%":
				if (leftResultType.equals(Integer.class)) {
					return ResultWithContext.fromExisting((Integer) leftResult.get() % (Integer) rightResult.get(), leftResult, rightResult);
				}

				throw new ExpressionEvaluationException(
					"Multiplication operator remainder ('%') not supported for type: " +
							leftResultType.getSimpleName());
			case "<<":
				if (leftResultType.equals(Integer.class)) {
					if (((Integer) rightResult.get()) >= 0) {
						return ResultWithContext.fromExisting((Integer) leftResult.get() << (Integer) rightResult.get(), leftResult, rightResult);
					}

					throw new ExpressionEvaluationException(
						"Left shift operator supports only non-negative integers as its right operand");
				}

				throw new ExpressionEvaluationException(
					"Multiplication operator left shift ('<<') not supported for type: " +
							leftResultType.getSimpleName());
			case ">>":
				if (leftResultType.equals(Integer.class)) {
					if (((Integer) rightResult.get()) >= 0) {
						return ResultWithContext.fromExisting((Integer) leftResult.get() >> (Integer) rightResult.get(), leftResult, rightResult);
					}

					throw new ExpressionEvaluationException("Right shift operator supports only non-negative integers as its right operand");
				}

				throw new ExpressionEvaluationException(
					"Multiplication operator right shift ('>>') not supported for type: " +
							leftResultType.getSimpleName());
			case "&":
				if (leftResultType.equals(Integer.class)) {
					return ResultWithContext.fromExisting((Integer) leftResult.get() & (Integer) rightResult.get(), leftResult, rightResult);
				}

				throw new ExpressionEvaluationException(
					"Addition operator bitwise and ('&') not supported for type: " +
							leftResultType.getSimpleName());
			case "&^":
				if (leftResultType.equals(Integer.class)) {
					return ResultWithContext.fromExisting((Integer) leftResult.get() & ~(Integer) rightResult.get(), leftResult, rightResult);
				}

				throw new ExpressionEvaluationException(
					"Addition operator bitwise or ('|') not supported for type: " +
							leftResultType.getSimpleName());

			default:
				log.error("Unsupported expression {}", op);
		}

		throw new ExpressionEvaluationException(
			"Trying to evaluate unknown multiplication expression: " +
					ExpressionHelper.exprToString(expr));
	}

	@NonNull
	private ResultWithContext evaluateUnaryExpr(@NonNull UnaryExpression expr) throws ExpressionEvaluationException {
		log.debug("Evaluating unary expression: {}", ExpressionHelper.exprToString(expr));

		String op = expr.getOp();

		ResultWithContext subExprResult = evaluateExpression(expr.getExp()); // evaluate the subexpression

		Class subExprResultType = subExprResult.get().getClass();

		// unbox
		if (subExprResultType.equals(ConstantValue.class)) {
			subExprResultType = ((ConstantValue) subExprResult.get()).getValue().getClass();
			subExprResult.set(((ConstantValue) subExprResult.get()).getValue());
		}

		switch (op) {
			case "+":
				if (subExprResultType.equals(Integer.class) || subExprResultType.equals(Float.class)) {
					return subExprResult;
				}

				throw new ExpressionEvaluationException(
					"Unary operator plus sign ('+') not supported for type: " +
							subExprResultType.getSimpleName());

			case "-":
				if (subExprResultType.equals(Integer.class)) {
					return ResultWithContext.fromExisting(-((Integer) subExprResult.get()), subExprResult);
				} else if (subExprResultType.equals(Float.class)) {
					return ResultWithContext.fromExisting(-((Float) subExprResult.get()), subExprResult);
				}

				throw new ExpressionEvaluationException(
					"Unary operator minus sign ('-') not supported for type: " +
							subExprResultType.getSimpleName());

			case "!":
				if (subExprResultType.equals(Boolean.class)) {
					return ResultWithContext.fromExisting(!((Boolean) subExprResult.get()), subExprResult);
				}

				throw new ExpressionEvaluationException(
					"Unary operator logical not ('!') not supported for type: " +
							subExprResultType.getSimpleName());

			case "^":
				if (subExprResultType.equals(Integer.class)) {
					return ResultWithContext.fromExisting(~((Integer) subExprResult.get()), subExprResult);
				}

				throw new ExpressionEvaluationException(
					"Unary operator bitwise complement ('~') not supported for type: " +
							subExprResultType.getSimpleName());

			default:
				log.error("Unsupported expresison {}", op);
		}

		throw new ExpressionEvaluationException(
			"Trying to evaluate unknown unary expression: " + ExpressionHelper.exprToString(expr));
	}

	@NonNull
	private ResultWithContext evaluateOperand(@NonNull Operand operand) throws ExpressionEvaluationException {
		CPGVertexWithValue vertexWithValue = variableContext.get(operand.getOperand());
		if (vertexWithValue == null) {
			throw new ExpressionEvaluationException("Does not have a value: " + operand.getOperand());
		}
		ResultWithContext result = ResultWithContext.fromLiteralOrOperand(vertexWithValue.getValue());
		result.addVertex(vertexWithValue.getArgumentVertex());
		return result;
	}

	public void setCPGVariableContext(@NonNull CPGVariableContext varContext) {
		this.variableContext = varContext;
	}

	public void setCPGInstanceContext(@NonNull CPGInstanceContext instanceContext) {
		this.instanceContext = instanceContext;
	}
}
