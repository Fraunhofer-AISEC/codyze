
package de.fraunhofer.aisec.analysis.markevaluation;

import com.google.common.collect.Lists;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.CPGInstanceContext;
import de.fraunhofer.aisec.analysis.structures.ConstantValue;
import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.analysis.structures.MarkContext;
import de.fraunhofer.aisec.analysis.structures.MarkContextHolder;
import de.fraunhofer.aisec.analysis.structures.MarkIntermediateResult;
import de.fraunhofer.aisec.analysis.structures.Pair;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.cpg.helpers.Benchmark;
import de.fraunhofer.aisec.crymlin.CrymlinQueryWrapper;
import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import de.fraunhofer.aisec.mark.markDsl.AliasedEntityExpression;
import de.fraunhofer.aisec.mark.markDsl.OpStatement;
import de.fraunhofer.aisec.mark.markDsl.RuleStatement;
import de.fraunhofer.aisec.markmodel.MEntity;
import de.fraunhofer.aisec.markmodel.MOp;
import de.fraunhofer.aisec.markmodel.MRule;
import de.fraunhofer.aisec.markmodel.Mark;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;

import static java.lang.Math.toIntExact;

/**
 * Evaluates all loaded MARK rules against the CPG.
 *
 * returns a number of Findings if any of the rules are violated
 */
public class Evaluator {
	private static final Logger log = LoggerFactory.getLogger(Evaluator.class);

	@NonNull
	private final Mark markModel;

	@NonNull
	private final ServerConfiguration config;

	public Evaluator(@NonNull Mark markModel, @NonNull ServerConfiguration config) {
		this.markModel = markModel;
		this.config = config;
	}

	/**
	 * Evaluates the {@code markModel} against the currently analyzed program (CPG).
	 *
	 * <p>
	 * This is the core of the MARK evaluation.
	 *
	 * @param result representing the analysed program, i.e., the CPG
	 * @param ctx [out] the context storing the result of the evaluation. This could also include results from previous steps
	 */
	public TranslationResult evaluate(@NonNull TranslationResult result, @NonNull final AnalysisContext ctx) {

		Benchmark bOuter = new Benchmark(this.getClass(), "Mark evaluation");

		try (TraversalConnection traversal = new TraversalConnection(TraversalConnection.Type.OVERFLOWDB)) { // connects to the DB

			log.info("Precalculating matching nodes");
			assignCallVerticesToOps(traversal.getCrymlinTraversal());

			log.info("Evaluate forbidden calls");
			Benchmark b = new Benchmark(this.getClass(), "Evaluate forbidden calls");
			ForbiddenEvaluator forbiddenEvaluator = new ForbiddenEvaluator(this.markModel);
			forbiddenEvaluator.evaluate(ctx);
			b.stop();

			log.info("Evaluate rules");
			b = new Benchmark(this.getClass(), "Evaluate rules");
			evaluateRules(ctx, traversal.getCrymlinTraversal());
			b.stop();

			bOuter.stop();

			return result;
		}
		finally {
			// reset everything attached to this model
			this.markModel.reset();
		}
	}

	/**
	 * Iterate over all MOps in all MEntities, find all call statements in CPG and assign them to their respective MOp.
	 *
	 * After this method, all call statements can be retrieved by MOp.getAllVertices(), MOp.getStatements(), and MOp.getVertexToCallStatementsMap().
	 *
	 * @param crymlinTraversal traversal-connection to the DB
	 */
	private void assignCallVerticesToOps(@NonNull CrymlinTraversalSource crymlinTraversal) {
		Benchmark b = new Benchmark(this.getClass(), "Precalculating matching nodes");
		// iterate over all entities and precalculate:
		// - call statements to vertices
		for (MEntity ent : markModel.getEntities()) {
			log.info("Precalculating call statements for entity {}", ent.getName());
			ent.parseVars();
			for (MOp op : ent.getOps()) {
				log.debug("Looking for call statements for {}", op.getName());
				int numMatches = 0;
				for (OpStatement a : op.getStatements()) {
					Set<Vertex> temp = CrymlinQueryWrapper.getVerticesForFunctionDeclaration(a.getCall(), ent, crymlinTraversal);
					log.debug(
						"{}({}):{}",
						a.getCall().getName(),
						String.join(", ", a.getCall().getParams()),
						temp.size());
					numMatches += temp.size();
					op.addVertex(a, temp);
				}
				op.setParsingFinished();
				if (numMatches > 0) {
					log.info("Found {} call statements in the cpg for {}", numMatches, op.getName());
				}
			}
		}
		b.stop();
	}

