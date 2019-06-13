package de.fhg.aisec.markmodel;

import de.fhg.aisec.mark.markDsl.OpStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;

public class MOp {

  private String name;
  private MEntity parent;
  @NonNull private List<OpStatement> statements = new ArrayList<>();

  private boolean parsed = false;
  private HashMap<OpStatement, HashSet<Vertex>> statementToCPGVertex = new HashMap<>();
  private HashMap<Vertex, HashSet<OpStatement>> vertexToStatements = new HashMap<>();
  private HashSet<Vertex> allVertices = new HashSet<>();

  public MOp(MEntity parent) {
    this.parent = parent;
  }

  @NonNull
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @NonNull
  public List<OpStatement> getStatements() {
    return this.statements;
  }

  public HashSet<Vertex> getVertices(OpStatement stmt) {
    if (!parsed) {
      throw new RuntimeException("MOp not parsed! Do not call getVertex!");
    }
    return statementToCPGVertex.get(stmt);
  }

  public HashSet<OpStatement> getCallStatements(Vertex v) {
    if (!parsed) {
      throw new RuntimeException("MOp not parsed! Do not call getCallStatements!");
    }
    return vertexToStatements.get(v);
  }

  public HashMap<Vertex, HashSet<OpStatement>> getVertexToCallStatementsMap() {
    return vertexToStatements;
  }

  public HashSet<Vertex> getAllVertices() {
    return allVertices;
  }

  public void addVertex(OpStatement stmt, HashSet<Vertex> verts) {
    statementToCPGVertex.put(stmt, verts);
    for (Vertex v : verts) {
      HashSet<OpStatement> callStatements =
          vertexToStatements.computeIfAbsent(v, k -> new HashSet<>());
      callStatements.add(stmt);
    }
    allVertices.addAll(verts);
  }

  public void setParsingFinished() {
    parsed = true;
  }

  public MEntity getParent() {
    return parent;
  }
}
