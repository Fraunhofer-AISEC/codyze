package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.mark.XtextParser;
import de.fraunhofer.aisec.mark.markDsl.Expression;
import de.fraunhofer.aisec.mark.markDsl.MarkModel;
import de.fraunhofer.aisec.markmodel.MRule;
import de.fraunhofer.aisec.markmodel.Mark;
import de.fraunhofer.aisec.markmodel.MarkInterpreter;
import de.fraunhofer.aisec.markmodel.MarkModelLoader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class RuleEnsureSemanticsTest {

  private static HashMap<String, MarkModel> markModels;

  @BeforeAll
  public static void startup() throws Exception {
    URL resource =
        RuleEnsureSemanticsTest.class.getClassLoader().getResource("mark/rules/ensure/semantics/");
    assertNotNull(resource);

    File markFile = new File(resource.getFile());
    assertNotNull(markFile);

    File[] directoryContent = markFile.listFiles((current, name) -> name.endsWith(".mark"));

    if (directoryContent == null) {
      directoryContent = new File[] {markFile};
    }

    assertNotNull(directoryContent);
    assertTrue(directoryContent.length > 0);

    XtextParser parser = new XtextParser();
    for (File mf : directoryContent) {
      parser.addMarkFile(mf);
    }

    markModels = parser.parse();
    assertFalse(markModels.isEmpty());
  }

  @AfterAll
  public static void teardown() throws Exception {
    // noop
  }

  @Test
  public void equalsTest() throws Exception {
    // TODO convenient checking but pretty ugly and difficult to see, what went wrong
    List<String> markFilePaths =
        markModels.keySet().stream()
            .filter(n -> n.endsWith("equals.mark"))
            .collect(Collectors.toList());
    assertTrue(markFilePaths.size() == 1);

    Mark mark = new MarkModelLoader().load(markModels, markFilePaths.get(0));

    MarkInterpreter mi = new MarkInterpreter(mark);
    Method evaluateExpression =
        mi.getClass().getDeclaredMethod("evaluateTopLevelExpr", Expression.class);
    evaluateExpression.setAccessible(true);

    Map<String, Optional<Boolean>> ensureExprResults = new TreeMap<>();
    for (MRule rule : mark.getRules()) {
      Expression ensureExpr = rule.getStatement().getEnsure().getExp();
      Optional<Boolean> result = (Optional<Boolean>) evaluateExpression.invoke(mi, ensureExpr);
      ensureExprResults.put(rule.getName(), result);
    }

    ensureExprResults
        .entrySet()
        .forEach(
            entry -> {
              if (entry.getKey().endsWith("true")) {
                assertTrue(entry.getValue().get());
              } else if (entry.getKey().endsWith("false")) {
                assertFalse(entry.getValue().get());
              } else {
                assertTrue(entry.getValue().isEmpty());
              }
            });
  }
}
