package de.fhg.aisec.markmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;

public class Mark {

  @NonNull private List<MEntity> entities = new ArrayList<>();

  @NonNull private List<MRule> rules = new ArrayList<>();

  @NonNull
  /**
   * Map (key=entity.name, value=entity) of "populated" entities, i.e. MARK entities whose variables
   * could be resolved.
   */
  private Map<String, MEntity> populatedEntities = new HashMap<>();

  @NonNull
  public List<MEntity> getEntities() {
    return this.entities;
  }

  public MEntity getEntity(@NonNull String name) {
    return entities.stream().filter(x -> name.equals(x.getName())).findAny().orElse(null);
  }

  @NonNull
  public List<MRule> getRules() {
    return this.rules;
  }

  /**
   * The list of MARK entities that could be "populated" after the analysis.
   *
   * <p>"Populated" means that their ops and variables could be assigned concrete values by
   * analyzing the source code.
   *
   * @return
   */
  public Map<String, MEntity> getPopulatedEntities() {
    return this.populatedEntities;
  }
}
