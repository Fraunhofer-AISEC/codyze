package de.fhg.aisec.markmodel;

import de.fhg.aisec.mark.markDsl.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.python.jline.internal.Log;

/**
 * Parses a MarkModel provided by XText in the form of an ECore hierarchy into a simple (ECore-free)
 * {@code Mark} model that the analysis server can work with.
 *
 * @author julian
 */
public class MarkModelLoader {

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
        m.getEntities().add(entity);
      }

      // Parse rules
      for (RuleDeclaration r : markModel.getRule()) {
        // todo should rules also have package names??
        MRule rule = parseRule(r);
        m.getRules().add(rule);
      }
    }
    return m;
  }

  @NonNull
  public Mark load(HashMap<String, MarkModel> markModels) {
    return load(markModels, null);
  }

  private MRule parseRule(RuleDeclaration rule) {
    MRule mRule = new MRule();
    mRule.setName(rule.getName());
    mRule.setStatement(rule.getStmt());
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
    MOp mOp = new MOp();
    mOp.setName(op.getName());
    for (OpStatement stmt : op.getStmts()) {
      if (stmt instanceof CallStatement) {
        mOp.getCallStatements().add((CallStatement) stmt);
      } else if (stmt instanceof DeclarationStatement) {
        mOp.getDeclStatements().add((DeclarationStatement) stmt);
      } else {
        Log.warn("Op not yet implemented: Handling of Mark {}", stmt.getClass().getName());
      }
    }
    me.getOps().add(mOp);
  }
}
