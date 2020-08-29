
package de.fraunhofer.aisec.markmodel;

import de.fraunhofer.aisec.mark.markDsl.OpStatement;
import de.fraunhofer.aisec.mark.markDsl.Parameter;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.emf.common.util.EList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MOp {

	private static final Logger log = LoggerFactory.getLogger(MOp.class);

	private String name;
	private MEntity parent;
	@NonNull
	private List<OpStatement> statements = new ArrayList<>();

	private boolean parsed = false;
	private Map<OpStatement, Set<Vertex>> statementToCPGVertex = new HashMap<>();
	private Map<Vertex, Set<OpStatement>> vertexToStatements = new HashMap<>();
	private Set<Vertex> allVertices = new HashSet<>();

	public MOp(MEntity parent) {
		this.parent = parent;
	}

	@NonNull
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@NonNull
	public List<OpStatement> getStatements() {
		return this.statements;
	}

	public Set<Vertex> getVertices(OpStatement stmt) {
		if (!parsed) {
			log.error("MOp not parsed! Do not call getVertex!");
			assert false;
		}
		return statementToCPGVertex.get(stmt);
	}

	public Set<OpStatement> getCallStatements(Vertex v) {
		if (!parsed) {
			log.error("MOp not parsed! Do not call getCallStatements!");
			assert false;
		}
		return vertexToStatements.get(v);
	}

	public Map<Vertex, Set<OpStatement>> getVertexToCallStatementsMap() {
		return vertexToStatements;
	}

	public Set<Vertex> getAllVertices() {
		return allVertices;
	}

	public void addVertex(OpStatement stmt, Set<Vertex> verts) {
		statementToCPGVertex.put(stmt, verts);
		for (Vertex v : verts) {
			Set<OpStatement> callStatements = vertexToStatements.computeIfAbsent(v, k -> new HashSet<>());
			callStatements.add(stmt);
		}
		allVertices.addAll(verts);
	}

	public void setParsingFinished() {
		parsed = true;
	}

	public MEntity getParent() {
		return parent;
	}

	public void reset() {
		parsed = false;
		statementToCPGVertex = new HashMap<>();
		vertexToStatements = new HashMap<>();
		allVertices = new HashSet<>();
	}

	public static List<String> paramsToString(EList<Parameter> params) {
		ArrayList<String> ret = new ArrayList<>();
		for (Parameter p : params) {
			StringBuilder sb = new StringBuilder();
			sb.append(p.getVar());
			if (!p.getTypes().isEmpty()) {
				sb.append(": ");
				sb.append(String.join("| ", p.getTypes()));
			}
			ret.add(sb.toString());
		}
		return ret;
	}
}
