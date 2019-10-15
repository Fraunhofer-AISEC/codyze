package de.fraunhofer.aisec.markmodel.wpds;

import de.fraunhofer.aisec.markmodel.fsm.Node;

class NFATransition {
  private final Node source;
  private final Node target;
  private final String label;

  public NFATransition(Node source, Node target, String label) {
    this.source = source;
    this.target = target;
    this.label = label;
  }

  public Node getSource() {
    return source;
  }

  public Node getTarget() {
    return target;
  }

  public String getLabel() {
    return label;
  }

  public String toString() {
    return source.toString() + " -- [" + label + "] --> " + target;
  }
}
