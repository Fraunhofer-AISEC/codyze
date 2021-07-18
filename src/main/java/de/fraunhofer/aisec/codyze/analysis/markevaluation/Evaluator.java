
package de.fraunhofer.aisec.codyze.analysis.markevaluation;

import com.google.common.collect.Lists;
import de.fraunhofer.aisec.codyze.analysis.*;
import de.fraunhofer.aisec.codyze.analysis.utils.Utils;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.cpg.graph.Graph;
import de.fraunhofer.aisec.cpg.graph.Node;
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression;
import de.fraunhofer.aisec.cpg.graph.statements.expressions.ConstructExpression;
import de.fraunhofer.aisec.cpg.graph.statements.expressions.DeclaredReferenceExpression;
import de.fraunhofer.aisec.cpg.graph.statements.expressions.StaticCallExpression;
import de.fraunhofer.aisec.cpg.helpers.Benchmark;
import de.fraunhofer.aisec.cpg.sarif.Region;
import de.fraunhofer.aisec.mark.markDsl.AliasedEntityExpression;
import de.fraunhofer.aisec.mark.markDsl.RuleStatement;
import de.fraunhofer.aisec.codyze.markmodel.MOp;
import de.fraunhofer.aisec.codyze.markmodel.MRule;
import de.fraunhofer.aisec.codyze.markmodel.Mark;
import kotlin.Pair;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static de.fraunhofer.aisec.codyze.analysis.markevaluation.EvaluationHelperKt.*;
import static de.fraunhofer.aisec.cpg.graph.GraphKt.getGraph;

/**
 * Evaluates all loaded MARK rules against the CPG.
 * <p>
 * returns a number of Findings if any of the rules are violated
 */
public class Evaluator {
	public static final Logger log = LoggerFactory.getLogger(Evaluator.class);

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
	 * @param ctx    [out] the context storing the result of the evaluation. This could also include results from previous steps
	 */
	public TranslationResult evaluate(@NonNull TranslationResult result, @NonNull final AnalysisContext ctx) {
		Benchmark bOuter = new Benchmark(this.getClass(), "Mark evaluation");

		try {
			// builds the graph. this is potentially expensive, so do it only once
			var graph = getGraph(result);

			log.info("Precalculating matching nodes");
			assignCallsToOps(graph);

			log.info("Evaluate forbidden calls");
			Benchmark b = new Benchmark(this.getClass(), "Evaluate forbidden calls");
			ForbiddenEvaluator forbiddenEvaluator = new ForbiddenEvaluator(this.markModel);
			forbiddenEvaluator.evaluate(ctx);
			b.stop();

			log.info("Evaluate rules");
			b = new Benchmark(this.getClass(), "Evaluate rules");
			evaluateRules(ctx, graph);
			b.stop();

			bOuter.stop();

			return result;
		}
		catch (Exception e) {
			log.debug(e.getMessage(), e);
			return result;
		}
		finally {
			// reset everything attached to this model
			this.markModel.reset();
		}
	}

