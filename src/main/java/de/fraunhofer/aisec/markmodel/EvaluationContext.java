package de.fraunhofer.aisec.markmodel;

public class EvaluationContext {

  private EvaluationContextType currentContextType;
  private Object currentContext;

  public EvaluationContext(EvaluationContextType type, Object context) {
    currentContextType = type;
    currentContext = context;
  }

  public boolean hasContext(EvaluationContextType t) {
    if (t == null) {
      return false;
    }
    return t.equals(currentContextType);
  }

  public MRule getRule() {
    if (hasContext(EvaluationContextType.RULE)) {
      return (MRule) currentContext;
    }
    return null;
  }

  public MEntity getEntity() {
    if (hasContext(EvaluationContextType.ENTITY)) {
      return (MEntity) currentContext;
    }
    return null;
  }

  enum EvaluationContextType {
    ENTITY,
    RULE
  }
}
