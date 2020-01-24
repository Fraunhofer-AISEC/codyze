
package de.fraunhofer.aisec.analysis.cpgpasses;

import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.cpg.passes.Pass;

/**
 * A {@code Pass} that has access to an in-memory analysis context.
 *
 * @author julian
 */
public abstract class PassWithContext extends Pass {
    protected AnalysisContext ctx;

    public void setContext(AnalysisContext ctx) {
        this.ctx = ctx;
    }
}
