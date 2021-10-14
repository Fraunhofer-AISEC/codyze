
package de.fraunhofer.aisec.codyze.analysis.generated;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * A function call within a stack trace.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "location",
    "module",
    "threadId",
    "parameters",
    "properties"
})
@Generated("jsonschema2pojo")
public class StackFrame {

    /**
     * A location within a programming artifact.
     * 
     */
    @JsonProperty("location")
    @JsonPropertyDescription("A location within a programming artifact.")
    private Location location;
    /**
     * The name of the module that contains the code of this stack frame.
     * 
     */
    @JsonProperty("module")
    @JsonPropertyDescription("The name of the module that contains the code of this stack frame.")
    private String module;
    /**
     * The thread identifier of the stack frame.
     * 
     */
    @JsonProperty("threadId")
    @JsonPropertyDescription("The thread identifier of the stack frame.")
    private Integer threadId;
    /**
     * The parameters of the call that is executing.
     * 
     */
    @JsonProperty("parameters")
    @JsonPropertyDescription("The parameters of the call that is executing.")
    private List<String> parameters = new ArrayList<String>();
    /**
     * Key/value pairs that provide additional information about the object.
     * 
     */
    @JsonProperty("properties")
    @JsonPropertyDescription("Key/value pairs that provide additional information about the object.")
    private PropertyBag properties;

    /**
     * A location within a programming artifact.
     * 
     */
    @JsonProperty("location")
    public Location getLocation() {
        return location;
    }

    /**
     * A location within a programming artifact.
     * 
     */
    @JsonProperty("location")
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * The name of the module that contains the code of this stack frame.
     * 
     */
    @JsonProperty("module")
    public String getModule() {
        return module;
    }

    /**
     * The name of the module that contains the code of this stack frame.
     * 
     */
    @JsonProperty("module")
    public void setModule(String module) {
        this.module = module;
    }

    /**
     * The thread identifier of the stack frame.
     * 
     */
    @JsonProperty("threadId")
    public Integer getThreadId() {
        return threadId;
    }

    /**
     * The thread identifier of the stack frame.
     * 
     */
    @JsonProperty("threadId")
    public void setThreadId(Integer threadId) {
        this.threadId = threadId;
    }

    /**
     * The parameters of the call that is executing.
     * 
     */
    @JsonProperty("parameters")
    public List<String> getParameters() {
        return parameters;
    }

    /**
     * The parameters of the call that is executing.
     * 
     */
    @JsonProperty("parameters")
    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
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
        sb.append(StackFrame.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("location");
        sb.append('=');
        sb.append(((this.location == null)?"<null>":this.location));
        sb.append(',');
        sb.append("module");
        sb.append('=');
        sb.append(((this.module == null)?"<null>":this.module));
        sb.append(',');
        sb.append("threadId");
        sb.append('=');
        sb.append(((this.threadId == null)?"<null>":this.threadId));
        sb.append(',');
        sb.append("parameters");
        sb.append('=');
        sb.append(((this.parameters == null)?"<null>":this.parameters));
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
        result = ((result* 31)+((this.threadId == null)? 0 :this.threadId.hashCode()));
        result = ((result* 31)+((this.location == null)? 0 :this.location.hashCode()));
        result = ((result* 31)+((this.parameters == null)? 0 :this.parameters.hashCode()));
        result = ((result* 31)+((this.properties == null)? 0 :this.properties.hashCode()));
        result = ((result* 31)+((this.module == null)? 0 :this.module.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StackFrame) == false) {
            return false;
        }
        StackFrame rhs = ((StackFrame) other);
        return ((((((this.threadId == rhs.threadId)||((this.threadId!= null)&&this.threadId.equals(rhs.threadId)))&&((this.location == rhs.location)||((this.location!= null)&&this.location.equals(rhs.location))))&&((this.parameters == rhs.parameters)||((this.parameters!= null)&&this.parameters.equals(rhs.parameters))))&&((this.properties == rhs.properties)||((this.properties!= null)&&this.properties.equals(rhs.properties))))&&((this.module == rhs.module)||((this.module!= null)&&this.module.equals(rhs.module))));
    }

}
