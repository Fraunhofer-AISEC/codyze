
package de.fraunhofer.aisec.codyze.analysis.generated;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * The replacement of a single region of an artifact.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deletedRegion",
    "insertedContent",
    "properties"
})
@Generated("jsonschema2pojo")
public class Replacement {

    /**
     * A region within an artifact where a result was detected.
     * (Required)
     * 
     */
    @JsonProperty("deletedRegion")
    @JsonPropertyDescription("A region within an artifact where a result was detected.")
    private Region deletedRegion;
    /**
     * Represents the contents of an artifact.
     * 
     */
    @JsonProperty("insertedContent")
    @JsonPropertyDescription("Represents the contents of an artifact.")
    private ArtifactContent insertedContent;
    /**
     * Key/value pairs that provide additional information about the object.
     * 
     */
    @JsonProperty("properties")
    @JsonPropertyDescription("Key/value pairs that provide additional information about the object.")
    private PropertyBag properties;

    /**
     * A region within an artifact where a result was detected.
     * (Required)
     * 
     */
    @JsonProperty("deletedRegion")
    public Region getDeletedRegion() {
        return deletedRegion;
    }

    /**
     * A region within an artifact where a result was detected.
     * (Required)
     * 
     */
    @JsonProperty("deletedRegion")
    public void setDeletedRegion(Region deletedRegion) {
        this.deletedRegion = deletedRegion;
    }

    /**
     * Represents the contents of an artifact.
     * 
     */
    @JsonProperty("insertedContent")
    public ArtifactContent getInsertedContent() {
        return insertedContent;
    }

    /**
     * Represents the contents of an artifact.
     * 
     */
    @JsonProperty("insertedContent")
    public void setInsertedContent(ArtifactContent insertedContent) {
        this.insertedContent = insertedContent;
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
        sb.append(Replacement.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("deletedRegion");
        sb.append('=');
        sb.append(((this.deletedRegion == null)?"<null>":this.deletedRegion));
        sb.append(',');
        sb.append("insertedContent");
        sb.append('=');
        sb.append(((this.insertedContent == null)?"<null>":this.insertedContent));
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
        result = ((result* 31)+((this.insertedContent == null)? 0 :this.insertedContent.hashCode()));
        result = ((result* 31)+((this.deletedRegion == null)? 0 :this.deletedRegion.hashCode()));
        result = ((result* 31)+((this.properties == null)? 0 :this.properties.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Replacement) == false) {
            return false;
        }
        Replacement rhs = ((Replacement) other);
        return ((((this.insertedContent == rhs.insertedContent)||((this.insertedContent!= null)&&this.insertedContent.equals(rhs.insertedContent)))&&((this.deletedRegion == rhs.deletedRegion)||((this.deletedRegion!= null)&&this.deletedRegion.equals(rhs.deletedRegion))))&&((this.properties == rhs.properties)||((this.properties!= null)&&this.properties.equals(rhs.properties))));
    }

}
