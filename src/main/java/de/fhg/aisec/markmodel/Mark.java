package de.fhg.aisec.markmodel;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;

public class Mark {

  private List<MEntity> entities = new ArrayList<>();
  private List<MRule> rules = new ArrayList<>();

  @NonNull
  public List<MEntity> getEntities() {
    return this.entities;
  }

  @NonNull
  public List<MRule> getRules() {
    return this.rules;
  }

}
