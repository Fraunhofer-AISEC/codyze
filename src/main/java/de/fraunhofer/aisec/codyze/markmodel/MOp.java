
package de.fraunhofer.aisec.codyze.markmodel;

import de.fraunhofer.aisec.cpg.graph.Node;
import de.fraunhofer.aisec.mark.markDsl.OpStatement;
import de.fraunhofer.aisec.mark.markDsl.Parameter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MOp {

	private static final Logger log = LoggerFactory.getLogger(MOp.class);

	private String name;
	private final MEntity parent;

	@NonNull
	private final List<OpStatement> statements = new ArrayList<>();

	private boolean parsed = false;

	private final Map<OpStatement, Set<Node>> statementToNodes = new HashMap<>();

	private final Map<Node, Set<OpStatement>> nodesToStatements = new HashMap<>();

	private final Set<Node> allNodes = new HashSet<>();

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

	public Map<Node, Set<OpStatement>> getNodesToStatements() {
		return nodesToStatements;
	}

	public Map<OpStatement, Set<Node>> getStatementsToNodes() {
		return statementToNodes;
	}

	public Set<Node> getAllNodes() {
		return allNodes;
	}

	public <T extends Node> void addNode(OpStatement stmt, Collection<T> nodes) {
		statementToNodes.put(stmt, new HashSet<>(nodes));
		for (var node : nodes) {
			Set<OpStatement> callStatements = nodesToStatements.computeIfAbsent(node, k -> new HashSet<>());
			callStatements.add(stmt);
		}
		allNodes.addAll(nodes);
	}

	public void setParsingFinished() {
		parsed = true;
	}

	public MEntity getParent() {
		return parent;
	}

	public void reset() {
		parsed = false;
		statementToNodes.clear();
		nodesToStatements.clear();
		allNodes.clear();
	}

	public static List<String> paramsToString(List<Parameter> params) {
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
