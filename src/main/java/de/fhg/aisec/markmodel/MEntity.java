package de.fhg.aisec.markmodel;

import java.util.ArrayList;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MEntity {

  private String name;

  @NonNull private final List<MOp> ops = new ArrayList<>();

  @Nullable
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @NonNull
  public List<MOp> getOps() {
    return this.ops;
  }
}
