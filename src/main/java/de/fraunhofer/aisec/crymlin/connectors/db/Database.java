
package de.fraunhofer.aisec.crymlin.connectors.db;

import java.util.Collection;
import java.util.Map;

public interface Database<N> {

	public boolean connect();

	public boolean isConnected();

	public <T extends N> T find(Class<T> clazz, Long id);

	public void saveAll(Collection<? extends N> list);

	public void clearDatabase();

	public void close();

	public <T> Iterable<T> search(Class<T> clazz, String query, Map<String, String> parameters);

	public long getNumNodes();

	public void setCancelled();
}
