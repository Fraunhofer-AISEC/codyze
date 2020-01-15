
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.crymlin.connectors.db.Database;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import de.fraunhofer.aisec.analysis.server.AnalysisServer;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

public class CppLanguageFeatureTest extends AbstractMarkTest {

	@Test
	public void functionTest() throws Exception {
		Set<Finding> result = performTest("cpp/function.cpp",
			"mark_cpp/mark_rule_eval.mark");
		//TODO Implement a proper test https://***REMOVED***/***REMOVED***/issues/57
	}

	@Test
	public void functionPointerTest() throws Exception {
		Set<Finding> result = performTest("cpp/function_ptr.cpp",
			"mark_cpp/mark_rule_eval.mark");
		//TODO Implement a proper test https://***REMOVED***/***REMOVED***/issues/57
	}
}
