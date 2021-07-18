package de.fraunhofer.aisec.bouncycastle;

import static de.fraunhofer.aisec.bouncycastle.Utils.RedisSet.ANALYZING;
import static de.fraunhofer.aisec.bouncycastle.Utils.RedisSet.DONE;
import static de.fraunhofer.aisec.bouncycastle.Utils.RedisSet.DOWNLOADING;
import static de.fraunhofer.aisec.bouncycastle.Utils.RedisSet.ERROR_ANALYSIS;
import static de.fraunhofer.aisec.bouncycastle.Utils.RedisSet.ERROR_DOWNLOAD;
import static de.fraunhofer.aisec.bouncycastle.Utils.RedisSet.ERROR_TIMEOUT;
import static de.fraunhofer.aisec.bouncycastle.Utils.RedisSet.FILES;
import static de.fraunhofer.aisec.bouncycastle.Utils.download;
import static de.fraunhofer.aisec.bouncycastle.Utils.humanReadableByteCountSI;
import static de.fraunhofer.aisec.bouncycastle.Utils.output;
import static de.fraunhofer.aisec.bouncycastle.Utils.smove;

import de.fraunhofer.aisec.codyze.analysis.server.AnalysisServer;
import de.fraunhofer.aisec.codyze.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.codyze.analysis.structures.Finding;
import de.fraunhofer.aisec.codyze.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.codyze.analysis.structures.TypestateMode;
import de.fraunhofer.aisec.bouncycastle.Main.AnalysisMode;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.codyze.crymlin.connectors.db.Database;
import de.fraunhofer.aisec.codyze.crymlin.connectors.db.OverflowDatabase;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Handler {

  private static final Logger log = LoggerFactory.getLogger(Handler.class);

  protected int timeout;
  protected String markPath;
  protected AnalysisMode analysisMode;

  public Handler(int timeout, String markPath, AnalysisMode analysisMode) {
    this.timeout = timeout;
    this.markPath = markPath;
    this.analysisMode = analysisMode;
  }

  public void handle(String url, String redisItem) {
    switch (analysisMode) {
      case FILES:
        handleFile(url, redisItem);
        break;
      case REPOSITORIES:
        handleRepo(url, redisItem);
        break;
    }
  }

  public Set<Finding> getFindings(File[] files, String includePath) throws Exception {
    ClassLoader classLoader = Main.class.getClassLoader();

    // Make sure we start with a clean (and connected) db
    Database<?> db = OverflowDatabase.getInstance();
    db.connect();
    db.clearDatabase();

    // Start an analysis server
    AnalysisServer server = AnalysisServer.builder()
        .config(ServerConfiguration.builder()
            .launchConsole(false)
            .launchLsp(false)
            .typestateAnalysis(TypestateMode.NFA)
            .markFiles(markPath)
            .build())
        .build();
    server.start();

    TranslationManager translationManager = TranslationManager.builder()
        .config(TranslationConfiguration.builder()
            .debugParser(true)
            .failOnError(false)
            .codeInNodes(true)
            .defaultPasses()
            .loadIncludes(true)
            .includePath(includePath)
            .sourceLocations(files)
            .build())
        .build();
    CompletableFuture<AnalysisContext> analyze = server.analyze(translationManager);
    AnalysisContext ctx;
    try {
      ctx = analyze.get(timeout, TimeUnit.SECONDS);
    } catch (TimeoutException t) {
      analyze.cancel(true);
      throw t;
    }

    return ctx.getFindings();
  }

  protected void handleFile(String url, String redisItem) {
    smove(FILES, DOWNLOADING, redisItem);
    File file = download(url);
    if (file != null && file.exists()) {
      output(url);
      try {
        long size = Files.size(file.toPath());
        String sizeString = "\t\tSize: " + size + " B";
        if (size >= 1000) {
          sizeString += " (" + humanReadableByteCountSI(size) + ")";
        }
        output(sizeString);
      } catch (IOException e) {
        log.error("Can't determine size of file {}", file.toString());
      }
      smove(DOWNLOADING, ANALYZING, redisItem);
      try {
        analyze(new File[]{file});
        smove(ANALYZING, DONE, redisItem);
      } catch (TimeoutException e) {
        log.error("Timeout during analysis");
        smove(ANALYZING, ERROR_TIMEOUT, redisItem);
      } catch (Exception e) {
        log.error("Error during analysis!", e);
        smove(ANALYZING, ERROR_ANALYSIS, redisItem);
      } finally {
        output("");
        Utils.deleteRecursive(file);
      }
    } else {
      smove(DOWNLOADING, ERROR_DOWNLOAD, redisItem);
    }
  }

  public abstract void handleRepo(String url, String redisItem);

  protected void analyze(File[] files) throws Exception {
    long startTime = System.currentTimeMillis();
    try {
      Set<Finding> findings = getFindings(files, "");
      output("\t\t" + findings.size() + " findings");
      for (Finding finding : findings) {
        output("\t\t\t" + finding);
      }
    } catch (TimeoutException e) {
      output("\t\tTimeout");
      throw e;
    } catch (Exception e) {
      output("\t\tError: " + e.getMessage());
      throw e;
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      String durationString = "\t\tDuration: " + duration + " ms";
      if (duration >= 1000) {
        durationString +=
            " (" + DurationFormatUtils.formatDurationWords(duration, true, true) + ")";
      }
      output(durationString);
    }
  }
}
