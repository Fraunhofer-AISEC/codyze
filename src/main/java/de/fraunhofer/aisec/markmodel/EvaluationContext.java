
package de.fraunhofer.aisec.markmodel;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvaluationContext {
	private static final Logger log = LoggerFactory.getLogger(EvaluationContext.class);

	@NonNull
	private Type contextType;
	@NonNull
	private Object context;

	public EvaluationContext(@NonNull Object context, @NonNull Type type) {
		this.context = context;

		if (!type.clazz.equals(context.getClass())) {
			log.error("Class mismatch {} vs. {}", type.clazz, context.getClass());
		}
		this.contextType = type;
	}

	public boolean hasContextType(EvaluationContext.Type t) {
		return (contextType == t);
	}

	public MRule getRule() {
		if (hasContextType(Type.RULE)) {
			return (MRule) context;
		}
		return null;
	}

	public MEntity getEntity() {
		if (hasContextType(Type.ENTITY)) {
			return (MEntity) context;
		}
		return null;
	}

	public enum Type {
		ENTITY(MEntity.class), RULE(MRule.class);

		private Class clazz;

		Type(Class clazz) {
			this.clazz = clazz;
		}
	}
}
