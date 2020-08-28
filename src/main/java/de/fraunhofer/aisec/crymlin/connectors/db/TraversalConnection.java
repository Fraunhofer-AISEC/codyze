
package de.fraunhofer.aisec.crymlin.connectors.db;

import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TraversalConnection implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(TraversalConnection.class);

    private final Graph tg;
    private final CrymlinTraversalSource crymlinSource;

    public TraversalConnection(@NonNull Database db) {
        this.tg = db.getGraph();
        this.crymlinSource = this.tg.traversal(CrymlinTraversalSource.class);
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
        try {
            if (tg != null) {
                tg.close();
            }
        } catch (Exception e) {
            log.debug("Closing TG: ", e);
        }

        try {
            if (crymlinSource != null) {
                crymlinSource.close();
            }
        } catch (Exception e) {
            log.debug("Closing crymlin: ", e);
        }
    }
}
