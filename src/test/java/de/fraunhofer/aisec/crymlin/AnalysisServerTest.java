
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.server.AnalysisServer;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AnalysisServerTest {

    private static AnalysisServer server;

    @Test
    void testGoodZip() {
        ClassLoader classLoader = AnalysisServerTest.class.getClassLoader();

        URL resource = classLoader.getResource("mark/PoC_MS1.zip");
        assertNotNull(resource);
        File markPoC1 = new File(resource.getFile());
        assertNotNull(markPoC1);

        // Start an analysis server
        server = AnalysisServer.builder()
                .config(
                        ServerConfiguration.builder().disableOverflow(true).launchConsole(false).launchLsp(false).markFiles(markPoC1.toString()).build())
                .build();
        server.start();

        server.stop();
    }

    @Test
    void testZipSlip() {
        ClassLoader classLoader = AnalysisServerTest.class.getClassLoader();

        URL resource = classLoader.getResource("zip-slip/zip-slip.zip");
        assertNotNull(resource);
        File markPoC1 = new File(resource.getFile());
        assertNotNull(markPoC1);

        // Start an analysis server
        server = AnalysisServer.builder()
                .config(
                        ServerConfiguration.builder().disableOverflow(true).launchConsole(false).launchLsp(false).markFiles(markPoC1.toString()).build())
                .build();
        server.start();

        server.stop();
    }

}
