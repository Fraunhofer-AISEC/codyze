package de.fraunhofer.aisec.crymlin;

import static org.junit.jupiter.api.Assertions.*;

import de.fhg.aisec.mark.XtextParser;
import de.fhg.aisec.mark.markDsl.MarkModel;
import de.fhg.aisec.mark.markDsl.OrderExpression;
import de.fhg.aisec.markmodel.MRule;
import de.fhg.aisec.markmodel.Mark;
import de.fhg.aisec.markmodel.MarkModelLoader;
import de.fhg.aisec.markmodel.fsm.FSM;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FSMTest {

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

  @AfterAll
  public static void teardown() throws Exception {}

  @Test
  public void fsmTest() throws Exception {

    FSM.clearDB();
    for (Map.Entry<String, Mark> entry : allModels.entrySet()) {

      Mark markModel = entry.getValue();
      for (MRule rule : markModel.getRules()) {
        if (rule.getStatement() != null
            && rule.getStatement().getEnsure() != null
            && rule.getStatement().getEnsure().getExp() instanceof OrderExpression) {
          OrderExpression inner = (OrderExpression) rule.getStatement().getEnsure().getExp();
          FSM fsm = new FSM();
          fsm.sequenceToFSM(inner.getExp());
          fsm.pushToDB();
        }
      }
    }
  }
}
