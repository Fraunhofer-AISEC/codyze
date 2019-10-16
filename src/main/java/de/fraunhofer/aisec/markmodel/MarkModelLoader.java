
package de.fraunhofer.aisec.markmodel;

import de.fraunhofer.aisec.crymlin.utils.Pair;
import de.fraunhofer.aisec.mark.markDsl.*;
import de.fraunhofer.aisec.markmodel.fsm.FSM;
import de.fraunhofer.aisec.markmodel.fsm.Node;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.python.jline.internal.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses a MarkModel provided by XText in the form of an ECore hierarchy into a simple (ECore-free) {@code Mark} model that the analysis server can work with.
 *
 * @author julian
 */
public class MarkModelLoader {

	private static final Logger log = LoggerFactory.getLogger(MarkModelLoader.class);

	@NonNull
	public Mark load(HashMap<String, MarkModel> markModels, String onlyfromthisfile) {
		Mark m = new Mark();

		for (Map.Entry<String, MarkModel> entry : markModels.entrySet()) {
			if (onlyfromthisfile != null && !onlyfromthisfile.equals(entry.getKey())) {
				// if we want the mark model only for one file, this can be specified here.
				// dependencies then still work, iff they were part of the xtext
				continue;
			}
			MarkModel markModel = entry.getValue();
			String packagename = null;
			if (markModel.getPackage() != null) {
				packagename = markModel.getPackage().getName();
			}
			// Parse "entities" (=cryptographic objects)
			for (EntityDeclaration decl : markModel.getDecl()) {
				MEntity entity = parseEntity(decl);
				entity.setPackageName(packagename);
				m.addEntities(entity.getName(), entity);
			}
		}
		for (Map.Entry<String, MarkModel> entry : markModels.entrySet()) {
			if (onlyfromthisfile != null && !onlyfromthisfile.equals(entry.getKey())) {
				// if we want the mark model only for one file, this can be specified here.
				// dependencies then still work, iff they were part of the xtext
				continue;
			}
			MarkModel markModel = entry.getValue();
			// Parse rules
			for (RuleDeclaration r : markModel.getRule()) {
				// todo @FW: should rules also have package names? what is the exact reasoning behind
				// packages?
				MRule rule = parseRule(r, m, entry.getKey());
				m.getRules().add(rule);
			}
		}

		// parse all FSMs
		for (MRule rule : m.getRules()) {
			if (rule.getStatement() != null && rule.getStatement().getEnsure() != null && rule.getStatement().getEnsure().getExp() instanceof OrderExpression) {
				OrderExpression inner = (OrderExpression) rule.getStatement().getEnsure().getExp();
				FSM fsm = new FSM();
				fsm.sequenceToFSM(inner.getExp());
				rule.setFSM(fsm);
			}
		}
		/*
		 * VALIDATE todo validate MARK files. check e.g.: - Entities - one function call is not part of two ops (of one or two entities) if this would be the case, many
		 * strange things will happen - Rules - each alias is defined in the using-part of the rule - each entity reference actually has an entity - one order is only
		 * allowed to reference the same alias to discuss: - should multiple aliases to a same entity be allowed? (at least for order, this is problematic for analysis,
		 * and likely not needed)
		 */

		// todo cleanup the following code, or remove if xtext performs this validation already
		// collect all references to entities
		for (MRule rule : m.getRules()) {
			final HashSet<String> entityRefs = new HashSet<>();
			final HashSet<String> functionRefs = new HashSet<>();
			collectEntityReferences(rule, entityRefs, functionRefs);
		}

		for (MRule rule : m.getRules()) {
			if (rule.getStatement() != null && rule.getStatement().getEnsure() != null && rule.getStatement().getEnsure().getExp() instanceof OrderExpression) {

				// check that the fsm is valid:
				// todo remove once the modelloader performs these checks!
				HashSet<Node> worklist = new HashSet<>(rule.getFSM().getStart());
				HashSet<Node> seen = new HashSet<>();
				while (!worklist.isEmpty()) {
					HashSet<Node> nextWorkList = new HashSet<>();
					seen.addAll(worklist);

					for (Node n : worklist) {
						// check that the op exists
						if (!n.isFake()) {
							MEntity entity = rule.getEntityReferences().get(n.getBase()).getValue1();
							String entityName = rule.getEntityReferences().get(n.getBase()).getValue0();
							if (entity == null) {
								log.error("Entity is not parsed: {} which is specified in rule {}", entityName, rule.getName());
							}
							else {
								MOp op = entity.getOp(n.getOp());
								if (op == null) {
									log.error("Entity {} does not contain op {} which is specified in rule {}", entity.getName(), n.getOp(), rule.getName());
								}
							}
						}
						for (Node s : n.getSuccessors()) {
							if (!seen.contains(s)) {
								nextWorkList.add(s);
							}
						}
					}
					worklist = nextWorkList;
				}
			}
		}
		return m;
	}

