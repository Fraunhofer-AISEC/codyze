package de.fhg.aisec.markmodel.fsm;

import java.util.HashSet;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Relationship;

public class Node {
  @Id @GeneratedValue private Long id;

  @Relationship(value = "s")
  @NonNull
  private HashSet<Node> successors = new HashSet<>();

  private String name;

  private boolean isStart = false;
  private boolean isEnd = false;

  public Node(String name) {
    this.name = name;
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
