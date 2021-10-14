
package de.fraunhofer.aisec.codyze.analysis.generated;

import java.net.URI;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Provides additional metadata related to translation.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "fullName",
    "shortDescription",
    "fullDescription",
    "downloadUri",
    "informationUri",
    "properties"
})
@Generated("jsonschema2pojo")
public class TranslationMetadata {

    /**
     * The name associated with the translation metadata.
     * (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("The name associated with the translation metadata.")
    private String name;
    /**
     * The full name associated with the translation metadata.
     * 
     */
    @JsonProperty("fullName")
    @JsonPropertyDescription("The full name associated with the translation metadata.")
    private String fullName;
    /**
     * A message string or message format string rendered in multiple formats.
     * 
     */
    @JsonProperty("shortDescription")
    @JsonPropertyDescription("A message string or message format string rendered in multiple formats.")
    private MultiformatMessageString shortDescription;
    /**
     * A message string or message format string rendered in multiple formats.
     * 
     */
    @JsonProperty("fullDescription")
    @JsonPropertyDescription("A message string or message format string rendered in multiple formats.")
    private MultiformatMessageString fullDescription;
    /**
     * The absolute URI from which the translation metadata can be downloaded.
     * 
     */
    @JsonProperty("downloadUri")
    @JsonPropertyDescription("The absolute URI from which the translation metadata can be downloaded.")
    private URI downloadUri;
    /**
     * The absolute URI from which information related to the translation metadata can be downloaded.
     * 
     */
    @JsonProperty("informationUri")
    @JsonPropertyDescription("The absolute URI from which information related to the translation metadata can be downloaded.")
    private URI informationUri;
    /**
     * Key/value pairs that provide additional information about the object.
     * 
     */
    @JsonProperty("properties")
    @JsonPropertyDescription("Key/value pairs that provide additional information about the object.")
    private PropertyBag properties;

    /**
     * The name associated with the translation metadata.
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * The name associated with the translation metadata.
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The full name associated with the translation metadata.
     * 
     */
    @JsonProperty("fullName")
    public String getFullName() {
        return fullName;
    }

    /**
     * The full name associated with the translation metadata.
     * 
     */
    @JsonProperty("fullName")
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * A message string or message format string rendered in multiple formats.
     * 
     */
    @JsonProperty("shortDescription")
    public MultiformatMessageString getShortDescription() {
        return shortDescription;
    }

    /**
     * A message string or message format string rendered in multiple formats.
     * 
     */
    @JsonProperty("shortDescription")
    public void setShortDescription(MultiformatMessageString shortDescription) {
        this.shortDescription = shortDescription;
    }

    /**
     * A message string or message format string rendered in multiple formats.
     * 
     */
    @JsonProperty("fullDescription")
    public MultiformatMessageString getFullDescription() {
        return fullDescription;
    }

    /**
     * A message string or message format string rendered in multiple formats.
     * 
     */
    @JsonProperty("fullDescription")
    public void setFullDescription(MultiformatMessageString fullDescription) {
        this.fullDescription = fullDescription;
    }

    /**
     * The absolute URI from which the translation metadata can be downloaded.
     * 
     */
    @JsonProperty("downloadUri")
    public URI getDownloadUri() {
        return downloadUri;
    }

    /**
     * The absolute URI from which the translation metadata can be downloaded.
     * 
     */
    @JsonProperty("downloadUri")
    public void setDownloadUri(URI downloadUri) {
        this.downloadUri = downloadUri;
    }

    /**
     * The absolute URI from which information related to the translation metadata can be downloaded.
     * 
     */
    @JsonProperty("informationUri")
    public URI getInformationUri() {
        return informationUri;
    }

    /**
     * The absolute URI from which information related to the translation metadata can be downloaded.
     * 
     */
    @JsonProperty("informationUri")
    public void setInformationUri(URI informationUri) {
        this.informationUri = informationUri;
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
        sb.append(TranslationMetadata.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null)?"<null>":this.name));
        sb.append(',');
        sb.append("fullName");
        sb.append('=');
        sb.append(((this.fullName == null)?"<null>":this.fullName));
        sb.append(',');
        sb.append("shortDescription");
        sb.append('=');
        sb.append(((this.shortDescription == null)?"<null>":this.shortDescription));
        sb.append(',');
        sb.append("fullDescription");
        sb.append('=');
        sb.append(((this.fullDescription == null)?"<null>":this.fullDescription));
        sb.append(',');
        sb.append("downloadUri");
        sb.append('=');
        sb.append(((this.downloadUri == null)?"<null>":this.downloadUri));
        sb.append(',');
        sb.append("informationUri");
        sb.append('=');
        sb.append(((this.informationUri == null)?"<null>":this.informationUri));
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
        result = ((result* 31)+((this.name == null)? 0 :this.name.hashCode()));
        result = ((result* 31)+((this.fullName == null)? 0 :this.fullName.hashCode()));
        result = ((result* 31)+((this.shortDescription == null)? 0 :this.shortDescription.hashCode()));
        result = ((result* 31)+((this.downloadUri == null)? 0 :this.downloadUri.hashCode()));
        result = ((result* 31)+((this.fullDescription == null)? 0 :this.fullDescription.hashCode()));
        result = ((result* 31)+((this.informationUri == null)? 0 :this.informationUri.hashCode()));
        result = ((result* 31)+((this.properties == null)? 0 :this.properties.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TranslationMetadata) == false) {
            return false;
        }
        TranslationMetadata rhs = ((TranslationMetadata) other);
        return ((((((((this.name == rhs.name)||((this.name!= null)&&this.name.equals(rhs.name)))&&((this.fullName == rhs.fullName)||((this.fullName!= null)&&this.fullName.equals(rhs.fullName))))&&((this.shortDescription == rhs.shortDescription)||((this.shortDescription!= null)&&this.shortDescription.equals(rhs.shortDescription))))&&((this.downloadUri == rhs.downloadUri)||((this.downloadUri!= null)&&this.downloadUri.equals(rhs.downloadUri))))&&((this.fullDescription == rhs.fullDescription)||((this.fullDescription!= null)&&this.fullDescription.equals(rhs.fullDescription))))&&((this.informationUri == rhs.informationUri)||((this.informationUri!= null)&&this.informationUri.equals(rhs.informationUri))))&&((this.properties == rhs.properties)||((this.properties!= null)&&this.properties.equals(rhs.properties))));
    }

}
