
package de.fraunhofer.aisec.analysis.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A MarkContextHolder contains:
 *
 * - a map of a unqiue "id" to a "MarkContext". - Each "MarkContext" contains a possible mapping from MARK instances to MARK entities ("CPGInstanceContext"). - "resolved"
 * operands TODO JS->DT: Was bedeutet "resolved" hier?
 */
public class MarkContextHolder {

	private Map<Integer, MarkContext> contexts = new HashMap<>();
	private int currentElements = 0;

	private Set<String> resolvedOperands = new HashSet<>();
	private Map<Integer, List<Integer>> copyStack = new HashMap<>();

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

	public Map<Integer, MarkIntermediateResult> generateNullResult() {
		Map<Integer, MarkIntermediateResult> ret = new HashMap<>();
		contexts.keySet()
				.forEach(
					x -> ret.put(x, ConstantValue.NULL));
		return ret;
	}

	public Map<Integer, MarkIntermediateResult> getResolvedOperand(String operand) {
		if (!resolvedOperands.contains(operand)) {
			return null;
		}
		final Map<Integer, MarkIntermediateResult> result = new HashMap<>();
		contexts.forEach((id, context) -> {
			CPGVertexWithValue vwv = context.getOperand(operand);
			ConstantValue constant = ConstantValue.of(vwv.getValue());
			constant.addResponsibleVertex(vwv.getArgumentVertex());
			result.put(id, constant);
		});
		return result;
	}

	public void addResolvedOperands(String operand, Map<Integer, List<CPGVertexWithValue>> operandVerticesForContext) {
		resolvedOperands.add(operand);
		final Map<Integer, MarkContext> toAdd = new HashMap<>();
		final Map<Integer, List<Integer>> copyStackToAdd = new HashMap<>();

		contexts.forEach((id, context) -> {
			List<CPGVertexWithValue> operandVertices = operandVerticesForContext.get(id);
			if (operandVertices == null || operandVertices.size() == 0) {
				context.setOperand(operand, new CPGVertexWithValue(null, ConstantValue.NULL));
			} else if (operandVertices.size() == 1) {
				context.setOperand(operand, operandVertices.get(0));
			} else {
				List<Integer> newStack = copyStack.computeIfAbsent(id, x -> new ArrayList<>());
				for (int i = 1; i < operandVertices.size(); i++) {
					MarkContext mk = new MarkContext(context); // create a shallow! copy
					mk.setOperand(operand, operandVertices.get(i));
					toAdd.put(currentElements, mk);

					newStack.add(currentElements);
					copyStackToAdd.put(currentElements, newStack);
					currentElements++;
				}
				context.setOperand(operand, operandVertices.get(0)); // set the current one to the first value

			}
		});
		contexts.putAll(toAdd);
		copyStack.putAll(copyStackToAdd);
	}

	public void removeContext(Integer key) {
		contexts.remove(key);
	}

	public List<Integer> getCopyStack(Integer key) {
		return copyStack.get(key);
	}
}
