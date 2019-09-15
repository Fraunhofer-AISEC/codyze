package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.cpg.graph.Node;
import de.fraunhofer.aisec.cpg.graph.TranslationUnitDeclaration;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class OGMTest {

  @Test
  void check() throws Exception {
    Path topLevel = Paths.get("src/test/resources/good");

    File[] files =
        Files.walk(topLevel, Integer.MAX_VALUE)
            .map(Path::toFile)
            .filter(File::isFile)
            .filter(f -> f.getName().endsWith(".java"))
            .toArray(File[]::new);

    TranslationConfiguration config =
        TranslationConfiguration.builder()
            .sourceFiles(files)
            .topLevel(topLevel.toFile())
            .defaultPasses()
            .debugParser(true)
            .failOnError(true)
            .build();

    TranslationManager analyzer = TranslationManager.builder().config(config).build();

    TranslationResult result = analyzer.analyze().get();
    List<TranslationUnitDeclaration> original = result.getTranslationUnits();
    //    Neo4jDatabase.getInstance().connect();
    //    Neo4jDatabase.getInstance().purgeDatabase();
    //    Neo4jDatabase.getInstance().saveAll(original);
    OverflowDatabase.<Node>getInstance().saveAll(original);

    GraphTraversal<Vertex, Vertex> traversal =
        OverflowDatabase.getInstance()
            .getGraph()
            .traversal()
            .V()
            .filter(t -> t.get().label().contains("TranslationUnitDeclaration"));

    List<TranslationUnitDeclaration> restored = new ArrayList<>();
    while (traversal.hasNext()) {
      Vertex v = traversal.next();
      Node n = OverflowDatabase.<Node>getInstance().vertexToNode(v);
      assert n instanceof TranslationUnitDeclaration : "n is not instanceof TranslationUnitDeclaration but " + n.getClass().getName();
      restored.add((TranslationUnitDeclaration) n);
    }

    // TODO looks like it is identical, but somehow it does not pass the "equals test yet
    //    restored.get(0).equals(original.get(0));
    //    Neo4jDatabase.getInstance().purgeDatabase();
    //    Neo4jDatabase.getInstance().saveAll(restored);
  }
}
