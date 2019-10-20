
package de.fraunhofer.aisec.crymlin.structures;

import de.fraunhofer.aisec.cpg.graph.MethodDeclaration;
import de.fraunhofer.aisec.cpg.graph.RecordDeclaration;
import de.fraunhofer.aisec.cpg.graph.Statement;
import de.fraunhofer.aisec.crymlin.utils.Utils;
import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a method.
 *
 * @author julian
 */
public class Method {
	private String signature;
	private final List<Statement> statements = new ArrayList<>();

	/** Class declaring this method. */
	private RecordDeclaration declaringClass;

	public Method(RecordDeclaration clazz, MethodDeclaration m) {
		this.signature = Utils.toFullyQualifiedSignature(clazz, m);
		this.setDeclaringClass(clazz);
	}

	/**
	 * Fully qualified method signature, should be unique per program.
	 *
	 * @return
	 */
	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	/**
	 * Statements of the method's body in the order they appear in source code.
	 *
	 * <p>
	 * Note that CompoundStatements are not contained in this list.
	 *
	 * @return
	 */
	public List<Statement> getStatements() {
		return statements;
	}

	public RecordDeclaration getDeclaringClass() {
		return declaringClass;
	}

	public void setDeclaringClass(RecordDeclaration declaringClass) {
		this.declaringClass = declaringClass;
	}
}
