package de.fraunhofer.aisec.crymlin.passes;

import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.cpg.graph.CompoundStatement;
import de.fraunhofer.aisec.cpg.graph.Declaration;
import de.fraunhofer.aisec.cpg.graph.MethodDeclaration;
import de.fraunhofer.aisec.cpg.graph.RecordDeclaration;
import de.fraunhofer.aisec.cpg.graph.Statement;
import de.fraunhofer.aisec.cpg.graph.TranslationUnitDeclaration;
import de.fraunhofer.aisec.crymlin.server.AnalysisContext;
import de.fraunhofer.aisec.crymlin.structures.Method;
import de.fraunhofer.aisec.crymlin.utils.Utils;

/**
 * This pass collects all statements in a method's body in the correct order.
 *
 * <p>TODO This is only an experiment for a Pass outside of the CPG project. It is not used at the
 * moment.
 *
 * @author julian
 */
public class StatementsPerMethodPass implements PassWithContext {
  private TranslationResult result;
  private AnalysisContext ctx;

  @Override
  public void setContext(AnalysisContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public void accept(TranslationResult t) {
    for (TranslationUnitDeclaration tu : t.getTranslationUnits()) {
      for (Declaration d : tu.getDeclarations()) {
        if (d instanceof RecordDeclaration) {
          handleRecordDeclaration((RecordDeclaration) d);
        }
      }
    }
  }

  private void handleRecordDeclaration(RecordDeclaration d) {
    for (MethodDeclaration m : d.getMethods()) {
      // Store method in analysis context for later use
      String methodSignature = Utils.toFullyQualifiedSignature(d, m);
      this.ctx.methods.put(methodSignature, new Method(d, m));

      handleMethodDeclaration(methodSignature, m);
    }
  }

  private void handleMethodDeclaration(String methodSignature, MethodDeclaration m) {
    handleStatement(methodSignature, m.getBody());
  }

  private void handleStatement(String methodSignature, Statement stmt) {
    Method m = this.ctx.methods.get(methodSignature);

    if (stmt instanceof CompoundStatement) {
      // Recursively handle compound statements
      handleCompoundStatement(methodSignature, (CompoundStatement) stmt);
    } else {
      // Add statement to method's statements
      m.getStatements().add(stmt);
    }
  }

  private void handleCompoundStatement(String methodSignature, CompoundStatement stmt) {
    for (Statement s : stmt.getStatements()) {
      handleStatement(methodSignature, s);
    }
  }
}
