package de.fraunhofer.aisec.markmodel;

import org.checkerframework.checker.nullness.qual.NonNull;

public class EvaluationContext {

  @NonNull private Type contextType;
  @NonNull private Object context;

  public EvaluationContext(@NonNull Object context, @NonNull Type type) {
    this.context = context;

    assert(type.clazz.equals(context.getClass()));
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

  enum Type {
    ENTITY(MEntity.class),
    RULE(MRule.class);

    private Class clazz;

    Type(Class clazz) {
      this.clazz = clazz;
    }
  }
}
