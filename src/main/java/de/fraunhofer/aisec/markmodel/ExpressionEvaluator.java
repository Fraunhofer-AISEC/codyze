
package de.fraunhofer.aisec.markmodel;

import com.steelbridgelabs.oss.neo4j.structure.Neo4JVertex;
import de.fraunhofer.aisec.cpg.graph.BinaryOperator;
import de.fraunhofer.aisec.cpg.graph.VariableDeclaration;
import de.fraunhofer.aisec.crymlin.builtin.Builtin;
import de.fraunhofer.aisec.crymlin.builtin.BuiltinRegistry;
import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversal;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import de.fraunhofer.aisec.crymlin.utils.CrymlinQueryWrapper;
import de.fraunhofer.aisec.crymlin.utils.Pair;
import de.fraunhofer.aisec.crymlin.utils.Utils;
import de.fraunhofer.aisec.mark.markDsl.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.emf.common.util.EList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.*;

public class ExpressionEvaluator {

	private static final Logger log = LoggerFactory.getLogger(ExpressionEvaluator.class);

	private final EvaluationContext context;

	public ExpressionEvaluator(EvaluationContext ec) {
		this.context = ec;
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
	public Optional<Boolean> evaluate(Expression expr) {
		log.debug("Evaluating top level expression: {}", ExpressionHelper.exprToString(expr));

		Optional result = evaluateExpression(expr);

		if (result.isEmpty()) {
			log.error("Expression could not be evaluated: {}", ExpressionHelper.exprToString(expr));
			return Optional.empty();
		}

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
		log.debug("Evaluating logical expression: {}", ExpressionHelper.exprToString(expr));

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
				ExpressionHelper.exprToString(left),
				ExpressionHelper.exprToString(right));

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
				ExpressionHelper.exprToString(left),
				ExpressionHelper.exprToString(right));

