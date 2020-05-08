package de.fraunhofer.aisec.bouncycastle;

import static de.fraunhofer.aisec.bouncycastle.Utils.RedisSet.ANALYZING;
import static de.fraunhofer.aisec.bouncycastle.Utils.RedisSet.DONE;
import static de.fraunhofer.aisec.bouncycastle.Utils.RedisSet.DOWNLOADING;
import static de.fraunhofer.aisec.bouncycastle.Utils.RedisSet.ERROR_ANALYSIS;
import static de.fraunhofer.aisec.bouncycastle.Utils.RedisSet.ERROR_CHECKOUT;
import static de.fraunhofer.aisec.bouncycastle.Utils.RedisSet.ERROR_DOWNLOAD;
import static de.fraunhofer.aisec.bouncycastle.Utils.RedisSet.ERROR_TIMEOUT;
import static de.fraunhofer.aisec.bouncycastle.Utils.RedisSet.REPOS;
import static de.fraunhofer.aisec.bouncycastle.Utils.getRemote;
import static de.fraunhofer.aisec.bouncycastle.Utils.output;
import static de.fraunhofer.aisec.bouncycastle.Utils.smove;

import de.fraunhofer.aisec.bouncycastle.Main.AnalysisMode;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CXXHandler extends Handler {

  private static final Logger log = LoggerFactory.getLogger(CXXHandler.class);

  public CXXHandler(int timeout, String markPath, AnalysisMode analysisMode) {
    super(timeout, markPath, analysisMode);
  }

  @Override
  public void handleRepo(String url, String redisItem) {
    File root = null;
    smove(REPOS, DOWNLOADING, redisItem);
    try {
      root = getRemote(url);
      if (root != null && root.isDirectory()) {
        output(url);
        smove(DOWNLOADING, ANALYZING, redisItem);
        File[] files =
            Files.walk(root.toPath(), Integer.MAX_VALUE)
                .map(Path::toFile)
                .filter(File::isFile)
                .filter(f -> f.getName().endsWith(".cpp") || f.getName().endsWith(".c"))
                .toArray(File[]::new);
        analyze(files);
        output("");

        Utils.markDone(url);
        smove(ANALYZING, DONE, redisItem);
        Utils.deleteRecursive(root);
      } else {
        log.error("Repository {} could not be checked out, skipping", root);
        smove(DOWNLOADING, ERROR_CHECKOUT, redisItem);
      }
    } catch (IOException e) {
      log.error("Repository {} could not be downloaded, skipping", url);
      smove(DOWNLOADING, ERROR_DOWNLOAD, redisItem);
    } catch (TimeoutException e) {
      log.error("Timeout during analysis");
      smove(ANALYZING, ERROR_TIMEOUT, redisItem);
    } catch (Exception e) {
      log.error("Error during analysis!", e);
      smove(ANALYZING, ERROR_ANALYSIS, redisItem);
    } finally {
      if (root != null) {
        Utils.deleteRecursive(root);
      }
    }
  }
}
