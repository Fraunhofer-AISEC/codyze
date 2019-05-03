package de.fraunhofer.aisec.crymlin.server;

import de.fraunhofer.aisec.crymlin.structures.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;

public class AnalysisContext {

  /**
   * List of violations of MARK rules. // TODO Instead of string, use some richer object including
   * the region, etc.
   */
  @NonNull private final List<String> findings = new ArrayList<>();

  /** Map of method signatures to {@code Method}s. */
  public final Map<String, Method> methods = new HashMap<>();

  /**
   * Returns a (possibly empty) list of findings, i.e. violations of MARK rules that were found
   * during analysis. Make sure to call {@code analyze()} before as otherwise this method will
   * return an empty list.
   *
   * @return
   */
  public @NonNull List<String> getFindings() {
    return this.findings;
  }
}
