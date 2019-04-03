package de.fhg.aisec.markmodel;

import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.emf.ecore.EObject;
import org.python.jline.internal.Log;

import de.fhg.aisec.mark.markDsl.CallStatement;
import de.fhg.aisec.mark.markDsl.EntityDeclaration;
import de.fhg.aisec.mark.markDsl.Expression;
import de.fhg.aisec.mark.markDsl.MarkModel;
import de.fhg.aisec.mark.markDsl.OpDeclaration;
import de.fhg.aisec.mark.markDsl.OpStatement;
import de.fhg.aisec.mark.markDsl.RuleDeclaration;

/**
 * Parses a MarkModel provided by XText in the form of an ECore hierarchy into a simple (ECore-free) {@code Mark} model that the analysis server can work with.
 *  
 * @author julian
 *
 */
public class MarkModelLoader {

  public Mark load(MarkModel model) {
    Mark m = new Mark();

    // Parse "entities" (=cryptographic objects)
    for (EntityDeclaration decl : model.getDecl()) {
      MEntity entity = parseEntity(decl);
      m.getEntities().add(entity);
    }
    
    // Parse rules
    for (RuleDeclaration r : model.getRule()) {
      MRule rule = parseRule(r);
      m.getRules().add(rule);
    }
    return m;
  }

  private MRule parseRule(RuleDeclaration rule) {
    MRule mRule = new MRule();
    mRule.setName(rule.getName());
    // TODO Statements
    return mRule;
  }

  private MEntity parseEntity(EntityDeclaration decl) {
    MEntity mEntity = new MEntity();
    mEntity.setName(decl.getName());
    parseEntityContent(decl.getContent(), mEntity);
    return mEntity;
  }

  private void parseEntityContent(@NonNull List<EObject> content, @NonNull MEntity me) {
    for (EObject c : content) {
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
    mOp.getParameters().addAll(op.getParams());
    for (OpStatement stmt : op.getStmts()) {
      if (stmt instanceof CallStatement) {
        MOpCallStmt mOpCallStmt = new MOpCallStmt();
        CallStatement callStmt = (CallStatement) stmt;
        mOpCallStmt.setName(callStmt.getCall().getName());
        mOpCallStmt.setCondition(exprToString(callStmt.getCond().getExp()));
        mOp.getStatements().add(mOpCallStmt);
      } else {
        Log.warn("Not yet implemented: Handling of Mark {}", stmt.getClass().getName());
      }
    }
  }

  @NonNull
  private String exprToString(Expression exp) {
    if (exp != null) {
      // TODO Quick n dirty.
      return exp.toString();
    }
    return "";
  }

}
