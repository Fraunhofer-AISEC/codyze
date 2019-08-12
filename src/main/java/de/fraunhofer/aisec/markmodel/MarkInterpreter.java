package de.fraunhofer.aisec.markmodel;

import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import de.fraunhofer.aisec.crymlin.server.AnalysisContext;
import de.fraunhofer.aisec.crymlin.structures.Finding;
import de.fraunhofer.aisec.crymlin.utils.Builtins;
import de.fraunhofer.aisec.crymlin.utils.CrymlinQueryWrapper;
import de.fraunhofer.aisec.crymlin.utils.Pair;
import de.fraunhofer.aisec.crymlin.utils.Utils;
import de.fraunhofer.aisec.mark.markDsl.AdditionExpression;
import de.fraunhofer.aisec.mark.markDsl.Argument;
import de.fraunhofer.aisec.mark.markDsl.BooleanLiteral;
import de.fraunhofer.aisec.mark.markDsl.CharacterLiteral;
import de.fraunhofer.aisec.mark.markDsl.ComparisonExpression;
import de.fraunhofer.aisec.mark.markDsl.Expression;
import de.fraunhofer.aisec.mark.markDsl.FloatingPointLiteral;
import de.fraunhofer.aisec.mark.markDsl.FunctionCallExpression;
import de.fraunhofer.aisec.mark.markDsl.FunctionDeclaration;
import de.fraunhofer.aisec.mark.markDsl.IntegerLiteral;
import de.fraunhofer.aisec.mark.markDsl.Literal;
import de.fraunhofer.aisec.mark.markDsl.LiteralListExpression;
import de.fraunhofer.aisec.mark.markDsl.LogicalAndExpression;
import de.fraunhofer.aisec.mark.markDsl.LogicalOrExpression;
import de.fraunhofer.aisec.mark.markDsl.MultiplicationExpression;
import de.fraunhofer.aisec.mark.markDsl.OpStatement;
import de.fraunhofer.aisec.mark.markDsl.Operand;
import de.fraunhofer.aisec.mark.markDsl.OrderExpression;
import de.fraunhofer.aisec.mark.markDsl.RepetitionExpression;
import de.fraunhofer.aisec.mark.markDsl.RuleStatement;
import de.fraunhofer.aisec.mark.markDsl.SequenceExpression;
import de.fraunhofer.aisec.mark.markDsl.StringLiteral;
import de.fraunhofer.aisec.mark.markDsl.Terminal;
import de.fraunhofer.aisec.mark.markDsl.UnaryExpression;
import de.fraunhofer.aisec.markmodel.fsm.Node;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.emf.common.util.EList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarkInterpreter {
  private static final Logger log = LoggerFactory.getLogger(MarkInterpreter.class);
  @NonNull private final Mark markModel;

  public MarkInterpreter(@NonNull Mark markModel) {
    this.markModel = markModel;
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
          "Done evaluating rules in {} ms.", Duration.between(start, Instant.now()).toMillis());

      log.info(
          "Done evaluating ALL MARK in {} ms.",
          Duration.between(outer_start, Instant.now()).toMillis());

      return result;
    } finally {

      // reset stuff attached to this model
      this.markModel.reset();
    }
  }

  private void evaluateOrder(AnalysisContext ctx, CrymlinTraversalSource crymlinTraversal) {
    /*
    We also look through forbidden nodes. The fact that these are forbidden is checked elsewhere
    Any function calls to functions which are not specified in an entity are _ignored_
     */

    // precalculate, if we have any order to evaluate
    boolean hasVertices = false;
    outer:
    for (MEntity ent : this.markModel.getEntities()) {
      for (MOp op : ent.getOps()) {
        if (!op.getAllVertices().isEmpty()) {
          hasVertices = true;
          break outer;
        }
      }
    }
    if (!hasVertices) {
      log.info("no nodes match for TU and MARK-model. Skipping evaluation.");
      return;
    }

    for (MRule rule : this.markModel.getRules()) {

      // if this is null, there is no order-statement for this rule
      if (rule.getFSM() == null) {
        continue;
      }

      // rule.getFSM().pushToDB(); //debug only
      log.info("\tEvaluating rule {}", rule.getName());

      // Cache which Vertex belongs to which Op/Entity
      // a vertex can _only_ belong to one entity/op!
      HashMap<Vertex, MOp> verticesToOp = new HashMap<>();
      for (Map.Entry<String, Pair<String, MEntity>> entry : rule.getEntityReferences().entrySet()) {
        MEntity ent = entry.getValue().getValue1();
        if (ent == null) {
          continue;
        }
        for (MOp op : ent.getOps()) {
          op.getAllVertices().forEach(v -> verticesToOp.put(v, op));
        }
      }

      if (verticesToOp.isEmpty()) {
        log.info("no nodes match this rule. Skipping rule.");
        continue;
      }

      for (Vertex functionDeclaration : crymlinTraversal.functiondeclarations().toList()) {
        log.info("Evaluating function {}", (Object) functionDeclaration.value("name"));

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
        // stores the current markings in the FSM (i.e., which base is at which
        // FSM-node)
        HashMap<String, HashSet<Node>> baseToFSMNodes = new HashMap<>();
        // last usage of base
        HashMap<String, Vertex> lastBaseUsage = new HashMap<>();

        HashMap<Long, HashSet<String>> nodeIDtoEOGPathSet = new HashMap<>();
        HashSet<String> startEOG = new HashSet<>();
        startEOG.add("0");
        nodeIDtoEOGPathSet.put((Long) functionDeclaration.id(), startEOG);

        HashSet<String> seenStates = new HashSet<>();
        long visitedNodes = 0;

        while (!currentWorklist.isEmpty()) {
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
              if (vertex.label().contains("MemberCallExpression")
                  // is the vertex part of any op of any mentioned entity? If not, ignore
                  && verticesToOp.get(vertex) != null) {

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
                  // base this way, we could differentiate between nodes with the same base
                  // name, but referencing different variables (e.g., if they are used in
                  // different blocks)
                  if (ref != null) {
                    base += "|" + ref;
                  }

                  String prefixedBase = eogPath + "." + base;

                  if (isDisallowedBase(disallowedBases, eogPath, base)) {
                    //                      Finding f =
                    //                          new Finding(
                    //                              "Violation against Order: "
                    //                                  + vertex.value("code")
                    //                                  + " is not allowed. Base contains errors
                    // already."
                    //                                  + " ("
                    //                                  + rule.getErrorMessage()
                    //                                  + ")",
                    //                              vertex.value("startLine"),
                    //                              vertex.value("endLine"),
                    //                              vertex.value("startColumn"),
                    //                              vertex.value("endColumn"));
                    // we hide base errors for now!
                    // ctx.getFindings().add(f);
                    // log.info("Finding: {}", f.toString());
                  } else {
                    HashSet<Node> nodesInFSM;
                    if (baseToFSMNodes.get(prefixedBase) == null) {
                      // we have not seen this base before. check if this is the start of an
                      // order
                      nodesInFSM = rule.getFSM().getStart(); // start nodes
                    } else {
                      nodesInFSM = baseToFSMNodes.get(prefixedBase); // nodes
                      // calculated in previous step
                    }

                    HashSet<Node> nextNodesInFSM = new HashSet<>();

                    // did at least one fsm-Node-match occur?
                    boolean match = false;
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
                      log.info("Finding: {}", f);
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
              ArrayList<Vertex> outVertices = new ArrayList<>();
              vertex
                  .edges(Direction.OUT, "EOG")
                  .forEachRemaining(edge -> outVertices.add(edge.inVertex()));

              // if more than one vertex follows the current one, we need to branch the eogPath
              if (outVertices.size() > 1) { // split
                HashSet<String> oldBases = new HashSet<>();
                HashMap<String, HashSet<Node>> newBases = new HashMap<>();
                // first we collect all entries which we need to remove from the baseToFSMNodes
                // map we also store these entries without the eog path prefix, to update later
                // in (1)
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
              } else if (outVertices.size() == 1) {
                // else, if we only have one vertex following this
                // vertex, simply propagate the current eogpath to the next vertex
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
                    entry.getKey().substring(entry.getKey().indexOf('.') + 1),
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
          log.info("Finding: {}", f);
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
            log.info("Finding: {}", f);
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

        if (s.getEnsure() != null && s.getEnsure().getExp() instanceof OrderExpression) {
          continue;
          // todo maybe comment in again, if the order-statements are generally caught by
          // this function. for now, order is done separately.
        }

        if (s.getCond() != null) {
          Optional<Boolean> condResult = evaluateTopLevelExpr(s.getCond().getExp());

          if (condResult.isEmpty()) {
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
          } else if (!condResult.get()) {
            log.info(
                "   terminate rule checking due to unsatisfied guarding condition: "
                    + exprToString(s.getCond().getExp()));
            ctx.getFindings()
                .add(
                    new Finding(
                        "MarkRuleEvaluationFinding: Rule "
                            + rule.getName()
                            + ": guarding condition unsatisfied"));
          }
        }

        log.debug("checking 'ensure'-statement");
        Optional<Boolean> ensureResult = evaluateTopLevelExpr(s.getEnsure().getExp());

        if (ensureResult.isEmpty()) {
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
        } else if (ensureResult.get()) {
          log.info("Rule '" + rule.getName() + "' is satisfied.");
          ctx.getFindings()
              .add(
                  new Finding(
                      "MarkRuleEvaluationFinding: Rule "
                          + rule.getName()
                          + ": ensure condition satisfied"));
        } else {
          log.error("Rule '" + rule.getName() + "' is violated.");
          ctx.getFindings()
              .add(
                  new Finding(
                      "MarkRuleEvaluationFinding: Rule "
                          + rule.getName()
                          + ": ensure condition violated"));
        }
      }
    }
  }

  private Optional<Boolean> evaluateTopLevelExpr(Expression expr) {
    if (expr instanceof OrderExpression) {
      return evaluateOrderExpression((OrderExpression) expr);
    }

    Optional result = evaluateExpression(expr);

    if (result.isEmpty()) {
      log.error("Expression could not be evaluated: {}", exprToString(expr));
      return Optional.empty();
    }

    log.debug("Top level expression was evaluated: {}", result.get());

    if (!result.get().getClass().equals(Boolean.class)) {
      log.error("Expression result is not Boolean");
      return Optional.empty();
    }

    return result;
  }

  private Optional<Boolean> evaluateOrderExpression(OrderExpression orderExpression) {
    // TODO integrate Dennis' evaluateOrder()-function
    return Optional.empty();
  }

  private Optional<Boolean> evaluateLogicalExpr(Expression expr) {
    log.debug("Evaluating logical expression: {}", exprToString(expr));

    if (expr instanceof ComparisonExpression) {
      return evaluateComparisonExpr((ComparisonExpression) expr);
    } else if (expr instanceof LogicalAndExpression) {
      LogicalAndExpression lae = (LogicalAndExpression) expr;

      Expression left = lae.getLeft();
      Expression right = lae.getRight();

      Optional leftResult = evaluateExpression(left);
      Optional rightResult = evaluateExpression(right);

      if (leftResult.isEmpty() || rightResult.isEmpty()) {
        log.error("At least one subexpression could not be evaluated");
        return Optional.empty();
      }

      if (leftResult.get().getClass().equals(Boolean.class)
          && rightResult.get().getClass().equals(Boolean.class)) {
        return Optional.of(
            Boolean.logicalAnd((Boolean) leftResult.get(), (Boolean) rightResult.get()));
      }

      // TODO #8
      log.error(
          "At least one subexpression is not of type Boolean: {} vs. {}",
          exprToString(left),
          exprToString(right));

      return Optional.empty();
    } else if (expr instanceof LogicalOrExpression) {
      LogicalOrExpression loe = (LogicalOrExpression) expr;

      Expression left = loe.getLeft();
      Expression right = loe.getRight();

      Optional leftResult = evaluateExpression(left);
      Optional rightResult = evaluateExpression(right);

      if (leftResult.isEmpty() || rightResult.isEmpty()) {
        log.error("At least one subexpression could not be evaluated");
        return Optional.empty();
      }

      if (leftResult.get().getClass().equals(Boolean.class)
          && rightResult.get().getClass().equals(Boolean.class)) {
        return Optional.of(
            Boolean.logicalAnd((Boolean) leftResult.get(), (Boolean) rightResult.get()));
      }

      // TODO #8
      log.error(
          "At least one subexpression is not of type Boolean: {} vs. {}",
          exprToString(left),
          exprToString(right));

      return Optional.empty();
    }

    log.error("Trying to evaluate unknown logical expression: {}", exprToString(expr));

    assert false; // not a logical expression
    return Optional.empty();
  }

  private Optional<Boolean> evaluateComparisonExpr(ComparisonExpression expr) {
    String op = expr.getOp();
    Expression left = expr.getLeft();
    Expression right = expr.getRight();

    log.debug(
        "comparing expression " + exprToString(left) + " with expression " + exprToString(right));

    Optional leftResult = evaluateExpression(left);
    Optional rightResult = evaluateExpression(right);

    if (leftResult.isEmpty() || rightResult.isEmpty()) {
      return Optional.empty();
    }

    Class leftType = leftResult.get().getClass();
    Class rightType = rightResult.get().getClass();

    log.debug("left result= " + leftResult.get() + " right result= " + rightResult.get());

    // TODO implement remaining operations
    switch (op) {
      case "==":
        if (leftType.equals(rightType)) {
          return Optional.of(leftResult.get().equals(rightResult.get()));
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftType.getSimpleName(),
            rightType.getSimpleName());

        return Optional.empty();
      case "!=":
        if (leftType.equals(rightType)) {
          return Optional.of(!leftResult.get().equals(rightResult.get()));
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftType.getSimpleName(),
            rightType.getSimpleName());

        return Optional.empty();
      case "<":
        if (leftType.equals(rightType)) {
          if (leftType.equals(Integer.class)) {
            return Optional.of(((Integer) leftResult.get()) < ((Integer) rightResult.get()));
          } else if (leftType.equals(Float.class)) {
            return Optional.of(((Float) leftResult.get()) < ((Float) rightResult.get()));
          }

          log.error("Comparison operator less-than ('<') not supported for type: {}", leftType);

          return Optional.empty();
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftType.getSimpleName(),
            rightType.getSimpleName());

        return Optional.empty();
      case "<=":
        if (leftType.equals(rightType)) {
          if (leftType.equals(Integer.class)) {
            return Optional.of(((Integer) leftResult.get()) <= ((Integer) rightResult.get()));
          } else if (leftType.equals(Float.class)) {
            return Optional.of(((Float) leftResult.get()) <= ((Float) rightResult.get()));
          }

          log.error(
              "Comparison operator less-than-or-equal ('<=') not supported for type: {}", leftType);

          return Optional.empty();
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftType.getSimpleName(),
            rightType.getSimpleName());

        return Optional.empty();
      case ">":
        if (leftType.equals(rightType)) {
          if (leftType.equals(Integer.class)) {
            return Optional.of(((Integer) leftResult.get()) > ((Integer) rightResult.get()));
          } else if (leftType.equals(Float.class)) {
            return Optional.of(((Float) leftResult.get()) > ((Float) rightResult.get()));
          }

          log.error("Comparison operator greater-than ('>') not supported for type: {}", leftType);

          return Optional.empty();
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftType.getSimpleName(),
            rightType.getSimpleName());

        return Optional.empty();
      case ">=":
        if (leftType.equals(rightType)) {
          if (leftType.equals(Integer.class)) {
            return Optional.of(((Integer) leftResult.get()) >= ((Integer) rightResult.get()));
          } else if (leftType.equals(Float.class)) {
            return Optional.of(((Float) leftResult.get()) >= ((Float) rightResult.get()));
          }

          log.error(
              "Comparison operator greater-than-or-equal ('>=') not supported for type: {}",
              leftType);

          return Optional.empty();
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftType.getSimpleName(),
            rightType.getSimpleName());

        return Optional.empty();
      case "in":
        if (rightResult.get() instanceof List) {
          List l = (List) rightResult.get();

          boolean evalValue = false;
          for (Object o : l) {
            log.debug(
                "Comparing left expression with element of right expression: {} vs. {}",
                leftResult.get(),
                o);

            if (o != null && leftType.equals(o.getClass())) {
              evalValue |= leftResult.get().equals(o);
            }
          }

          return Optional.of(evalValue);
        }

        // TODO #8
        log.error("Type of right expression must be List; given: {}", rightType);

        return Optional.empty();
      case "like":
        if (leftType.equals(rightType)) {
          if (leftType.equals(String.class)) {
            return Optional.of(
                Pattern.matches(
                    Pattern.quote((String) rightResult.get()), (String) leftResult.get()));
          }

          log.error("Comparison operator like ('like') not supported for type: {}", leftType);

          return Optional.empty();
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftType.getSimpleName(),
            rightType.getSimpleName());

        return Optional.empty();
    }

    assert false;
    return Optional.empty();
  }

  private List<Optional> evaluateArgs(EList<Argument> argList, int n) {
    List<Optional> result = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      Expression arg = (Expression) argList.get(i);
      result.add(evaluateExpression(arg));
    }
    return result;
  }

  private Optional evaluateFunctionCallExpr(FunctionCallExpression expr) {
    String functionName = expr.getName();
    if (functionName.startsWith("_")) {
      // call to built-in functions

      // TODO: use Java reflections to get name and arguments of builtin functions
      if (functionName.equals("_split")) {
        List<Optional> argOptionals = evaluateArgs(expr.getArgs(), 3);

        for (Optional arg : argOptionals) {
          if (arg.isEmpty()) {
            return Optional.empty();
          }
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
    String v = literal.getValue();
    log.debug("Literal with value: {}", v);

    // ordering based on Mark grammar
    if (literal instanceof IntegerLiteral) {
      log.debug("Literal is Integer: {}", v);

      try {
        if (v.startsWith("0x")) {
          return Optional.of(Integer.parseInt(v.substring(2), 16));
        }
        return Optional.of(Integer.parseInt(v));
      } catch (NumberFormatException nfe) {
        log.error("Unable to convert integer literal to Integer: {}\n{}", v, nfe);
      }
      return Optional.empty();
    } else if (literal instanceof FloatingPointLiteral) {
      log.debug("Literal is Floating Point: {}", v);
      return Optional.of(Float.parseFloat(v));
    } else if (literal instanceof BooleanLiteral) {
      log.debug("Literal is Boolean: {}", v);
      return Optional.of(Boolean.parseBoolean(v));
    } else if (literal instanceof CharacterLiteral) {
      log.debug("Literal is Character: {}", v);
      String strippedV = Utils.stripQuotedCharacter(v);

      if (strippedV.length() > 1) {
        log.warn("Character literal with length greater 1 found: {}", strippedV);
      }
      return Optional.of(strippedV.charAt(0));
    } else if (literal instanceof StringLiteral) {
      log.debug("Literal is String: {}", v);
      return Optional.of(Utils.stripQuotedString(v));
    }

    log.error("Unknown literal encountered: {}", v);
    return Optional.empty();
  }

  private Optional evaluateExpression(Expression expr) {
    // from lowest to highest operator precedence

    if (expr instanceof LogicalOrExpression) {
      log.debug("evaluating LogicalOrExpression: " + exprToString(expr));
      return evaluateLogicalExpr(expr);
    } else if (expr instanceof LogicalAndExpression) {
      log.debug("evaluating LogicalAndExpression: " + exprToString(expr));
      return evaluateLogicalExpr(expr);
    } else if (expr instanceof ComparisonExpression) {
      log.debug("evaluating ComparisonExpression: " + exprToString(expr));
      return evaluateLogicalExpr(expr);
    } else if (expr instanceof AdditionExpression) {
      log.debug("evaluating AdditionExpression: " + exprToString(expr));
      return evaluateAdditionExpr((AdditionExpression) expr);
    } else if (expr instanceof MultiplicationExpression) {
      log.debug("evaluating MultiplicationExpression: " + exprToString(expr));
      return evaluateMultiplicationExpr((MultiplicationExpression) expr);
    } else if (expr instanceof UnaryExpression) {
      log.debug("evaluating UnaryExpression: " + exprToString(expr));
      return evaluateUnaryExpr((UnaryExpression) expr);
    } else if (expr instanceof Literal) {
      log.debug("evaluating Literal expression: " + exprToString(expr));
      return evaluateLiteral((Literal) expr);
    } else if (expr instanceof Operand) {
      log.debug("evaluating Operand expression: " + exprToString(expr));

      // TODO just for test. Implement!
      Operand o = (Operand) expr;
      return Optional.of(o.getOperand());
    } else if (expr instanceof FunctionCallExpression) {
      log.debug("evaluating FunctionCallExpression: " + exprToString(expr));
      return evaluateFunctionCallExpr((FunctionCallExpression) expr);
    } else if (expr instanceof LiteralListExpression) {
      log.debug("evaluating LiteralListExpression: " + exprToString(expr));

      List literalList = new ArrayList<>();

      for (Literal l : ((LiteralListExpression) expr).getValues()) {
        Optional v = evaluateLiteral(l);

        literalList.add(v.orElse(null));
      }
      return Optional.of(literalList);
    }

    log.error("unknown expression: " + exprToString(expr));
    assert false; // all expression types must be handled
    return Optional.empty();
  }

  private Optional evaluateAdditionExpr(AdditionExpression expr) {
    log.debug("Evaluating addition expression: {}", exprToString(expr));

    String op = expr.getOp();
    Expression left = expr.getLeft();
    Expression right = expr.getRight();

    Optional leftResult = evaluateExpression(left);
    Optional rightResult = evaluateExpression(right);

    if (leftResult.isEmpty() || rightResult.isEmpty()) {
      log.error("Unable to evaluate at least one subexpression");
      return Optional.empty();
    }

    Class leftResultType = leftResult.get().getClass();
    Class rightResultType = rightResult.get().getClass();

    switch (op) {
      case "+":
        if (leftResultType.equals(rightResultType)) {
          if (leftResultType.equals(Integer.class)) {
            return Optional.of(((Integer) leftResult.get()) + ((Integer) rightResult.get()));
          } else if (leftResultType.equals(Float.class)) {
            return Optional.of(((Float) leftResult.get()) + ((Float) rightResult.get()));
          }

          // TODO #8
          log.error(
              "Addition operator plus ('+') not supported for type: {}",
              leftResultType.getSimpleName());
          return Optional.empty();
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftResultType.getSimpleName(),
            rightResultType.getSimpleName());

        return Optional.empty();
      case "-":
        if (leftResultType.equals(rightResultType)) {
          if (leftResultType.equals(Integer.class)) {
            return Optional.of(((Integer) leftResult.get()) - ((Integer) rightResult.get()));
          } else if (leftResultType.equals(Float.class)) {
            return Optional.of(((Float) leftResult.get()) - ((Float) rightResult.get()));
          }

          // TODO #8
          log.error(
              "Addition operator minus ('-') not supported for type: {}",
              leftResultType.getSimpleName());
          return Optional.empty();
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftResultType.getSimpleName(),
            rightResultType.getSimpleName());

        return Optional.empty();
      case "|":
        if (leftResultType.equals(rightResultType)) {
          if (leftResultType.equals(Integer.class)) {
            return Optional.of(((Integer) leftResult.get()) | ((Integer) rightResult.get()));
          }

          // TODO #8
          log.error(
              "Addition operator bitwise or ('|') not supported for type: {}",
              leftResultType.getSimpleName());
          return Optional.empty();
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftResultType.getSimpleName(),
            rightResultType.getSimpleName());

        return Optional.empty();
      case "^":
        if (leftResultType.equals(rightResultType)) {
          if (leftResultType.equals(Integer.class)) {
            return Optional.of(((Integer) leftResult.get()) ^ ((Integer) rightResult.get()));
          }

          // TODO #8
          log.error(
              "Addition operator bitwise xor ('^') not supported for type: {}",
              leftResultType.getSimpleName());
          return Optional.empty();
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftResultType.getSimpleName(),
            rightResultType.getSimpleName());

        return Optional.empty();
    }

    log.error("Trying to evaluate unknown addition expression: {}", exprToString(expr));

    assert false; // not an addition expression
    return Optional.empty();
  }

  private Optional evaluateMultiplicationExpr(MultiplicationExpression expr) {
    log.debug("Evaluating multiplication expression: {}", exprToString(expr));

    String op = expr.getOp();
    Expression left = expr.getLeft();
    Expression right = expr.getRight();

    Optional leftResult = evaluateExpression(left);
    Optional rightResult = evaluateExpression(right);

    if (leftResult.isEmpty() || rightResult.isEmpty()) {
      log.error("Unable to evaluate at least one subexpression");
      return Optional.empty();
    }

    Class leftResultType = leftResult.get().getClass();
    Class rightResultType = rightResult.get().getClass();

    switch (op) {
      case "*":
        if (leftResultType.equals(rightResultType)) {
          // TODO check if an overflow occurs
          if (leftResultType.equals(Integer.class)) {
            return Optional.of(((Integer) leftResult.get()) * ((Integer) rightResult.get()));
          } else if (leftResultType.equals(Float.class)) {
            return Optional.of(((Float) leftResult.get()) * ((Float) rightResult.get()));
          }

          // TODO #8
          log.error(
              "Multiplication operator multiplication ('*') not supported for type: {}",
              leftResultType.getSimpleName());
          return Optional.empty();
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftResultType.getSimpleName(),
            rightResultType.getSimpleName());

        return Optional.empty();
      case "/":
        if (leftResultType.equals(rightResultType)) {
          if (leftResultType.equals(Integer.class)) {
            return Optional.of(((Integer) leftResult.get()) / ((Integer) rightResult.get()));
          } else if (leftResultType.equals(Float.class)) {
            return Optional.of(((Float) leftResult.get()) / ((Float) rightResult.get()));
          }

          // TODO #8
          log.error(
              "Multiplication operator division ('/') not supported for type: {}",
              leftResultType.getSimpleName());
          return Optional.empty();
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftResultType.getSimpleName(),
            rightResultType.getSimpleName());

        return Optional.empty();
      case "%":
        if (leftResultType.equals(rightResultType)) {
          if (leftResultType.equals(Integer.class)) {
            return Optional.of(((Integer) leftResult.get()) % ((Integer) rightResult.get()));
          }

          // TODO #8
          log.error(
              "Multiplication operator remainder ('%') not supported for type: {}",
              leftResultType.getSimpleName());
          return Optional.empty();
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftResultType.getSimpleName(),
            rightResultType.getSimpleName());

        return Optional.empty();
      case "<<":
        if (leftResultType.equals(rightResultType)) {
          if (leftResultType.equals(Integer.class)) {
            if (((Integer) rightResult.get()) >= 0) {
              Optional.of(((Integer) leftResult.get()) << ((Integer) rightResult.get()));
            }

            // TODO #8
            log.error(
                "Left shift operator supports only non-negative integers as its right operand");
            return Optional.empty();
          }

          // TODO #8
          log.error(
              "Multiplication operator left shift ('<<') not supported for type: {}",
              leftResultType.getSimpleName());
          return Optional.empty();
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftResultType.getSimpleName(),
            rightResultType.getSimpleName());

        return Optional.empty();
      case ">>":
        if (leftResultType.equals(rightResultType)) {
          if (leftResultType.equals(Integer.class)) {
            if (((Integer) rightResult.get()) >= 0) {
              Optional.of(((Integer) leftResult.get()) >> ((Integer) rightResult.get()));
            }

            // TODO #8
            log.error(
                "Right shift operator supports only non-negative integers as its right operand");
            return Optional.empty();
          }

          // TODO #8
          log.error(
              "Multiplication operator right shift ('>>') not supported for type: {}",
              leftResultType.getSimpleName());
          return Optional.empty();
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftResultType.getSimpleName(),
            rightResultType.getSimpleName());

        return Optional.empty();
      case "&":
        if (leftResultType.equals(rightResultType)) {
          if (leftResultType.equals(Integer.class)) {
            return Optional.of(((Integer) leftResult.get()) & ((Integer) rightResult.get()));
          }

          // TODO #8
          log.error(
              "Addition operator bitwise and ('&') not supported for type: {}",
              leftResultType.getSimpleName());
          return Optional.empty();
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftResultType.getSimpleName(),
            rightResultType.getSimpleName());

        return Optional.empty();
      case "&^":
        if (leftResultType.equals(rightResultType)) {
          if (leftResultType.equals(Integer.class)) {
            return Optional.of(((Integer) leftResult.get()) & ~((Integer) rightResult.get()));
          }

          // TODO #8
          log.error(
              "Addition operator bitwise or ('|') not supported for type: {}",
              leftResultType.getSimpleName());
          return Optional.empty();
        }

        // TODO #8
        log.error(
            "Type of left expression does not match type of right expression: {} vs. {}",
            leftResultType.getSimpleName(),
            rightResultType.getSimpleName());

        return Optional.empty();
    }

    // TODO #8
    log.error("Trying to evaluate unknown multiplication expression: {}", exprToString(expr));

    assert false; // not an addition expression
    return Optional.empty();
  }

  private Optional evaluateUnaryExpr(UnaryExpression expr) {
    log.debug("Evaluating unary expression: {}", exprToString(expr));

    String op = expr.getOp();
    Expression subExpr = expr.getExp();

    Optional subExprResult = evaluateExpression(subExpr);

    if (subExprResult.isEmpty()) {
      log.error("Unable to evaluate subexpression");
      return Optional.empty();
    }

    Class subExprResultType = subExprResult.get().getClass();

    switch (op) {
      case "+":
        if (subExprResultType.equals(Integer.class) || subExprResultType.equals(Float.class)) {
          return subExprResult;
        }

        // TODO #8
        log.error(
            "Unary operator plus sign ('+') not supported for type: {}",
            subExprResultType.getSimpleName());

        return Optional.empty();
      case "-":
        if (subExprResultType.equals(Integer.class)) {
          return Optional.of(-((Integer) subExprResult.get()));
        } else if (subExprResultType.equals(Float.class)) {
          return Optional.of(-((Float) subExprResult.get()));
        }

        // TODO #8
        log.error(
            "Unary operator minus sign ('-') not supported for type: {}",
            subExprResultType.getSimpleName());

        return Optional.empty();
      case "!":
        if (subExprResultType.equals(Boolean.class)) {
          return Optional.of(!((Boolean) subExprResult.get()));
        }

        // TODO #8
        log.error(
            "Unary operator logical not ('!') not supported for type: {}",
            subExprResultType.getSimpleName());

        return Optional.empty();
      case "^":
        if (subExprResultType.equals(Integer.class)) {
          return Optional.of(~((Integer) subExprResult.get()));
        }

        // TODO #8
        log.error(
            "Unary operator bitwise complement ('~') not supported for type: {}",
            subExprResultType.getSimpleName());

        return Optional.empty();
    }

    // TODO #8
    log.error("Trying to evaluate unknown unary expression: {}", exprToString(expr));

    assert false; // not an addition expression
    return Optional.empty();
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
