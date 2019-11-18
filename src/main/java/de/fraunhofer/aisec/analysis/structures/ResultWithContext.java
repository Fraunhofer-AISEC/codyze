
package de.fraunhofer.aisec.analysis.structures;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ResultWithContext is created during evaluation of Mark expressions.
 */
public class ResultWithContext {
	private static final Logger log = LoggerFactory.getLogger(ResultWithContext.class);

	private CPGInstanceContext instanceContext;
	private CPGVariableContext variableContext;
	private boolean findingAlreadyAdded = false;
	private Object value;

	// optionally stores a vertex where the value came from
	private Vertex vertex = null;

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
			Vertex prev = null;
			for (ResultWithContext existing : existingResults) {
				if (prev == null) {
					prev = existing.getVertex(); // prev can now still be null!
				} else if (existing.getVertex() != null) {
					log.warn("Multiple vertices would be set for one result, this is not supported (yet).");
					break;
				}
			}
			result.setVertex(prev);
		}
		return result;
	}
	//public List<String> explanations = new ArrayList<>();
	// public int certaintyPercentage = -1;

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

	public void setVertex(Vertex vertex) {
		this.vertex = vertex;
	}

	public Vertex getVertex() {
		return this.vertex;
	}

	public boolean isFindingAlreadyAdded() {
		return findingAlreadyAdded;
	}

	public void setFindingAlreadyAdded(boolean findingAlreadyAdded) {
		this.findingAlreadyAdded = findingAlreadyAdded;
	}
}