	/**
	 * Iterate over all MOps in all MEntities, find all call statements in CPG and assign them to their respective MOp.
	 * <p>
	 * After this method, all call statements can be retrieved by MOp.getAllNodes(), MOp.getStatements(), and MOp.getVertexToCallStatementsMap().
	 *
	 * @param graph the Graph
	 */
	private void assignCallsToOps(@NonNull Graph graph) {
		Benchmark b = new Benchmark(this.getClass(), "Precalculating matching nodes");
		// iterate over all entities and precalculate:
		// - call statements to vertices
		for (var ent : markModel.getEntities()) {
			log.info("Precalculating call statements for entity {}", ent.getName());
			ent.parseVars();

			for (var op : ent.getOps()) {
				log.debug("Looking for call statements for {}", op.getName());

				int numMatches = 0;
				for (var opStmt : op.getStatements()) {
					var temp = getNodesForFunctionReference(graph, opStmt.getCall());
					log.debug(
						"Call {}({}) of op {} found {} times",
						opStmt.getCall().getName(),
						String.join(", ", MOp.paramsToString(opStmt.getCall().getParams())),
						op.getName(),
						temp.size());
					numMatches += temp.size();
					op.addNode(opStmt, temp);
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
	 * @param ctx              the result/analysis context
	 * @param graph the Graph
	 */
	private void evaluateRules(AnalysisContext ctx, @NonNull Graph graph) {
		for (MRule rule : this.markModel.getRules()) {
			log.info("checking rule {}", rule.getName());

			// Evaluate "using" part and collect the instances of MARK entities, as well as
			// the potential vertex representing the base object variables.
			var entities = findInstancesForEntities(rule);

			// skip evaluation if there are no cpg-nodes which would be used in this evaluation
			if (!entities.isEmpty()) {
				boolean hasCPGNodes = false;
				outer: for (var entity : rule.getEntityReferences().entrySet()) {
					if (entity.getValue() == null || entity.getValue().getSecond() == null) {
						log.warn("Rule {} references an unknown entity {}", rule.getName(), entity.getKey());
						break;
					}

					for (var op : entity.getValue().getSecond().getOps()) {
						if (!op.getAllNodes().isEmpty()) {
							hasCPGNodes = true;
							break outer;
						}
					}
				}
				if (!hasCPGNodes) {
					log.warn("Rule {} does not have any corresponding CPG-nodes. Skipping", rule.getName());
					continue;
				}
			}

			/* Create evaluation context. */
			// Generate all combinations of instances for each entity.
			// We take the n-th cartesian product of all _possible_ program variables that correspond to Mark entities.
			// A CPGInstanceContext is a specific interpretation of a Mark rule that needs to be evaluated.
			MarkContextHolder markCtxHolder = createMarkContext(entities);

			ExpressionEvaluator ee = new ExpressionEvaluator(graph, this.markModel, rule, ctx, config, markCtxHolder);

			// Evaluate "when" part, if present (will possibly remove entries from markCtxhHlder)
			evaluateWhen(rule, markCtxHolder, ee);

			// Evaluate "ensure" part. This may already add findings, i.e. from the order expression
			Map<Integer, MarkIntermediateResult> result = ee.evaluateExpression(rule.getStatement().getEnsure().getExp());

			// Get findings from "result"
			Collection<Finding> findings = getFindings(result, markCtxHolder, rule);
			ctx.getFindings().addAll(findings);

			log.info("Got {} findings in analysis context: {}", ctx.getFindings().size(),
				ctx.getFindings().stream().map(Finding::getLogMsg).collect(Collectors.toList()));
		}
	}

	private Collection<Finding> getFindings(@NonNull Map<Integer, MarkIntermediateResult> result, @NonNull MarkContextHolder markCtxHolder,
			@NonNull MRule rule) {
		Collection<Finding> findings = new HashSet<>();

		for (Map.Entry<Integer, MarkIntermediateResult> entry : result.entrySet()) {
			// the value of the result should always be boolean, as this should be the result of the topmost expression
			int markCtx = entry.getKey();
			Object evaluationResultUb = ConstantValue.unbox(entry.getValue());

			if (evaluationResultUb instanceof Boolean) {
				ConstantValue evalResult = (ConstantValue) entry.getValue();
				/*
				 * if we did not add a finding during expression evaluation (e.g., as it is the case in the order evaluation), add a new finding which references all
				 * responsible vertices.
				 */

				var c = markCtxHolder.getContext(markCtx);

				URI currentFile = null;

				if (!c.isFindingAlreadyAdded()) {
					List<Region> ranges = new ArrayList<>();
					if (evalResult.getResponsibleNodes().isEmpty() || evalResult.getResponsibleNodes().stream().noneMatch(Objects::nonNull)) {
						// use the line of the instances
						if (!c.getInstanceContext().getMarkInstances().isEmpty()) {
							for (var node : c.getInstanceContext().getMarkInstanceVertices()) {
								if (node == null) {
									continue;
								}

								var location = node.getLocation();

								if (location != null) {
									ranges.add(Utils.getRegionByNode(node));
								} else {
									ranges.add(new Region(-1, -1, -1, -1));
								}

								currentFile = new File(node.getFile()).toURI();
							}
						}
						if (ranges.isEmpty()) {
							ranges.add(new Region());
						}
					} else {
						// responsible vertices are stored in the result
						for (var node : evalResult.getResponsibleNodes()) {
							if (node == null) {
								continue;
							}
							ranges.add(Utils.getRegionByNode(node));

							currentFile = new File(node.getFile()).toURI();
						}
					}

					boolean isRuleViolated = !(Boolean) evaluationResultUb;
					findings.add(new Finding(
						"Rule "
								+ rule.getName()
								+ (isRuleViolated ? " violated" : " verified"),
						currentFile,
						rule.getErrorMessage(),
						ranges,
						isRuleViolated));
				}
			} else if (evaluationResultUb == null) {
				log.warn("Unable to evaluate rule {} in MARK context " + markCtx + "/" + markCtxHolder.getAllContexts().size()
						+ ", result was null, this should not happen.",
					rule.getName());
			} else if (ConstantValue.isError(entry.getValue())) {
				log.warn("Unable to evaluate rule {} in MARK context " + markCtx + "/" + markCtxHolder.getAllContexts().size() + ", result had an error: \n\t{}",
					rule.getName(),
					((ErrorValue) entry.getValue()).getDescription().replace("\n", "\n\t"));
			} else {
				log.error(
					"Unable to evaluate rule {} in MARK context " + markCtx + "/" + markCtxHolder.getAllContexts().size() + ", result is not a boolean, but {}",
					rule.getName(), evaluationResultUb.getClass().getSimpleName());
			}
		}
		return findings;
	}

	/**
	 * Evaluates the "when" part of a rule and removes all entries from "markCtxHolder" to which the condition does not apply.
	 * <p>
	 * If the "when" part is not satisfied, the "MarkContextHolder" will be empty.
	 * <p>
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
				if (value == null) {
					log.warn("Unable to evaluate rule {}, result was null, this should not happen.", rule.getName());
					continue;
				} else if (ConstantValue.isError(entry.getValue())) {
					log.warn("Unable to evaluate when-part of rule {}, result had an error: \n\t{}", rule.getName(),
						((ErrorValue) entry.getValue()).getDescription().replace("\n", "\n\t"));
					// FIXME do we want to evaluate the ensure-part if the when-part had an error?
					continue;
				} else if (!(value instanceof Boolean)) {
					log.error("Unable to evaluate when-part of rule {}, result is not a boolean, but {}", rule.getName(), value.getClass().getSimpleName());
					continue;
				}

				if (value.equals(false) || ConstantValue.isError(entry.getValue())) {
					log.info("Precondition of {} is false or error, do not evaluate ensure for this combination of instances.", rule.getName());
					markCtxHolder.removeContext(entry.getKey());
				} else {
					log.debug("Precondition of {} is true, we will evaluate this context in the following.", rule.getName());
				}
			}
		}

		// now we can create inline findings
		markCtxHolder.setCreateFindingsDuringEvaluation(true);
	}

	private MarkContextHolder createMarkContext(List<List<Pair<String, Node>>> entities) {
		MarkContextHolder context = new MarkContextHolder();
		for (var list : Lists.cartesianProduct(entities)) {
			var instanceCtx = new GraphInstanceContext();
			for (var p : list) {
				String markInstanceName = p.getFirst();
				var v = p.getSecond();
				instanceCtx.putMarkInstance(markInstanceName, v);
			}

			context.addInitialInstanceContext(instanceCtx);
		}

		return context;
	}

	/**
	 * Collect all entities, and calculate which instances correspond to the entity.
	 * <p>
	 * For instance, an entity "Cipher" may be referenced by "c1" and "c2" in a MARK rule.
	 * <p>
	 * The "Cipher" entity might refer to three actual object variables in the program: "v1", "v2", "v3".
	 * <p>
	 * In that case, the function will return [ [ (c1, v1) , (c1, v2), (c1, v3) ] [ (c2, c1), (c2, v2), (c2, v3) ] ]
	 *
	 * @param rule the MARK rule
	 *
	 * @return
	 */
	private List<List<Pair<String, Node>>> findInstancesForEntities(MRule rule) {
		var ruleStmt = rule.getStatement();
		var entities = new ArrayList<List<Pair<String, Node>>>();

		// Find entities whose ops are used in the current Mark rule.
		// We collect all entities and calculate which instances (=program variables) correspond to the entity.
		// entities is a map with key: name of the Mark Entity (e.g., "b"). value: Vertex to which the program variable REFERS_TO.
		for (AliasedEntityExpression entity : ruleStmt.getEntities()) {
			var instanceVariables = new HashSet<Node>();
			var referencedEntity = this.markModel.getEntity(entity.getE());

			if (referencedEntity == null) {
				log.warn("Unexpected: Mark rule {} references an unknown entity {}", rule.getName(), entity.getN());
				continue;
			}

			for (MOp op : referencedEntity.getOps()) {
				for (var node : op.getAllNodes()) {
					Node ref = null;

					// TODO(oxisto): There is similar code in EvaluationHelper::getMatchingReferences
					if (node instanceof ConstructExpression) {
						ref = getAssignee((ConstructExpression) node);
					} else if (node instanceof StaticCallExpression) {
						// mainly for builder functions
						ref = getSuitableDFGTarget(node);
					} else if (node instanceof CallExpression) {
						// Program variable is either the Base of some method call ...
						ref = getBaseDeclaration((CallExpression) node);
					}

					if (ref == null) { // if we did not find a base the "easy way", try to find a base using the simple-DFG
						ref = getSuitableDFGTarget(node);
					}

					// make sure, that we are actually targeting the variable declaration and not the reference
					if (ref instanceof DeclaredReferenceExpression) {
						if (((DeclaredReferenceExpression) ref).getRefersTo() != null) {
							ref = ((DeclaredReferenceExpression) ref).getRefersTo();
						}
					}

					if (ref != null) {
						instanceVariables.add(ref);
					} else {
						log.warn("Did not find an instance variable for entity {} when searching at node {}", referencedEntity.getName(),
							node.getCode() != null ? node.getCode().replaceAll("\n\\s*", " ") : null);
					}
				}
			}

			var innerList = new ArrayList<Pair<String, Node>>();
			for (var v : instanceVariables) {
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
