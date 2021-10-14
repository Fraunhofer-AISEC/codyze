
package de.fraunhofer.aisec.codyze.analysis.generated;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Represents a directed edge in a graph.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "label",
    "sourceNodeId",
    "targetNodeId",
    "properties"
})
@Generated("jsonschema2pojo")
public class Edge {

    /**
     * A string that uniquely identifies the edge within its graph.
     * (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("A string that uniquely identifies the edge within its graph.")
    private String id;
    /**
     * Encapsulates a message intended to be read by the end user.
     * 
     */
    @JsonProperty("label")
    @JsonPropertyDescription("Encapsulates a message intended to be read by the end user.")
    private Message label;
    /**
     * Identifies the source node (the node at which the edge starts).
     * (Required)
     * 
     */
    @JsonProperty("sourceNodeId")
    @JsonPropertyDescription("Identifies the source node (the node at which the edge starts).")
    private String sourceNodeId;
    /**
     * Identifies the target node (the node at which the edge ends).
     * (Required)
     * 
     */
    @JsonProperty("targetNodeId")
    @JsonPropertyDescription("Identifies the target node (the node at which the edge ends).")
    private String targetNodeId;
    /**
     * Key/value pairs that provide additional information about the object.
     * 
     */
    @JsonProperty("properties")
    @JsonPropertyDescription("Key/value pairs that provide additional information about the object.")
    private PropertyBag properties;

    /**
     * A string that uniquely identifies the edge within its graph.
     * (Required)
     * 
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * A string that uniquely identifies the edge within its graph.
     * (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Encapsulates a message intended to be read by the end user.
     * 
     */
    @JsonProperty("label")
    public Message getLabel() {
        return label;
    }

    /**
     * Encapsulates a message intended to be read by the end user.
     * 
     */
    @JsonProperty("label")
    public void setLabel(Message label) {
        this.label = label;
    }

    /**
     * Identifies the source node (the node at which the edge starts).
     * (Required)
     * 
     */
    @JsonProperty("sourceNodeId")
    public String getSourceNodeId() {
        return sourceNodeId;
    }

    /**
     * Identifies the source node (the node at which the edge starts).
     * (Required)
     * 
     */
    @JsonProperty("sourceNodeId")
    public void setSourceNodeId(String sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
    }

    /**
     * Identifies the target node (the node at which the edge ends).
     * (Required)
     * 
     */
    @JsonProperty("targetNodeId")
    public String getTargetNodeId() {
        return targetNodeId;
    }

    /**
     * Identifies the target node (the node at which the edge ends).
     * (Required)
     * 
     */
    @JsonProperty("targetNodeId")
    public void setTargetNodeId(String targetNodeId) {
        this.targetNodeId = targetNodeId;
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
        sb.append(Edge.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null)?"<null>":this.id));
        sb.append(',');
        sb.append("label");
        sb.append('=');
        sb.append(((this.label == null)?"<null>":this.label));
        sb.append(',');
        sb.append("sourceNodeId");
        sb.append('=');
        sb.append(((this.sourceNodeId == null)?"<null>":this.sourceNodeId));
        sb.append(',');
        sb.append("targetNodeId");
        sb.append('=');
        sb.append(((this.targetNodeId == null)?"<null>":this.targetNodeId));
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
        result = ((result* 31)+((this.id == null)? 0 :this.id.hashCode()));
        result = ((result* 31)+((this.label == null)? 0 :this.label.hashCode()));
        result = ((result* 31)+((this.targetNodeId == null)? 0 :this.targetNodeId.hashCode()));
        result = ((result* 31)+((this.properties == null)? 0 :this.properties.hashCode()));
        result = ((result* 31)+((this.sourceNodeId == null)? 0 :this.sourceNodeId.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Edge) == false) {
            return false;
        }
        Edge rhs = ((Edge) other);
        return ((((((this.id == rhs.id)||((this.id!= null)&&this.id.equals(rhs.id)))&&((this.label == rhs.label)||((this.label!= null)&&this.label.equals(rhs.label))))&&((this.targetNodeId == rhs.targetNodeId)||((this.targetNodeId!= null)&&this.targetNodeId.equals(rhs.targetNodeId))))&&((this.properties == rhs.properties)||((this.properties!= null)&&this.properties.equals(rhs.properties))))&&((this.sourceNodeId == rhs.sourceNodeId)||((this.sourceNodeId!= null)&&this.sourceNodeId.equals(rhs.sourceNodeId))));
    }

}
