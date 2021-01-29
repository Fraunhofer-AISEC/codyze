
package de.fraunhofer.aisec.crymlin;

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.cpg.graph.Node;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import java.net.URI;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.jupiter.api.Test;

class CrymlinQueryWrapperTest {

	/** Make sure getFileLocation() can handle Windows and Linux paths likewise. */
	@Test
	void testFilePath() {
		Node win = new Node();
		win.setFile("C:\\Users\\test\\Documents\\Some Path\\test");
		var db = new OverflowDatabase(ServerConfiguration.builder().disableOverflow(true).build());
		db.connect();

		Vertex vWin = db.createVertex(win);
		URI locWin = CrymlinQueryWrapper.getFileLocation(vWin);

		Node linux = new Node();
		linux.setFile("/Users/test/Documents/Some Path/test");
		Vertex vLinux = db.createVertex(linux);
		URI locLinux = CrymlinQueryWrapper.getFileLocation(vLinux);

		// Above all, we expect no exception here. One of the two will not be a valid
		// file path, depending on which OS we are running test.
		assertTrue(locWin.getPath().contains("Users\\test\\Documents\\Some Path\\test"));
		assertTrue(locLinux.getPath().contains("Users/test/Documents/Some Path/test"));
	}
}