			return Optional.empty();
		}

		log.error(
			"Trying to evaluate unknown logical expression: {}", ExpressionHelper.exprToString(expr));

		assert false; // not a logical expression
		return Optional.empty();
	}

	private Optional<Boolean> evaluateComparisonExpr(ComparisonExpression expr) {
		String op = expr.getOp();
		Expression left = expr.getLeft();
		Expression right = expr.getRight();

		log.debug(
			"comparing expression {} with expression {}",
			ExpressionHelper.exprToString(left),
			ExpressionHelper.exprToString(right));

		Optional leftResult = evaluateExpression(left);
		Optional rightResult = evaluateExpression(right);

		if (leftResult.isEmpty() || rightResult.isEmpty()) {
			return Optional.empty();
		}

		Class leftType = leftResult.get().getClass();
		Class rightType = rightResult.get().getClass();

		log.debug("left result={} right result={}", leftResult.get(), rightResult.get());

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
			default:
				log.error("Unsupported operand {}", op);
		}

		assert false;
		return Optional.empty();
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
	public List<Optional> evaluateArgs(@NonNull List<Argument> argList) {
		List<Optional> result = new ArrayList<>();
		for (Argument arg : argList) {
			result.add(evaluateExpression((Expression) arg));
		}
		return result;
	}

	/**
	 * Evaluate built-in functions.
	 *
	 * @param expr
	 * @return
	 */
	private Optional evaluateBuiltin(FunctionCallExpression expr) {
		String functionName = expr.getName();

		// Call built-in function (if available)
		for (Builtin b : BuiltinRegistry.getInstance().getRegisteredBuiltins()) {
			if (b.getName().equals(functionName)) {
				return b.execute(expr.getArgs(), this.context);
			}
		}

		log.error("Unsupported function {}", functionName);
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
			}
			catch (NumberFormatException nfe) {
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

	/**
	 * Evaluates a non-order MARK expression.
	 *
	 * @param expr
	 * @return
	 */
	// TODO JS->FW: Should return a  Optional<Boolean>. Evaluation of Expressions which do not return
	// a boolean should be pushed down into separate evaluation functions.
	public Optional evaluateExpression(Expression expr) {
		// from lowest to highest operator precedence

		if (expr instanceof OrderExpression) {
			return evaluateOrderExpression((OrderExpression) expr);
		} else if (expr instanceof LogicalOrExpression) {
			log.debug("evaluating LogicalOrExpression: {}", ExpressionHelper.exprToString(expr));
			return evaluateLogicalExpr(expr);
		} else if (expr instanceof LogicalAndExpression) {
			log.debug("evaluating LogicalAndExpression: {}", ExpressionHelper.exprToString(expr));
			return evaluateLogicalExpr(expr);
		} else if (expr instanceof ComparisonExpression) {
			log.debug("evaluating ComparisonExpression: {}", ExpressionHelper.exprToString(expr));
			return evaluateLogicalExpr(expr);
		} else if (expr instanceof MultiplicationExpression) {
			log.debug("evaluating MultiplicationExpression: {}", ExpressionHelper.exprToString(expr));
			return evaluateMultiplicationExpr((MultiplicationExpression) expr);
		} else if (expr instanceof UnaryExpression) {
			log.debug("evaluating UnaryExpression: {}", ExpressionHelper.exprToString(expr));
			return evaluateUnaryExpr((UnaryExpression) expr);
		} else if (expr instanceof Literal) {
			log.debug("evaluating Literal expression: {}", ExpressionHelper.exprToString(expr));
			return evaluateLiteral((Literal) expr);
		} else if (expr instanceof Operand) {
			log.debug("evaluating Operand expression: {}", ExpressionHelper.exprToString(expr));
			return evaluateOperand((Operand) expr);
		} else if (expr instanceof FunctionCallExpression) {
			log.debug("evaluating FunctionCallExpression: {}", ExpressionHelper.exprToString(expr));
			return evaluateBuiltin((FunctionCallExpression) expr);
		} else if (expr instanceof LiteralListExpression) {
			// TODO JS->FW: What is the semantics of LiteralListExpression? Seems like the Optional<List>
			// result is not used anywhere.
			log.debug("evaluating LiteralListExpression: {}", ExpressionHelper.exprToString(expr));

			List literalList = new ArrayList<>();

			for (Literal l : ((LiteralListExpression) expr).getValues()) {
				Optional v = evaluateLiteral(l);

				literalList.add(v.orElse(null));
			}
			return Optional.of(literalList);
		}

		log.error("unknown expression: {}", ExpressionHelper.exprToString(expr));
		assert false; // all expression types must be handled
		return Optional.empty();
	}

	private Optional evaluateMultiplicationExpr(MultiplicationExpression expr) {
		log.debug("Evaluating multiplication expression: {}", ExpressionHelper.exprToString(expr));

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
							return Optional.of(((Integer) leftResult.get()) << ((Integer) rightResult.get()));
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
							// todo commentDT: return this?
							return Optional.of(((Integer) leftResult.get()) >> ((Integer) rightResult.get()));
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

			default:
				log.error("Unsupported expression {}", op);
		}

		// TODO #8
		log.error(
			"Trying to evaluate unknown multiplication expression: {}",
			ExpressionHelper.exprToString(expr));

		assert false; // not an addition expression
		return Optional.empty();
	}

	private Optional evaluateUnaryExpr(UnaryExpression expr) {
		log.debug("Evaluating unary expression: {}", ExpressionHelper.exprToString(expr));

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

			default:
				log.error("Unsupported expresison {}", op);
		}

		// TODO #8
		log.error(
			"Trying to evaluate unknown unary expression: {}", ExpressionHelper.exprToString(expr));

		assert false; // not an addition expression
		return Optional.empty();
	}

	/**
	 * TODO JS->FW: Write a comment here, describing what vertices this method returns for given operand. Maybe give an example.
	 *
	 * @param operand
	 * @return
	 */
	public static List<Vertex> getMatchingVertices(Operand operand, EvaluationContext context) {
		final ArrayList<Vertex> ret = new ArrayList<>();

		if (!context.hasContextType(EvaluationContext.Type.RULE)) {
			log.error("Context is not a rule!");
			return ret;
		}
		if (StringUtils.countMatches(operand.getOperand(), ".") != 1) {
			// TODO JS->FW: Add comment explaining what the "." is for.
			log.error("operand contains more than one '.' which is not supported yet");
			return ret;
		}

		// Split operand "myInstance.attribute" into "myInstance" and "attribute".
		final String[] operandParts = operand.getOperand().split("\\.");
		final String instance = operandParts[0];
		final String attribute = operandParts[1];

		// Get the MARK entity corresponding to the operator's instance.
		Pair<String, MEntity> ref = context.getRule().getEntityReferences().get(instance);
		String entityName = ref.getValue0();
		MEntity referencedEntity = ref.getValue1();

		List<Pair<MOp, Set<OpStatement>>> usesAsVar = new ArrayList<>();
		List<Pair<MOp, Set<OpStatement>>> usesAsFunctionArgs = new ArrayList<>();

		// Collect *variables* assigned in Ops of this entity and *arguments* used in Ops.
		for (MOp operation : referencedEntity.getOps()) {
			Set<OpStatement> vars = new HashSet<>();
			Set<OpStatement> args = new HashSet<>();

			// Iterate over all statements of that op
			for (OpStatement opStmt : operation.getStatements()) {
				// simple assignment, i.e. "var = something()"
				if (attribute.equals(opStmt.getVar())) {
					vars.add(opStmt);
				}
				// Function parameter, i.e. "something(..., var, ...)"
				if (opStmt.getCall().getParams().stream().anyMatch(p -> p.equals(attribute))) {
					args.add(opStmt);
				}
			}

			if (!vars.isEmpty()) {
				usesAsVar.add(new Pair<>(operation, vars));
			}

			if (!args.isEmpty()) {
				usesAsFunctionArgs.add(new Pair<>(operation, args));
			}
		}

		// TODO JS->FW: (less important) ExpressionEvaluator should not decide on its own which type of
		// DB to use but rather receive a connection when instantiated.
		try (TraversalConnection conn = new TraversalConnection(TraversalConnection.Type.OVERFLOWDB)) {
			CrymlinTraversalSource crymlin = conn.getCrymlinTraversal();

			for (Pair<MOp, Set<OpStatement>> p : usesAsVar) {
				for (OpStatement opstmt : p.getValue1()) {

					String fqFunctionName = opstmt.getCall().getName();

					String functionName = Utils.extractMethodName(fqFunctionName);
					String fqNamePart = Utils.extractType(fqFunctionName);

					List<String> functionArgumentTypes = referencedEntity.replaceArgumentVarsWithTypes(opstmt.getCall().getParams());

					Set<Vertex> vertices = CrymlinQueryWrapper.getCalls(
						crymlin, fqNamePart, functionName, null, functionArgumentTypes);

					for (Vertex v : vertices) {
						// check if there was an assignment

						// todo: move this to crymlintraversal. For some reason, the .toList() blocks if the
						// step is in the crymlin traversal
						List<Vertex> nextVertices = CrymlinQueryWrapper.lhsVariableOfAssignment(crymlin, (long) v.id());

						if (!nextVertices.isEmpty()) {
							log.info("found RHS traversals: {}", nextVertices);
							ret.addAll(nextVertices);
						}

						// check if there was a direct initialization (i.e., int i = call(foo);)
						nextVertices = crymlin.byID((long) v.id()).initializerVariable().toList();

						if (!nextVertices.isEmpty()) {
							log.info("found Initializer traversals: {}", nextVertices);
							ret.addAll(nextVertices);
						}
					}
				}
			}

			for (Pair<MOp, Set<OpStatement>> p : usesAsFunctionArgs) {
				for (OpStatement opstmt : p.getValue1()) {

					String fqFunctionName = opstmt.getCall().getName();
					String functionName = Utils.extractMethodName(fqFunctionName);
					String fqName = fqFunctionName.substring(0, fqFunctionName.lastIndexOf(functionName));

					if (fqName.endsWith("::")) {
						fqName = fqName.substring(0, fqName.length() - 2);
					}

					EList<String> params = opstmt.getCall().getParams();
					List<String> argumentTypes = referencedEntity.replaceArgumentVarsWithTypes(params);
					OptionalInt argumentIndexOptional = IntStream.range(0, params.size()).filter(i -> attribute.equals(params.get(i))).findFirst();
					if (argumentIndexOptional.isEmpty()) {
						log.error("argument not found in parameters. This should not happen");
						continue;
					}
					int argumentIndex = argumentIndexOptional.getAsInt();

					Set<Vertex> vertices = CrymlinQueryWrapper.getCalls(
						crymlin, fqName, functionName, entityName, argumentTypes);

					for (Vertex v : vertices) {
						List<Vertex> argumentVertices = crymlin.byID((long) v.id()).argument(argumentIndex).toList();

						if (argumentVertices.size() == 1) {
							ret.add(argumentVertices.get(0));
						} else {
							log.warn("Did not find one matching argument node, got {}", argumentVertices.size());
						}
					}
				}
			}
		}
		return ret;
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

		if (!context.hasContextType(EvaluationContext.Type.RULE)) {
			log.error("Unexpected: MarkExpression Evaluation outside of a rule: {}", operandString);
			return Optional.empty();
		}

		final MRule rule = context.getRule();

		// Split the operand (e.g., "cm.cipher") into instance ("cm") and attribute ("cipher")
		String[] operandParts = operandString.split("\\.");
		if (operandParts.length != 2) { // todo add error-case, if operand is e.g. without separator, and a case if we have fqn for the entity
			return Optional.empty();
		}
		String instance = operandParts[0];
		String attribute = operandParts[1];

		// Find out the "type" (i.e. the Mark entity) of instance
		Pair<String, MEntity> ref = rule.getEntityReferences().get(instance);
		if (ref == null || ref.getValue0() == null) {
			log.warn("Mark rule contains unresolved instance variable {}. Rule will fail: {}", instance, rule.getName());
			return Optional.empty();
		}
		String entityName = ref.getValue0();
		MEntity referencedEntity = ref.getValue1();
		if (referencedEntity == null) {
			log.warn("Mark rule contains reference to unresolved entity {}. Rule will fail: {}", entityName, rule.getName());
			return Optional.empty();
		}

		// Find out the uses of the Mark attribute in all Mark ops.
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
			if (!vars.isEmpty()) {
				usesAsVar.add(new Pair<>(operation, vars));
			}
			if (!args.isEmpty()) {
				usesAsFunctionArgs.add(new Pair<>(operation, args));
				// todo: argumentindizes vorberechnen!
			}
		}
		dump(usesAsVar, usesAsFunctionArgs);

		// know which ops and opstatements use operand
		// resolve vars
		// TODO JS->FW: Do not decide within this class what kind of DB is used.
		try (TraversalConnection conn = new TraversalConnection(TraversalConnection.Type.OVERFLOWDB)) {
			CrymlinTraversalSource crymlin = conn.getCrymlinTraversal();

			for (Pair<MOp, Set<OpStatement>> p : usesAsVar) {
				for (OpStatement opstmt : p.getValue1()) {
					/*
					 * TODO how would we do this? wouldn't we need to evaluate the calling function to determine the value? I think the intention was to signal that a
					 * variable was set by a specific function call. It's basically a Boolean decision can I find an assignment to the variable where on the rhs the
					 * specified function is called --> YES or NO.
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
						functionArgumentTypes.add(i, referencedEntity.getTypeForVar(functionArguments.get(i)));
					}

					// 1. find callexpression with opstatment signature
					Set<Vertex> vertices = CrymlinQueryWrapper.getCalls(crymlin, fqNamePart, functionName, entityName, functionArgumentTypes);

					List<Vertex> nextVertices = new ArrayList<>();
					for (Vertex v : vertices) {
						Traversal<Vertex, Vertex> nextTraversalStep = crymlin.byID((long) v.id())
								.in("RHS")
								.where(
									has(T.label, LabelP.of(BinaryOperator.class.getSimpleName())).and().has("operatorCode", "="))
								.out("LHS")
								.out("REFERS_TO")
								.has(
									T.label, LabelP.of(VariableDeclaration.class.getSimpleName()));

						nextVertices.addAll(nextTraversalStep.toList());
					}
					log.warn("found traversals: {}", nextVertices);
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
					String packageClass = fqFunctionName.substring(0, fqFunctionName.lastIndexOf(functionName));

					if (packageClass.endsWith(".")) {
						packageClass = packageClass.substring(0, packageClass.length() - 1);
					}

					List<String> argumentTypes = new ArrayList<>(fd.getParams());
					int argumentIndex = -1;
					for (int i = 0; i < argumentTypes.size(); i++) {
						String argVar = argumentTypes.get(i);

						if (attribute.equals(argVar)) {
							argumentIndex = i;
						}

						if (Constants.UNDERSCORE.equals(argVar) || Constants.ELLIPSIS.equals(argVar)) {
							continue;
						}

						argumentTypes.set(i, referencedEntity.getTypeForVar(argVar));
					}

					// vertices in CPG where we call a function from a MARK OpStatement and we're
					// looking for one of the arguments
					Set<Vertex> vertices = CrymlinQueryWrapper.getCalls(crymlin, packageClass, functionName, null, argumentTypes);

					/*
					 * TODO JS->FW: The following code is okay, but it should be moved in to a separate method. The problem is that we are trying to be too smart here and
					 * resolve an Operand "as far as we can". For "Immediates", (i.e. String/integer/boolean/float constants), the result of this method is the actual
					 * value, in other cases it is a Vertex representing the variable. This makes this method hard to use, .e.g from IsInstanceBuiltin, because we do not
					 * know what to expect. It should rather always return a Vertex. The constant resolution should be done elsewhere.
					 */
					String attributeType = referencedEntity.getTypeForVar(attribute);

					// further investigate each function call
					for (Vertex v : vertices) {
						CrymlinTraversal<Vertex, Vertex> variableDeclarationTraversal = crymlin.byID((long) v.id())
								.out("ARGUMENTS")
								.has("argumentIndex",
									argumentIndex)
								.out("REFERS_TO");
						// vertices (SHOULD ONLY BE ONE) representing a variable declaration for the
						// argument we're using in the function call

						// TODO potential NullPointerException
						Vertex variableDeclarationVertex = variableDeclarationTraversal.toList().get(0);

						// FIXME and now this code doesn't work as expected, especially path()

						/*
						 * TODO general idea 1. from CPG vertex representing the function argument ('crymlin.byID((long) v.id()).out("ARGUMENTS").has("argumentIndex",
						 * argumentIndex)') create all paths to vertex with variable declaration ('variableDeclarationVertex') in theory 'crymlin.byID((long)
						 * v.id()).repeat(in("EOG").simplePath()) .until(hasId(variableDeclarationVertex.id())).path()' 2. traverse this path from 'v' --->
						 * 'variableDeclarationVertex' 3. for each assignment, i.e. BinaryOperator{operatorCode: "="} 4. check if -{"LHS"}-> v -{"REFERS_TO"}->
						 * variableDeclarationVertex 5. then determine value RHS 6. done 7. {no interjacent assignment} determine value of variableDeclarationVertex (e.g.
						 * from its initializer) 8. {no intializer with value e.g. function argument} continue traversing the graph
						 */

						log.debug("Vertex for function call: {}", v);
						log.debug("Vertex of variable declaration: {}", variableDeclarationVertex);

						// traverse in reverse along EOG edges from v until variableDeclarationVertex -->
						// one of them must have more information on the value of the operand
						CrymlinTraversal<Vertex, Vertex> traversal = crymlin.byID((long) v.id())
								.repeat(in("EOG"))
								.until(
									is(variableDeclarationVertex))
								.emit();
						dumpVertices(traversal.clone().toList());

						while (traversal.hasNext()) {
							Vertex tVertex = traversal.next();

							boolean isBinaryOperatorVertex = Arrays.asList(tVertex.label().split(Neo4JVertex.LabelDelimiter)).contains("BinaryOperator");

							if (isBinaryOperatorVertex && "=".equals(tVertex.property("operatorCode").value())) {
								// this is an assignment that may set the value of our operand
								Vertex lhs = tVertex.vertices(Direction.OUT, "LHS").next();

								if (lhs.vertices(Direction.OUT, "REFERS_TO").next().equals(variableDeclarationVertex)) {
									Vertex rhs = tVertex.vertices(Direction.OUT, "RHS").next();

									boolean isRhsLiteral = Arrays.asList(rhs.label().split(Neo4JVertex.LabelDelimiter)).contains("Literal");

									if (isRhsLiteral) {
										Object literalValue = rhs.property("value").value();
										Class literalValueClass = literalValue.getClass();

										if (literalValueClass.equals(Long.class) || literalValueClass.equals(Integer.class)) {
											return Optional.of(((Number) literalValue).intValue());
										}

										if (literalValueClass.equals(Double.class) || literalValueClass.equals(Float.class)) {
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
											if ("char".equals(attributeType) || "char".equals(variableDeclarationVertex.property("type").value())) {
												// FIXME this will likely break on an empty string
												return Optional.of(valueString.charAt(0));
											}
											return Optional.of(valueString);
										}
										log.error("Unknown literal type encountered: {} (value: {})", literalValue.getClass(), literalValue);
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
						Iterator<Vertex> itInitializerVertex = variableDeclarationVertex.vertices(Direction.OUT, "INITIALIZER");
						while (itInitializerVertex.hasNext()) {
							// there should be at most one
							Vertex initializerVertex = itInitializerVertex.next();

							if (Arrays.asList(initializerVertex.label().split(Neo4JVertex.LabelDelimiter)).contains("Literal")) {
								Object literalValue = initializerVertex.property("value").value();
								Class literalValueClass = literalValue.getClass();

								if (literalValueClass.equals(Long.class) || literalValueClass.equals(Integer.class)) {
									return Optional.of(((Number) literalValue).intValue());
								}

								if (literalValueClass.equals(Double.class) || literalValueClass.equals(Float.class)) {
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
									if ("char".equals(attributeType) || "char".equals(variableDeclarationVertex.property("type").value())) {
										// FIXME this will likely break on an empty string
										return Optional.of(valueString.charAt(0));
									}
									return Optional.of(valueString);
								}
								log.error("Unknown literal type encountered: {} (value: {})", literalValue.getClass(), literalValue);
							}
						}
					}
				}
			}
		}

		// TODO at this point I know what I need to look for: either assignments or function calls

		/*
		 * FIXME need to differentiate between entity reference and actual code references to Botan/BouncyCastle Currently, I just get some text that can be anything --
		 * entity references, classes/variables/ types from Java or C++
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

				vertices.forEach(v -> log.warn("Operand used in OpStatement with vertex: {}", v));
			}
		}
	}

	private void dumpVertices(Collection<Vertex> vertices) {
		log.debug("Dumping vertices: {}", vertices.size());

		int i = 0;
		for (Vertex v : vertices) {
			log.debug("Vertex {}: {}", i++, v);
		}
	}

	private void dumpPaths(Collection<Path> paths) {
		log.debug("Number of paths: {}", paths.size());

		for (Path p : paths) {
			log.debug("Path of length: {}", p.size());
			for (Object o : p) {
				log.debug("Path step: {}", o);
			}
		}
	}
}
