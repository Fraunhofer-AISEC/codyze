package de.fhg.aisec.markmodel;

import de.fhg.aisec.mark.markDsl.CallStatement;
import de.fhg.aisec.mark.markDsl.DeclarationStatement;
import de.fhg.aisec.mark.markDsl.EntityDeclaration;
import de.fhg.aisec.mark.markDsl.EntityStatement;
import de.fhg.aisec.mark.markDsl.MarkModel;
import de.fhg.aisec.mark.markDsl.OpDeclaration;
import de.fhg.aisec.mark.markDsl.OpStatement;
import de.fhg.aisec.mark.markDsl.RuleDeclaration;
import java.util.List;
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
  public Mark load(List<MarkModel> markModels) {
    Mark m = new Mark();

    for (MarkModel markModel : markModels) {
      // Parse "entities" (=cryptographic objects)
      for (EntityDeclaration decl : markModel.getDecl()) {
        MEntity entity = parseEntity(decl);
        m.getEntities().add(entity);
      }

      // Parse rules
      for (RuleDeclaration r : markModel.getRule()) {
        MRule rule = parseRule(r);
        m.getRules().add(rule);
      }
    }
    return m;
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
      } else {
        Log.warn("Not yet implemented: Handling of Mark {}", c.getClass().getName());
      }
    }
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
        Log.warn("Not yet implemented: Handling of Mark {}", stmt.getClass().getName());
      }
    }
    me.getOps().add(mOp);
  }
}
