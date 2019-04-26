package de.fraunhofer.aisec.crymlin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.fhg.aisec.mark.XtextParser;
import de.fhg.aisec.mark.markDsl.MarkModel;
import de.fhg.aisec.markmodel.MEntity;
import de.fhg.aisec.markmodel.MRule;
import de.fhg.aisec.markmodel.Mark;
import de.fhg.aisec.markmodel.MarkModelLoader;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MarkLoadOutputTest {

  private static Map<String, Mark> allModels = new HashMap<>();

  @BeforeAll
  public static void startup() throws Exception {

    URL resource =
        MarkLoadOutputTest.class
            .getClassLoader()
            .getResource("mark/PoC_MS1/Botan_AutoSeededRNG.mark");
    assertNotNull(resource);
    File markPoC1 = new File(resource.getFile());
    assertNotNull(markPoC1);
    String markModelFiles = markPoC1.getParent();

    String[] directories =
        (new File(markModelFiles)).list((current, name) -> name.endsWith(".mark"));

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

  @AfterAll
  public static void teardown() throws Exception {}

  @Test
  public void markModelLoaderTest() throws Exception {

    for (Map.Entry<String, Mark> entry : allModels.entrySet()) {

      Mark markModel = entry.getValue();

      StringBuilder reconstructed = new StringBuilder();
      if (!markModel.getEntities().isEmpty()) {
        // they all have the same packed name in our context
        reconstructed.append("package " + markModel.getEntities().get(0).getPackageName() + "\n");
      }

      for (MEntity entity : markModel.getEntities()) {
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
          sanitizedOriginal.append(line.strip() + "\n");
        }
      }

      StringBuilder sanitizedReconstructed = new StringBuilder();
      for (String line : reconstructed.toString().split("\n")) {
        if (!line.strip().isEmpty()) {
          sanitizedReconstructed.append(line.strip() + "\n");
        }
      }

      assertEquals(sanitizedOriginal.toString(), sanitizedReconstructed.toString());
    }
  }
}
