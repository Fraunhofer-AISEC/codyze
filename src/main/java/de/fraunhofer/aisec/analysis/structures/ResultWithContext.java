
package de.fraunhofer.aisec.analysis.structures;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * A ResultWithContext is created during evaluation of Mark expressions.
 */
public class ResultWithContext {
	private static final Logger log = LoggerFactory.getLogger(ResultWithContext.class);

	private CPGInstanceContext instanceContext;
	private CPGVariableContext variableContext;
	private boolean findingAlreadyAdded = false;
	private Object value;

	// optionally stores a vertex "responsible" for this finding
	private Set<Vertex> vertices = new HashSet<>();

	private ResultWithContext(Object value) {
		this.value = value;
	}

	/**
	 * Creates a new ResultWithContext that represents a leaf in a Mark expression (Literal or Operand).
	 *
	 * @param value
	 * @return
	 */
	public static ResultWithContext fromLiteralOrOperand(Object value) {
		return new ResultWithContext(value);
	}

	/**
	 * Creates a new ResultWithContext that does not refer to a Mark Literal or Operand.
	 *
	 * @param other New value.
	 * @param existingResults Possibly already existing ResultWithContext objects, e.g. result of left and right part of a binary expression.
	 * @return
	 */
	public static ResultWithContext fromExisting(Object other, ResultWithContext... existingResults) {
		ResultWithContext result = new ResultWithContext(other);
		if (existingResults != null) {
			for (ResultWithContext existing : existingResults) {
				result.vertices.addAll(existing.getResponsibleVertices());
			}
		}
		return result;
	}

	public CPGVariableContext getVariableContext() {
		return this.variableContext;
	}

	public CPGInstanceContext getInstanceContext() {
		return this.instanceContext;
	}

	public void setInstanceContext(CPGInstanceContext instanceContext) {
		this.instanceContext = instanceContext;
	}

	public void setVariableContext(CPGVariableContext variableContext) {
		this.variableContext = variableContext;
	}

	public Object get() {
		return value;
	}

	public void set(Object value) {
		this.value = value;
	}

	public Set<Vertex> getResponsibleVertices() {
		return this.vertices;
	}

	public boolean isFindingAlreadyAdded() {
		return findingAlreadyAdded;
	}

	public void setFindingAlreadyAdded(boolean findingAlreadyAdded) {
		this.findingAlreadyAdded = findingAlreadyAdded;
	}

	public void addVertex(Vertex argument) {
		this.vertices.add(argument);
	}
}
