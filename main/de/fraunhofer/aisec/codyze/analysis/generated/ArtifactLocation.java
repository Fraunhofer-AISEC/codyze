
package de.fraunhofer.aisec.codyze.analysis.generated;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Specifies the location of an artifact.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "uri",
    "uriBaseId",
    "index",
    "description",
    "properties"
})
@Generated("jsonschema2pojo")
public class ArtifactLocation {

    /**
     * A string containing a valid relative or absolute URI.
     * 
     */
    @JsonProperty("uri")
    @JsonPropertyDescription("A string containing a valid relative or absolute URI.")
    private String uri;
    /**
     * A string which indirectly specifies the absolute URI with respect to which a relative URI in the "uri" property is interpreted.
     * 
     */
    @JsonProperty("uriBaseId")
    @JsonPropertyDescription("A string which indirectly specifies the absolute URI with respect to which a relative URI in the \"uri\" property is interpreted.")
    private String uriBaseId;
    /**
     * The index within the run artifacts array of the artifact object associated with the artifact location.
     * 
     */
    @JsonProperty("index")
    @JsonPropertyDescription("The index within the run artifacts array of the artifact object associated with the artifact location.")
    private Integer index = -1;
    /**
     * Encapsulates a message intended to be read by the end user.
     * 
     */
    @JsonProperty("description")
    @JsonPropertyDescription("Encapsulates a message intended to be read by the end user.")
    private Message description;
    /**
     * Key/value pairs that provide additional information about the object.
     * 
     */
    @JsonProperty("properties")
    @JsonPropertyDescription("Key/value pairs that provide additional information about the object.")
    private PropertyBag properties;

    /**
     * A string containing a valid relative or absolute URI.
     * 
     */
    @JsonProperty("uri")
    public String getUri() {
        return uri;
    }

    /**
     * A string containing a valid relative or absolute URI.
     * 
     */
    @JsonProperty("uri")
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * A string which indirectly specifies the absolute URI with respect to which a relative URI in the "uri" property is interpreted.
     * 
     */
    @JsonProperty("uriBaseId")
    public String getUriBaseId() {
        return uriBaseId;
    }

    /**
     * A string which indirectly specifies the absolute URI with respect to which a relative URI in the "uri" property is interpreted.
     * 
     */
    @JsonProperty("uriBaseId")
    public void setUriBaseId(String uriBaseId) {
        this.uriBaseId = uriBaseId;
    }

    /**
     * The index within the run artifacts array of the artifact object associated with the artifact location.
     * 
     */
    @JsonProperty("index")
    public Integer getIndex() {
        return index;
    }

    /**
     * The index within the run artifacts array of the artifact object associated with the artifact location.
     * 
     */
    @JsonProperty("index")
    public void setIndex(Integer index) {
        this.index = index;
    }

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
        sb.append(ArtifactLocation.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("uri");
        sb.append('=');
        sb.append(((this.uri == null)?"<null>":this.uri));
        sb.append(',');
        sb.append("uriBaseId");
        sb.append('=');
        sb.append(((this.uriBaseId == null)?"<null>":this.uriBaseId));
        sb.append(',');
        sb.append("index");
        sb.append('=');
        sb.append(((this.index == null)?"<null>":this.index));
        sb.append(',');
        sb.append("description");
        sb.append('=');
        sb.append(((this.description == null)?"<null>":this.description));
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
        result = ((result* 31)+((this.index == null)? 0 :this.index.hashCode()));
        result = ((result* 31)+((this.description == null)? 0 :this.description.hashCode()));
        result = ((result* 31)+((this.uri == null)? 0 :this.uri.hashCode()));
        result = ((result* 31)+((this.properties == null)? 0 :this.properties.hashCode()));
        result = ((result* 31)+((this.uriBaseId == null)? 0 :this.uriBaseId.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ArtifactLocation) == false) {
            return false;
        }
        ArtifactLocation rhs = ((ArtifactLocation) other);
        return ((((((this.index == rhs.index)||((this.index!= null)&&this.index.equals(rhs.index)))&&((this.description == rhs.description)||((this.description!= null)&&this.description.equals(rhs.description))))&&((this.uri == rhs.uri)||((this.uri!= null)&&this.uri.equals(rhs.uri))))&&((this.properties == rhs.properties)||((this.properties!= null)&&this.properties.equals(rhs.properties))))&&((this.uriBaseId == rhs.uriBaseId)||((this.uriBaseId!= null)&&this.uriBaseId.equals(rhs.uriBaseId))));
    }

}
