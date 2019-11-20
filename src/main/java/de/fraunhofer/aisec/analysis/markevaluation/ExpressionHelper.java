
package de.fraunhofer.aisec.analysis.markevaluation;

import de.fraunhofer.aisec.analysis.scp.ConstantValue;
import de.fraunhofer.aisec.analysis.structures.Pair;
import de.fraunhofer.aisec.analysis.structures.ResultWithContext;
import de.fraunhofer.aisec.mark.markDsl.*;
import de.fraunhofer.aisec.mark.markDsl.impl.AlternativeExpressionImpl;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/** Static helper methods for evaluating MARK expressions. */
public class ExpressionHelper {

	private static final Logger log = LoggerFactory.getLogger(ExpressionHelper.class);

	private ExpressionHelper() {
		// hide
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
					+ fExpr.getArgs().stream().map(ExpressionHelper::argToString).collect(Collectors.joining(", "))
					+ ")";
		} else if (expr instanceof LiteralListExpression) {
			return "[ "
					+ ((LiteralListExpression) expr).getValues().stream().map(Literal::getValue).collect(Collectors.joining(", "))
					+ " ]";
		} else if (expr instanceof RepetitionExpression) {
			RepetitionExpression inner = (RepetitionExpression) expr;
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

	@Nullable
	public static String asString(ResultWithContext opt) {
		if (opt == null) {
			return null;
		}

		return asString(opt.get());
	}

	@Nullable
	public static String asString(Object opt) {
		if (opt == null) {
			return null;
		}

		if (opt instanceof String) {
			return (String) opt;
		}

		if (opt instanceof ConstantValue && ((ConstantValue) opt).isString()) {
			return (String) ((ConstantValue) opt).getValue();
		}

		return null;
	}

	@Nullable
	public static Number asNumber(ResultWithContext opt) {
		if (opt == null) {
			return null;
		}
		return asNumber(opt.get());
	}

	public static Number asNumber(Object opt) {
		if (opt == null) {
			return null;
		}
		if (opt instanceof Integer) {
			return (Integer) opt;
		}
		if (opt instanceof ConstantValue && ((ConstantValue) opt).isNumeric()) {
			return (Number) ((ConstantValue) opt).getValue();
		}

		return null;
	}

	@Nullable
	public static Boolean asBoolean(ResultWithContext opt) {
		if (opt == null) {
			return null;
		}

		return asBoolean(opt.get());
	}

	@Nullable
	public static Boolean asBoolean(Object opt) {
		if (opt == null) {
			return null;
		}

		if (opt instanceof Boolean) {
			return (Boolean) opt;
		}

		if (opt instanceof ConstantValue && ((ConstantValue) opt).isBoolean()) {
			return (Boolean) ((ConstantValue) opt).getValue();
		}

		return null;

	}

	/**
	 * Traverses the whole expression and collects all markvars
	 *
	 * @param expr the expression to traverse
	 * @param vars [out] collects all vars in the expression
	 */
	public static void collectVars(Expression expr, Set<String> vars) {
		if (expr instanceof OrderExpression) {
			// will not contain vars
		} else if (expr instanceof LogicalOrExpression) {
			collectVars(((LogicalOrExpression) expr).getLeft(), vars);
			collectVars(((LogicalOrExpression) expr).getRight(), vars);
		} else if (expr instanceof LogicalAndExpression) {
			collectVars(((LogicalAndExpression) expr).getLeft(), vars);
			collectVars(((LogicalAndExpression) expr).getRight(), vars);
		} else if (expr instanceof ComparisonExpression) {
			collectVars(((ComparisonExpression) expr).getLeft(), vars);
			collectVars(((ComparisonExpression) expr).getRight(), vars);
		} else if (expr instanceof MultiplicationExpression) {
			collectVars(((MultiplicationExpression) expr).getLeft(), vars);
			collectVars(((MultiplicationExpression) expr).getRight(), vars);
		} else if (expr instanceof UnaryExpression) {
			collectVars(((UnaryExpression) expr).getExp(), vars);
		} else if (expr instanceof Literal) {
			// not a var
		} else if (expr instanceof Operand) {
			vars.add(((Operand) expr).getOperand());
		} else if (expr instanceof FunctionCallExpression) {
			for (Argument ex : ((FunctionCallExpression) expr).getArgs()) {
				if (ex instanceof Expression) {
					collectVars((Expression) ex, vars);
				} else {
					log.error("This should not happen in MARK. Not an expression: " + ex.getClass());
				}
			}
		} else if (expr instanceof LiteralListExpression) {
			// does not contain vars
		}
	}

	/**
	 * Collects all markinstances in the expression
	 *
	 * @param expr the expression to traverse
	 * @param instances [out] Set of used markinstances in the expression
	 */
	public static void collectMarkInstances(OrderExpression expr, Set<String> instances) {
		if (expr instanceof Terminal) {
			Terminal inner = (Terminal) expr;
			instances.add(inner.getEntity());
		} else if (expr instanceof SequenceExpression) {
			SequenceExpression inner = (SequenceExpression) expr;
			collectMarkInstances(inner.getLeft(), instances);
			collectMarkInstances(inner.getRight(), instances);
		} else if (expr instanceof RepetitionExpression) {
			RepetitionExpression inner = (RepetitionExpression) expr;
			collectMarkInstances(inner.getExpr(), instances);
		} else if (expr instanceof AlternativeExpressionImpl) {
			AlternativeExpression inner = (AlternativeExpression) expr;
			collectMarkInstances(inner.getLeft(), instances);
			collectMarkInstances(inner.getRight(), instances);
		}
	}

	public static void getRefsFromExp(
			Expression exp, Set<String> entityRefs, Set<String> functionRefs) {
		if (exp == null) {
			log.error("Expression is null, cannot get refs");
			return;
		}
		if (exp instanceof ComparisonExpression) {
			getRefsFromExp((((ComparisonExpression) exp).getLeft()), entityRefs, functionRefs);
			getRefsFromExp((((ComparisonExpression) exp).getRight()), entityRefs, functionRefs);
		} else if (exp instanceof LiteralListExpression) {
			// only literals
		} else if (exp instanceof LogicalAndExpression) {
			getRefsFromExp((((LogicalAndExpression) exp).getLeft()), entityRefs, functionRefs);
			getRefsFromExp((((LogicalAndExpression) exp).getRight()), entityRefs, functionRefs);
		} else if (exp instanceof AlternativeExpression) {
			getRefsFromExp((((AlternativeExpression) exp).getLeft()), entityRefs, functionRefs);
			getRefsFromExp((((AlternativeExpression) exp).getRight()), entityRefs, functionRefs);
		} else if (exp instanceof Terminal) {
			entityRefs.add(((Terminal) exp).getEntity() + "." + ((Terminal) exp).getOp());
		} else if (exp instanceof SequenceExpression) {
			getRefsFromExp((((SequenceExpression) exp).getLeft()), entityRefs, functionRefs);
			getRefsFromExp((((SequenceExpression) exp).getRight()), entityRefs, functionRefs);
		} else if (exp instanceof RepetitionExpression) {
			getRefsFromExp((((RepetitionExpression) exp).getExpr()), entityRefs, functionRefs);
		} else if (exp instanceof OrderExpression) { // collects also ExclusionExpression
			getRefsFromExp(((OrderExpression) exp).getExp(), entityRefs, functionRefs);
		} else if (exp instanceof MultiplicationExpression) {
			getRefsFromExp((((MultiplicationExpression) exp).getLeft()), entityRefs, functionRefs);
			getRefsFromExp((((MultiplicationExpression) exp).getRight()), entityRefs, functionRefs);
		} else if (exp instanceof LogicalOrExpression) {
			getRefsFromExp((((LogicalOrExpression) exp).getLeft()), entityRefs, functionRefs);
			getRefsFromExp((((LogicalOrExpression) exp).getRight()), entityRefs, functionRefs);
		} else if (exp instanceof FunctionCallExpression) {
			functionRefs.add(((FunctionCallExpression) exp).getName());
			for (Argument s : ((FunctionCallExpression) exp).getArgs()) {
				if (s instanceof Expression) {
					getRefsFromExp((Expression) s, entityRefs, functionRefs);
				} else {
					log.error("Argument is not an Expression, but a {}", s.getClass());
				}
			}
		} else if (exp instanceof Literal) {
			// only literal
		} else if (exp instanceof Operand) {
			entityRefs.add(((Operand) exp).getOperand());
		} else if (exp instanceof UnaryExpression) {
			getRefsFromExp((((UnaryExpression) exp).getExp()), entityRefs, functionRefs);
		} else {
			log.error("Not implemented yet: {} {}", exp.getClass(), exp);
		}
	}

}
