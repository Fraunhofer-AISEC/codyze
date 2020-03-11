
package de.fraunhofer.aisec.analysis.wpds;

import de.breakpointsec.pushdown.WPDS;
import de.fraunhofer.aisec.cpg.sarif.Region;

/**
 * Simply a concrete child of the abstract <code>WPDS</code> class.
 */
public class CpgWpds extends WPDS<Stmt, Val, TypestateWeight> {

	@Override
	public Stmt epsilon() {
		return new Stmt("EPSILON", new Region(-1, -1, -1, -1));
	}

}
