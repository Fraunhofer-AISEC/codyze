
package de.fraunhofer.aisec.analysis.structures;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MarkContext {
	private static final Logger log = LoggerFactory.getLogger(MarkContext.class);

	private CPGInstanceContext instances = null;
	private Map<String, CPGVertexWithValue> operands = new HashMap<>();
	private HashSet<Vertex> responsibleVertices = new HashSet<>();
	private boolean findingAlreadyAdded = false;

	public MarkContext(MarkContext other) {
		// shallow copy
		responsibleVertices = other.responsibleVertices;
		other.operands.forEach(operands::put);
		instances = other.instances;
	}

	public MarkContext() {
	}

	public void addInstanceContext(CPGInstanceContext instance) {
		if (instances != null) {
			log.warn("overwriting existing instance context");
		}
		this.instances = instance;
	}

	public CPGInstanceContext getInstanceContext() {
		return instances;
	}

	public void setOperand(String operand, CPGVertexWithValue value) {
		operands.put(operand, value);
		if (value != null) {
			responsibleVertices.add(value.getArgumentVertex());
		}
	}

	public CPGVertexWithValue getOperand(String operand) {
		return operands.get(operand);
	}

	public void addResponsibleVertices(Set<Vertex> vertices) {
		responsibleVertices.addAll(vertices);
	}

	public void addResponsibleVertex(Vertex vertex) {
		responsibleVertices.add(vertex);
	}

	public boolean isFindingAlreadyAdded() {
		return this.findingAlreadyAdded;
	}

	public void setFindingAlreadyAdded(boolean b) {
		this.findingAlreadyAdded = b;
	}

	public Set<Vertex> getResponsibleVertices() {
		return responsibleVertices;
	}
}
