package de.fraunhofer.aisec.markmodel.wpds;

import boomerang.WeightedForwardQuery;
import boomerang.callgraph.ObservableICFG;
import boomerang.debugger.Debugger;
import boomerang.debugger.IDEVizDebugger;
import boomerang.jimple.Statement;
import boomerang.jimple.Val;
import ideal.IDEALAnalysisDefinition;
import ideal.IDEALResultHandler;
import ideal.IDEALSeedSolver;
import ideal.StoreIDEALResultHandler;
import soot.SootMethod;
import soot.Unit;
import sync.pds.solver.WeightFunctions;
import typestate.TransitionFunction;
import typestate.finiteautomata.TypeStateMachineWeightFunctions;
import typestate.impl.statemachines.FileMustBeClosedStateMachine;

import java.io.File;
import java.util.Collection;

public class IdealMachine extends IDEALAnalysisDefinition<TransitionFunction> {

    private final StoreIDEALResultHandler<TransitionFunction> resultHandler;
    private boolean VISUALIZATION = false;

    public IdealMachine(StoreIDEALResultHandler<TransitionFunction> resultHandler) {
        this.resultHandler = resultHandler;
    }

    @Override
  public Collection<WeightedForwardQuery<TransitionFunction>> generate(
      SootMethod method, Unit stmt) {
    return this.getStateMachine().generateSeed(method, stmt);
  }

  @Override
  public WeightFunctions<Statement, Val, Statement, TransitionFunction> weightFunctions() {
    return this.getStateMachine();
  }

  @Override
  public Debugger<TransitionFunction> debugger(IDEALSeedSolver<TransitionFunction> solver) {
    return VISUALIZATION
        ? new IDEVizDebugger<>(
            new File(
                new File("visualization")
                    .getAbsolutePath()
                    .replace(".json", " " + solver.getSeed() + ".json")),
            icfg)
        : new Debugger<>();
  }

  @Override
  public IDEALResultHandler<TransitionFunction> getResultHandler() {
    return this.resultHandler;
  }

  @Override
  public ObservableICFG icfg() {
    return null;  // TODO We want to use CPG instead of the soot-dependent ObservableICFG here.
  }

  protected TypeStateMachineWeightFunctions getStateMachine() {
    return new FileMustBeClosedStateMachine();
  }
}