	private void collectEntityReferences(MRule rule, HashSet<String> entityRefs, HashSet<String> functionRefs) {
		if (rule.getStatement() == null) {
			return;
		}

		//    for (Map.Entry<String, Pair<String, MEntity>> entry :
		// rule.getEntityReferences().entrySet()) {
		//      if (entry.getValue().getValue1() == null) {
		//        System.out.println(entry.getKey() + ": " + entry.getValue().getValue0() + " - NULL");
		//      } else {
		//        System.out.println(
		//            entry.getKey()
		//                + ": "
		//                + entry.getValue().getValue0()
		//                + " - "
		//                + entry.getValue().getValue1().getName());
		//      }
		//    }

		if (rule.getStatement().getEnsure() != null) {
			getRefsFromExp(rule.getStatement().getEnsure().getExp(), entityRefs, functionRefs);
		}
		//    System.out.println("eref");
		//    for(String s: entityRefs) {
		//      System.out.println("\t" + s);
		//    }
		//    System.out.println("fref");
		//    for(String s: functionRefs) {
		//      System.out.println("\t" + s);
		//    }
	}

	private void getRefsFromExp(Expression exp, HashSet<String> entityRefs, HashSet<String> functionRefs) {
		if (exp instanceof ComparisonExpression) {
			getRefsFromExp((((ComparisonExpression) exp).getLeft()), entityRefs, functionRefs);
			getRefsFromExp((((ComparisonExpression) exp).getRight()), entityRefs, functionRefs);
		}
		else if (exp instanceof LiteralListExpression) {
			// only literals
		}
		else if (exp instanceof LogicalAndExpression) {
			getRefsFromExp((((LogicalAndExpression) exp).getLeft()), entityRefs, functionRefs);
			getRefsFromExp((((LogicalAndExpression) exp).getRight()), entityRefs, functionRefs);
		}
		else if (exp instanceof AlternativeExpression) {
			getRefsFromExp((((AlternativeExpression) exp).getLeft()), entityRefs, functionRefs);
			getRefsFromExp((((AlternativeExpression) exp).getRight()), entityRefs, functionRefs);
		}
		else if (exp instanceof Terminal) {
			entityRefs.add(((Terminal) exp).getEntity() + "." + ((Terminal) exp).getOp());
		}
		else if (exp instanceof SequenceExpression) {
			getRefsFromExp((((SequenceExpression) exp).getLeft()), entityRefs, functionRefs);
			getRefsFromExp((((SequenceExpression) exp).getRight()), entityRefs, functionRefs);
		}
		else if (exp instanceof RepetitionExpression) {
			getRefsFromExp((((RepetitionExpression) exp).getExpr()), entityRefs, functionRefs);
		}
		else if (exp instanceof OrderExpression) { // collects also ExclusionExpression
			getRefsFromExp(((OrderExpression) exp).getExp(), entityRefs, functionRefs);
		}
		else if (exp instanceof MultiplicationExpression) {
			getRefsFromExp((((MultiplicationExpression) exp).getLeft()), entityRefs, functionRefs);
			getRefsFromExp((((MultiplicationExpression) exp).getRight()), entityRefs, functionRefs);
		}
		else if (exp instanceof LogicalOrExpression) {
			getRefsFromExp((((LogicalOrExpression) exp).getLeft()), entityRefs, functionRefs);
			getRefsFromExp((((LogicalOrExpression) exp).getRight()), entityRefs, functionRefs);
		}
		else if (exp instanceof FunctionCallExpression) {
			functionRefs.add(((FunctionCallExpression) exp).getName());
			for (Argument s : ((FunctionCallExpression) exp).getArgs()) {
				if (s instanceof Expression) {
					getRefsFromExp((Expression) s, entityRefs, functionRefs);
				}
				else {
					log.error("Argument is not an Expression, but a {}", s.getClass());
				}
			}
		}
		else if (exp instanceof Literal) {
			// only literal
		}
		else if (exp instanceof Operand) {
			entityRefs.add(((Operand) exp).getOperand());
		}
		else if (exp instanceof UnaryExpression) {
			getRefsFromExp((((UnaryExpression) exp).getExp()), entityRefs, functionRefs);
		}
		else {
			log.error("Not implemented yet: {} {}", exp.getClass(), exp);
		}
	}

