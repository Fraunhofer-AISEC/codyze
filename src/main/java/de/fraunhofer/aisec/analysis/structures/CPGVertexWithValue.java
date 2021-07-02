
package de.fraunhofer.aisec.analysis.structures;

import org.apache.tinkerpop.gremlin.structure.Vertex;

@Deprecated
public class CPGVertexWithValue {
	private Vertex argumentVertex;
	private ConstantValue value;
	private Vertex base = null;

	public CPGVertexWithValue(Vertex argument, ConstantValue value) {
		this.argumentVertex = argument;
		this.value = value;
	}

	public static CPGVertexWithValue of(CPGVertexWithValue v) {
		CPGVertexWithValue cpgVertexWithValue = new CPGVertexWithValue(v.getArgumentVertex(), v.value);
		cpgVertexWithValue.setBase(v.getBase());
		return cpgVertexWithValue;
	}

	public Vertex getArgumentVertex() {
		return argumentVertex;
	}

	public Vertex getBase() {
		return base;
	}

	public void setBase(Vertex base) {
		this.base = base;
	}

	public ConstantValue getValue() {
		return value;
	}

	public void setValue(ConstantValue value) {
		this.value = value;
	}
}
