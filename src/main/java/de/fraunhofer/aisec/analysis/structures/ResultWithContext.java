
package de.fraunhofer.aisec.analysis.structures;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

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

	public static ResultWithContext fromLiteralOrOperand(Object value) {
		return new ResultWithContext(value);
	}

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
