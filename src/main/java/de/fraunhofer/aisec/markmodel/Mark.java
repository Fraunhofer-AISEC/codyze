
package de.fraunhofer.aisec.markmodel;

import de.fraunhofer.aisec.mark.markDsl.Argument;
import de.fraunhofer.aisec.mark.markDsl.ComparisonExpression;
import de.fraunhofer.aisec.mark.markDsl.EntityDeclaration;
import de.fraunhofer.aisec.mark.markDsl.Expression;
import de.fraunhofer.aisec.mark.markDsl.FunctionCallExpression;
import de.fraunhofer.aisec.mark.markDsl.Literal;
import de.fraunhofer.aisec.mark.markDsl.LiteralListExpression;
import de.fraunhofer.aisec.mark.markDsl.LogicalAndExpression;
import de.fraunhofer.aisec.mark.markDsl.LogicalOrExpression;
import de.fraunhofer.aisec.mark.markDsl.MultiplicationExpression;
import de.fraunhofer.aisec.mark.markDsl.Operand;
import de.fraunhofer.aisec.mark.markDsl.OrderExpression;
import de.fraunhofer.aisec.mark.markDsl.UnaryExpression;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class Mark {

	private static final Logger log = LoggerFactory.getLogger(Mark.class);

	@NonNull
	private HashMap<String, MEntity> entityByName = new HashMap<>();

	@NonNull
	private List<MRule> rules = new ArrayList<>();

	public static void collectVars(Expression expr, HashSet<String> vars) {
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

	public void addEntities(String name, MEntity ent) {
		this.entityByName.put(name, ent);
	}

	public Collection<MEntity> getEntities() {
		return this.entityByName.values();
	}

	public MEntity getEntity(EntityDeclaration e) {
		if (e == null) {
			return null;
		}
		return getEntity(e.getName());
	}

	public MEntity getEntity(@NonNull String name) {
		return entityByName.get(name);
	}

	@NonNull
	public List<MRule> getRules() {
		return this.rules;
	}

	public void reset() {
		// nothing to do for the rules
		for (MEntity entity : getEntities()) {
			for (MOp op : entity.getOps()) {
				op.reset();
			}
		}
	}
}
