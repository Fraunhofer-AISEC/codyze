
package de.fraunhofer.aisec.analysis.structures;

import org.apache.tinkerpop.gremlin.structure.Vertex;

public class CPGVertexWithValue {
	private Vertex argumentVertex;
	private Object value;

	public CPGVertexWithValue(Vertex argument, Object value) {
		this.argumentVertex = argument;
		this.value = value;
	}

	public Vertex getArgumentVertex() {
		return argumentVertex;
	}

	public Object getValue() {
		return value;
	}
}
