
package de.fraunhofer.aisec.analysis.structures;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// A MarkContextHolder contains:
//
// - a map of a unqiue "id" to a "MarkContext".
//      - Each "MarkContext" contains a possible mapping from MARK instances to MARK entities ("CPGInstanceContext").
// - "resolved operands": A set of Mark Operands (t.foo, cm.algorithm). This Operand is already analysed _for all Contexts_,
//      the actual values/vertices for the operand are stored in the MarkContext
// - "copyStack": If a context is a copy of another context (can e.g. happen if a constant resolving returns multiple values),
//      this stack stores the origin of the copy. This is required, if we need to compare MarkIntermediateResult which might not have all contexts filled:
//      I.e., during evaluation of an expression, we first evaluate the left part of the expression-tree, then the right part.
//      If the left part e.g. returns a result for the contexts 1 and 2, the right part of the result might create a copy one context (e.g. context 2),
//      and e.g. return results for context 1, 2 and 3. Since we might have to compare all contexts, we need to remember that we need to compare
//      result 3 from the right side with the result 2 from the left side.
//		copyStack is a map from a context_id to a list of contexts_ids, where the right side are the contexts this was copied from.
//		The last entry of the right side list is the most recently added one.
// - "createFindingsDuringEvaluation": Indicates, if the analysis should create findings directly. This is currently only
//      used to tell the order-evaluation to not create a finding if it occurs in the when-part of a rule.
public class MarkContextHolder {

	private static final Logger log = LoggerFactory.getLogger(MarkContextHolder.class);

	@Deprecated
	private final Map<Integer, LegacyMarkContext> legacyContexts = new HashMap<>();

	private int currentElements = 0;

	private final Set<String> resolvedOperands = new HashSet<>();
	private final Map<Integer, List<Integer>> copyStack = new HashMap<>();
	private boolean createFindingsDuringEvaluation = true;

	public void addInitialInstanceContext(LegacyCPGInstanceContext instance) {
		LegacyMarkContext mk = new LegacyMarkContext();
		mk.addInstanceContext(instance);
		legacyContexts.put(currentElements++, mk);
	}

	public LegacyMarkContext getContext(int id) {
		return legacyContexts.get(id);
	}

	public Map<Integer, LegacyMarkContext> getAllContexts() {
		return legacyContexts;
	}

	public Map<Integer, MarkIntermediateResult> generateNullResult() {
		Map<Integer, MarkIntermediateResult> ret = new HashMap<>();
		legacyContexts.keySet()
				.forEach(
					x -> ret.put(x, ConstantValue.newUninitialized()));
		return ret;
	}

	public Map<Integer, MarkIntermediateResult> getResolvedOperand(String operand) {
		if (!resolvedOperands.contains(operand)) {
			return null;
		}
		final Map<Integer, MarkIntermediateResult> result = new HashMap<>();
		legacyContexts.forEach((id, context) -> {
			CPGVertexWithValue vwv = context.getOperand(operand);
			ConstantValue constant = ConstantValue.of(vwv.getValue());
			constant.addResponsibleVertex(getVertexFromSelfOrFromParent(operand, context));
			result.put(id, constant);
		});
		return result;
	}

	// return the vertex responsible for this operand, or (if the vertex would be null), the vertex of the base of this
	// operand
	private Vertex getVertexFromSelfOrFromParent(String operand, LegacyMarkContext context) {
		CPGVertexWithValue vwv = context.getOperand(operand);
		if (vwv == null) {
			return null;
		}
		Vertex argumentVertex = vwv.getArgumentVertex();
		if (argumentVertex != null) {
			return argumentVertex;
		}
		String[] split = operand.split("\\.");
		if (split.length >= 2) {
			return getVertexFromSelfOrFromParent(operand.substring(0, operand.lastIndexOf('.')), context);
		}
		return null;
	}

	public void addResolvedOperands(String operand, Map<Integer, List<CPGVertexWithValue>> operandVerticesForContext) {
		resolvedOperands.add(operand);
		final Map<Integer, LegacyMarkContext> toAdd = new HashMap<>();
		final Map<Integer, List<Integer>> copyStackToAdd = new HashMap<>();

		legacyContexts.forEach((id, context) -> {
			List<CPGVertexWithValue> operandVertices = operandVerticesForContext.get(id);
			if (operandVertices == null || operandVertices.isEmpty()) {
				log.warn("Did not find any vertices for {}, following evaluation will be imprecise", operand);
				context.setOperand(operand, new CPGVertexWithValue(null,
					ErrorValue.newErrorValue(String.format("Did not find any vertices for %s, following evaluation will be imprecise", operand))));
			} else if (operandVertices.size() == 1) {
				context.setOperand(operand, operandVertices.get(0));
			} else {
				List<Integer> oldStack = copyStack.computeIfAbsent(id, x -> new ArrayList<>());
				for (int i = 1; i < operandVertices.size(); i++) {
					LegacyMarkContext mk = new LegacyMarkContext(context); // create a shallow! copy
					mk.setOperand(operand, operandVertices.get(i));
					toAdd.put(currentElements, mk);

					List<Integer> stackForNewContext = new ArrayList<>(oldStack);
					stackForNewContext.add(id);
					copyStackToAdd.put(currentElements, stackForNewContext);
					currentElements++;
				}
				context.setOperand(operand, operandVertices.get(0)); // set the current one to the first value

			}
		});
		legacyContexts.putAll(toAdd);
		copyStack.putAll(copyStackToAdd);
	}

	public void removeContext(Integer key) {
		legacyContexts.remove(key);
	}

	public List<Integer> getCopyStack(Integer key) {
		return copyStack.get(key);
	}

	public void setCreateFindingsDuringEvaluation(boolean b) {
		this.createFindingsDuringEvaluation = b;
	}

	public boolean isCreateFindingsDuringEvaluation() {
		return createFindingsDuringEvaluation;
	}

	public void dump(PrintStream out) {
		out.println("====== Mark Context ========");
		for (Map.Entry<Integer, LegacyMarkContext> ctx : legacyContexts.entrySet()) {
			int id = ctx.getKey();
			LegacyMarkContext mCtx = ctx.getValue();
			out.println(id + ":");
			mCtx.dump(out);
		}
		out.println("===========================");
	}
}
