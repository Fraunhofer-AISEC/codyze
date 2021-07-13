
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.mark.XtextParser;
import de.fraunhofer.aisec.mark.markDsl.MarkModel;
import de.fraunhofer.aisec.markmodel.MEntity;
import de.fraunhofer.aisec.markmodel.MRule;
import de.fraunhofer.aisec.markmodel.Mark;
import de.fraunhofer.aisec.markmodel.MarkModelLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MarkLoadOutputTest {

	private static final Map<String, Mark> allModels = new HashMap<>();

	@BeforeAll
	public static void startup() {

		URL resource = MarkLoadOutputTest.class.getClassLoader().getResource("mark/PoC_MS1/Botan_AutoSeededRNG.mark");
		assertNotNull(resource);
		File markPoC1 = new File(resource.getFile());
		assertNotNull(markPoC1);
		String markModelFiles = markPoC1.getParent();

		String[] directories = (new File(markModelFiles)).list((current, name) -> name.endsWith(".mark"));

		assertNotNull(directories);

		XtextParser parser = new XtextParser();
		for (String markFile : directories) {
			String fullName = markModelFiles + File.separator + markFile;
			parser.addMarkFile(new File(fullName));
		}
		HashMap<String, MarkModel> markModels = parser.parse();
		for (String markFile : directories) {
			String fullName = markModelFiles + File.separator + markFile;
			allModels.put(
				fullName,
				new MarkModelLoader().load(markModels, fullName)); // only load the model from this file
		}
	}

	@Test
	void markModelLoaderTest() throws Exception {

		for (Map.Entry<String, Mark> entry : allModels.entrySet()) {

			Mark markModel = entry.getValue();

			StringBuilder reconstructed = new StringBuilder();
			ArrayList<MEntity> entities = new ArrayList<>(markModel.getEntities());
			if (!entities.isEmpty()) {
				// they all have the same packed name in our context
				reconstructed.append("package ").append(entities.get(0).getPackageName()).append("\n");
			}

			for (MEntity entity : entities) {
				reconstructed.append(entity.toString());
				reconstructed.append("\n");
			}

			for (MRule rule : markModel.getRules()) {
				reconstructed.append(rule.toString());
				reconstructed.append("\n");
			}

			// remove identation and comments
			StringBuilder sanitizedOriginal = new StringBuilder();
			String full = new String(Files.readAllBytes(Paths.get(entry.getKey())));
			full = full.replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)", "");
			for (String line : full.split("\n")) {
				if (!line.strip().isEmpty()) {
					sanitizedOriginal.append(line.strip()).append("\n");
				}
			}

			StringBuilder sanitizedReconstructed = new StringBuilder();
			for (String line : reconstructed.toString().split("\n")) {
				if (!line.strip().isEmpty()) {
					sanitizedReconstructed.append(line.strip()).append("\n");
				}
			}

			assertEquals(sanitizedOriginal.toString(), sanitizedReconstructed.toString());
		}
	}
}
