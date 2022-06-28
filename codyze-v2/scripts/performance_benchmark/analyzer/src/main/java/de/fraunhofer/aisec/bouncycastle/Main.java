package de.fraunhofer.aisec.bouncycastle;

import static de.fraunhofer.aisec.bouncycastle.Utils.RedisSet.DONE;
import static de.fraunhofer.aisec.bouncycastle.Utils.RedisSet.FILES;
import static de.fraunhofer.aisec.bouncycastle.Utils.RedisSet.REPOS;

import de.fraunhofer.aisec.bouncycastle.Utils.RedisSet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

public class Main {

  public enum AnalysisMode {FILES, REPOSITORIES}

  private static final Logger log = LoggerFactory.getLogger(Main.class);

  public Main() throws IOException, InterruptedException, ExecutionException {
    String redisHost = System.getenv("REDIS_HOST");
    if (redisHost == null) {
      log.error("REDIS_HOST not set!");
      System.exit(1);
    }
    Jedis jedis = new Jedis(redisHost);
    Utils.setJedis(jedis);
    int timeout = Integer.MAX_VALUE;
    String timeoutEnv = System.getenv("ANALYSIS_TIMEOUT");
    if (timeoutEnv != null) {
      try {
        timeout = Integer.parseInt(timeoutEnv);
      } catch (NumberFormatException e) {
        log.error("Timeout value {} can't be parsed!", timeoutEnv);
        System.exit(1);
      }
    }

    String outputPathEnv = System.getenv("OUTPUT_PATH");
    Path outputPath = outputPathEnv != null ? Paths.get(outputPathEnv) : null;
    Utils.setOutputPath(outputPath);
    String markPath = System.getenv("MARK_PATH");
    if (markPath == null || !new File(markPath).exists()) {
      System.out.println("Incorrect mark path");
      System.exit(1);
    }

    String modeString = System.getenv("ANALYSIS_MODE");
    AnalysisMode analysisMode = AnalysisMode.FILES;
    if (modeString != null) {
      try {
        analysisMode = AnalysisMode.valueOf(modeString.toUpperCase());
      } catch (IllegalArgumentException e) {
        log.error("Unknown analysis mode: {}", modeString);
        System.exit(1);
      }
    }

    File done = new File("done-repos");
    done.createNewFile();

    BufferedReader reader = new BufferedReader(new FileReader(done));
    Set<String> doneRepos = reader.lines()
        .collect(Collectors.toSet());
    reader.close();

    JavaHandler javaHandler = new JavaHandler(timeout, markPath, analysisMode);
    CXXHandler cxxHandler = new CXXHandler(timeout, markPath, analysisMode);

    while (true) {
      RedisSet target = analysisMode == AnalysisMode.REPOSITORIES ? REPOS : FILES;
      String url = jedis.srandmember(target.toString());
      if (url == null) {
        // Check if we're done here or if we just started
        if (jedis.srandmember(DONE.toString()) != null) {
          jedis.disconnect();
          jedis.close();
          return;
        } else {
          Thread.sleep(5);
          continue;
        }
      }

      if (doneRepos.contains(url)) {
        log.info("Skipping already seen repo {}", url);
        jedis.srem(target.toString(), url);
        continue;
      }

      Handler handler = url.startsWith("java::") ? javaHandler : cxxHandler;
      handler.handle(url.replaceFirst(".*::", ""), url);
    }
  }

  public static void main(String[] args) throws Exception {
    new Main();
  }
}
