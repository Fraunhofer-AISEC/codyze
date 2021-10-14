
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
 * The analysis tool that was run.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "driver",
    "extensions",
    "properties"
})
@Generated("jsonschema2pojo")
public class Tool {

    /**
     * A component, such as a plug-in or the driver, of the analysis tool that was run.
     * (Required)
     * 
     */
    @JsonProperty("driver")
    @JsonPropertyDescription("A component, such as a plug-in or the driver, of the analysis tool that was run.")
    private ToolComponent driver;
    /**
     * Tool extensions that contributed to or reconfigured the analysis tool that was run.
     * 
     */
    @JsonProperty("extensions")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("Tool extensions that contributed to or reconfigured the analysis tool that was run.")
    private Set<ToolComponent> extensions = new LinkedHashSet<ToolComponent>();
    /**
     * Key/value pairs that provide additional information about the object.
     * 
     */
    @JsonProperty("properties")
    @JsonPropertyDescription("Key/value pairs that provide additional information about the object.")
    private PropertyBag properties;

    /**
     * A component, such as a plug-in or the driver, of the analysis tool that was run.
     * (Required)
     * 
     */
    @JsonProperty("driver")
    public ToolComponent getDriver() {
        return driver;
    }

    /**
     * A component, such as a plug-in or the driver, of the analysis tool that was run.
     * (Required)
     * 
     */
    @JsonProperty("driver")
    public void setDriver(ToolComponent driver) {
        this.driver = driver;
    }

    /**
     * Tool extensions that contributed to or reconfigured the analysis tool that was run.
     * 
     */
    @JsonProperty("extensions")
    public Set<ToolComponent> getExtensions() {
        return extensions;
    }

    /**
     * Tool extensions that contributed to or reconfigured the analysis tool that was run.
     * 
     */
    @JsonProperty("extensions")
    public void setExtensions(Set<ToolComponent> extensions) {
        this.extensions = extensions;
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
        sb.append(Tool.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("driver");
        sb.append('=');
        sb.append(((this.driver == null)?"<null>":this.driver));
        sb.append(',');
        sb.append("extensions");
        sb.append('=');
        sb.append(((this.extensions == null)?"<null>":this.extensions));
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
        result = ((result* 31)+((this.extensions == null)? 0 :this.extensions.hashCode()));
        result = ((result* 31)+((this.driver == null)? 0 :this.driver.hashCode()));
        result = ((result* 31)+((this.properties == null)? 0 :this.properties.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Tool) == false) {
            return false;
        }
        Tool rhs = ((Tool) other);
        return ((((this.extensions == rhs.extensions)||((this.extensions!= null)&&this.extensions.equals(rhs.extensions)))&&((this.driver == rhs.driver)||((this.driver!= null)&&this.driver.equals(rhs.driver))))&&((this.properties == rhs.properties)||((this.properties!= null)&&this.properties.equals(rhs.properties))));
    }

}
