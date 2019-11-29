
package de.fraunhofer.aisec.analysis.markevaluation;

import com.google.common.collect.Lists;
import de.fraunhofer.aisec.analysis.scp.ConstantValue;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.CPGInstanceContext;
import de.fraunhofer.aisec.analysis.structures.CPGVariableContext;
import de.fraunhofer.aisec.analysis.structures.CPGVertexWithValue;
import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.analysis.structures.MarkContext;
import de.fraunhofer.aisec.analysis.structures.MarkContextHolder;
import de.fraunhofer.aisec.analysis.structures.Pair;
import de.fraunhofer.aisec.analysis.structures.ResultWithContext;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.cpg.graph.Literal;
import de.fraunhofer.aisec.cpg.helpers.Benchmark;
import de.fraunhofer.aisec.crymlin.CrymlinQueryWrapper;
import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import de.fraunhofer.aisec.mark.markDsl.AliasedEntityExpression;
import de.fraunhofer.aisec.mark.markDsl.Expression;
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
	 * Evaluates the "when" and "ensure" part of a rule
	 *
	 * @param ctx the result/analysis context
	 * @param crymlinTraversal connection to the db
	 */
	private void evaluateRules(AnalysisContext ctx, @NonNull CrymlinTraversalSource crymlinTraversal) {

		for (MRule rule : this.markModel.getRules()) {

			RuleStatement s = rule.getStatement();
			log.info("checking rule {}", rule.getName());

			// collect all entities, and calculate which instances correspond to the entity
			// Stores one List for each markinstance with corresponding vertices. E.g.:
			// [[(r, v123), (r, v23)], [(cm, v163), (cm, v33)],
			List<List<Pair<String, Vertex>>> entities = new ArrayList<>();
			// Evaluation of "using" part:
			// Find entities whose ops are used in the current Mark rule.
			// We collect all entities and calculate which instances (=program variables) correspond to the entity.
			// entities is a map with key: name of the Mark Entity (e.g., "b"). value: Vertex to which the program variable REFERS_TO.
			for (AliasedEntityExpression entity : s.getEntities()) {
				HashSet<Vertex> instanceVariables = new HashSet<>();
				MEntity referencedEntity = this.markModel.getEntity(entity.getE());
				if (referencedEntity != null) {
					for (MOp op : referencedEntity.getOps()) {
						for (Vertex vertex : op.getAllVertices()) {

							// Program variable is either the Base of some method call ...
							Optional<Vertex> ref = CrymlinQueryWrapper.getBaseOfCallExpression(vertex);
							ref.ifPresent(instanceVariables::add);
							// .. or assignee of a VariableDeclaration with a ConstructorExpression.
							Optional<Vertex> assignee = CrymlinQueryWrapper.getAssigneeOfConstructExpression(vertex);
							assignee.ifPresent(instanceVariables::add);

							if (ref.isEmpty() && assignee.isEmpty()) {
								log.warn("Did not find an instance variable for {} when searching at node {}", referencedEntity, vertex.property("code").value());
							}
						}
					}
					ArrayList<Pair<String, Vertex>> innerList = new ArrayList<>();
					for (Vertex v : instanceVariables) {
						innerList.add(new Pair<>(entity.getN(), v));
					}
					if (!innerList.isEmpty()) {
						// we add a NULL-entry, maybe this rule can be evaluated with one entity missing anyway.
						innerList.add(new Pair<>(entity.getN(), null));
					}
					entities.add(innerList);
				} // else: unknown Entity referenced, this rule does not make much sense
			}

			// Generate all combinations of instances for each entity.
			// We take the cartesian product of all _possible_ program variables that correspond to Mark entities.
			// A CPGInstanceContext is a specific interpretation of a Mark rule that needs to be evaluated.
			MarkContextHolder context = new MarkContextHolder();
			for (List<Pair<String, Vertex>> list : Lists.cartesianProduct(entities)) {
				CPGInstanceContext instance = new CPGInstanceContext();
				for (Pair<String, Vertex> p : list) {
					instance.putMarkInstance(p.getValue0(), p.getValue1());
				}
				context.addInitialInstanceContext(instance);
			}
			@NonNull
			Map<Integer, Object> result;

			ExpressionEvaluator ee = new ExpressionEvaluator(rule, ctx, config, crymlinTraversal, context);

			// Evaluate "when" part, if present
			if (s.getCond() != null) {
				result = ee.evaluate(s.getCond().getExp());

				for (Map.Entry<Integer, Object> entry : result.entrySet()) {
					if (!(entry.getValue() instanceof Boolean)) {
						log.error("Result is of type {}, expected boolean.", entry.getValue().getClass());
						continue;
					}
					if (entry.getValue().equals(false)) {
						log.info("Precondition is false, do not evaluate ensure.");
						context.removeContext(entry.getKey());
					}
				}

				result = ee.evaluate(s.getEnsure().getExp());
			} else {
				result = ee.evaluate(s.getEnsure().getExp());
			}

			log.info("Got {} results", result.size());
			for (Map.Entry<Integer, Object> entry : result.entrySet()) {
				// the value of the result should always be boolean, as this should be the result of the topmost expression
				if (entry.getValue() instanceof Boolean) {
					/*
					 * if we did not add a finding during expression evaluation (e.g., as it is the case in the order evaluation), add a new finding which references all
					 * responsible vertices.
					 */

					MarkContext c = context.getContext(entry.getKey());

					if (!c.isFindingAlreadyAdded()) {
						List<Range> ranges = new ArrayList<>();
						if (c.getResponsibleVertices().isEmpty()) {
							ranges.add(new Range(new Position(-1, -1),
								new Position(-1, -1)));
						} else {
							for (Vertex v : c.getResponsibleVertices()) {
								int startLine = toIntExact((Long) v.property("startLine").value()) - 1;
								int endLine = toIntExact((Long) v.property("endLine").value()) - 1;
								int startColumn = toIntExact((Long) v.property("startColumn").value()) - 1;
								int endColumn = toIntExact((Long) v.property("endColumn").value()) - 1;
								ranges.add(new Range(new Position(startLine, startColumn),
									new Position(endLine, endColumn)));
							}
						}
						boolean isRuleViolated = !(Boolean) entry.getValue();
						ctx.getFindings()
								.add(new Finding(
									"MarkRuleEvaluationFinding: Rule "
											+ rule.getName()
											+ (isRuleViolated ? " violated" : " verified"),
									ctx.getCurrentFile(),
									rule.getErrorMessage(),
									ranges,
									isRuleViolated));
					}
				} else {
					log.error("Unable to evaluate rule {}, result is not a boolean", rule.getName());
				}
			}
		}
	}

}
