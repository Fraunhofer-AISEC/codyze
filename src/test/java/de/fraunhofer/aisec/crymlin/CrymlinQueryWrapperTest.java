
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.cpg.graph.Node;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CrymlinQueryWrapperTest {

	/**
	 * Make sure getFileLocation() can handle Windows and Linux paths likewise.
	 */
	@Test
	public void testFilePath() {
		Node win = new Node();
		win.setFile("C:\\Users\\test\\Documents\\Some Path\\test");
		Vertex vWin = OverflowDatabase.getInstance()
				.createVertex(win);
		URI locWin = CrymlinQueryWrapper.getFileLocation(vWin);

		Node linux = new Node();
		linux.setFile("/Users/test/Documents/Some Path/test");
		Vertex vLinux = OverflowDatabase.getInstance()
				.createVertex(linux);
		URI locLinux = CrymlinQueryWrapper.getFileLocation(vLinux);

		// Above all, we expect no exception here. One of the two will not be a valid
		// file path, depending on which OS we are running test.
		assertTrue(locWin.getPath().contains("Users\\test\\Documents\\Some Path\\test"));
		assertTrue(locLinux.getPath().contains("Users/test/Documents/Some Path/test"));
	}
}