	/**
	 * Evaluates all rules and creates findings.
	 *
	 * @param ctx the result/analysis context
	 * @param crymlinTraversal connection to the db
	 */
	private void evaluateRules(AnalysisContext ctx, @NonNull CrymlinTraversalSource crymlinTraversal) {

		for (MRule rule : this.markModel.getRules()) {
			log.info("checking rule {}", rule.getName());

			/* Evaluate "using" part and collect the instances of MARK entities, as well as the potential vertex representing the base object variables. */
			List<List<Pair<String, Vertex>>> entities = findInstancesForEntities(rule);

			/* Create evaluation context. */
			// Generate all combinations of instances for each entity.
			// We take the n-th cartesian product of all _possible_ program variables that correspond to Mark entities.
			// A CPGInstanceContext is a specific interpretation of a Mark rule that needs to be evaluated.
			MarkContextHolder markCtxHolder = createMarkContext(entities);

			ExpressionEvaluator ee = new ExpressionEvaluator(rule, ctx, config, crymlinTraversal, markCtxHolder);

			// Evaluate "when" part, if present (will possibly remove entries from markCtxhHlder)
			evaluateWhen(rule, markCtxHolder, ee);

			/* Evaluate "ensure" part */
			Map<Integer, MarkIntermediateResult> result = ee.evaluateExpression(rule.getStatement().getEnsure().getExp());

			log.info("Got {} results", result.size());

			/* Get findings from "result" */
			Collection<Finding> findings = getFindings(result, ctx.getCurrentFile(), markCtxHolder, rule);
			ctx.getFindings().addAll(findings);
		}
	}

	private Collection<Finding> getFindings(@NonNull Map<Integer, MarkIntermediateResult> result, @NonNull URI currentFile, @NonNull MarkContextHolder markCtxHolder,
			@NonNull MRule rule) {
		Collection<Finding> findings = new HashSet<>();

		for (Map.Entry<Integer, MarkIntermediateResult> entry : result.entrySet()) {
			// the value of the result should always be boolean, as this should be the result of the topmost expression
			Object value = ConstantValue.unbox(entry.getValue());
			if (value instanceof Boolean && entry.getValue() instanceof ConstantValue) {
				/*
				 * if we did not add a finding during expression evaluation (e.g., as it is the case in the order evaluation), add a new finding which references all
				 * responsible vertices.
				 */

				MarkContext c = markCtxHolder.getContext(entry.getKey());

				if (!c.isFindingAlreadyAdded()) {
					List<Range> ranges = new ArrayList<>();
					if (((ConstantValue) entry.getValue()).getResponsibleVertices().isEmpty()) {
						ranges.add(new Range(new Position(-1, -1),
							new Position(-1, -1)));
					} else {
						// responsible vertices are stored in the result
						for (Vertex v : ((ConstantValue) entry.getValue()).getResponsibleVertices()) {
							if (v == null) {
								continue;
							}
							int startLine = toIntExact((Long) v.property("startLine").value()) - 1;
							int endLine = toIntExact((Long) v.property("endLine").value()) - 1;
							int startColumn = toIntExact((Long) v.property("startColumn").value()) - 1;
							int endColumn = toIntExact((Long) v.property("endColumn").value()) - 1;
							ranges.add(new Range(new Position(startLine, startColumn),
								new Position(endLine, endColumn)));
						}
					}
					boolean isRuleViolated = !(Boolean) value;
					findings.add(new Finding(
						"MarkRuleEvaluationFinding: Rule "
								+ rule.getName()
								+ (isRuleViolated ? " violated" : " verified"),
						currentFile,
						rule.getErrorMessage(),
						ranges,
						isRuleViolated));
				}
			} else if (value == null || value.equals(ConstantValue.NULL)) {
				log.warn("Unable to evaluate rule {}, result was null", rule.getName());
			} else {
				log.error("Unable to evaluate rule {}, result is not a boolean, but {}", rule.getName(), value.getClass().getSimpleName());
			}
		}
		return findings;
	}

