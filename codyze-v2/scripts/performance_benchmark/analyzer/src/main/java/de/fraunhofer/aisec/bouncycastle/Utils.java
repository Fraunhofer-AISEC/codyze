package de.fraunhofer.aisec.bouncycastle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

public class Utils {

  private static final Logger log = LoggerFactory.getLogger(Utils.class);
  private static Path outputPath;
  private static Jedis jedis;

  public enum RedisSet {
    REPOS("github-repos"), FILES("github-files"), DOWNLOADING("github-downloading"), ANALYZING(
        "github-analyzing"), DONE("github-done"), ERROR_DOWNLOAD(
        "github-error-download"), ERROR_CHECKOUT("github-error-checkout"), ERROR_ANALYSIS(
        "github-error-analysis"), ERROR_TIMEOUT("github-error-timeout");

    private final String name;

    RedisSet(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  public static void smove(RedisSet from, RedisSet to, String item) {
    if (jedis.smove(from.toString(), to.toString(), item) != 1) {
      log.error("Error moving redis item {} from {} to {}", item, from, to);
    }
  }

  public static void setOutputPath(Path outputPath) {
    Utils.outputPath = outputPath;
  }

  public static void setJedis(Jedis jedis) {
    Utils.jedis = jedis;
  }

  /**
   * By default File#delete fails for non-empty directories, it works like "rm". We need something a
   * little more brutal - this does the equivalent of "rm -r"
   *
   * @param path Root File Path
   * @return true iff the file and all sub files/directories have been removed
   */
  public static boolean deleteRecursive(File path) {
    if (!path.exists()) {
      return false;
    }
    boolean ret = true;
    if (path.isDirectory()) {
      File[] files = path.listFiles();
      if (files != null) {
        for (File f : files) {
          ret = ret && deleteRecursive(f);
        }
      }
    }
    return ret && path.delete();
  }

  public static void output(String message) {
    if (outputPath != null) {
      try {
        Files.write(outputPath, List.of(message), StandardCharsets.UTF_8,
            Files.exists(outputPath) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
      } catch (final IOException e) {
        log.error("Could not write to {}", outputPath);
      }
    }
    log.info(message);
    System.out.println(message);
  }

  public static File getRemote(String url) throws IOException {
    File destination = Files.createTempDirectory("cpg-bouncycastle-")
        .toFile();
    log.info("Cloning repo {} into {}", url, destination);
    Git git = cloneRepo(url, destination);
    if (git == null) {
      return null;
    } else {
      File root = git.getRepository()
          .getWorkTree();
      git.close();
      return root;
    }
  }

  public static Git cloneRepo(String url, File destination) {
    try {
      return Git.cloneRepository()
          .setURI(url)
          .setDirectory(destination)
          .call();
    } catch (GitAPIException e) {
      log.error("Error cloning repository at {}: {}", url, e);
      return null;
    }
  }

  public static File download(String urlString) {
    URL url;
    try {
      url = new URL(urlString);
    } catch (MalformedURLException e) {
      log.error("Malformed URL: {}", urlString);
      return null;
    }
    String tmpDir = System.getProperty("java.io.tmpdir");
    String fileName = url.getFile().replaceAll(".*/", "");
    File output = new File(tmpDir, fileName);
    if (fileName.isEmpty()) {
      log.error("Cannot determine file name from URL: {}", urlString);
      return null;
    }

    try {
      ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
      FileOutputStream fileOutputStream = new FileOutputStream(output);
      fileOutputStream.getChannel()
          .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
      return output;
    } catch (IOException e) {
      log.error("Error downloading file at {}: {}", urlString, e);
      return null;
    }
  }

  public static void markDone(String repo) throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter("done-repos", true));
    writer.append(repo);
    writer.newLine();
    writer.close();
  }

  public static String humanReadableByteCountSI(long bytes) {
    String s = bytes < 0 ? "-" : "";
    long b = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
    return b < 1000L ? bytes + " B"
        : b < 999_950L ? String.format("%s%.1f kB", s, b / 1e3)
            : (b /= 1000) < 999_950L ? String.format("%s%.1f MB", s, b / 1e3)
                : (b /= 1000) < 999_950L ? String.format("%s%.1f GB", s, b / 1e3)
                    : (b /= 1000) < 999_950L ? String.format("%s%.1f TB", s, b / 1e3)
                        : (b /= 1000) < 999_950L ? String.format("%s%.1f PB", s, b / 1e3)
                            : String.format("%s%.1f EB", s, b / 1e6);
  }
}