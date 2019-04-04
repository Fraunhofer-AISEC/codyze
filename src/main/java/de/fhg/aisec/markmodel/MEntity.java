package de.fhg.aisec.markmodel;

import org.checkerframework.checker.nullness.qual.Nullable;

public class MEntity {

  private String name;

  @Nullable
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
