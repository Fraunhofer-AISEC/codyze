
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
 * An artifact relevant to a result.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "description",
    "artifactLocation",
    "regions",
    "rectangles",
    "properties"
})
@Generated("jsonschema2pojo")
public class Attachment {

    /**
     * Encapsulates a message intended to be read by the end user.
     * 
     */
    @JsonProperty("description")
    @JsonPropertyDescription("Encapsulates a message intended to be read by the end user.")
    private Message description;
    /**
     * Specifies the location of an artifact.
     * (Required)
     * 
     */
    @JsonProperty("artifactLocation")
    @JsonPropertyDescription("Specifies the location of an artifact.")
    private ArtifactLocation artifactLocation;
    /**
     * An array of regions of interest within the attachment.
     * 
     */
    @JsonProperty("regions")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of regions of interest within the attachment.")
    private Set<Region> regions = new LinkedHashSet<Region>();
    /**
     * An array of rectangles specifying areas of interest within the image.
     * 
     */
    @JsonProperty("rectangles")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of rectangles specifying areas of interest within the image.")
    private Set<Rectangle> rectangles = new LinkedHashSet<Rectangle>();
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
     * Specifies the location of an artifact.
     * (Required)
     * 
     */
    @JsonProperty("artifactLocation")
    public ArtifactLocation getArtifactLocation() {
        return artifactLocation;
    }

    /**
     * Specifies the location of an artifact.
     * (Required)
     * 
     */
    @JsonProperty("artifactLocation")
    public void setArtifactLocation(ArtifactLocation artifactLocation) {
        this.artifactLocation = artifactLocation;
    }

    /**
     * An array of regions of interest within the attachment.
     * 
     */
    @JsonProperty("regions")
    public Set<Region> getRegions() {
        return regions;
    }

    /**
     * An array of regions of interest within the attachment.
     * 
     */
    @JsonProperty("regions")
    public void setRegions(Set<Region> regions) {
        this.regions = regions;
    }

    /**
     * An array of rectangles specifying areas of interest within the image.
     * 
     */
    @JsonProperty("rectangles")
    public Set<Rectangle> getRectangles() {
        return rectangles;
    }

    /**
     * An array of rectangles specifying areas of interest within the image.
     * 
     */
    @JsonProperty("rectangles")
    public void setRectangles(Set<Rectangle> rectangles) {
        this.rectangles = rectangles;
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
        sb.append(Attachment.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("description");
        sb.append('=');
        sb.append(((this.description == null)?"<null>":this.description));
        sb.append(',');
        sb.append("artifactLocation");
        sb.append('=');
        sb.append(((this.artifactLocation == null)?"<null>":this.artifactLocation));
        sb.append(',');
        sb.append("regions");
        sb.append('=');
        sb.append(((this.regions == null)?"<null>":this.regions));
        sb.append(',');
        sb.append("rectangles");
        sb.append('=');
        sb.append(((this.rectangles == null)?"<null>":this.rectangles));
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
        result = ((result* 31)+((this.description == null)? 0 :this.description.hashCode()));
        result = ((result* 31)+((this.regions == null)? 0 :this.regions.hashCode()));
        result = ((result* 31)+((this.rectangles == null)? 0 :this.rectangles.hashCode()));
        result = ((result* 31)+((this.artifactLocation == null)? 0 :this.artifactLocation.hashCode()));
        result = ((result* 31)+((this.properties == null)? 0 :this.properties.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Attachment) == false) {
            return false;
        }
        Attachment rhs = ((Attachment) other);
        return ((((((this.description == rhs.description)||((this.description!= null)&&this.description.equals(rhs.description)))&&((this.regions == rhs.regions)||((this.regions!= null)&&this.regions.equals(rhs.regions))))&&((this.rectangles == rhs.rectangles)||((this.rectangles!= null)&&this.rectangles.equals(rhs.rectangles))))&&((this.artifactLocation == rhs.artifactLocation)||((this.artifactLocation!= null)&&this.artifactLocation.equals(rhs.artifactLocation))))&&((this.properties == rhs.properties)||((this.properties!= null)&&this.properties.equals(rhs.properties))));
    }

}
