
package de.fraunhofer.aisec.codyze.analysis.generated;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Information about how a specific rule or notification was reconfigured at runtime.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "configuration",
    "descriptor",
    "properties"
})
@Generated("jsonschema2pojo")
public class ConfigurationOverride {

    /**
     * Information about a rule or notification that can be configured at runtime.
     * (Required)
     * 
     */
    @JsonProperty("configuration")
    @JsonPropertyDescription("Information about a rule or notification that can be configured at runtime.")
    private ReportingConfiguration configuration;
    /**
     * Information about how to locate a relevant reporting descriptor.
     * (Required)
     * 
     */
    @JsonProperty("descriptor")
    @JsonPropertyDescription("Information about how to locate a relevant reporting descriptor.")
    private ReportingDescriptorReference descriptor;
    /**
     * Key/value pairs that provide additional information about the object.
     * 
     */
    @JsonProperty("properties")
    @JsonPropertyDescription("Key/value pairs that provide additional information about the object.")
    private PropertyBag properties;

    /**
     * Information about a rule or notification that can be configured at runtime.
     * (Required)
     * 
     */
    @JsonProperty("configuration")
    public ReportingConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Information about a rule or notification that can be configured at runtime.
     * (Required)
     * 
     */
    @JsonProperty("configuration")
    public void setConfiguration(ReportingConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Information about how to locate a relevant reporting descriptor.
     * (Required)
     * 
     */
    @JsonProperty("descriptor")
    public ReportingDescriptorReference getDescriptor() {
        return descriptor;
    }

    /**
     * Information about how to locate a relevant reporting descriptor.
     * (Required)
     * 
     */
    @JsonProperty("descriptor")
    public void setDescriptor(ReportingDescriptorReference descriptor) {
        this.descriptor = descriptor;
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
        sb.append(ConfigurationOverride.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("configuration");
        sb.append('=');
        sb.append(((this.configuration == null)?"<null>":this.configuration));
        sb.append(',');
        sb.append("descriptor");
        sb.append('=');
        sb.append(((this.descriptor == null)?"<null>":this.descriptor));
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
        result = ((result* 31)+((this.descriptor == null)? 0 :this.descriptor.hashCode()));
        result = ((result* 31)+((this.configuration == null)? 0 :this.configuration.hashCode()));
        result = ((result* 31)+((this.properties == null)? 0 :this.properties.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ConfigurationOverride) == false) {
            return false;
        }
        ConfigurationOverride rhs = ((ConfigurationOverride) other);
        return ((((this.descriptor == rhs.descriptor)||((this.descriptor!= null)&&this.descriptor.equals(rhs.descriptor)))&&((this.configuration == rhs.configuration)||((this.configuration!= null)&&this.configuration.equals(rhs.configuration))))&&((this.properties == rhs.properties)||((this.properties!= null)&&this.properties.equals(rhs.properties))));
    }

}
