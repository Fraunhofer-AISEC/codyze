
package de.fraunhofer.aisec.codyze.analysis.wpds;

import de.breakpointsec.pushdown.WPDS;
import de.fraunhofer.aisec.cpg.graph.Node;
import de.fraunhofer.aisec.cpg.sarif.PhysicalLocation;
import de.fraunhofer.aisec.cpg.sarif.Region;

/**
 * Simply a concrete child of the abstract <code>WPDS</code> class.
 */
public class GraphWPDS extends WPDS<Node, Val, TypestateWeight> {
	public static final String EPSILON = "EPSILON";

	public static class EpsilonNode extends Node {
		public EpsilonNode() {
			this.setName(EPSILON);
			this.setLocation(new PhysicalLocation(null, new Region(-1, -1, -1, -1)));
		}
	}

	@Override
	public Node epsilon() {
		return new EpsilonNode();
	}

}
