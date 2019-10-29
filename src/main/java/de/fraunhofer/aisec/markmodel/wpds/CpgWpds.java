
package de.fraunhofer.aisec.markmodel.wpds;

import de.breakpoint.pushdown.WPDS;
import de.fraunhofer.aisec.cpg.graph.Region;

/**
 * Simply a concrete child of the abstract <code>WPDS</code> class.
 */
public class CpgWpds extends WPDS<Stmt, Val, Weight> {

	@Override
	public Stmt epsilon() {
		return new Stmt("EPSILON", new Region(-1, -1, -1, -1));
	}

}
