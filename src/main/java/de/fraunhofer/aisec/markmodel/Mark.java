
package de.fraunhofer.aisec.markmodel;

import de.fraunhofer.aisec.mark.markDsl.EntityDeclaration;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class Mark {

	private static final Logger log = LoggerFactory.getLogger(Mark.class);

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

	public MEntity getEntity(EntityDeclaration e) {
		if (e == null) {
			return null;
		}
		return getEntity(e.getName());
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
