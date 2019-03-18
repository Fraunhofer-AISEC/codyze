package de.fraunhofer.aisec.crymlin.dsl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.detached.DetachedVertex;
import org.apache.tinkerpop.gremlin.structure.util.detached.DetachedVertexProperty;

/**
 * A TransientVertex is a vertex that cannot be stored in the graph.
 *
 * <p>This is useful when using <code>inject()</code> in Gremlin queries to return artificial nodes
 * which are not actually contained in the graph.
 *
 * @author julian
 */
public class TransientVertex extends DetachedVertex {
  private static final long serialVersionUID = 1L;

  protected TransientVertex(final Vertex vertex, final boolean withProperties) {
    super(vertex, withProperties);
  }

  public TransientVertex(
      final Object id, final String label, final Map<String, Object> properties) {
    super(id, label, properties);
  }

  public TransientVertex(final String label, final Object... properties) {
    super(new Random().nextLong(), label, null);
    this.properties = new HashMap<>();
    if (properties != null) {
      if (properties.length % 2 != 0) {
        throw new IllegalArgumentException(
            "Expecting even number of properties for key/value pairs");
      }
      for (int i = 0; i < properties.length - 1; i = i + 2) {
        Map props = new HashMap<>();
        String key = (String) properties[i];
        Object value = properties[i + 1];
        props.put(key, value);
        List<Property> p =
            Collections.singletonList(
                new DetachedVertexProperty(this.id, this.label, value, props));
        this.properties.put(key, p);
      }
    }
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Vertex attach(Function f) {
    // Do nothing.
    return this;
  }
}
