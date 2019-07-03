package de.fraunhofer.aisec.markmodel.fsm;

import java.util.HashSet;
import org.neo4j.ogm.annotation.*;

@NodeEntity
public class Node {
  @GeneratedValue @Id private Long id;

  @Relationship(value = "s")
  private HashSet<Node> successors = new HashSet<>();

  private HashSet<String> markings = new HashSet<>();

  private String base;
  private String op;

  private boolean isStart = false;
  private boolean isEnd = false;
  private boolean isFake = false; // if this is a fake start/end node

  public Node() {}

  public Node(String base, String op) {
    this.base = base;
    this.op = op;
  }

  public void addSuccessor(Node s) {
    successors.add(s);
  }

  public void addSuccessor(HashSet<Node> s) {
    successors.addAll(s);
  }

  public String getName() {
    if (base == null) {
      return op;
    } else {
      return base + "." + op;
    }
  }

  public String getBase() {
    return base;
  }

  public String getOp() {
    return op;
  }

  public void setStart(boolean b) {
    this.isStart = b;
  }

  public boolean isStart() {
    return isStart;
  }

  public void setEnd(boolean b) {
    this.isEnd = b;
  }

  public boolean isEnd() {
    return isEnd;
  }

  public void setFake(boolean b) {
    this.isFake = b;
  }

  public boolean isFake() {
    return isFake;
  }

  public HashSet<Node> getSuccessors() {
    return successors;
  }

  public String toStringWithAddress() {
    String addr = super.toString();
    addr = addr.substring(addr.lastIndexOf("@") + 1);
    return getName() + "(" + addr + "), MARKING: " + String.join(", ", markings);
  }

  public String toString() {
    return getName(); // + ", MARKING: " + String.join(", ", markings);
  }
}
