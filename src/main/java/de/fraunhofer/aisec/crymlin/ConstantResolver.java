
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.structures.ConstantValue;
import de.fraunhofer.aisec.cpg.graph.statements.expressions.DeclaredReferenceExpression;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Set;

/**
 * A ConstantResolver is given the <i>use</i> site of a variable (possibly a <code>DeclaredReferenceExpression</code> and tries to resolve the concrete value(s)
 * the variable may hold at runtime.
 * <p>
 * Depending on the implementation of this interface, the constant resolution may operate intra- or interprocedurally, correctly consider branches or not, or
 * resolve arithmetic and string operations over values.
 */
public interface ConstantResolver {

	@NonNull
	public Set<ConstantValue> resolveConstantValues(@NonNull DeclaredReferenceExpression declRefExpr);
}
