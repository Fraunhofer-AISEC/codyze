package de.fraunhofer.aisec.markmodel;

import de.fhg.aisec.mark.markDsl.*;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import de.fraunhofer.aisec.crymlin.server.AnalysisContext;
import de.fraunhofer.aisec.crymlin.structures.Finding;
import de.fraunhofer.aisec.crymlin.utils.Builtins;
import de.fraunhofer.aisec.crymlin.utils.CrymlinQueryWrapper;
import de.fraunhofer.aisec.crymlin.utils.Utils;
import de.fraunhofer.aisec.markmodel.fsm.Node;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.emf.common.util.EList;
import org.python.antlr.base.expr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarkInterpreter {
  private static final Logger log = LoggerFactory.getLogger(MarkInterpreter.class);
  @NonNull private final Mark markModel;

  public MarkInterpreter(@NonNull Mark markModel) {
    this.markModel = markModel;
  }

  enum TRISTATE {
    TRUE,
    FALSE,
    UNKNOWN
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
          + fExpr.getArgs().stream()
              .map(MarkInterpreter::argToString)
              .collect(Collectors.joining(", "))
          + ")";
    } else if (expr instanceof LiteralListExpression) {
      return "[ "
          + ((LiteralListExpression) expr)
              .getValues().stream().map(Literal::getValue).collect(Collectors.joining(", "))
          + " ]";
    } else if (expr instanceof RepetitionExpression) {
      RepetitionExpression inner = (RepetitionExpression) expr;
      // todo @FW do we want this optimization () can be omitted if inner is no sequence
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

  private HashSet<Vertex> getVerticesForFunctionDeclaration(
      FunctionDeclaration functionDeclaration,
      MEntity ent,
      CrymlinTraversalSource crymlinTraversal) {
    String functionName = Utils.extractMethodName(functionDeclaration.getName());
    String baseType = Utils.extractType(functionDeclaration.getName());

    EList<String> params = functionDeclaration.getParams();
    // resolve parameters which have a corresponding var part in the entity
    ArrayList<String> cloned = new ArrayList<>(params);
    for (int i = 0; i < cloned.size(); i++) {
      String typeForVar = ent.getTypeForVar(cloned.get(i));
      if (typeForVar != null) {
        cloned.set(i, typeForVar);
      }
    }

    return CrymlinQueryWrapper.getCalls(crymlinTraversal, functionName, baseType, cloned);
  }

  /**
   * Evaluates the {@code markModel} against the currently analyzed program.
   *
   * <p>This is the core of the MARK evaluation.s
   *
   * @param result
   */
  public TranslationResult evaluate(TranslationResult result, AnalysisContext ctx) {

    Instant outer_start = Instant.now();

    // reset stuff attached to this model
    this.markModel.reset();

    try (TraversalConnection t = new TraversalConnection()) { // connects to the DB
      CrymlinTraversalSource crymlinTraversal = t.getCrymlinTraversal();

      log.info("Precalculating matching nodes");
      Instant start = Instant.now();
      /*
      iterate all entities and precalculate some things:
         - call statements to vertices
      */
      for (MEntity ent : this.markModel.getEntities()) {
        log.info("Precalculating call statments for entity {}", ent.getName());
        ent.parseVars();
        for (MOp op : ent.getOps()) {
          log.debug("Looking for call statements for {}", op.getName());
          int numMatches = 0;
          for (OpStatement a : op.getStatements()) {
            HashSet<Vertex> temp =
                getVerticesForFunctionDeclaration(a.getCall(), ent, crymlinTraversal);
            log.debug(
                a.getCall().getName()
                    + "("
                    + String.join(", ", a.getCall().getParams())
                    + "): "
                    + temp.size());
            numMatches += temp.size();
            op.addVertex(a, temp);
          }
          op.setParsingFinished();
          if (numMatches > 0) {
            log.info("Found {} call statements in the cpg for {}", numMatches, op.getName());
          }
        }
      }
      log.info(
          "Done precalculating matching nodes in {} ms.",
          Duration.between(start, Instant.now()).toMillis());

      log.info("Evaluate forbidden calls");
      start = Instant.now();
      evaluateForbiddenCalls(ctx);
      log.info(
          "Done evaluate forbidden calls in {} ms.",
          Duration.between(start, Instant.now()).toMillis());

      log.info("Evaluate order");
      start = Instant.now();
      evaluateOrder(ctx, crymlinTraversal);
      log.info(
          "Done evaluating order in {} ms.", Duration.between(start, Instant.now()).toMillis());

      log.info("Evaluate rules");
      start = Instant.now();
      evaluateRules(ctx);
      log.info(
          "Done evaluating all MARK rules in {} ms.",
          Duration.between(start, Instant.now()).toMillis());

      return result;
    }
  }

  private void evaluateOrder(AnalysisContext ctx, CrymlinTraversalSource crymlinTraversal) {
    /*
    We also look through forbidden nodes. The fact that these are forbidden is checked elsewhere
    Any function calls to functions which are not specified in an entity are _ignored_
     */

    // Cache which Vertex belongs to which Op/Entity
    // a vertex can _only_ belong to one entity/op!
    HashMap<Vertex, MOp> verticesToOp = new HashMap<>();
    for (MEntity ent : this.markModel.getEntities()) {
      for (MOp op : ent.getOps()) {
        op.getAllVertices().forEach(v -> verticesToOp.put(v, op));
      }
    }

    if (verticesToOp.isEmpty()) {
      log.info("no nodes match for TU and MARK-model. Skipping evaluation.");
      return;
    }

    for (Vertex functionDeclaration : crymlinTraversal.functiondeclarations().toList()) {
      log.info("Evaluating function " + functionDeclaration.value("name"));

      for (MRule rule : this.markModel.getRules()) {

        if (rule.getFSM() != null) {
          // rule.getFSM().pushToDB(); //debug only
          log.info("\tEvaluating rule " + rule.getName());
          /* todo
          should we allow this different entities in an order?
          rule UseOfBotan_CipherMode {
            using Forbidden as cm, Foo as f
            ensure
                order cm.start(), cm.finish(), f.done()
            onfail WrongUseOfBotan_CipherMode
          }
          -> this does currently not work, as we store for each base, where in the FSM it is. BUT in this case, an instance of cm would always have a different base than f.

          is aliasing inside an order rule allowed? I.e. order x.a, x.b, x.c
          x i1;
          i1.a();
          i1.b();
          x i2 = i1;
          i2.c();
          -> do we know if i2 is a copy, or an alias?
          -> always mark as error?
          -> currently this will result in:
              Violation against Order: i2.c(); (c) is not allowed. Expected one of: x.a
              Violation against Order: Base i1 is not correctly terminated. Expected one of [x.c] to follow the last call on this base.
          */

          HashSet<Vertex> currentWorklist = new HashSet<>();
          currentWorklist.add(functionDeclaration);

          // which bases did we already see, but are not initialized correctly
          // base to set of eogpaths
          HashMap<String, HashSet<String>> disallowedBases = new HashMap<>();
          // stores the current markings in the FSM (i.e., which base is at which FSM-node)
          HashMap<String, HashSet<Node>> baseToFSMNodes = new HashMap<>();
          // last usage of base
          HashMap<String, Vertex> lastBaseUsage = new HashMap<>();

          HashMap<Long, HashSet<String>> nodeIDtoEOGPathSet = new HashMap<>();
          HashSet<String> startEOG = new HashSet<>();
          startEOG.add("0");
          nodeIDtoEOGPathSet.put((Long) functionDeclaration.id(), startEOG);

          HashSet<String> seenStates = new HashSet<>();
          long visitedNodes = 0;

          while (currentWorklist.size() > 0) {
            HashSet<Vertex> nextWorklist = new HashSet<>();
            //            System.out.println("SEEN: " + String.join(", ", seenStates));
            //            printWorklist(currentWorklist, nodeIDtoEOGPathSet);
            //            System.out.println();

            for (Vertex vertex : currentWorklist) {
              visitedNodes++;

              String currentState = getStateSnapshot(vertex, baseToFSMNodes);
              seenStates.add(currentState);

              HashSet<String> eogPathSet = nodeIDtoEOGPathSet.get((Long) vertex.id());
              for (String eogPath : eogPathSet) {

                // ... no direct access to the labels TreeSet of Neo4JVertex
                if (vertex.label().contains("MemberCallExpression")) {
                  // is the vertex part of any op of any mentioned entity? If not, ignore
                  if (verticesToOp.get(vertex) != null) {

                    MOp op = verticesToOp.get(vertex);
                    // check if the vertex actually belongs to a entity used in this rule
                    if (rule.getEntityReferences().values().stream()
                        .anyMatch(x -> x.getValue1().equals(op.getParent()))) {

                      Iterator<Edge> it = vertex.edges(Direction.OUT, "BASE");
                      String base = null;
                      String ref = null;
                      if (it.hasNext()) {
                        Vertex baseVertex = it.next().inVertex();
                        base = baseVertex.value("name");
                        Iterator<Edge> it_ref = baseVertex.edges(Direction.OUT, "REFERS_TO");
                        if (it_ref.hasNext()) {
                          ref = it_ref.next().inVertex().id().toString();
                        }
                      } else {
                        log.error("base must not be null for MemberCallExpressions");
                        assert false;
                      }

                      // if we have a reference to a node in the cpg, we add this to the prefixed
                      // base
                      // this way, we could differentiate between nodes with the same base name, but
                      // referencing
                      // different variables (e.g., if they are used in different blocks)
                      if (ref != null) {
                        base += "|" + ref;
                      }

                      String prefixedBase = eogPath + "." + base;

                      if (isDisallowedBase(disallowedBases, eogPath, base)) {
                        Finding f =
                            new Finding(
                                "Violation against Order: "
                                    + vertex.value("code")
                                    + " is not allowed. Base contains errors already."
                                    + " ("
                                    + rule.getErrorMessage()
                                    + ")",
                                vertex.value("startLine"),
                                vertex.value("endLine"),
                                vertex.value("startColumn"),
                                vertex.value("endColumn"));
                        // we hide base errors for now!
                        // ctx.getFindings().add(f);
                        // log.info("Finding: {}", f.toString());
                      } else {
                        HashSet<Node> nodesInFSM;
                        if (baseToFSMNodes.get(prefixedBase) == null) {
                          // we have not seen this base before. check if this is the
                          // start of an order
                          nodesInFSM = rule.getFSM().getStart(); // start nodes
                        } else {
                          nodesInFSM =
                              baseToFSMNodes.get(prefixedBase); // nodes calculated in previous step
                        }

                        HashSet<Node> nextNodesInFSM = new HashSet<>();

                        boolean match = false; // did at least one fsm-Node-match occur?
                        for (Node n : nodesInFSM) {
                          // are there any ops corresponding to the current base and the current
                          // function name?
                          if (op != null && op.getName().equals(n.getOp())) {
                            // this also has as effect, that if the FSM is in a end-state and a
                            // intermediate state, and we follow the intermediate state, the
                            // end-state is removed again, which is correct!
                            nextNodesInFSM.addAll(n.getSuccessors());
                            match = true;
                          }
                        }
                        if (!match) {
                          // if not, this call is not allowed, and this base must not be used in the
                          // following eog
                          Finding f =
                              new Finding(
                                  "Violation against Order: "
                                      + vertex.value("code")
                                      + " ("
                                      + (op == null ? "null" : op.getName())
                                      + ") is not allowed. Expected one of: "
                                      + nodesInFSM.stream()
                                          .map(Node::getName)
                                          .sorted()
                                          .collect(Collectors.joining(", "))
                                      + " ("
                                      + rule.getErrorMessage()
                                      + ")",
                                  vertex.value("startLine"),
                                  vertex.value("endLine"),
                                  vertex.value("startColumn"),
                                  vertex.value("endColumn"));
                          ctx.getFindings().add(f);
                          log.info("Finding: {}", f.toString());
                          disallowedBases.computeIfAbsent(base, x -> new HashSet<>()).add(eogPath);
                        } else {
                          String baseLocal = prefixedBase.split("\\.")[1]; // remove eogpath
                          Vertex vertex1 = lastBaseUsage.get(baseLocal);
                          long prevMaxLine = 0;
                          if (vertex1 != null) {
                            prevMaxLine = vertex1.value("startLine");
                          }
                          long newLine = vertex.value("startLine");
                          if (prevMaxLine <= newLine) {
                            lastBaseUsage.put(baseLocal, vertex);
                          }
                          baseToFSMNodes.put(prefixedBase, nextNodesInFSM);
                        }
                      }
                    }
                  }
                }
                ArrayList<Vertex> outVertices = new ArrayList<>();
                vertex
                    .edges(Direction.OUT, "EOG")
                    .forEachRemaining(edge -> outVertices.add(edge.inVertex()));

                // if more than one vertex follows the current one, we need to branch the eogPath
                if (outVertices.size() > 1) { // split
                  if (eogPath.length() > 20) {
                    log.error(
                        "Very long eogpath. This is very likely a bug. Do not split, ignore this node");
                    throw new RuntimeException(
                        "Very long eogpath. This is very likely a bug. Do not split, ignore this node");
                  } else {
                    HashSet<String> oldBases = new HashSet<>();
                    HashMap<String, HashSet<Node>> newBases = new HashMap<>();
                    // first we collect all entries which we need to remove from the baseToFSMNodes
                    // map we also store these entries without the eog path prefix, to update
                    // later in (1)
                    for (Map.Entry<String, HashSet<Node>> entry : baseToFSMNodes.entrySet()) {
                      if (entry.getKey().startsWith(eogPath)) {
                        oldBases.add(entry.getKey());
                        // keep the "." before the real base, as we need it later anyway
                        newBases.put(entry.getKey().substring(eogPath.length()), entry.getValue());
                      }
                    }
                    oldBases.forEach(baseToFSMNodes::remove);

                    // (1) update all entries previously removed from the baseToFSMNodes map with
                    // the new eogpath as prefix to the base
                    for (int i = outVertices.size() - 1; i >= 0; i--) {
                      // also update them in the baseToFSMNodes map
                      String newEOGPath = eogPath + i;
                      newBases.forEach((k, v) -> baseToFSMNodes.put(newEOGPath + k, v));

                      String stateOfNext = getStateSnapshot(outVertices.get(i), baseToFSMNodes);
                      if (seenStates.contains(stateOfNext)) {
                        log.warn(
                            "node/FSM state already visited: "
                                + stateOfNext
                                + ". Do not split into this.");
                        // todo log.debug
                        outVertices.remove(i);
                      } else {

                        // update the eogpath directly in the vertices for the next step
                        nodeIDtoEOGPathSet
                            .computeIfAbsent((Long) outVertices.get(i).id(), x -> new HashSet<>())
                            .add(newEOGPath);
                      }
                    }
                  }
                } else if (outVertices.size()
                    == 1) { // else, if we only have one vertex following this vertex, simply
                  // propagate the current eogpath to the next vertex
                  nodeIDtoEOGPathSet
                      .computeIfAbsent((Long) outVertices.get(0).id(), x -> new HashSet<>())
                      .add(eogPath);
                }

                nextWorklist.addAll(outVertices);
              }
              // the current vertex has been analyzed with all these eogpath. remove from map, if we
              // visit it in another iteration
              nodeIDtoEOGPathSet.remove((Long) vertex.id());
            }
            currentWorklist = nextWorklist;
          }

          log.info(
              "Done evaluating function {}, rule {}. Visited Nodes: {}",
              functionDeclaration.value("name"),
              rule.getName(),
              visitedNodes);
          // now the whole function was evaluated.
          // Check that the FSM is in its end/beginning state for all bases
          HashMap<String, HashSet<String>> nonterminatedBases = new HashMap<>();
          for (Map.Entry<String, HashSet<Node>> entry : baseToFSMNodes.entrySet()) {
            boolean hasEnd = false;
            HashSet<String> notEnded = new HashSet<>();
            for (Node n : entry.getValue()) {
              if (n.isEnd()) {
                // if one of the nodes in this fsm is at an END-node, this is fine.
                hasEnd = true;
                break;
              } else {
                notEnded.add(n.getName());
              }
            }
            if (!hasEnd) {
              // extract the real base name from eogpath.base
              HashSet<String> next =
                  nonterminatedBases.computeIfAbsent(
                      entry.getKey().substring(entry.getKey().indexOf(".") + 1),
                      x -> new HashSet<>());
              next.addAll(notEnded);
            }
          }
          for (Map.Entry<String, HashSet<String>> entry : nonterminatedBases.entrySet()) {
            Vertex vertex = lastBaseUsage.get(entry.getKey());
            String base = entry.getKey().split("\\|")[0]; // remove potential refers_to local
            Finding f =
                new Finding(
                    "Violation against Order: Base "
                        + base
                        + " is not correctly terminated. Expected one of ["
                        + entry.getValue().stream().sorted().collect(Collectors.joining(", "))
                        + "] to follow the correct last call on this base."
                        + " ("
                        + rule.getErrorMessage()
                        + ")",
                    vertex.value("startLine"),
                    vertex.value("endLine"),
                    vertex.value("startColumn"),
                    vertex.value("endColumn"));
            ctx.getFindings().add(f);
            log.info("Finding: {}", f.toString());
          }
        }
      }
    }
  }

  private boolean isDisallowedBase(
      HashMap<String, HashSet<String>> disallowedBases, String eogpath, String base) {
    HashSet<String> disallowedEOGPaths = disallowedBases.get(base);
    if (disallowedEOGPaths != null) {
      return disallowedEOGPaths.stream().anyMatch(eogpath::startsWith);
    }
    return false;
  }

  private String getStateSnapshot(Vertex v, HashMap<String, HashSet<Node>> baseToFSMNodes) {
    HashMap<String, HashSet<Node>> simplified = new HashMap<>();

    for (Map.Entry<String, HashSet<Node>> entry : baseToFSMNodes.entrySet()) {
      simplified
          .computeIfAbsent(entry.getKey().split("\\.")[1], x -> new HashSet<>())
          .addAll(entry.getValue());
    }

    List<String> fsmStates =
        simplified.entrySet().stream()
            .map(
                x ->
                    x.getKey()
                        + "("
                        + x.getValue().stream().map(Node::toString).collect(Collectors.joining(","))
                        + ")")
            .distinct()
            .sorted()
            .collect(Collectors.toList());

    return v.id() + " " + String.join(",", fsmStates);
  }

  private void printWorklist(
      HashSet<Vertex> currentWorklist, HashMap<Long, HashSet<String>> nodeIDtoEOGPathSet) {
    for (Vertex s : currentWorklist) {
      HashSet<String> eogPathSet = nodeIDtoEOGPathSet.get((Long) s.id());
      System.out.print("WL: " + eogPathSet + " ");
      System.out.print(s.id() + " ");
      try {

        System.out.println(s.value("code").toString().split("\\n")[0]);
      } catch (Exception e) {
        System.out.println("<< no code");
      }
    }
  }

  private void evaluateForbiddenCalls(AnalysisContext ctx) {
    /*
     * For a call to be forbidden, it needs to:
     * - matches any forbidden signature (as callstatment in an op)
     *    - with * for arbitrary parameters,
     *    - _ for ignoring one parameter type, or
     *    - a reference to a var in the entity to specify a concrete type (no type hierarchy is analyzed!)
     * - _and_ is not allowed by any other non-forbidden matching call statement (in _any_ op)
     */

    for (MEntity ent : this.markModel.getEntities()) {

      for (MOp op : ent.getOps()) {
        for (Map.Entry<Vertex, HashSet<OpStatement>> entry :
            op.getVertexToCallStatementsMap().entrySet()) {
          if (entry.getValue().stream()
              .noneMatch(call -> "forbidden".equals(call.getForbidden()))) {
            // only allowed entries
            continue;
          }
          Vertex v = entry.getKey();
          boolean vertex_allowed = false;
          HashSet<String> violating = new HashSet<>();
          for (OpStatement call : entry.getValue()) {
            String callString =
                call.getCall().getName() + "(" + String.join(",", call.getCall().getParams()) + ")";

            if (!"forbidden".equals(call.getForbidden())) {
              // there is at least one CallStatement which explicitly allows this Vertex!
              log.info(
                  "Vertex |{}| is allowed, since it matches whitelist entry {}",
                  v.value("code"),
                  callString);
              vertex_allowed = true;
              break;
            } else {
              violating.add(callString);
            }
          }
          if (!vertex_allowed) {
            Finding f =
                new Finding(
                    "Violation against forbidden call(s) "
                        + String.join(", ", violating)
                        + " in Entity "
                        + ent.getName()
                        + ". Call was "
                        + v.value("code").toString(),
                    v.value("startLine"),
                    v.value("endLine"),
                    v.value("startColumn"),
                    v.value("endColumn"));
            ctx.getFindings().add(f);
            log.info("Finding: {}", f.toString());
          }
        }
      }
    }
  }

  private void evaluateRules(AnalysisContext ctx) {
    for (MRule rule : markModel.getRules()) {
      if (rule.getStatement() != null && rule.getStatement().getEnsure() != null) {
        RuleStatement s = rule.getStatement();
        log.info("checking rule " + rule.getName());
        if (s.getCond() != null) {
          if (evaluateTopLevelExpr(s.getCond().getExp()) == TRISTATE.FALSE) {
            log.info(
                "   terminate rule checking due to unsatisfied guarding condition: "
                    + exprToString(s.getCond().getExp()));
            ctx.getFindings()
                .add(
                    new Finding(
                        "MarkRuleEvaluationFinding: Rule "
                            + rule.getName()
                            + ": guarding condition unsatisfied"));
          } else if (evaluateTopLevelExpr(s.getCond().getExp()) == TRISTATE.UNKNOWN) {
            log.warn(
                "The rule '"
                    + rule.getName()
                    + "' will not be checked because it's guarding condition cannot be evaluated: "
                    + exprToString(s.getCond().getExp()));
            ctx.getFindings()
                .add(
                    new Finding(
                        "MarkRuleEvaluationFinding: Rule "
                            + rule.getName()
                            + ": guarding condition unknown"));
          }
        }

        log.debug("checking 'ensure'-statement");
        if (evaluateTopLevelExpr(s.getEnsure().getExp()) == TRISTATE.UNKNOWN) {
          log.warn(
              "Ensure statement of rule '"
                  + rule.getName()
                  + "' cannot be evaluated: "
                  + exprToString(s.getEnsure().getExp()));
          ctx.getFindings()
              .add(
                  new Finding(
                      "MarkRuleEvaluationFinding: Rule "
                          + rule.getName()
                          + ": ensure condition unknown"));
        } else if (evaluateTopLevelExpr(s.getEnsure().getExp()) == TRISTATE.FALSE) {
          log.error("Rule '" + rule.getName() + "' is violated.");
          ctx.getFindings()
              .add(
                  new Finding(
                      "MarkRuleEvaluationFinding: Rule "
                          + rule.getName()
                          + ": ensure condition violated"));
        } else if (evaluateTopLevelExpr(s.getEnsure().getExp()) == TRISTATE.TRUE) {
          log.info("Rule '" + rule.getName() + "' is satisfied.");
          ctx.getFindings()
              .add(
                  new Finding(
                      "MarkRuleEvaluationFinding: Rule "
                          + rule.getName()
                          + ": ensure condition satisfied"));
        } else {
          assert false; // no other paths expected here
        }
      }
    }
  }

  private TRISTATE evaluateTopLevelExpr(Expression expr) {
    if (expr instanceof OrderExpression) {
      return evaluateOrderExpression((OrderExpression) expr);
    }

    Optional<Boolean> result = evaluateLogicalExpr(expr);
    if (result.isEmpty()) {
      return TRISTATE.UNKNOWN;
    }

    if (result.get()) {
      return TRISTATE.TRUE;
    } else {
      return TRISTATE.FALSE;
    }
  }

  private TRISTATE evaluateOrderExpression(OrderExpression orderExpression) {
    if (orderExpression instanceof Terminal) {
      return containedInModel((Terminal) orderExpression) ? TRISTATE.TRUE : TRISTATE.FALSE;
    } else {
      return TRISTATE.UNKNOWN;
    }
  }

  private Optional<Boolean> evaluateLogicalExpr(Expression expr) {
    if (expr instanceof ComparisonExpression) {
      return evaluateComparisonExpr((ComparisonExpression) expr);

    } else if (expr instanceof LogicalAndExpression) {
      Expression left = ((LogicalAndExpression) expr).getLeft();
      Expression right = ((LogicalAndExpression) expr).getRight();
      Optional<Boolean> leftResult = evaluateLogicalExpr(left);
      Optional<Boolean> rightResult = evaluateLogicalExpr(right);
      if (leftResult.isEmpty() || rightResult.isEmpty()) {
        return Optional.empty();
      } else {
        return Optional.of(leftResult.get() && rightResult.get());
      }

    } else if (expr instanceof LogicalOrExpression) {
      Expression left = ((LogicalOrExpression) expr).getLeft();
      Expression right = ((LogicalOrExpression) expr).getRight();
      Optional<Boolean> leftResult = evaluateLogicalExpr(left);
      Optional<Boolean> rightResult = evaluateLogicalExpr(right);
      if (leftResult.isEmpty()) {
        return rightResult;
      } else if (rightResult.isEmpty()) {
        return leftResult;
      } else {
        return Optional.of(leftResult.get() || rightResult.get());
      }
    } else if (expr instanceof FunctionCallExpression) {
      return evaluateFunctionCallExpr((FunctionCallExpression) expr);
    } else {
      // TODO: create custom exceptions
      throw new RuntimeException("not a logical expression: " + expr);
    }
  }

  private Optional<Boolean> evaluateComparisonExpr(ComparisonExpression expr) {
    String op = expr.getOp();
    Expression left = expr.getLeft();
    Expression right = expr.getRight();
    log.debug(
        "comparing expression " + exprToString(left) + " with expression " + exprToString(right));
    Optional leftResult = evaluateUnknownExpr(left);
    Optional rightResult = evaluateUnknownExpr(right);
    if (leftResult.isEmpty() || rightResult.isEmpty()) {
      return Optional.empty();
    }
    log.debug("left result= " + leftResult.get() + " right result= " + rightResult.get());

    // TODO implement remaining operations
    switch (op) {
      case "==":
        return Optional.of(leftResult.get().equals(rightResult.get()));
      case "!=":
      case "<":
      case "<=":
      case ">":
      case ">=":
      case "in":
      case "like":
        return Optional.empty();
      default:
        assert false;
        return Optional.empty();
    }
  }

  private Vector<Optional> evaluateArgs(EList<Argument> argList, int n) {
    Vector<Optional> result = new Vector<Optional>();
    for (int i = 0; i < n; i++) {
      Expression arg = (Expression) argList.get(i);
      result.add(evaluateUnknownExpr(arg));
    }
    return result;
  }

  private Optional evaluateFunctionCallExpr(Expression expr) {
    assert expr instanceof FunctionCallExpression;

    FunctionCallExpression callExpr = (FunctionCallExpression) expr;
    String functionName = callExpr.getName();
    if (functionName.startsWith("_")) {
      // call to built-in functions

      if (functionName.equals("_split")) {
        Vector<Optional> argOptionals = evaluateArgs(callExpr.getArgs(), 3);
        int i = 0;
        for (Optional arg : argOptionals) {
          if (arg.isEmpty()) {
            return Optional.empty();
          }
          i++;
        }
        String s = (String) argOptionals.get(0).get();
        String regex = (String) argOptionals.get(1).get();
        int index = (Integer) argOptionals.get(2).get();
        log.debug("args are: " + s + "; " + regex + "; " + index);
        return Optional.of(Builtins._split(s, regex, index));
      }

      if (functionName.contains("receives_value_from")) {
        return Optional.of(Builtins._receives_value_from());
      }
    }

    return Optional.empty();
  }

  private Optional evaluateLiteral(Literal literal) {
    if (literal instanceof StringLiteral) {
      return Optional.of(Utils.stripQuotedString(literal.getValue()));
    } else if (literal instanceof IntegerLiteral) {
      return Optional.of(Integer.valueOf(literal.getValue()));
    } else {
      throw new RuntimeException("not yet implemented");
    }
  }

  private Optional evaluateUnknownExpr(Expression expr) {
    // TODO implement!

    if (expr instanceof AdditionExpression) {
      log.debug("evaluating AdditionExpression: " + exprToString(expr));
      return Optional.empty();
    } else if (expr instanceof FunctionCallExpression) {
      log.debug("evaluating FunctionCallExpression: " + exprToString(expr));
      return evaluateFunctionCallExpr(expr);
    } else if (expr instanceof Literal) {
      log.debug("evaluating Literal expression: " + exprToString(expr));
      Literal literal = (Literal) expr;
      return evaluateLiteral(literal);
    } else if (expr instanceof LiteralListExpression) {
      log.debug("evaluating LiteralListExpression: " + exprToString(expr));
      return Optional.empty();
    } else if (expr instanceof LogicalAndExpression) {
      log.debug("evaluating LogicalAndExpression: " + exprToString(expr));
      return evaluateLogicalExpr(expr);
    } else if (expr instanceof LogicalOrExpression) {
      log.debug("evaluating LogicalOrExpression: " + exprToString(expr));
      return evaluateLogicalExpr(expr);
    } else if (expr instanceof MultiplicationExpression) {
      log.debug("evaluating MultiplicationExpression: " + exprToString(expr));
      return Optional.empty();
    } else if (expr instanceof Operand) {
      log.debug("evaluating Operand expression: " + exprToString(expr));
      Operand o = (Operand) expr;
      // TODO just for test. Implement!
      return Optional.of(o.getOperand());
    } else if (expr instanceof UnaryExpression) {
      log.debug("evaluating UnaryExpression: " + exprToString(expr));
      return Optional.empty();
    } else {
      log.error("unknown expression: " + exprToString(expr));
      assert false; // all expression types must be handled
      return Optional.empty();
    }
  }

  /**
   * DUMMY FOR DEMO.
   *
   * <p>Method fakes that a statement is contained in the a MARK entity
   *
   * @return
   */
  private boolean containedInModel(Terminal expr) {
    return true;
  }
}
