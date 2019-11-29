
package de.fraunhofer.aisec.analysis.structures;

import de.fraunhofer.aisec.analysis.scp.ConstantValue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MarkContextHolder {

	private Map<Integer, MarkContext> contexts = new HashMap<>();
	private int currentElements = 0;

	private Set<String> resolvedOperands = new HashSet<>();

	public void addInitialInstanceContext(CPGInstanceContext instance) {
		MarkContext mk = new MarkContext();
		mk.addInstanceContext(instance);
		contexts.put(currentElements++, mk);
	}

	public MarkContext getContext(int id) {
		return contexts.get(id);
	}

	public Map<Integer, MarkContext> getAllContexts() {
		return contexts;
	}

	public Map<Integer, Object> getResolvedOperand(String operand) {
		if (!resolvedOperands.contains(operand)) {
			return null;
		}
		final Map<Integer, Object> result = new HashMap<>();
		contexts.forEach((id, context) -> {
			result.put(id, context.getOperand(operand).getValue());
		});
		return result;
	}

	public void addResolvedOperands(String operand, List<CPGVertexWithValue> operandVertices) {
		resolvedOperands.add(operand);
		final Map<Integer, MarkContext> toAdd = new HashMap<>();
		contexts.forEach((id, context) -> {
			if (operandVertices.size() == 0) {
				context.setOperand(operand, new CPGVertexWithValue(null, ConstantValue.NULL)); // fixme this should not happen! return NullValue!
			} else if (operandVertices.size() == 1) {
				context.setOperand(operand, operandVertices.get(0));
			} else {
				for (int i = 1; i < operandVertices.size(); i++) {
					MarkContext mk = new MarkContext(context); // create a shallow! copy
					mk.setOperand(operand, operandVertices.get(i));
					toAdd.put(currentElements++, mk);
				}
				context.setOperand(operand, operandVertices.get(0)); // set the current one to the first value
			}
		});
		contexts.putAll(toAdd);
	}

	public void removeContext(Integer key) {
		contexts.remove(key);
	}
}
