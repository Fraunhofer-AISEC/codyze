
package de.fraunhofer.aisec.crymlin.connectors.db;

import de.fraunhofer.aisec.cpg.graph.Node;
import de.fraunhofer.aisec.cpg.helpers.Benchmark;
import de.fraunhofer.aisec.cpg.helpers.ShutDownException;
import de.fraunhofer.aisec.cpg.helpers.SubgraphWalker;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.exception.ConnectionException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class Neo4jDatabase<N> implements Database<N> {

	private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jDatabase.class);
	private static final int MAX_TRIES = 10;
	private static Neo4jDatabase INSTANCE = null;

	private static AtomicBoolean isCancelled = new AtomicBoolean(false);
	private static AtomicBoolean isConnected = new AtomicBoolean(false);

	static {
		// bridge java.util.logging to slf4j (used in neo4j)
		// we do this here and in the Analysisserver, as both can be the entrypoint (Main for normal
		// start, Analysserver for tests)
		LOGGER.debug("Resetting logging handlers (log4j, jul)");
		SLF4JBridgeHandler.removeHandlersForRootLogger(); // (since SLF4J 1.6.5)
		SLF4JBridgeHandler.install();
	}

	private SessionFactory sessionFactory = null;
	private Session session = null;
	private int tries = 0;
	private long numNodes = 0;

	private Neo4jDatabase() {
	}

	public static <N> Neo4jDatabase<N> getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new Neo4jDatabase<N>();
		}
		return INSTANCE;
	}

	public boolean reconnect() {
		LOGGER.info("Reconnecting");
		if (session != null) {
			close();
			try {
				Thread.sleep(2000);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		return connect();
	}

	public boolean connect() {
		String uri = System.getenv().getOrDefault("NEO4J_URI", "bolt://localhost");
		String username = System.getenv().getOrDefault("NEO4J_USERNAME", "neo4j");
		String password = System.getenv().getOrDefault("NEO4J_PASSWORD", "password");
		return connect(uri, username, password);
	}

	public boolean connect(String uri, String username, String password) {
		if (isConnected()) { // do not connect again if already connected
			return true;
		}
		try {
			Configuration configuration = new Configuration.Builder().uri(uri).autoIndex("none").credentials(username, password).verifyConnection(true).build();
			LOGGER.info("Trying to connect to {}...", uri);

			sessionFactory = new SessionFactory(configuration, "de.fraunhofer.aisec.cpg.graph");
			session = sessionFactory.openSession();

			LOGGER.info("Successfully connected.");
			isConnected.set(true);
			isCancelled.set(false);

		}
		catch (ConnectionException ex) {
			LOGGER.error("Could not connect to database ({}/{}): {}", tries, MAX_TRIES, ex);

			try {
				Thread.sleep(tries * 500L);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return false;
			}

			tries++;

			if (tries < MAX_TRIES) {
				return connect(uri, username, password);
			} else {
				return false;
			}
		}

		return true;
	}

	public boolean isConnected() {
		return isConnected.get();
	}

	public <T extends N> T find(Class<T> clazz, Long id) {
		return session.load(clazz, id);
	}

	public Session getSession() {
		return this.session;
	}

	public void saveAll(Collection<? extends N> list) {
		LOGGER.debug("Session id: {}", session.hashCode());
		Transaction tx = session.beginTransaction();
		boolean hadException = false;
		try {
			Benchmark bench = new Benchmark(Neo4jDatabase.class, "save all");
			List<N> worklist = new ArrayList<>(list);
			numNodes += worklist.size();
			for (int i = 0; i < worklist.size(); i++) {
				if (isCancelled.get()) {
					throw new ShutDownException();
				}

				final N n = worklist.get(i);
				session.save(n, 1);
				numNodes++;

				if (!Node.class.isAssignableFrom(n.getClass())) {
					throw new RuntimeException("Cannot apply SubgraphWalker to node of class " + n.getClass().getName());
				}
				Set<Node> children = SubgraphWalker.getAstChildren((Node) n);
				worklist.addAll((Collection<? extends N>) children);
			}
			bench.stop();
		}
		catch (ShutDownException r) {
			LOGGER.error("Exception", r);
			hadException = true;
		}
		finally {
			if (tx != null) {
				tx.commit();
				tx.close();
			}
			session.clear();
		}
		if (hadException) {
			this.close();
		}
	}

	public void purgeDatabase() {
		if (isConnected()) {
			session.purgeDatabase();
			session.clear();
		}
		numNodes = 0;
		LOGGER.info("Database purged");
	}

	public void close() {
		if (session != null) {
			session.clear();
		}
		if (sessionFactory != null) {
			sessionFactory.close();
		}
		session = null;
		sessionFactory = null;
		isConnected.set(false);
	}

	public <T> Iterable<T> search(Class<T> clazz, String query, Map<String, String> parameters) {
		return session.query(clazz, query, parameters);
	}

	public long getNumNodes() {
		return numNodes;
	}

	public void setCancelled() {
		isCancelled.set(true);
	}
}
