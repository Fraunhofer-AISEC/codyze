package de.fhg.aisec.markmodel;

import de.fhg.aisec.mark.markDsl.*;
import de.fhg.aisec.markmodel.fsm.FSM;
import de.fhg.aisec.markmodel.fsm.Node;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import de.fraunhofer.aisec.crymlin.server.AnalysisContext;
import de.fraunhofer.aisec.crymlin.server.AnalysisServer;
import de.fraunhofer.aisec.crymlin.structures.Finding;
import de.fraunhofer.aisec.crymlin.utils.CrymlinQueryWrapper;
import de.fraunhofer.aisec.crymlin.utils.Utils;
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
  private static final Logger log = LoggerFactory.getLogger(AnalysisServer.class);
  private final CrymlinTraversalSource crymlinTraversal;
  @NonNull private final Mark markModel;

  public MarkInterpreter(@NonNull Mark markModel, CrymlinTraversalSource crymlinTraversal) {
    this.markModel = markModel;
    this.crymlinTraversal = crymlinTraversal;
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

  public void dumpCFG(Edge edge, String pref, HashSet<Vertex> seen) {

    Vertex outV = edge.inVertex();
    if (seen.contains(outV)) {
      System.out.println("Already seen: " + edge);
      return;
    }
    seen.add(outV);
    System.out.println(pref + outV.label() + " - " + outV.property("name"));
    Iterator<Edge> cfg = outV.edges(Direction.OUT, "CFG");
    while (cfg.hasNext()) {
      Edge next = cfg.next();
      dumpCFG(next, pref + "\t", seen);
    }
  }

  private HashSet<Vertex> getVerticesForFunctionDeclaration(
      FunctionDeclaration functionDeclaration, MEntity ent) {
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

    // reset stuff attached to this model
    this.markModel.reset();

    /*
    iterate all entities and precalculate some things:
       - call statements to vertices
    */
    for (MEntity ent : this.markModel.getEntities()) {
      ent.parseVars();
      for (MOp op : ent.getOps()) {
        log.info("Parsing Call Statements for {}", op.getName());
        for (OpStatement a : op.getStatements()) {
          HashSet<Vertex> temp = getVerticesForFunctionDeclaration(a.getCall(), ent);
          log.info(
              a.getCall().getName()
                  + "("
                  + String.join(", ", a.getCall().getParams())
                  + "): "
                  + temp.size());
          op.addVertex(a, temp);
        }
        op.setParsingFinished();
      }
    }

    for (MRule rule : markModel.getRules()) {
      if (rule.getStatement() != null
          && rule.getStatement().getEnsure() != null
          && rule.getStatement().getEnsure().getExp() instanceof OrderExpression) {
        OrderExpression inner = (OrderExpression) rule.getStatement().getEnsure().getExp();
        FSM fsm = new FSM();
        fsm.sequenceToFSM(inner.getExp());
        rule.setFSM(fsm);

        // check that the fsm is valid:
        // TODO fw: does the grammar validate that each function in an order is actually
        //  an op already? then the following might be obsolete
        HashSet<Node> worklist = new HashSet<>(fsm.getStart());
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
                log.error(
                    "Entity is not parsed: "
                        + entityName
                        + " which is specified in rule "
                        + rule.getName());
              } else {
                MOp op = entity.getOp(n.getOp());
                if (op == null) {
                  log.error(
                      "Entity "
                          + entity.getName()
                          + " does not contain op "
                          + n.getOp()
                          + " which is specified in rule "
                          + rule.getName());
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

    evaluateForbiddenCalls(ctx);

    evaluateOrder(ctx);

    /*
     *
     *  // Iterate over all statements of the program, along the CFG (created by SimpleForwardCFG)
     *  for tu in translationunits:
     *    for func in tu.functions:
     *      for stmt in func.cfg:
     *
     *        // If an object of interest is created -> track it as an "abstract object"
     *        // "of interest" = mentioned as an Entity in at least one MARK "rule".
     *        if is_object_creation(stmt):
     *          a_obj = create_abstract_object(stmt)
     *          init_typestate(a_obj)
     *          object_table.add(a_obj)
     *
     *        // If an abstract object is "used" (= one of its fields is set or one of its methods is called) -> update its typestate.
     *        // "update its typestate" needs further detailing
     *        if uses_abstract_object(stmt):
     *          update_typestate(stmt)
     *
     */

    /*
     * A "typestate" item is an object that approximates the "states" of a real object instances at runtime.
     *
     * States are defined as the (approximated) values of the object's member fields.
     */

    // Maintain all method calls in a list
    //    CrymlinTraversal<Vertex, Vertex> calls =
    //        (CrymlinTraversal<Vertex, Vertex>) crymlinTraversal.calls().clone();
    //    List<Vertex> vertices = calls.toList();
    //    for (Vertex v : vertices) {
    //      v.property("dennis", "test"); // attach temporary property
    //      // System.out.println(v + " " + v.label() + " (" + v.property("name") + ")");
    //      //      v.edges(Direction.OUT)
    //      //          .forEachRemaining(
    //      //              x ->
    //      //                  System.out.println(
    //      //                      x.label()
    //      //                          + ": "
    //      //                          + x.inVertex().label()
    //      //                          + " ("
    //      //                          + x.inVertex().property("name")
    //      //                          + ")"
    //      //                          + " -> "
    //      //                          + x.outVertex().label()
    //      //                          + " ("
    //      //                          + x.inVertex().property("name")
    //      //                          + ")"));
    //
    //      if (v.graph().tx().isOpen()) {
    //        v.graph()
    //            .tx()
    //            .readWrite(); // should not be called by a program according to docu, but this
    // persists
    //        // our new property
    //      } else {
    //        System.out.println("cannot persist, tx is not open");
    //      }
    //    }

    // TEST
    /*
    List<Vertex> do_crypt = crymlinTraversal.functiondeclaration("do_crypt").clone().toList();
    if (do_crypt.size() != 1) {
      System.err.println("Multiple functions with name do_crypt found");
    }
    Edge current = do_crypt.get(0).edges(Direction.OUT, "BODY").next();
    */
    // dumpCFG(current, "", new HashSet<>());

    //    calls = (CrymlinTraversal<Vertex, Vertex>) crymlinTraversal.calls().clone();
    //    List<String> myCalls = calls.name().toList();
    //
    //    // "Populate" MARK objects
    //    for (MEntity ent : this.markModel.getEntities()) {
    //      Set<String> collect =
    //          ent.getOps().stream()
    //              .map(
    //                  x ->
    //                      x.getStatements().stream()
    //                          .map(cs -> cs.getCall().getName())
    //                          .collect(Collectors.toSet()))
    //              .flatMap(Collection::stream)
    //              .collect(Collectors.toSet());
    //
    //      Optional<String> any =
    //          myCalls.stream()
    //              .filter(
    //                  call -> collect.stream().anyMatch(x ->
    // x.endsWith(Utils.extractMethodName(call))))
    //              .findAny();
    //      // TODO now, only the function name is checked. We also need to check the Type the
    //      // function is executed on.
    //
    //      if (any.isPresent()) {
    //        System.out.println("MARK MATCHED - " + ent.getName());
    //        System.out.println("\t\t" + any.get());
    //        // TODO if myCalls.size()>0, we found a call that was specified in MARK. "Populate"
    // the
    //        // object.
    //        // TODO Find out arguments of the call and try to resolve concrete values for them
    //        // TODO "Populate" the entity and assign the resolved values to the entity's variables
    //        this.markModel.getPopulatedEntities().put(ent.getName(), ent);
    //      }
    //    }
    //
    //    // Evaluate rules against populated objects
    //    for (MRule r : this.markModel.getRules()) {
    //      // System.out.println("Processing rule " + r.getName());
    //      // TODO Result of rule evaluation will not be a boolean but "not triggered/triggered and
    //      // violated/triggered and satisfied".
    //      if (evaluateRule(r)) {
    //        ctx.getFindings().add("Rule " + r.getName() + " is satisfied");
    //      }
    //    }
    return result;
  }

  private String eogPathToString(HashSet<String> in) {
    return String.join("|", in);
  }

  private HashSet<String> stringToEogPath(String s) {
    if (s == null || s.isEmpty()) {
      return new HashSet<>();
    } else {
      String[] split = s.split("\\|");
      return new HashSet<>(Arrays.asList(split));
    }
  }

  private void evaluateOrder(AnalysisContext ctx) {
    log.info("Evaluating order");

    /*
    We also look through forbidden nodes. The fact that these are forbidden is checked elsewhere
    Any function calls to functions which are not specified in an entity are _ignored_
     */

    // Cache which Vertex belongs to which Op/Entity
    HashMap<Vertex, MOp> verticesToOp = new HashMap<>();
    for (MEntity ent : this.markModel.getEntities()) {
      for (MOp op : ent.getOps()) {
        op.getAllVertices().forEach(v -> verticesToOp.put(v, op));
      }
    }

    for (Vertex functionDeclaration : crymlinTraversal.functiondeclarations().toList()) {
      log.info("Evaluating function " + functionDeclaration.value("name"));

      for (MRule rule : this.markModel.getRules()) {

        if (rule.getFSM() != null) {
          // rule.getFSM().pushToDB();
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
          functionDeclaration.property("eogpath", "0");
          currentWorklist.add(functionDeclaration);

          // which vertices did we already visit
          // at each branching, we allow each fork to visit all eog nodes
          HashSet<String> seen = new HashSet<>();
          // which bases did we already see, but are not initialized correctly
          HashSet<String> disallowedBases = new HashSet<>();
          // stores the current markings in the FSM (i.e., which base is at which FSM-node)
          HashMap<String, HashSet<Node>> baseToFSMNodes = new HashMap<>();
          // last usage of base
          HashMap<String, Vertex> lastBaseUsage = new HashMap<>();

          while (currentWorklist.size() > 0) {
            HashSet<Vertex> nextWorklist = new HashSet<>();

            //            System.out.println("--");
            //            for(Vertex s: currentWorklist) {
            //              HashSet<String> eogPathSet =
            // stringToEogPath(s.property("eogpath").value().toString());
            //              System.out.print("WL: " + eogPathSet + " ");
            //              try {
            //                System.out.println(s.value("code").toString());
            //              } catch (Exception e) {
            //                System.out.println("<< no code");
            //              }
            //            }

            for (Vertex vertex : currentWorklist) {
              HashSet<String> eogPathSet =
                  stringToEogPath(vertex.property("eogpath").value().toString());
              for (String eogPath : eogPathSet) {
                if (seen.contains(eogPath + "." + vertex.id())) {
                  // this path has already been taken. skip
                  continue;
                }

                if (vertex
                    .label()
                    .contains(
                        "MemberCallExpression")) { // ... no direct access to the labels TreeSet of
                  // Neo4JVertex
                  if (verticesToOp.get(vertex)
                      != null) { // is the vertex part of any op of any mentioned entity? If not,

                    // ignore.
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
                      throw new RuntimeException("base must not be null for MemberCallExpressions");
                    }

                    String prefixedBase = eogPath + "." + base;

                    if (ref != null) {
                      prefixedBase += "|" + ref;
                    }

                    // todo: potential optimization: move prefixBase-String to own data structure

                    //                    System.out.println(
                    //                        "\t"
                    //                            + prefixedBase
                    //                            + " "
                    //                            + vertex.value("code").toString()
                    //                            + " "
                    //                            + vertex.label()
                    //                            + " "
                    //                            + verticesToOp.get(vertex));
                    if (disallowedBases.contains(prefixedBase)) {
                      // !! FINDING
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
                      ctx.getFindings().add(f);
                      log.info("Finding: {}", f.toString());
                    } else {
                      HashSet<Node> nodesInFSM;
                      if (baseToFSMNodes.get(prefixedBase)
                          == null) { // we have not seen this base before. check if this is the
                        // start
                        // of an order
                        nodesInFSM = rule.getFSM().getStart(); // start nodes
                      } else {
                        nodesInFSM =
                            baseToFSMNodes.get(prefixedBase); // nodes calculated in previous step
                      }
                      assert nodesInFSM.size()
                          > 0; // =0 only happens if the fsm is broken, i.e., has a node without
                      // successors which is not an END node

                      HashSet<Node> nextNodesInFSM = new HashSet<>();
                      // which op does this vertex belong to?
                      MOp op = verticesToOp.get(vertex);

                      boolean match = false; // did at least one fsm-Node-match occur?
                      for (Node n : nodesInFSM) {
                        // are there any ops corresponding to the current base and the current
                        // function name?
                        if (op != null && op.getName().equals(n.getOp())) {
                          // this also has as effect, that if the FSM is in a end-state and a
                          // intermediate state, and we follow the intermediate state, the end-state
                          // is removed again, which is correct!
                          nextNodesInFSM.addAll(n.getSuccessors());
                          match = true;
                        }
                      }
                      if (!match) {
                        // if not, this call is not allowed, and this base must not be used in the
                        // following eog
                        // !! FINDING
                        Finding f =
                            new Finding(
                                "Violation against Order: "
                                    + vertex.value("code")
                                    + " ("
                                    + (op == null ? "null" : op.getName())
                                    + ") is not allowed. Expected one of: "
                                    + nodesInFSM.stream()
                                        .map(Node::getName)
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
                        disallowedBases.add(prefixedBase);
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
                        // System.out.println("[" +
                        // nextNodesInFSM.stream().map(Node::getName).collect(Collectors.joining(",
                        // ")) + "]");
                        baseToFSMNodes.put(prefixedBase, nextNodesInFSM);
                      }
                    }
                  }
                }
                ArrayList<Vertex> outVertices = new ArrayList<>();
                vertex
                    .edges(Direction.OUT, "EOG")
                    .forEachRemaining(
                        edge -> {
                          Vertex v = edge.inVertex();
                          if (!seen.contains(eogPath + "." + v.id())) {
                            outVertices.add(v);
                          }
                        });
                // we are now done with this node over this path. will skip if we ever reach it
                // again
                seen.add(eogPath + "." + vertex.id());

                // if more than one vertex follows the current one, we need to branch the eogPath
                if (outVertices.size() > 1) { // split
                  HashSet<String> oldBases = new HashSet<>();
                  HashMap<String, HashSet<Node>> newBases = new HashMap<>();
                  // first we collect all entries which we need to remove from the baseToFSMNodes
                  // map
                  // we also store these entries without the eog path prefix, to update later in (1)
                  for (Map.Entry<String, HashSet<Node>> entry : baseToFSMNodes.entrySet()) {
                    if (entry.getKey().startsWith(eogPath)) {
                      oldBases.add(entry.getKey());
                      // keep the "." before the real base, as we need it later anyway
                      newBases.put(entry.getKey().substring(eogPath.length()), entry.getValue());
                    }
                  }
                  oldBases.forEach(baseToFSMNodes::remove);

                  // (1) update all entries previously removed from the baseToFSMNodes map with the
                  // new eogpath as prefix to the base
                  for (int i = 0; i < outVertices.size(); i++) {
                    String newEOGPath = eogPath + i;
                    // update the eogpath directly in the vertices for the next step
                    HashSet<String> eogpathSet =
                        stringToEogPath((String) outVertices.get(i).property("eogpath").orElse(""));
                    eogpathSet.add(newEOGPath);
                    outVertices.get(i).property("eogpath", eogPathToString(eogpathSet));
                    // also update them in the baseToFSMNodes map
                    newBases.forEach((k, v) -> baseToFSMNodes.put(newEOGPath + k, v));
                  }
                } else if (outVertices.size()
                    == 1) { // else, if we only have one vertex following this vertex, simply
                  // propagate the current eogpath to the next vertex
                  HashSet<String> eogpathSet =
                      stringToEogPath((String) outVertices.get(0).property("eogpath").orElse(""));
                  eogpathSet.add(eogPath);
                  outVertices.get(0).property("eogpath", eogPathToString(eogpathSet));
                }

                /* this optimization would prevent the detection of:
                Botan p4 = new Botan(2);
                if (true) {
                  p4.start(iv);
                  p4.finish(buf);
                }
                p4.start(iv); // not ok, p4 is already finished
                p4.finish(buf);

                  outer:
                  for (int i = outVertices.size() - 1; i >= 0; i--) {
                    Vertex v = outVertices.get(i);
                    Optional<String> any = seen.stream().filter(x -> x.endsWith("." + v.id())).findAny();
                    if (any.isPresent()) {
                      System.out.println("Vertex " + v.id() + " was already done for " + any.get() + ", now needs to be handled for " + eogPath);
                      baseToFSMNodes.forEach((k, j) -> System.out.println("\t" + k + " " + j.stream().map(Node::getName).collect(Collectors.joining(", "))));
                      if (baseToFSMNodes.get(eogPath + "." + v.id()) != null) {
                        for (Node n : baseToFSMNodes.get(eogPath + "." + v.id())) {
                          if (!n.isEnd()) { // if at least one node is not at END, we potentially have to look at this again, do not shortcut
                            continue outer;
                          }
                        }
                      }

                      // all FSM-nodes for this base (not prefixed are either non-existent, or already in an end state. break
                      System.out.println("Skip this path");
                      outVertices.remove(i);
                    }
                  }
                    */
                nextWorklist.addAll(outVertices);
              }

              // if the current path has already been visited, and all stuff in baseToFSMNodes is at
              // start or beginning, remove the current path
              // weiter
              currentWorklist = nextWorklist;
            }
          }
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
            // !! FINDING
            Vertex vertex = lastBaseUsage.get(entry.getKey());
            String base = entry.getKey().split("\\|")[0]; // remove potential refers_to local
            Finding f =
                new Finding(
                    "Violation against Order: Base "
                        + base
                        + " is not correctly terminated. Expected one of ["
                        + String.join(", ", entry.getValue())
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
    log.info("Done evaluating order");
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

    log.info("Looking for forbidden calls");
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
            ;
            ctx.getFindings().add(f);
            log.info("Finding: {}", f.toString());
          }
        }
      }
    }
  }

  //  /**
  //   * DUMMY. JUST FOR DEMO. REWRITE.
  //   *
  //   * <p>Evaluates a MARK rule against the results of the analysis.
  //   *
  //   * @param r
  //   * @return
  //   */
  //  public boolean evaluateRule(MRule r) {
  //    // TODO parse rule and do something with it
  //    Optional<String> matchingEntity =
  //        r.getStatement().getEntities().stream()
  //            .map(ent -> ent.getE().getName())
  //            .filter(entityName -> this.markModel.getPopulatedEntities().containsKey(entityName))
  //            .findAny();
  //    if (matchingEntity.isPresent()) {
  //      // System.out.println("Found matching entity " + matchingEntity.get());
  //      Expression ensureExpr = r.getStatement().getEnsure().getExp();
  //      // System.out.println(exprToString(ensureExpr));
  //      // TODO evaluate expression against populated mark entities
  //      if (evaluateExpr(ensureExpr)) {
  //        // System.out.println("Rule " + r.getName() + " is satisfied.");
  //        return true;
  //      } else {
  //        // System.out.println("Rule " + r.getName() + " is matched but violated.");
  //        return false;
  //      }
  //    }
  //    return false;
  //  }

  private boolean evaluateExpr(Expression expr) {
    if (expr instanceof SequenceExpression) {
      OrderExpression left = ((SequenceExpression) expr).getLeft();
      OrderExpression right = ((SequenceExpression) expr).getRight();
      return evaluateExpr(left) && evaluateExpr(right);
    } else if (expr instanceof Terminal) {
      return containedInModel((Terminal) expr);
    } else if (expr instanceof OrderExpression) {
      SequenceExpression seqxpr = (SequenceExpression) ((OrderExpression) expr).getExp();
      if (seqxpr != null) {
        return evaluateExpr(seqxpr);
      }
    } else {
      // System.out.println("Cannot evaluate " + expr.getClass());
    }
    return false;
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
