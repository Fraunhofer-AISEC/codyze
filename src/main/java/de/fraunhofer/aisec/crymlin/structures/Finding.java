package de.fraunhofer.aisec.crymlin.structures;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

public class Finding {
  private String finding;
  private Range range;
  private Range humanRange;

  public Finding(String name) {
    this(name, -1, -1, -1, -1);
  }

  public Finding(String name, long startLine, long endLine, long startColumn, long endColumn) {
    this.finding = name;
    assert startLine < Integer.MAX_VALUE;
    assert endLine < Integer.MAX_VALUE;
    assert startColumn < Integer.MAX_VALUE;
    assert endColumn < Integer.MAX_VALUE;

    // adjust off-by-one
    this.range =
        new Range(
            new Position((int) startLine - 1, (int) startColumn - 1),
            new Position((int) endLine - 1, (int) endColumn - 1));
    this.humanRange =
        new Range(
            new Position((int) startLine, (int) startColumn),
            new Position((int) endLine, (int) endColumn));
  }

  public String getFinding() {
    return finding;
  }

  public Range getRange() {
    return range;
  }

  public String toString() {
    // simple for now
    return "line " + humanRange.getStart().getLine() + ": " + finding;
  }
}