	@NonNull
	public Mark load(HashMap<String, MarkModel> markModels) {
		return load(markModels, null);
	}

	private MRule parseRule(RuleDeclaration rule, Mark mark, String containedInThisFile) {
		MRule mRule = new MRule(rule.getName());
		mRule.setStatement(rule.getStmt()); // todo remove in the long run
		mRule.setErrorMessage(rule.getStmt().getMsg());

		HashMap<String, Pair<String, MEntity>> entityReferences = new HashMap<>();
		rule.getStmt().getEntities().forEach(entity -> {
			MEntity ref = mark.getEntity(entity.getE().getName());
			if (ref == null) {
				log.error("Entity {} not loaded. Referenced in rule {} in file {}", entity.getE().getName(), rule.getName(), containedInThisFile);
			}
			Pair<String, MEntity> e = new Pair<>(entity.getE().getName(), ref);
			entityReferences.put(entity.getN(), e);
		});

		mRule.setEntityReferences(entityReferences);

		return mRule;
	}

	private MEntity parseEntity(EntityDeclaration decl) {
		MEntity mEntity = new MEntity();
		mEntity.setName(decl.getName());
		if (decl.getSuperType() != null) {
			mEntity.setSuper(decl.getSuperType().getName());
		}
		parseEntityContent(decl.getContent(), mEntity);
		return mEntity;
	}

	/**
	 * Note: Modifies the given MEntity object.
	 *
	 * @param stmts
	 * @param me
	 */
	private void parseEntityContent(List<EntityStatement> stmts, @NonNull MEntity me) {
		if (stmts == null) {
			return;
		}
		for (EntityStatement c : stmts) {
			if (c instanceof OpDeclaration) {
				OpDeclaration op = (OpDeclaration) c;
				parseOp(op, me);
			}
			else if (c instanceof VariableDeclaration) {
				VariableDeclaration op = (VariableDeclaration) c;
				parseVar(op, me);
			}
			else {
				Log.warn("Entity not yet implemented: Handling of Mark {}", c.getClass().getName());
			}
		}
	}

	private void parseVar(VariableDeclaration varDecl, MEntity me) {
		MVar mVar = new MVar();
		mVar.setName(varDecl.getName());
		mVar.setType(varDecl.getType());
		me.getVars().add(mVar);
	}

	private void parseOp(OpDeclaration op, MEntity me) {
		MOp mOp = new MOp(me);
		mOp.setName(op.getName());
		mOp.getStatements().addAll(op.getStmts());
		me.getOps().add(mOp);
	}
}
