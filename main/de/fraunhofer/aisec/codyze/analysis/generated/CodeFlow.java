
package de.fraunhofer.aisec.codyze.analysis.generated;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * A set of threadFlows which together describe a pattern of code execution relevant to detecting a result.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "message",
    "threadFlows",
    "properties"
})
@Generated("jsonschema2pojo")
public class CodeFlow {

    /**
     * Encapsulates a message intended to be read by the end user.
     * 
     */
    @JsonProperty("message")
    @JsonPropertyDescription("Encapsulates a message intended to be read by the end user.")
    private Message message;
    /**
     * An array of one or more unique threadFlow objects, each of which describes the progress of a program through a thread of execution.
     * (Required)
     * 
     */
    @JsonProperty("threadFlows")
    @JsonPropertyDescription("An array of one or more unique threadFlow objects, each of which describes the progress of a program through a thread of execution.")
    private List<ThreadFlow> threadFlows = new ArrayList<ThreadFlow>();
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
    @JsonProperty("message")
    public Message getMessage() {
        return message;
    }

    /**
     * Encapsulates a message intended to be read by the end user.
     * 
     */
    @JsonProperty("message")
    public void setMessage(Message message) {
        this.message = message;
    }

    /**
     * An array of one or more unique threadFlow objects, each of which describes the progress of a program through a thread of execution.
     * (Required)
     * 
     */
    @JsonProperty("threadFlows")
    public List<ThreadFlow> getThreadFlows() {
        return threadFlows;
    }

    /**
     * An array of one or more unique threadFlow objects, each of which describes the progress of a program through a thread of execution.
     * (Required)
     * 
     */
    @JsonProperty("threadFlows")
    public void setThreadFlows(List<ThreadFlow> threadFlows) {
        this.threadFlows = threadFlows;
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
        sb.append(CodeFlow.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("message");
        sb.append('=');
        sb.append(((this.message == null)?"<null>":this.message));
        sb.append(',');
        sb.append("threadFlows");
        sb.append('=');
        sb.append(((this.threadFlows == null)?"<null>":this.threadFlows));
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
        result = ((result* 31)+((this.message == null)? 0 :this.message.hashCode()));
        result = ((result* 31)+((this.threadFlows == null)? 0 :this.threadFlows.hashCode()));
        result = ((result* 31)+((this.properties == null)? 0 :this.properties.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CodeFlow) == false) {
            return false;
        }
        CodeFlow rhs = ((CodeFlow) other);
        return ((((this.message == rhs.message)||((this.message!= null)&&this.message.equals(rhs.message)))&&((this.threadFlows == rhs.threadFlows)||((this.threadFlows!= null)&&this.threadFlows.equals(rhs.threadFlows))))&&((this.properties == rhs.properties)||((this.properties!= null)&&this.properties.equals(rhs.properties))));
    }

}
