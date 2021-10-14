
package de.fraunhofer.aisec.codyze.analysis.generated;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Defines locations of special significance to SARIF consumers.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "displayBase",
    "properties"
})
@Generated("jsonschema2pojo")
public class SpecialLocations {

    /**
     * Specifies the location of an artifact.
     * 
     */
    @JsonProperty("displayBase")
    @JsonPropertyDescription("Specifies the location of an artifact.")
    private ArtifactLocation displayBase;
    /**
     * Key/value pairs that provide additional information about the object.
     * 
     */
    @JsonProperty("properties")
    @JsonPropertyDescription("Key/value pairs that provide additional information about the object.")
    private PropertyBag properties;

    /**
     * Specifies the location of an artifact.
     * 
     */
    @JsonProperty("displayBase")
    public ArtifactLocation getDisplayBase() {
        return displayBase;
    }

    /**
     * Specifies the location of an artifact.
     * 
     */
    @JsonProperty("displayBase")
    public void setDisplayBase(ArtifactLocation displayBase) {
        this.displayBase = displayBase;
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
        sb.append(SpecialLocations.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("displayBase");
        sb.append('=');
        sb.append(((this.displayBase == null)?"<null>":this.displayBase));
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
        result = ((result* 31)+((this.displayBase == null)? 0 :this.displayBase.hashCode()));
        result = ((result* 31)+((this.properties == null)? 0 :this.properties.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SpecialLocations) == false) {
            return false;
        }
        SpecialLocations rhs = ((SpecialLocations) other);
        return (((this.displayBase == rhs.displayBase)||((this.displayBase!= null)&&this.displayBase.equals(rhs.displayBase)))&&((this.properties == rhs.properties)||((this.properties!= null)&&this.properties.equals(rhs.properties))));
    }

}
