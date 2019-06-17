package de.fhg.aisec.markmodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;

public class Mark {

  @NonNull private HashMap<String, MEntity> entityByName = new HashMap<>();

  @NonNull private List<MRule> rules = new ArrayList<>();

  @NonNull
  /**
   * Map (key=entity.name, value=entity) of "populated" entities, i.e. MARK entities whose variables
   * could be resolved.
   */
  private Map<String, MEntity> populatedEntities = new HashMap<>();

  public void addEntities(String name, MEntity ent) {
    this.entityByName.put(name, ent);
  }

  public Collection<MEntity> getEntities() {
    return this.entityByName.values();
  }

  public MEntity getEntity(@NonNull String name) {
    return entityByName.get(name);
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
