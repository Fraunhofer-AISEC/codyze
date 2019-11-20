
package de.fraunhofer.aisec.analysis.markevaluation;

import de.breakpoint.pushdown.IllegalTransitionException;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.CPGInstanceContext;
import de.fraunhofer.aisec.analysis.structures.ResultWithContext;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.analysis.wpds.TypeStateAnalysis;
import de.fraunhofer.aisec.cpg.helpers.Benchmark;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import de.fraunhofer.aisec.mark.markDsl.OrderExpression;
import de.fraunhofer.aisec.markmodel.MRule;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderEvaluator {

	private static final Logger log = LoggerFactory.getLogger(OrderEvaluator.class);
	private final MRule rule;
	private final ServerConfiguration config;

	public OrderEvaluator(@NonNull MRule rule, ServerConfiguration config) {
		this.rule = rule;
		this.config = config;
	}

	public ResultWithContext evaluate(OrderExpression orderExpression, CPGInstanceContext instanceContext, AnalysisContext resultCtx,
			CrymlinTraversalSource crymlinTraversal) {
		Benchmark tsBench = new Benchmark(TypeStateAnalysis.class, "Typestate Analysis");

		ResultWithContext result = null;

		switch (config.typestateAnalysis) {

			case WPDS:
				log.info("Evaluating order with WPDS");
				TypeStateAnalysis ts = new TypeStateAnalysis();
				try {
					// NOTE: rule and orderExpression might be redundant as arguments
					result = ts.analyze(orderExpression, instanceContext, resultCtx, crymlinTraversal, rule);
				}
				catch (IllegalTransitionException e) {
					log.error("Unexpected error in typestate WPDS", e);
				}
				break;

			case NFA:
				log.info("Evaluating order with NFA");
				OrderNFAEvaluator orderNFAEvaluator = new OrderNFAEvaluator(rule);
				result = orderNFAEvaluator.evaluate(orderExpression, instanceContext, resultCtx, crymlinTraversal);
				break;
		}

		tsBench.stop();
		return result;
	}
}
