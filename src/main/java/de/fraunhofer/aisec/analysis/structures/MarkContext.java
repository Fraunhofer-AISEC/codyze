
package de.fraunhofer.aisec.analysis.structures;

import de.fraunhofer.aisec.analysis.utils.Utils;
import de.fraunhofer.aisec.cpg.graph.Node;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class MarkContext {
	private static final Logger log = LoggerFactory.getLogger(MarkContext.class);

	private GraphInstanceContext instances = null;
	private final Map<String, NodeWithValue<? extends Node>> operands = new HashMap<>();
	private boolean findingAlreadyAdded = false;

	public MarkContext(MarkContext other) {
		// shallow copy
		other.operands.forEach(operands::put);
		instances = other.instances;
	}

	public MarkContext() {
	}

	public void addInstanceContext(GraphInstanceContext instance) {
		if (instances != null) {
			log.warn("overwriting existing instance context");
		}
		this.instances = instance;
	}

	public GraphInstanceContext getInstanceContext() {
		return instances;
	}

	public void setOperand(String operand, NodeWithValue<? extends Node> value) {
		operands.put(operand, value);
	}

	public NodeWithValue<? extends Node> getOperand(String operand) {
		return operands.get(operand);
	}

	public boolean isFindingAlreadyAdded() {
		return this.findingAlreadyAdded;
	}

	public void setFindingAlreadyAdded(boolean b) {
		this.findingAlreadyAdded = b;
	}

	/**
	 * Dump this {@link MarkContext} to the given {@link PrintStream}.
	 *
	 * @param out
	 */
	public void dump(@NonNull PrintStream out) {
		if (instances != null) {
			for (String instance : instances.getMarkInstances()) {
				var node = instances.getNode(instance);
				if (node != null) {
					out.println("  MARK instance " + instance + " " + node.getClass().getSimpleName());
				} else {
					out.println("  MARK instance " + instance + " <null>");
				}
				for (Map.Entry<String, NodeWithValue<? extends Node>> op : operands.entrySet()) {
					out.println("     " + op.getKey() + " : " + op.getValue().getValue() + " base: " + op.getValue().getBase() + " resp. vertices: "
							+ Utils.prettyPrint(op.getValue().getValue().getResponsibleVertices()));
				}
			}
		}
	}

}
