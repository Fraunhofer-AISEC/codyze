
package de.fraunhofer.aisec.codyze.analysis.generated;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;


/**
 * A network of nodes and directed edges that describes some aspect of the structure of the code (for example, a call graph).
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "description",
    "nodes",
    "edges",
    "properties"
})
@Generated("jsonschema2pojo")
public class Graph {

    /**
     * Encapsulates a message intended to be read by the end user.
     * 
     */
    @JsonProperty("description")
    @JsonPropertyDescription("Encapsulates a message intended to be read by the end user.")
    private Message description;
    /**
     * An array of node objects representing the nodes of the graph.
     * 
     */
    @JsonProperty("nodes")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of node objects representing the nodes of the graph.")
    private Set<Node> nodes = new LinkedHashSet<Node>();
    /**
     * An array of edge objects representing the edges of the graph.
     * 
     */
    @JsonProperty("edges")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of edge objects representing the edges of the graph.")
    private Set<Edge> edges = new LinkedHashSet<Edge>();
    /**
     * Key/value pairs that provide additional information about the object.
     * 
     */
    @JsonProperty("properties")
    @JsonPropertyDescription("Key/value pairs that provide additional information about the object.")
    private PropertyBag properties;

    /**
     * Encapsulates a message intended to be read by the end user.
     * 
     */
    @JsonProperty("description")
    public Message getDescription() {
        return description;
    }

    /**
     * Encapsulates a message intended to be read by the end user.
     * 
     */
    @JsonProperty("description")
    public void setDescription(Message description) {
        this.description = description;
    }

    /**
     * An array of node objects representing the nodes of the graph.
     * 
     */
    @JsonProperty("nodes")
    public Set<Node> getNodes() {
        return nodes;
    }

    /**
     * An array of node objects representing the nodes of the graph.
     * 
     */
    @JsonProperty("nodes")
    public void setNodes(Set<Node> nodes) {
        this.nodes = nodes;
    }

    /**
     * An array of edge objects representing the edges of the graph.
     * 
     */
    @JsonProperty("edges")
    public Set<Edge> getEdges() {
        return edges;
    }

    /**
     * An array of edge objects representing the edges of the graph.
     * 
     */
    @JsonProperty("edges")
    public void setEdges(Set<Edge> edges) {
        this.edges = edges;
    }

    /**
     * Key/value pairs that provide additional information about the object.
     * 
     */
    @JsonProperty("properties")
    public PropertyBag getProperties() {
        return properties;
    }

    /**
     * Key/value pairs that provide additional information about the object.
     * 
     */
    @JsonProperty("properties")
    public void setProperties(PropertyBag properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Graph.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("description");
        sb.append('=');
        sb.append(((this.description == null)?"<null>":this.description));
        sb.append(',');
        sb.append("nodes");
        sb.append('=');
        sb.append(((this.nodes == null)?"<null>":this.nodes));
        sb.append(',');
        sb.append("edges");
        sb.append('=');
        sb.append(((this.edges == null)?"<null>":this.edges));
        sb.append(',');
        sb.append("properties");
        sb.append('=');
        sb.append(((this.properties == null)?"<null>":this.properties));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.edges == null)? 0 :this.edges.hashCode()));
        result = ((result* 31)+((this.description == null)? 0 :this.description.hashCode()));
        result = ((result* 31)+((this.nodes == null)? 0 :this.nodes.hashCode()));
        result = ((result* 31)+((this.properties == null)? 0 :this.properties.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Graph) == false) {
            return false;
        }
        Graph rhs = ((Graph) other);
        return (((((this.edges == rhs.edges)||((this.edges!= null)&&this.edges.equals(rhs.edges)))&&((this.description == rhs.description)||((this.description!= null)&&this.description.equals(rhs.description))))&&((this.nodes == rhs.nodes)||((this.nodes!= null)&&this.nodes.equals(rhs.nodes))))&&((this.properties == rhs.properties)||((this.properties!= null)&&this.properties.equals(rhs.properties))));
    }

}
