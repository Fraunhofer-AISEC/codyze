package de.fraunhofer.aisec.markmodel;

import de.fhg.aisec.mark.markDsl.*;
import de.fraunhofer.aisec.crymlin.utils.Pair;
import de.fraunhofer.aisec.markmodel.fsm.FSM;
import de.fraunhofer.aisec.markmodel.fsm.Node;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.python.jline.internal.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses a MarkModel provided by XText in the form of an ECore hierarchy into a simple (ECore-free)
 * {@code Mark} model that the analysis server can work with.
 *
 * @author julian
 */
public class MarkModelLoader {

  private static final Logger log = LoggerFactory.getLogger(MarkModelLoader.class);

  @NonNull
  public Mark load(HashMap<String, MarkModel> markModels, String onlyfromthisfile) {
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
      // Parse rules
      for (RuleDeclaration r : markModel.getRule()) {
        // todo @FW: should rules also have package names? what is the exact reasoning behind
        // packages?
        MRule rule = parseRule(r, m);
        m.getRules().add(rule);
      }
    }

    /*
    VALIDATE
    each function in an order is actually in an op already? then the following might be obsolete

    todo validate MARK files. check e.g.:
     - one function call is not part of two ops (of one or two entities)
         if this would be the case, many strange things will happen
    */

    for (MRule rule : m.getRules()) {
      if (rule.getStatement() != null
          && rule.getStatement().getEnsure() != null
          && rule.getStatement().getEnsure().getExp() instanceof OrderExpression) {
        OrderExpression inner = (OrderExpression) rule.getStatement().getEnsure().getExp();
        FSM fsm = new FSM();
        fsm.sequenceToFSM(inner.getExp());
        rule.setFSM(fsm);

        // check that the fsm is valid:
        // todo remove once the modelloader performs these checks!
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
    return m;
  }

  @NonNull
  public Mark load(HashMap<String, MarkModel> markModels) {
    return load(markModels, null);
  }

  private MRule parseRule(RuleDeclaration rule, Mark mark) {
    MRule mRule = new MRule();
    mRule.setName(rule.getName());
    mRule.setStatement(rule.getStmt()); // todo remove in the long run
    mRule.setErrorMessage(rule.getStmt().getMsg());

    HashMap<String, Pair<String, MEntity>> entityReferences = new HashMap<>();
    rule.getStmt()
        .getEntities()
        .forEach(
            entity -> {
              Pair<String, MEntity> e =
                  new Pair<>(entity.getE().getName(), mark.getEntity(entity.getE().getName()));
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
