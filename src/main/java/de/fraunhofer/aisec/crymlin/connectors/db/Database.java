
package de.fraunhofer.aisec.crymlin.connectors.db;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Collection;

public interface Database<N> {

	void connect();

	boolean isConnected();

	<T extends N> T find(Class<T> clazz, Long id);

	void saveAll(Collection<? extends N> list);

	void clearDatabase();

	void close();

	long getNumNodes();

	N vertexToNode(Vertex v);

	Graph getGraph();

}
