package de.fraunhofer.aisec.crymlin.connectors.db;

import de.fraunhofer.aisec.cpg.graph.Node;
import java.util.Collection;
import java.util.Map;

public interface Database {

  public boolean connect();

  public boolean isConnected();

  public <T extends Node> T find(Class<T> clazz, Long id);

  public void saveAll(Collection<? extends Node> list);

  public void purgeDatabase();

  public void close();

  public <T> Iterable<T> search(Class<T> clazz, String query, Map<String, String> parameters);

  public long getNumNodes();

  public void setCancelled();
}
