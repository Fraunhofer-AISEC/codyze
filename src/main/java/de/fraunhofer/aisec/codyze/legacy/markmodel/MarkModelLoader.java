
package de.fraunhofer.aisec.codyze.legacy.markmodel;

import de.fraunhofer.aisec.codyze.legacy.analysis.markevaluation.ExpressionHelper;
import de.fraunhofer.aisec.codyze.legacy.config.DisabledMarkRulesValue;
import de.fraunhofer.aisec.mark.markDsl.EntityDeclaration;
import de.fraunhofer.aisec.mark.markDsl.EntityStatement;
import de.fraunhofer.aisec.mark.markDsl.MarkModel;
import de.fraunhofer.aisec.mark.markDsl.OpDeclaration;
import de.fraunhofer.aisec.mark.markDsl.RuleDeclaration;
import de.fraunhofer.aisec.mark.markDsl.VariableDeclaration;
import kotlin.Pair;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.python.jline.internal.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Parses a MarkModel provided by XText in the form of an ECore hierarchy into a simple (ECore-free) {@code Mark} model that the analysis server can work with.
 *
 * @author julian
 */
public class MarkModelLoader {

	private static final Logger log = LoggerFactory.getLogger(MarkModelLoader.class);

	@NonNull
	public Mark load(Map<String, MarkModel> markModels, Map<String, DisabledMarkRulesValue> packageToDisabledMarkRules, @Nullable String onlyfromthisfile) {
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

			// For mark files without package the key is an empty string in the map
			String key = markModel.getPackage() == null ? "" : markModel.getPackage().getName();

			DisabledMarkRulesValue disabledRules = packageToDisabledMarkRules.getOrDefault(key,
				new DisabledMarkRulesValue(false, Collections.emptySet()));

			// check if entire package should be disabled
			if (disabledRules.isDisablePackage()) {
				log.info("Disabled all mark rules in package {}", markModel.getPackage().getName());
			} else {
				// Parse rules
				for (RuleDeclaration r : markModel.getRule()) {
					// todo @FW: should rules also have package names? what is the exact reasoning behind
					// packages?

					// check if rule should be disabled
					if (disabledRules.getDisabledMarkRuleNames().contains(r.getName())) {
						log.info("Disabled mark rule {} in package {}", r.getName(), markModel.getPackage() == null ? "" : markModel.getPackage().getName());
						continue;
					}

					MRule rule = parseRule(r, m, entry.getKey());
					m.getRules().add(rule);
				}
			}
		}

		/*
		 * VALIDATE todo validate MARK files. check e.g.: - Entities - one function call is not part of two ops (of one or two entities) if this would be the case, many
		 * strange things will happen - Rules - each alias is defined in the using-part of the rule - each entity reference actually has an entity - one order is only
		 * allowed to reference the same alias to discuss: - should multiple aliases to a same entity be allowed? (at least for order, this is problematic for analysis,
		 * and likely not needed)
		 */

		// collect all references to entities
		for (MRule rule : m.getRules()) {
			final HashSet<String> entityRefs = new HashSet<>();
			final HashSet<String> functionRefs = new HashSet<>();
			collectEntityReferences(rule, entityRefs, functionRefs);
		}

		return m;
	}

	private static void collectEntityReferences(MRule rule, Set<String> entityRefs, Set<String> functionRefs) {
		if (rule.getStatement() == null) {
			return;
		}

		if (rule.getStatement().getEnsure() != null) {
			ExpressionHelper.getRefsFromExp(rule.getStatement().getEnsure().getExp(), entityRefs, functionRefs);
		}
	}

	@NonNull
	public Mark load(Map<String, MarkModel> markModels, Map<String, DisabledMarkRulesValue> packageToDisabledMarkRules) {
		return load(markModels, packageToDisabledMarkRules, null);
	}

	@NonNull
	public Mark load(Map<String, MarkModel> markModels, @Nullable String onlyfromthisfile) {
		return load(markModels, Collections.emptyMap(), onlyfromthisfile);
	}

	@NonNull
	public Mark load(Map<String, MarkModel> markModels) {
		return load(markModels, Collections.emptyMap(), null);
	}

	private MRule parseRule(RuleDeclaration rule, Mark mark, String containedInThisFile) {
		MRule mRule = new MRule(rule.getName());
		mRule.setStatement(rule.getStmt());
		mRule.setErrorMessage(rule.getStmt().getMsg());

		var entityReferences = new HashMap<String, Pair<String, MEntity>>();
		rule.getStmt()
				.getEntities()
				.forEach(
					entity -> {
						MEntity ref = mark.getEntity(entity.getE().getName());
						if (ref == null) {
							log.error(
								"Entity {} not loaded. Referenced in rule {} in file {}",
								entity.getN(),
								rule.getName(),
								containedInThisFile);
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
			} else if (c instanceof VariableDeclaration) {
				VariableDeclaration op = (VariableDeclaration) c;
				parseVar(op, me);
			} else {
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
