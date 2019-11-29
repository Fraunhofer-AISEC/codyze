
package de.fraunhofer.aisec.analysis.structures;

import de.fraunhofer.aisec.analysis.scp.ConstantValue;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class CPGVertexWithValue {
	private Vertex argumentVertex;
	private ConstantValue value;

	public CPGVertexWithValue(Vertex argument, ConstantValue value) {
		this.argumentVertex = argument;
		this.value = value;
	}

	public Vertex getArgumentVertex() {
		return argumentVertex;
	}

	public ConstantValue getValue() {
		return value;
	}
}
