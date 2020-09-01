
package de.fraunhofer.aisec.analysis.markevaluation;

import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.CPGVertexWithValue;
import de.fraunhofer.aisec.analysis.structures.ConstantValue;
import de.fraunhofer.aisec.analysis.structures.ErrorValue;
import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.analysis.structures.ListValue;
import de.fraunhofer.aisec.analysis.structures.MarkContext;
import de.fraunhofer.aisec.analysis.structures.MarkContextHolder;
import de.fraunhofer.aisec.analysis.structures.MarkIntermediateResult;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.analysis.utils.Utils;
import de.fraunhofer.aisec.cpg.sarif.Region;
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
import de.fraunhofer.aisec.markmodel.Mark;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
	private final MarkContextHolder markContextHolder;
	private final Mark markModel;

	public ExpressionEvaluator(Mark markModel, MRule rule, AnalysisContext resultCtx, ServerConfiguration config, CrymlinTraversalSource traversal,
			MarkContextHolder context) {
		this.markModel = markModel;
		this.markRule = rule;
		this.resultCtx = resultCtx;
		this.config = config;
		this.traversal = traversal;
		this.markContextHolder = context;
	}

	/**
	 * Checks MARK expression against the CPG using the given instance and markvar assignments
	 * <p>
	 * the value of the result is true/false if the expression is true/false null if the expression could not be evaluated (i.e., an error in the mark rule or the
	 * evaluation)
	 *
	 * @param expr The MARK expression to evaluate.
	 * @return one result (value and context)
	 */
	@NonNull
	public Map<Integer, MarkIntermediateResult> evaluateExpression(Expression expr) {

		if (expr == null) {
			log.error("Cannot evaluate null Expression");
			return markContextHolder.generateNullResult();
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
			Map<Integer, MarkIntermediateResult> literalList = new HashMap<>();
			for (Literal l : ((LiteralListExpression) expr).getValues()) {
				Map<Integer, MarkIntermediateResult> res = evaluateLiteral(l);
				for (Map.Entry<Integer, MarkIntermediateResult> entry : res.entrySet()) {
					ListValue inner = (ListValue) literalList.computeIfAbsent(entry.getKey(), x -> new ListValue());
					inner.add(entry.getValue());
				}
			}
			return literalList;
		}

		throw new ExpressionEvaluationException("unknown expression: " + ExpressionHelper.exprToString(expr));
	}

	@NonNull
	private Map<Integer, MarkIntermediateResult> evaluateOrderExpression(OrderExpression orderExpression) {
		log.info("Evaluating order expression: {}", ExpressionHelper.exprToString(orderExpression));
		Map<Integer, MarkIntermediateResult> result = new HashMap<>();
		for (Map.Entry<Integer, MarkContext> entry : markContextHolder.getAllContexts().entrySet()) {

			OrderEvaluator orderEvaluator = new OrderEvaluator(this.markRule, this.config);
			ConstantValue res = orderEvaluator.evaluate(orderExpression, entry.getKey(), this.resultCtx, this.traversal, this.markContextHolder);

			if (markContextHolder.isCreateFindingsDuringEvaluation() && res != null && Objects.equals(res.getValue(), true)) {
				Set<String> markInstances = new HashSet<>();
				ExpressionHelper.collectMarkInstances(orderExpression.getExp(), markInstances); // extract all used markvars from the expression
				if (markInstances.size() == 1) { // otherwise, the analysis did not work anyway and we did not have a result
					@Nullable
					Vertex operand = entry.getValue().getInstanceContext().getVertex(markInstances.iterator().next());
					if (operand != null) {
						List<Region> ranges = List.of(Utils.getRegionByVertex(operand));
						Finding f = new Finding(
							"Verified Order: " + this.markRule.getName(),
							CrymlinQueryWrapper.getFileLocation(operand),
							"",
							ranges,
							false);
						this.resultCtx.getFindings().add(f);
					}
				}
			}

			result.put(entry.getKey(), res);
		}
		return result;
	}

	@NonNull
	private Map<Integer, MarkIntermediateResult> evaluateLogicalExpr(Expression expr) {
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

		Map<Integer, MarkIntermediateResult> leftResult = evaluateExpression(leftExp);
		Map<Integer, MarkIntermediateResult> rightResult = evaluateExpression(rightExp);

		Map<Integer, MarkIntermediateResult> combinedResult = new HashMap<>();

		for (Map.Entry<Integer, MarkIntermediateResult> entry : rightResult.entrySet()) {
			// we only need to look at the keys from the right side.
			// the right side of the evaluation can add new values, then we have more values on the right than on the left.
			// the right side currently cannot remove values!
			Integer key = entry.getKey();
			ConstantValue leftBoxed = (ConstantValue) getcorrespondingLeftResult(leftResult, key);
			ConstantValue rightBoxed = (ConstantValue) entry.getValue();

			Object left = leftBoxed.getValue();
			Object right = rightBoxed.getValue();

			boolean leftHasError = ConstantValue.isError(leftBoxed);
			boolean rightHasError = ConstantValue.isError(rightBoxed);

			if (leftHasError && rightHasError) {
				// null & null = null, null | null = null

				combinedResult.put(key, ErrorValue.newErrorValue("Cannot perform logical expression, left and right expression have an error", leftBoxed, rightBoxed));

			} else if (!(leftHasError || left.getClass().equals(Boolean.class))
					||
					!(rightHasError || right.getClass().equals(Boolean.class))) {

				log.warn("At least one subexpression is not of type Boolean: {} vs {}",
					ExpressionHelper.exprToString(leftExp),
					ExpressionHelper.exprToString(rightExp));
				combinedResult.put(key, ErrorValue.newErrorValue(String.format("Cannot perform logical expression, left is %s, right is %s",
					leftHasError ? "error" : left.toString(),
					rightHasError ? "error" : right.toString()), leftBoxed, rightBoxed));

			} else if (expr instanceof LogicalAndExpression) {
				if (leftHasError || rightHasError) {
					// null & true = null
					// null & false = false
					if ((!rightHasError && right.equals(false))
							||
							(!leftHasError && left.equals(false))) {
						combinedResult.put(key, ConstantValue.of(false));
					} else {
						combinedResult.put(key, ErrorValue.newErrorValue(String.format("Cannot perform logical AND, left is %s, right is %s",
							leftHasError ? "error" : left.toString(),
							rightHasError ? "error" : right.toString())));
					}
				} else if (left.getClass().equals(Boolean.class)
						&&
						right.getClass().equals(Boolean.class)) {

					ConstantValue cv = ConstantValue.of(Boolean.logicalAnd((Boolean) left, (Boolean) right));
					cv.addResponsibleVerticesFrom(leftBoxed, rightBoxed);
					combinedResult.put(key, cv);
				}

			} else { // LogicalOrExpression
				if (leftHasError || rightHasError) {
					// null | true = true
					// null | false = null
					if ((!rightHasError && right.equals(true))
							||
							(!leftHasError && left.equals(true))) {
						combinedResult.put(key, ConstantValue.of(true));
					} else {
						combinedResult.put(key, ErrorValue.newErrorValue(String.format("Cannot perform logical or, left is %s, right is %s",
							leftHasError ? "error" : left.toString(),
							rightHasError ? "error" : right.toString())));
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

	private Map<Integer, MarkIntermediateResult> evaluateComparisonExpr(ComparisonExpression expr) {
		String op = expr.getOp();
		Expression leftExpr = expr.getLeft();
		Expression rightExpr = expr.getRight();

		log.debug(
			"comparing expression {} with expression {}",
			ExpressionHelper.exprToString(leftExpr),
			ExpressionHelper.exprToString(rightExpr));

		Map<Integer, MarkIntermediateResult> leftResult = evaluateExpression(leftExpr);
		Map<Integer, MarkIntermediateResult> rightResult = evaluateExpression(rightExpr);

		Map<Integer, MarkIntermediateResult> combinedResult = new HashMap<>();

		for (Map.Entry<Integer, MarkIntermediateResult> entry : rightResult.entrySet()) {
			ExpressionComparator<String> comp = new ExpressionComparator<>();

			// we only need to look at the keys from the right side.
			// the right side of the evaluation can add new values, then we have more values on the right than on the left.
			// the right side currently cannot remove values!
			Integer key = entry.getKey();
			ConstantValue leftBoxed = (ConstantValue) getcorrespondingLeftResult(leftResult, key);
			Object left = leftBoxed.getValue();

			if (entry.getValue() instanceof ListValue) {

				if (op.equals("in")) {
					ListValue l = (ListValue) entry.getValue();
					ConstantValue cv = ConstantValue.of(false);

					for (MarkIntermediateResult o : l) {
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
					combinedResult.put(key, ErrorValue.newErrorValue(String.format("Unknown op %s for List on the right side", op)));
				}

			} else {

				ConstantValue rightBoxed = (ConstantValue) entry.getValue();
				Object right = rightBoxed.getValue();

				if (ConstantValue.isError(leftBoxed) || ConstantValue.isError(rightBoxed)) {

					// result of comparison is not known
					combinedResult.put(key, ErrorValue.newErrorValue(
						"Cannot perform comparison, " + (ConstantValue.isError(leftBoxed) ? "left" : "right") + " expression has errors", leftBoxed, rightBoxed));
				} else {

					String leftComp = ExpressionHelper.toComparableString(left);
					String rightComp = ExpressionHelper.toComparableString(right);

					log.debug("left result={} right result={}", left, right);

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
							cv = ErrorValue.newErrorValue(String.format("Unsupported operand %s", op));
					}
					combinedResult.put(key, cv);
				}
			}
		}
		return combinedResult;
	}

	private MarkIntermediateResult getcorrespondingLeftResult(Map<Integer, MarkIntermediateResult> leftResult, Integer key) {
		if (leftResult == null || key == null) {
			return ErrorValue.newErrorValue("Could not find a result");
		}
		if (leftResult.containsKey(key)) {
			return leftResult.get(key);
		}
		List<Integer> copyStack = markContextHolder.getCopyStack(key);
		if (copyStack != null && !copyStack.isEmpty()) {
			for (int i = copyStack.size() - 1; i >= 0; i--) {
				int newKey = copyStack.get(i);
				if (leftResult.containsKey(newKey)) {
					return leftResult.get(newKey);
				}
			}
		}

		return ErrorValue.newErrorValue("Could not find a result");
	}

	/**
	 * Returns evaluated argument values of a Builtin-call.
	 * <p>
	 * A Builtin function "myFunction" may accept 3 arguments: "myFunction(a,b,c)". Each argument may be given in form of an Expression, e.g. "myFunction(0==1, cm.init(),
	 * 42)".
	 * <p>
	 * This method evaluates the Expressions of all arguments and return them as a list contained in the ResultWithContext
	 *
	 * @param argList the list of arguments to evaluate
	 * @return one result with a list of MarkIntermediateResult as results for each argument
	 */

	public Map<Integer, MarkIntermediateResult> evaluateArgs(List<Argument> argList) {
		Map<Integer, MarkIntermediateResult> result = new HashMap<>();
		for (Argument arg : argList) {
			Map<Integer, MarkIntermediateResult> r = evaluateExpression((Expression) arg);

			// if the argument contains more than one var, the evaluation of the n+1-th argument could have increased the number of contexts
			// ex: for argument 0 t.foo we get back the result [1, 2] (i.e., two possible values of t.foo)
			//     evaluating the argument 1 t.bar, we could get back [3, 4, 3, 4] which means we only have another two possible values for t.bar,
			//          but since we need to look at each combination, this is the cross-product of the possible values for t.foo and t.bar.
			//     to correctly fill the argumentlist, we therefore look through all currently returned results, and iff their context was copied during the evaluation
			//     in the example, the third element (second 3) would have been a copy, the un-copied context would be [t.foo=1, t.bar=3], the copy [t.foo=2, t.bar=3]
			//     we remember what the previous argument was, and add that to the current argument return list.
			//     in the example above, this means, that once we get to the second 3, we not only add the 3 to the argument list, but also add the parameter it was
			//     copied from, i.e., the 1.
			//     The resulting argument list for this example would be [(1,3), (2,4), (1,4), (2,4)]

			Map<Integer, ArrayList<MarkIntermediateResult>> previousArgument = new HashMap<>();
			for (Integer key : r.keySet()) {
				if (!result.containsKey(key)) {
					MarkIntermediateResult prev = getcorrespondingLeftResult(result, key);
					if (!((prev instanceof ErrorValue) && ((ErrorValue) prev).getDescription().equals("Could not find a result"))) {
						// this is a real result
						if (!(prev instanceof ListValue)) {
							log.error("Previous result of an Argument evaluation was not a Listvalue");
						} else {
							ArrayList<MarkIntermediateResult> deepCopy = new ArrayList<>(((ListValue) prev).getAll());
							previousArgument.put(key, deepCopy);
						}
					}
				}
			}

			for (Map.Entry<Integer, MarkIntermediateResult> entry : r.entrySet()) {
				MarkIntermediateResult o = result.get(entry.getKey());
				if (o == null) {
					o = new ListValue();
					if (previousArgument.containsKey(entry.getKey())) {
						((ListValue) o).addAll(previousArgument.get(entry.getKey()));
					}
					result.put(entry.getKey(), o);
				}
				((ListValue) o).add(entry.getValue());
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
	@NonNull
	private Map<Integer, MarkIntermediateResult> evaluateBuiltin(FunctionCallExpression expr) {
		String functionName = expr.getName();

		// Call built-in function (if available)
		Optional<Builtin> builtin = BuiltinRegistry.getInstance()
				.getRegisteredBuiltins()
				.stream()
				.filter(b -> b.getName()
						.equals(functionName))
				.findFirst();

		Map<Integer, MarkIntermediateResult> arguments = evaluateArgs(expr.getArgs());

		if (builtin.isPresent()) {
			Map<Integer, MarkIntermediateResult> result = new HashMap<>();
			for (Map.Entry<Integer, MarkIntermediateResult> entry : arguments.entrySet()) {

				if (!(entry.getValue() instanceof ListValue)) {
					log.error("Arguments must be a list");
					result.put(entry.getKey(), ErrorValue.newErrorValue(String.format("arguments must be a list, are %s", entry.getValue().getClass().getSimpleName())));
					continue;
				}

				ConstantValue cv = builtin.get().execute(resultCtx, (ListValue) (entry.getValue()), entry.getKey(), markContextHolder, this);

				result.put(entry.getKey(), cv);

			}
			return result;
		}

		log.error("Unsupported builtin {}", functionName);
		Map<Integer, MarkIntermediateResult> result = new HashMap<>();
		for (Map.Entry<Integer, MarkIntermediateResult> entry : arguments.entrySet()) {
			result.put(entry.getKey(), ErrorValue.newErrorValue(String.format("Unsupported builtin %s", functionName)));
		}
		return result;
	}

	private Map<Integer, MarkIntermediateResult> evaluateLiteral(Literal literal) {
		String v = literal.getValue();

		ConstantValue value;

		// ordering based on Mark grammar
		if (literal instanceof IntegerLiteral) {
			log.debug("Literal is Integer: {}", v);

			try {
				if (v.startsWith("0x")) {
					value = ConstantValue.of(Integer.parseInt(v.substring(2), 16));
				} else {
					value = ConstantValue.of(Long.parseLong(v));
				}
			}
			catch (NumberFormatException nfe) {
				log.warn("Unable to convert integer literal {}", v, nfe);
				value = ErrorValue.newErrorValue(String.format("Unable to convert integer literal %s: %s", v, nfe.getMessage()));
			}
		} else if (literal instanceof BooleanLiteral) {
			log.debug("Literal is Boolean: {}", v);
			value = ConstantValue.of(Boolean.parseBoolean(v));
		} else if (literal instanceof StringLiteral) {
			log.debug("Literal is String: {}", v);
			value = ConstantValue.of(Utils.stripQuotedString(v));
		} else {
			log.warn("Unknown literal encountered: {}", v);
			value = ErrorValue.newErrorValue(String.format("Unknown literal encountered: %s", v));
		}

		Map<Integer, MarkIntermediateResult> ret = new HashMap<>();
		for (Integer key : markContextHolder.getAllContexts()
				.keySet()) {
			ret.put(key, value);
		}
		return ret;
	}

	@NonNull
	private Map<Integer, MarkIntermediateResult> evaluateMultiplicationExpr(MultiplicationExpression expr) {
		log.debug("Evaluating multiplication expression: {}", ExpressionHelper.exprToString(expr));

		String op = expr.getOp();

		Map<Integer, MarkIntermediateResult> leftResult = evaluateExpression(expr.getLeft());
		Map<Integer, MarkIntermediateResult> rightResult = evaluateExpression(expr.getRight());

		Map<Integer, MarkIntermediateResult> combinedResult = new HashMap<>();

		for (Map.Entry<Integer, MarkIntermediateResult> entry : rightResult.entrySet()) {
			// we only need to look at the keys from the right side.
			// the right side of the evaluation can add new values, then we have more values on the right than on the left.
			// the right side currently cannot remove values!
			Integer key = entry.getKey();
			ConstantValue leftBoxed = (ConstantValue) getcorrespondingLeftResult(leftResult, key);
			ConstantValue rightBoxed = (ConstantValue) entry.getValue();

			Object left = leftBoxed.getValue();
			Object right = rightBoxed.getValue();

			if (ConstantValue.isError(leftBoxed) || ConstantValue.isError(rightBoxed)) {

				// result of expr is not known
				combinedResult.put(key, ErrorValue.newErrorValue("cannot multiply expressions, left or right expression has errors", leftBoxed, rightBoxed));
			} else {

				Class leftResultType = left.getClass();
				Class rightResultType = right.getClass();

				if (!leftResultType.equals(rightResultType)) {
					log.warn("Type of left expression does not match type of right expression: {} vs {}",
						leftResultType.getSimpleName(),
						rightResultType.getSimpleName());
					combinedResult.put(key, ErrorValue.newErrorValue(String.format("Type of left expression does not match type of right expression: %s vs %s",
						leftResultType.getSimpleName(),
						rightResultType.getSimpleName())));
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
							unboxedResult = ErrorValue.newErrorValue(String.format("Multiplication operator multiplication ('*') not supported for type: %s",
								leftResultType.getSimpleName()));
						}
						break;
					case "/":
						if (leftResultType.equals(Integer.class)) {
							unboxedResult = ((Integer) left / (Integer) right);
						} else if (leftResultType.equals(Float.class)) {
							unboxedResult = ((Float) left / (Float) right);
						} else {
							log.warn("Multiplication operator division ('/') not supported for type: {}", leftResultType.getSimpleName());
							unboxedResult = ErrorValue
									.newErrorValue(String.format("Multiplication operator division ('/') not supported for type: %s", leftResultType.getSimpleName()));
						}
						break;
					case "%":
						if (leftResultType.equals(Integer.class)) {
							unboxedResult = ((Integer) left % (Integer) right);
						} else {
							log.warn("Multiplication operator remainder ('%') not supported for type: {}", leftResultType.getSimpleName());
							unboxedResult = ErrorValue.newErrorValue(String.format("Multiplication operator remainder ('%%') not supported for type: %s",
								leftResultType.getSimpleName()));
						}
						break;
					case "<<":
						if (leftResultType.equals(Integer.class)) {
							if (((Integer) right) >= 0) {
								unboxedResult = ((Integer) left << (Integer) right);
							} else {
								log.warn("Left shift operator supports only non-negative integers as its right operand");
								unboxedResult = ErrorValue.newErrorValue("Left shift operator supports only non-negative integers as its right operand");
							}
						} else {
							log.warn("Multiplication operator left shift ('<<') not supported for type: {}", leftResultType.getSimpleName());
							unboxedResult = ErrorValue.newErrorValue(String.format("Multiplication operator left shift ('<<') not supported for type: %s",
								leftResultType.getSimpleName()));
						}
						break;
					case ">>":
						if (leftResultType.equals(Integer.class)) {
							if (((Integer) right) >= 0) {
								unboxedResult = ((Integer) left >> (Integer) right);
							} else {
								log.warn("Right shift operator supports only non-negative integers as its right operand");
								unboxedResult = ErrorValue.newErrorValue("Right shift operator supports only non-negative integers as its right operand");
							}
						} else {
							log.warn("Multiplication operator right shift ('>>') not supported for type: {}", leftResultType.getSimpleName());
							unboxedResult = ErrorValue.newErrorValue(String.format("Multiplication operator right shift ('>>') not supported for type: %s",
								leftResultType.getSimpleName()));
						}
						break;
					case "&":
						if (leftResultType.equals(Integer.class)) {
							unboxedResult = ((Integer) left & (Integer) right);
						} else {
							log.warn("Addition operator bitwise and ('&') not supported for type: {}", leftResultType.getSimpleName());
							unboxedResult = ErrorValue
									.newErrorValue(String.format("Addition operator bitwise and ('&') not supported for type: %s", leftResultType.getSimpleName()));
						}
						break;
					case "&^":
						if (leftResultType.equals(Integer.class)) {
							unboxedResult = ((Integer) left & ~(Integer) right);
						} else {
							log.warn("Addition operator bitwise or ('|') not supported for type: {}", leftResultType.getSimpleName());
							unboxedResult = ErrorValue
									.newErrorValue(String.format("Addition operator bitwise or ('|') not supported for type: %s", leftResultType.getSimpleName()));
						}
						break;
					default:
						log.error("Unsupported expression {}", op);
						unboxedResult = ErrorValue.newErrorValue(String.format("Unsupported expression %s", op));
				}
				ConstantValue cv = ConstantValue.of(unboxedResult);
				cv.addResponsibleVerticesFrom(leftBoxed, rightBoxed);
				combinedResult.put(key, cv);
			}
		}
		return combinedResult;
	}

	@NonNull
	private Map<Integer, MarkIntermediateResult> evaluateUnaryExpr(UnaryExpression expr) {
		log.debug("Evaluating unary expression: {}", ExpressionHelper.exprToString(expr));

		String op = expr.getOp();

		Map<Integer, MarkIntermediateResult> subExprResult = evaluateExpression(expr.getExp()); // evaluate the subexpression

		for (Map.Entry<Integer, MarkIntermediateResult> entry : subExprResult.entrySet()) {

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
						unboxedResult = ErrorValue
								.newErrorValue(String.format("Unary operator plus sign ('+') not supported for type: %s", subExprResultType.getSimpleName()));
					}
					break;
				case "-":
					if (subExprResultType.equals(Integer.class)) {
						unboxedResult = -((Integer) value);
					} else if (subExprResultType.equals(Float.class)) {
						unboxedResult = -((Float) value);
					} else {
						log.warn("Unary operator minus sign ('-') not supported for type: {}", subExprResultType.getSimpleName());
						unboxedResult = ErrorValue
								.newErrorValue(String.format("Unary operator minus sign ('-') not supported for type: %s", subExprResultType.getSimpleName()));
					}
					break;
				case "!":
					if (subExprResultType.equals(Boolean.class)) {
						unboxedResult = !((Boolean) value);
					} else {
						log.warn("Unary operator logical not ('!') not supported for type: {}", subExprResultType.getSimpleName());
						unboxedResult = ErrorValue
								.newErrorValue(String.format("Unary operator logical not ('!') not supported for type: %s", subExprResultType.getSimpleName()));
					}
					break;
				case "^":
					if (subExprResultType.equals(Integer.class)) {
						unboxedResult = ~((Integer) value);
					} else {
						log.warn("Unary operator bitwise complement ('~') not supported for type: {}", subExprResultType.getSimpleName());
						unboxedResult = ErrorValue
								.newErrorValue(String.format("Unary operator bitwise complement ('~') not supported for type: %s", subExprResultType.getSimpleName()));
					}
					break;
				default:
					log.warn("Trying to evaluate unknown unary expression: {}", ExpressionHelper.exprToString(expr));
					unboxedResult = ErrorValue.newErrorValue(String.format("Trying to evaluate unknown unary expression: %s", ExpressionHelper.exprToString(expr)));
			}
			ConstantValue cv = ConstantValue.of(unboxedResult);
			cv.addResponsibleVerticesFrom(valueBoxed);
			subExprResult.put(entry.getKey(), cv);
		}
		return subExprResult;
	}

	@NonNull
	private Map<Integer, MarkIntermediateResult> evaluateOperand(Operand operand) {

		// operands are split by "."
		String[] split = operand.getOperand().split("\\.");

		StringBuilder sb = new StringBuilder();
		sb.append(split[0]); // add base

		Map<Integer, MarkIntermediateResult> result = markContextHolder.generateNullResult();

		for (int i = 1; i < split.length; i++) {
			sb.append(".");
			sb.append(split[i]);

			// sequentially resolve an operand from the left to the right.
			// i.e., if the operand is t.foo.bla, resolve t.foo, then resolve t.foo.bla
			result = evaluateSingleOperand(sb.toString());
		}

		if (split.length == 1) { // also return the markvar itself, might be needed by a builtin
			for (Map.Entry<Integer, MarkIntermediateResult> entry : result.entrySet()) {
				Vertex vertex = markContextHolder.getContext(entry.getKey()).getInstanceContext().getVertex(operand.getOperand());
				CPGVertexWithValue vwv = new CPGVertexWithValue(vertex, ConstantValue.newUninitialized());
				ConstantValue constant = ConstantValue.of(vwv.getValue());
				constant.addResponsibleVertex(vertex);
				entry.setValue(constant);
			}
		}

		return result;
	}

	@NonNull
	private Map<Integer, MarkIntermediateResult> evaluateSingleOperand(String operand) {

		Map<Integer, MarkIntermediateResult> resolvedOperand = markContextHolder.getResolvedOperand(operand);

		if (resolvedOperand == null) {
			// if this operand is not resolved yet in this expressionevaluation, resolve it
			Map<Integer, List<CPGVertexWithValue>> operandVertices = CrymlinQueryWrapper.resolveOperand(resultCtx.getDatabase(), markContextHolder, operand, markRule,
				markModel, traversal);
			if (operandVertices.size() == 0) {
				log.warn("Did not find any vertices for {}, following evaluation will be imprecise", operand);
			}
			markContextHolder.addResolvedOperands(operand, operandVertices); // cache the resolved operand
			resolvedOperand = markContextHolder.getResolvedOperand(operand);
		}

		return resolvedOperand;
	}

	public CrymlinTraversalSource getCrymlinTraversal() {
		return this.traversal;
	}
}
