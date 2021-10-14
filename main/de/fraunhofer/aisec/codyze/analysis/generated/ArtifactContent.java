
package de.fraunhofer.aisec.codyze.analysis.generated;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Represents the contents of an artifact.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "text",
    "binary",
    "rendered",
    "properties"
})
@Generated("jsonschema2pojo")
public class ArtifactContent {

    /**
     * UTF-8-encoded content from a text artifact.
     * 
     */
    @JsonProperty("text")
    @JsonPropertyDescription("UTF-8-encoded content from a text artifact.")
    private String text;
    /**
     * MIME Base64-encoded content from a binary artifact, or from a text artifact in its original encoding.
     * 
     */
    @JsonProperty("binary")
    @JsonPropertyDescription("MIME Base64-encoded content from a binary artifact, or from a text artifact in its original encoding.")
    private String binary;
    /**
     * A message string or message format string rendered in multiple formats.
     * 
     */
    @JsonProperty("rendered")
    @JsonPropertyDescription("A message string or message format string rendered in multiple formats.")
    private MultiformatMessageString rendered;
    /**
     * Key/value pairs that provide additional information about the object.
     * 
     */
    @JsonProperty("properties")
    @JsonPropertyDescription("Key/value pairs that provide additional information about the object.")
    private PropertyBag properties;

    /**
     * UTF-8-encoded content from a text artifact.
     * 
     */
    @JsonProperty("text")
    public String getText() {
        return text;
    }

    /**
     * UTF-8-encoded content from a text artifact.
     * 
     */
    @JsonProperty("text")
    public void setText(String text) {
        this.text = text;
    }

    /**
     * MIME Base64-encoded content from a binary artifact, or from a text artifact in its original encoding.
     * 
     */
    @JsonProperty("binary")
    public String getBinary() {
        return binary;
    }

    /**
     * MIME Base64-encoded content from a binary artifact, or from a text artifact in its original encoding.
     * 
     */
    @JsonProperty("binary")
    public void setBinary(String binary) {
        this.binary = binary;
    }

    /**
     * A message string or message format string rendered in multiple formats.
     * 
     */
    @JsonProperty("rendered")
    public MultiformatMessageString getRendered() {
        return rendered;
    }

    /**
     * A message string or message format string rendered in multiple formats.
     * 
     */
    @JsonProperty("rendered")
    public void setRendered(MultiformatMessageString rendered) {
        this.rendered = rendered;
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
        sb.append(ArtifactContent.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("text");
        sb.append('=');
        sb.append(((this.text == null)?"<null>":this.text));
        sb.append(',');
        sb.append("binary");
        sb.append('=');
        sb.append(((this.binary == null)?"<null>":this.binary));
        sb.append(',');
        sb.append("rendered");
        sb.append('=');
        sb.append(((this.rendered == null)?"<null>":this.rendered));
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
        result = ((result* 31)+((this.text == null)? 0 :this.text.hashCode()));
        result = ((result* 31)+((this.rendered == null)? 0 :this.rendered.hashCode()));
        result = ((result* 31)+((this.properties == null)? 0 :this.properties.hashCode()));
        result = ((result* 31)+((this.binary == null)? 0 :this.binary.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ArtifactContent) == false) {
            return false;
        }
        ArtifactContent rhs = ((ArtifactContent) other);
        return (((((this.text == rhs.text)||((this.text!= null)&&this.text.equals(rhs.text)))&&((this.rendered == rhs.rendered)||((this.rendered!= null)&&this.rendered.equals(rhs.rendered))))&&((this.properties == rhs.properties)||((this.properties!= null)&&this.properties.equals(rhs.properties))))&&((this.binary == rhs.binary)||((this.binary!= null)&&this.binary.equals(rhs.binary))));
    }

}
