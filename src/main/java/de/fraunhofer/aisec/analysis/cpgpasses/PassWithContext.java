
package de.fraunhofer.aisec.analysis.cpgpasses;

import de.fraunhofer.aisec.cpg.passes.Pass;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A {@code Pass} that has access to an in-memory analysis context.
 *
 * @author julian
 */
public interface PassWithContext extends Pass {

	public void setContext(@NonNull AnalysisContext ctx);
}
