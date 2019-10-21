
package de.fraunhofer.aisec.markmodel;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class Mark {

	@NonNull
	private HashMap<String, MEntity> entityByName = new HashMap<>();

	@NonNull
	private List<MRule> rules = new ArrayList<>();

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

	public void reset() {
		// nothing to do for the rules
		for (MEntity entity : getEntities()) {
			for (MOp op : entity.getOps()) {
				op.reset();
			}
		}
	}
}
