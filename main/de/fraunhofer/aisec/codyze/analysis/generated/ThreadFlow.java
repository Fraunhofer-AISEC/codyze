
package de.fraunhofer.aisec.codyze.analysis.generated;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Describes a sequence of code locations that specify a path through a single thread of execution such as an operating system or fiber.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "message",
    "initialState",
    "immutableState",
    "locations",
    "properties"
})
@Generated("jsonschema2pojo")
public class ThreadFlow {

    /**
     * An string that uniquely identifies the threadFlow within the codeFlow in which it occurs.
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("An string that uniquely identifies the threadFlow within the codeFlow in which it occurs.")
    private String id;
    /**
     * Encapsulates a message intended to be read by the end user.
     * 
     */
    @JsonProperty("message")
    @JsonPropertyDescription("Encapsulates a message intended to be read by the end user.")
    private Message message;
    /**
     * Values of relevant expressions at the start of the thread flow that may change during thread flow execution.
     * 
     */
    @JsonProperty("initialState")
    @JsonPropertyDescription("Values of relevant expressions at the start of the thread flow that may change during thread flow execution.")
    private InitialState initialState;
    /**
     * Values of relevant expressions at the start of the thread flow that remain constant.
     * 
     */
    @JsonProperty("immutableState")
    @JsonPropertyDescription("Values of relevant expressions at the start of the thread flow that remain constant.")
    private ImmutableState immutableState;
    /**
     * A temporally ordered array of 'threadFlowLocation' objects, each of which describes a location visited by the tool while producing the result.
     * (Required)
     * 
     */
    @JsonProperty("locations")
    @JsonPropertyDescription("A temporally ordered array of 'threadFlowLocation' objects, each of which describes a location visited by the tool while producing the result.")
    private List<ThreadFlowLocation> locations = new ArrayList<ThreadFlowLocation>();
    /**
     * Key/value pairs that provide additional information about the object.
     * 
     */
    @JsonProperty("properties")
    @JsonPropertyDescription("Key/value pairs that provide additional information about the object.")
    private PropertyBag properties;

    /**
     * An string that uniquely identifies the threadFlow within the codeFlow in which it occurs.
     * 
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * An string that uniquely identifies the threadFlow within the codeFlow in which it occurs.
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
     * Values of relevant expressions at the start of the thread flow that may change during thread flow execution.
     * 
     */
    @JsonProperty("initialState")
    public InitialState getInitialState() {
        return initialState;
    }

    /**
     * Values of relevant expressions at the start of the thread flow that may change during thread flow execution.
     * 
     */
    @JsonProperty("initialState")
    public void setInitialState(InitialState initialState) {
        this.initialState = initialState;
    }

    /**
     * Values of relevant expressions at the start of the thread flow that remain constant.
     * 
     */
    @JsonProperty("immutableState")
    public ImmutableState getImmutableState() {
        return immutableState;
    }

    /**
     * Values of relevant expressions at the start of the thread flow that remain constant.
     * 
     */
    @JsonProperty("immutableState")
    public void setImmutableState(ImmutableState immutableState) {
        this.immutableState = immutableState;
    }

    /**
     * A temporally ordered array of 'threadFlowLocation' objects, each of which describes a location visited by the tool while producing the result.
     * (Required)
     * 
     */
    @JsonProperty("locations")
    public List<ThreadFlowLocation> getLocations() {
        return locations;
    }

    /**
     * A temporally ordered array of 'threadFlowLocation' objects, each of which describes a location visited by the tool while producing the result.
     * (Required)
     * 
     */
    @JsonProperty("locations")
    public void setLocations(List<ThreadFlowLocation> locations) {
        this.locations = locations;
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
        sb.append(ThreadFlow.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null)?"<null>":this.id));
        sb.append(',');
        sb.append("message");
        sb.append('=');
        sb.append(((this.message == null)?"<null>":this.message));
        sb.append(',');
        sb.append("initialState");
        sb.append('=');
        sb.append(((this.initialState == null)?"<null>":this.initialState));
        sb.append(',');
        sb.append("immutableState");
        sb.append('=');
        sb.append(((this.immutableState == null)?"<null>":this.immutableState));
        sb.append(',');
        sb.append("locations");
        sb.append('=');
        sb.append(((this.locations == null)?"<null>":this.locations));
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
        result = ((result* 31)+((this.initialState == null)? 0 :this.initialState.hashCode()));
        result = ((result* 31)+((this.immutableState == null)? 0 :this.immutableState.hashCode()));
        result = ((result* 31)+((this.locations == null)? 0 :this.locations.hashCode()));
        result = ((result* 31)+((this.id == null)? 0 :this.id.hashCode()));
        result = ((result* 31)+((this.message == null)? 0 :this.message.hashCode()));
        result = ((result* 31)+((this.properties == null)? 0 :this.properties.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ThreadFlow) == false) {
            return false;
        }
        ThreadFlow rhs = ((ThreadFlow) other);
        return (((((((this.initialState == rhs.initialState)||((this.initialState!= null)&&this.initialState.equals(rhs.initialState)))&&((this.immutableState == rhs.immutableState)||((this.immutableState!= null)&&this.immutableState.equals(rhs.immutableState))))&&((this.locations == rhs.locations)||((this.locations!= null)&&this.locations.equals(rhs.locations))))&&((this.id == rhs.id)||((this.id!= null)&&this.id.equals(rhs.id))))&&((this.message == rhs.message)||((this.message!= null)&&this.message.equals(rhs.message))))&&((this.properties == rhs.properties)||((this.properties!= null)&&this.properties.equals(rhs.properties))));
    }

}
