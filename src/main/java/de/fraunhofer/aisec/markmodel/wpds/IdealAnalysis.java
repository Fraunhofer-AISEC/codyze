package de.fraunhofer.aisec.markmodel.wpds;

import boomerang.WeightedForwardQuery;
import boomerang.results.ForwardBoomerangResults;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import de.fraunhofer.aisec.crymlin.server.AnalysisContext;
import de.fraunhofer.aisec.markmodel.MRule;
import ideal.IDEALAnalysis;
import ideal.StoreIDEALResultHandler;
import typestate.TransitionFunction;

import java.util.Map;

public class IdealAnalysis {

    public void analyze(AnalysisContext ctx, CrymlinTraversalSource crymlinTraversal, MRule r) {
        StoreIDEALResultHandler<TransitionFunction> resultHandler = new StoreIDEALResultHandler<>();
        IdealMachine m = new IdealMachine(resultHandler);
        IDEALAnalysis<TransitionFunction> analysis = new IDEALAnalysis<TransitionFunction>(m);
        analysis.PRINT_OPTIONS = true;

        analysis.run();

        Map<WeightedForwardQuery<TransitionFunction>, ForwardBoomerangResults<TransitionFunction>> results = resultHandler.getResults();

    }
}