	/**
	 * Evaluates the "when" part of a rule and removes all entries from "markCtxHolder" to which the condition does not apply.
	 *
	 * If the "when" part is not satisfied, the "MarkContextHolder" will be empty.
	 *
	 * The markContextHolder parameter is modified as a side effect.
	 *
	 * @param rule
	 * @param markCtxHolder
	 * @param ee
	 * @return
	 */
	private void evaluateWhen(@NonNull MRule rule, @NonNull MarkContextHolder markCtxHolder, @NonNull ExpressionEvaluator ee) {
		// do not create any findings during When-Evaluation
		markCtxHolder.setCreateFindingsDuringEvaluation(false);

		RuleStatement s = rule.getStatement();
		if (s.getCond() != null) {
			Map<Integer, MarkIntermediateResult> result = ee.evaluateExpression(s.getCond().getExp());

			for (Map.Entry<Integer, MarkIntermediateResult> entry : result.entrySet()) {
				Object value = ConstantValue.unbox(entry.getValue());
				if (value == null || value.equals(ConstantValue.NULL)) {
					log.warn("Unable to evaluate when-part of rule {}, result was null", rule.getName());
					continue;
				} else if (!(value instanceof Boolean)) {
					log.error("Unable to evaluate when-part of rule {}, result is not a boolean, but {}", rule.getName(), value.getClass().getSimpleName());
					continue;
				}

				if (value.equals(false)) {
					log.info("Precondition is false, do not evaluate ensure.");
					markCtxHolder.removeContext(entry.getKey());
				} else {
					log.debug("Precondition is true, we will evaluate this context in the following.");
				}
			}
		}

		// now we can create inline findings
		markCtxHolder.setCreateFindingsDuringEvaluation(true);
	}

	private MarkContextHolder createMarkContext(List<List<Pair<String, Vertex>>> entities) {
		MarkContextHolder context = new MarkContextHolder();
		for (List<Pair<String, Vertex>> list : Lists.cartesianProduct(entities)) {
			CPGInstanceContext instanceCtx = new CPGInstanceContext();
			for (Pair<String, Vertex> p : list) {
				String markInstanceName = p.getValue0();
				Vertex v = p.getValue1();
				if (v != null) {
					instanceCtx.putMarkInstance(markInstanceName, v);
				}
			}
			context.addInitialInstanceContext(instanceCtx);
		}
		return context;
	}

	/**
	 * Collect all entities, and calculate which instances correspond to the entity.
	 *
	 * For instance, an entity "Cipher" may be referenced by "c1" and "c2" in a MARK rule.
	 *
	 * The "Cipher" entity might refer to three actual object variables in the program: "v1", "v2", "v3".
	 *
	 * In that case, the function will return [ [ (c1, v1) , (c1, v2), (c1, v3) ] [ (c2, c1), (c2, v2), (c2, v3) ] ]
	 *
	 * @param rule
	 * @return
	 */
	private List<List<Pair<String, Vertex>>> findInstancesForEntities(MRule rule) {
		RuleStatement ruleStmt = rule.getStatement();
		List<List<Pair<String, Vertex>>> entities = new ArrayList<>();
		// Find entities whose ops are used in the current Mark rule.
		// We collect all entities and calculate which instances (=program variables) correspond to the entity.
		// entities is a map with key: name of the Mark Entity (e.g., "b"). value: Vertex to which the program variable REFERS_TO.
		for (AliasedEntityExpression entity : ruleStmt.getEntities()) {
			HashSet<Vertex> instanceVariables = new HashSet<>();
			MEntity referencedEntity = this.markModel.getEntity(entity.getE());
			if (referencedEntity == null) {
				log.warn("Unexpected: Mark rule {} references an unknown entity {}", rule.getName(), entity.getN());
				continue;
			}
			for (MOp op : referencedEntity.getOps()) {
				for (Vertex vertex : op.getAllVertices()) {

					// Program variable is either the Base of some method call ...
					Optional<Vertex> ref = CrymlinQueryWrapper.getBaseOfCallExpression(vertex);
					ref.ifPresent(instanceVariables::add);
					// .. or assignee of a VariableDeclaration with a ConstructorExpression.
					Optional<Vertex> assignee = CrymlinQueryWrapper.getAssigneeOfConstructExpression(vertex);
					assignee.ifPresent(instanceVariables::add);

					if (ref.isEmpty() && assignee.isEmpty()) {
						log.warn("Did not find an instance variable for entity {} when searching at node {}", referencedEntity.getName(),
							vertex.property("code").value());
					}
				}
			}
			ArrayList<Pair<String, Vertex>> innerList = new ArrayList<>();
			for (Vertex v : instanceVariables) {
				innerList.add(new Pair<>(entity.getN(), v));
			}
			if (innerList.isEmpty()) {
				// we add a NULL-entry, maybe this rule can be evaluated with one entity missing anyway.
				innerList.add(new Pair<>(entity.getN(), null));
			}
			entities.add(innerList);
		}
		return entities;
	}

}
