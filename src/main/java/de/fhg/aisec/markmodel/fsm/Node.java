package de.fhg.aisec.markmodel.fsm;

import java.util.HashSet;
import org.neo4j.ogm.annotation.*;

@NodeEntity
public class Node {
  @Id private Long id;

  @Relationship(value = "s")
  private HashSet<Node> successors = new HashSet<>();

  private String name;

  private boolean isStart = false;
  private boolean isEnd = false;

  public Node() {}

  public Node(String name, long id) {
    this.name = name;
    this.id = id;
  }

  public void addSuccessor(Node s) {
    successors.add(s);
  }

  public void addSuccessor(HashSet<Node> s) {
    successors.addAll(s);
  }

  public String getName() {
    return name;
  }

  public void setStart(boolean b) {
    this.isStart = b;
  }

  public boolean getStart() {
    return isStart;
  }

  public void setEnd(boolean b) {
    this.isEnd = b;
  }

  public boolean getEnd() {
    return isEnd;
  }

  public HashSet<Node> getSuccessors() {
    return successors;
  }

  public String toString() {
    String addr = super.toString();
    addr = addr.substring(addr.lastIndexOf("@") + 1);
    return getName() + "(" + addr + ")";
  }
}
