
package de.fraunhofer.aisec.analysis.cpgpasses;

import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.cpg.frontends.LanguageFrontend;
import de.fraunhofer.aisec.cpg.graph.*;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.Method;
import de.fraunhofer.aisec.analysis.utils.Utils;

/**
 * This pass collects all statements in a method's body in the correct order.
 *
 * <p>
 * This is only an experiment for a Pass outside of the CPG project. It is not used at the moment.
 *
 * @author julian
 */
public class StatementsPerMethodPass implements PassWithContext {
	private AnalysisContext ctx;

	@Override
	public void setContext(AnalysisContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void accept(TranslationResult t) {
		for (TranslationUnitDeclaration tu : t.getTranslationUnits()) {
			for (Declaration d : tu.getDeclarations()) {
				if (d instanceof TranslationUnitDeclaration) { // anything which has Declarations
					// loop through functions
					for (Declaration child : ((TranslationUnitDeclaration) d).getDeclarations()) {
						handleDeclaration(child);
					}
				} else {
					handleDeclaration(d);
				}
			}
		}
	}

	private void handleDeclaration(Declaration d) {
		if (d instanceof RecordDeclaration) {
			handleRecordDeclaration((RecordDeclaration) d);
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

	@Override
	public void setLang(LanguageFrontend languageFrontend) {
	}

	@Override
	public LanguageFrontend getLang() {
		return null;
	}

	@Override
	public void cleanup() {
		ctx = null;
	}
}
