
package de.fraunhofer.aisec.analysis.markevaluation;

import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.crymlin.CrymlinQueryWrapper;
import de.fraunhofer.aisec.mark.markDsl.OpStatement;
import de.fraunhofer.aisec.markmodel.MEntity;
import de.fraunhofer.aisec.markmodel.MOp;
import de.fraunhofer.aisec.markmodel.Mark;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;

import static java.lang.Math.toIntExact;

public class ForbiddenEvaluator {

	private static final Logger log = LoggerFactory.getLogger(ForbiddenEvaluator.class);
	private final Mark markModel;

	public ForbiddenEvaluator(@NonNull Mark markModel) {
		this.markModel = markModel;
	}

	/**
	 * For a call to be forbidden, it needs to:
	 *
	 * <p>
	 * - match any forbidden signature (as callstatment in an op) with * for arbitrary parameters, _ for ignoring one parameter type, or - a reference to a var in the
	 * entity to specify a concrete type (no type hierarchy is analyzed!) _and_ is not allowed by any other non-forbidden matching call statement (in _any_ op).
	 *
	 * <p>
	 * After this method, findings have been added to ctx.getFindings().
	 */
	public void evaluate(@NonNull AnalysisContext ctx) {
		for (MEntity ent : this.markModel.getEntities()) {

			for (MOp op : ent.getOps()) {
				for (Map.Entry<Vertex, HashSet<OpStatement>> entry : op.getVertexToCallStatementsMap().entrySet()) {
					if (entry.getValue().stream().noneMatch(call -> "forbidden".equals(call.getForbidden()))) {
						// only allowed entries
						continue;
					}
					Vertex v = entry.getKey();
					boolean vertex_allowed = false;
					HashSet<String> violating = new HashSet<>();
					for (OpStatement call : entry.getValue()) {
						String callString = call.getCall().getName() + "(" + String.join(",", call.getCall().getParams()) + ")";

						if (!"forbidden".equals(call.getForbidden())) {
							// there is at least one CallStatement which explicitly allows this Vertex!
							log.info(
								"Vertex |{}| is allowed, since it matches whitelist entry {}",
								v.value("code"),
								callString);
							vertex_allowed = true;
							break;
						} else {
							violating.add(callString);
						}
					}
					if (!vertex_allowed) {
						// lines are human-readable, i.e., off-by-one
						int startLine = toIntExact(v.value("startLine")) - 1;
						int endLine = toIntExact(v.value("endLine")) - 1;
						int startColumn = toIntExact(v.value("startColumn")) - 1;
						int endColumn = toIntExact(v.value("endColumn")) - 1;
						String message = "Violation against forbidden call(s) "
								+ String.join(", ", violating)
								+ " in entity "
								+ ent.getName()
								+ ". Call was "
								+ v.value("code").toString();
						Finding f = new Finding(message, "FORBIDDEN", ctx.getCurrentFile(), startLine, endLine, startColumn, endColumn);
						ctx.getFindings().add(f);
						log.info("Finding: {}", f);
					}
				}
			}
		}
	}
}
