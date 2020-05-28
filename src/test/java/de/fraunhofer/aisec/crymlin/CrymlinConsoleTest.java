
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.CrymlinConsole;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CrymlinConsoleTest {

	@Test
	public void crymlinConsoleTest() throws Exception {
		CrymlinConsole con = new CrymlinConsole();

		final boolean[] stopped = { false };
		new Thread(() -> {
			// blocks until stop()
			con.interact(null);
			stopped[0] = true;
		}).start();

		// Give console time to enter while loop
		Thread.sleep(100);

		// stop console
		con.stop();

		// Give console time to leave while loop
		Thread.sleep(100);
		assertTrue(stopped[0]);
	}
}
