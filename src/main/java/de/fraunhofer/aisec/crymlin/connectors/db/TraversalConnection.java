
package de.fraunhofer.aisec.crymlin.connectors.db;

import com.steelbridgelabs.oss.neo4j.structure.Neo4JElementIdProvider;
import com.steelbridgelabs.oss.neo4j.structure.Neo4JGraph;
import com.steelbridgelabs.oss.neo4j.structure.providers.Neo4JNativeElementIdProvider;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TraversalConnection implements AutoCloseable {

	private static final Logger log = LoggerFactory.getLogger(TraversalConnection.class);

	private Type type;
	private final Graph tg;
	private final CrymlinTraversalSource crymlinSource;

	public enum Type {
		NEO4J,
		OVERFLOWDB
	}

	public TraversalConnection(@NonNull Type type) {
		this.type = type;
		if (type.equals(Type.OVERFLOWDB)) {
			this.tg = OverflowDatabase.getInstance().getGraph();
			this.crymlinSource = this.tg.traversal(CrymlinTraversalSource.class);

		} else {
			Neo4JElementIdProvider<?> vertexIdProvider = new Neo4JNativeElementIdProvider();
			Neo4JElementIdProvider<?> edgeIdProvider = new Neo4JNativeElementIdProvider();

			this.tg = new Neo4JGraph(DriverSingleton.getInstance(), vertexIdProvider, edgeIdProvider);
			this.crymlinSource = this.tg.traversal(CrymlinTraversalSource.class);
		}
	}

	public CrymlinTraversalSource getCrymlinTraversal() {
		return this.crymlinSource;
	}

	public GraphTraversalSource getGremlinTraversal() {
		return this.tg.traversal();
	}

	public Graph getGraph() {
		return tg;
	}

	public void close() {
		/*
		 * Only Neo4J TraversalConnections (tg) will be closed, as they can be resumed from disk. If we would close the graph for OverflowDB, it would be lost.
		 */
		if (type.equals(Type.OVERFLOWDB)) {
			return;
		}

		try {
			if (tg != null) {
				tg.close();
			}
		}
		catch (Exception e) {
			log.debug("Closing TG: ", e);
		}

		try {
			if (crymlinSource != null) {
				crymlinSource.close();
			}
		}
		catch (Exception e) {
			log.debug("Closing crymlin: ", e);
		}
	}
}
