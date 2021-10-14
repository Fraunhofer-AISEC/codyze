
package de.fraunhofer.aisec.codyze.analysis.generated;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Contains information that enables a SARIF consumer to locate the external property file that contains the value of an externalized property associated with the run.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "location",
    "guid",
    "itemCount",
    "properties"
})
@Generated("jsonschema2pojo")
public class ExternalPropertyFileReference {

    /**
     * Specifies the location of an artifact.
     * 
     */
    @JsonProperty("location")
    @JsonPropertyDescription("Specifies the location of an artifact.")
    private ArtifactLocation location;
    /**
     * A stable, unique identifier for the external property file in the form of a GUID.
     * 
     */
    @JsonProperty("guid")
    @JsonPropertyDescription("A stable, unique identifier for the external property file in the form of a GUID.")
    private String guid;
    /**
     * A non-negative integer specifying the number of items contained in the external property file.
     * 
     */
    @JsonProperty("itemCount")
    @JsonPropertyDescription("A non-negative integer specifying the number of items contained in the external property file.")
    private Integer itemCount = -1;
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
    @JsonProperty("location")
    public ArtifactLocation getLocation() {
        return location;
    }

    /**
     * Specifies the location of an artifact.
     * 
     */
    @JsonProperty("location")
    public void setLocation(ArtifactLocation location) {
        this.location = location;
    }

    /**
     * A stable, unique identifier for the external property file in the form of a GUID.
     * 
     */
    @JsonProperty("guid")
    public String getGuid() {
        return guid;
    }

    /**
     * A stable, unique identifier for the external property file in the form of a GUID.
     * 
     */
    @JsonProperty("guid")
    public void setGuid(String guid) {
        this.guid = guid;
    }

    /**
     * A non-negative integer specifying the number of items contained in the external property file.
     * 
     */
    @JsonProperty("itemCount")
    public Integer getItemCount() {
        return itemCount;
    }

    /**
     * A non-negative integer specifying the number of items contained in the external property file.
     * 
     */
    @JsonProperty("itemCount")
    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
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
        sb.append(ExternalPropertyFileReference.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("location");
        sb.append('=');
        sb.append(((this.location == null)?"<null>":this.location));
        sb.append(',');
        sb.append("guid");
        sb.append('=');
        sb.append(((this.guid == null)?"<null>":this.guid));
        sb.append(',');
        sb.append("itemCount");
        sb.append('=');
        sb.append(((this.itemCount == null)?"<null>":this.itemCount));
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
        result = ((result* 31)+((this.guid == null)? 0 :this.guid.hashCode()));
        result = ((result* 31)+((this.location == null)? 0 :this.location.hashCode()));
        result = ((result* 31)+((this.properties == null)? 0 :this.properties.hashCode()));
        result = ((result* 31)+((this.itemCount == null)? 0 :this.itemCount.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExternalPropertyFileReference) == false) {
            return false;
        }
        ExternalPropertyFileReference rhs = ((ExternalPropertyFileReference) other);
        return (((((this.guid == rhs.guid)||((this.guid!= null)&&this.guid.equals(rhs.guid)))&&((this.location == rhs.location)||((this.location!= null)&&this.location.equals(rhs.location))))&&((this.properties == rhs.properties)||((this.properties!= null)&&this.properties.equals(rhs.properties))))&&((this.itemCount == rhs.itemCount)||((this.itemCount!= null)&&this.itemCount.equals(rhs.itemCount))));
    }

}
