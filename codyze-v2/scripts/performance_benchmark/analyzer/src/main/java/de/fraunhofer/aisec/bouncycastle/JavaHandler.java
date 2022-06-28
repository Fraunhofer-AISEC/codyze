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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaHandler extends Handler {

  private static final Logger log = LoggerFactory.getLogger(JavaHandler.class);

  public JavaHandler(int timeout, String markPath, AnalysisMode analysisMode) {
    super(timeout, markPath, analysisMode);
  }

  @Override
  public void handleRepo(String url, String redisItem) {
    File root = null;
    File newRoot = null;
    smove(REPOS, DOWNLOADING, redisItem);
    try {
      root = getRemote(url);
      if (root != null && root.isDirectory()) {
        newRoot = Files.createTempDirectory("cpg-bouncycastle-grouped-")
            .toFile();

        output(url);
        smove(DOWNLOADING, ANALYZING, redisItem);
        analyzeModules(root, newRoot);
        output("");

        Utils.markDone(url);
        smove(ANALYZING, DONE, redisItem);
        Utils.deleteRecursive(root);
        Utils.deleteRecursive(newRoot);
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
      if (newRoot != null) {
        Utils.deleteRecursive(newRoot);
      }
    }
  }

  public void analyzeModules(File root, File newRoot) throws Exception {
    log.info("Collecting Java files");
    List<JavaFile> javaFiles = findJavaFiles(root);
    Map<String, List<JavaFile>> modules = groupFilesByModule(javaFiles);

    for (String module : modules.keySet()) {
      List<JavaFile> moduleFiles = modules.get(module);
      String moduleName = module.replace(File.separator, ".");
      String relativeName = root.toPath()
          .relativize(Path.of(module))
          .toString();
      output("\tModule: " + relativeName);
      output("\t\t" + moduleFiles.size() + " files");
      log.info("Module {}: {} Java files. Grouping into packages", module, moduleFiles.size());
      File modulePath = new File(newRoot, moduleName);
      moduleFiles = moveFilesToPackage(moduleFiles, modulePath);

      log.info("Creating CPG for module {}", moduleName);
      File[] files = moduleFiles.stream()
          .map(JavaFile::getPath)
          .collect(Collectors.toList())
          .toArray(File[]::new);

      analyze(files);
    }
  }

  private Map<String, List<JavaFile>> groupFilesByModule(List<JavaFile> javaFiles) {
    return javaFiles.stream()
        .collect(Collectors.groupingBy(JavaFile::getModulePrefix));
  }

  private List<JavaFile> moveFilesToPackage(List<JavaFile> javaFiles, File destination)
      throws IOException {
    List<JavaFile> result = new ArrayList<>();
    Map<String, List<JavaFile>> packages = javaFiles.stream()
        .collect(Collectors.groupingBy(JavaFile::getPackageName));
    for (String p : packages.keySet()) {
      File packageDir = new File(destination, p.replaceAll("\\.", File.separator));
      if (!packageDir.exists()) {
        Files.createDirectories(packageDir.toPath());
      }
      for (JavaFile javaFile : packages.get(p)) {
        File newPath = new File(packageDir, javaFile.getClassName() + ".java");
        if (newPath.exists()) {
          log.warn("Java class duplicate! {}", javaFile.getFullyQualifiedName());
        } else {
          Files.copy(javaFile.getPath()
              .toPath(), newPath.toPath());
          result.add(new JavaFile(newPath, javaFile.getPackageName(), javaFile.getClassName()));
        }
      }
    }
    log.info("{}/{} files remaining, grouped into {} packages: {}", result.size(), javaFiles.size(),
        packages.keySet()
            .size(), packages.keySet());
    return result;
  }

  private List<JavaFile> findJavaFiles(File root) throws IOException {
    return Files.walk(root.toPath())
        .map(Path::toFile)
        .filter(f -> f.getName()
            .endsWith(".java"))
        .map(JavaFile::parse)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }
}
