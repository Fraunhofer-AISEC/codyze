
package de.fraunhofer.aisec.codyze.analysis.generated;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * An area within an image.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "top",
    "left",
    "bottom",
    "right",
    "message",
    "properties"
})
@Generated("jsonschema2pojo")
public class Rectangle {

    /**
     * The Y coordinate of the top edge of the rectangle, measured in the image's natural units.
     * 
     */
    @JsonProperty("top")
    @JsonPropertyDescription("The Y coordinate of the top edge of the rectangle, measured in the image's natural units.")
    private Double top;
    /**
     * The X coordinate of the left edge of the rectangle, measured in the image's natural units.
     * 
     */
    @JsonProperty("left")
    @JsonPropertyDescription("The X coordinate of the left edge of the rectangle, measured in the image's natural units.")
    private Double left;
    /**
     * The Y coordinate of the bottom edge of the rectangle, measured in the image's natural units.
     * 
     */
    @JsonProperty("bottom")
    @JsonPropertyDescription("The Y coordinate of the bottom edge of the rectangle, measured in the image's natural units.")
    private Double bottom;
    /**
     * The X coordinate of the right edge of the rectangle, measured in the image's natural units.
     * 
     */
    @JsonProperty("right")
    @JsonPropertyDescription("The X coordinate of the right edge of the rectangle, measured in the image's natural units.")
    private Double right;
    /**
     * Encapsulates a message intended to be read by the end user.
     * 
     */
    @JsonProperty("message")
    @JsonPropertyDescription("Encapsulates a message intended to be read by the end user.")
    private Message message;
    /**
     * Key/value pairs that provide additional information about the object.
     * 
     */
    @JsonProperty("properties")
    @JsonPropertyDescription("Key/value pairs that provide additional information about the object.")
    private PropertyBag properties;

    /**
     * The Y coordinate of the top edge of the rectangle, measured in the image's natural units.
     * 
     */
    @JsonProperty("top")
    public Double getTop() {
        return top;
    }

    /**
     * The Y coordinate of the top edge of the rectangle, measured in the image's natural units.
     * 
     */
    @JsonProperty("top")
    public void setTop(Double top) {
        this.top = top;
    }

    /**
     * The X coordinate of the left edge of the rectangle, measured in the image's natural units.
     * 
     */
    @JsonProperty("left")
    public Double getLeft() {
        return left;
    }

    /**
     * The X coordinate of the left edge of the rectangle, measured in the image's natural units.
     * 
     */
    @JsonProperty("left")
    public void setLeft(Double left) {
        this.left = left;
    }

    /**
     * The Y coordinate of the bottom edge of the rectangle, measured in the image's natural units.
     * 
     */
    @JsonProperty("bottom")
    public Double getBottom() {
        return bottom;
    }

    /**
     * The Y coordinate of the bottom edge of the rectangle, measured in the image's natural units.
     * 
     */
    @JsonProperty("bottom")
    public void setBottom(Double bottom) {
        this.bottom = bottom;
    }

    /**
     * The X coordinate of the right edge of the rectangle, measured in the image's natural units.
     * 
     */
    @JsonProperty("right")
    public Double getRight() {
        return right;
    }

    /**
     * The X coordinate of the right edge of the rectangle, measured in the image's natural units.
     * 
     */
    @JsonProperty("right")
    public void setRight(Double right) {
        this.right = right;
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
        sb.append(Rectangle.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("top");
        sb.append('=');
        sb.append(((this.top == null)?"<null>":this.top));
        sb.append(',');
        sb.append("left");
        sb.append('=');
        sb.append(((this.left == null)?"<null>":this.left));
        sb.append(',');
        sb.append("bottom");
        sb.append('=');
        sb.append(((this.bottom == null)?"<null>":this.bottom));
        sb.append(',');
        sb.append("right");
        sb.append('=');
        sb.append(((this.right == null)?"<null>":this.right));
        sb.append(',');
        sb.append("message");
        sb.append('=');
        sb.append(((this.message == null)?"<null>":this.message));
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
        result = ((result* 31)+((this.top == null)? 0 :this.top.hashCode()));
        result = ((result* 31)+((this.left == null)? 0 :this.left.hashCode()));
        result = ((result* 31)+((this.bottom == null)? 0 :this.bottom.hashCode()));
        result = ((result* 31)+((this.right == null)? 0 :this.right.hashCode()));
        result = ((result* 31)+((this.message == null)? 0 :this.message.hashCode()));
        result = ((result* 31)+((this.properties == null)? 0 :this.properties.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Rectangle) == false) {
            return false;
        }
        Rectangle rhs = ((Rectangle) other);
        return (((((((this.top == rhs.top)||((this.top!= null)&&this.top.equals(rhs.top)))&&((this.left == rhs.left)||((this.left!= null)&&this.left.equals(rhs.left))))&&((this.bottom == rhs.bottom)||((this.bottom!= null)&&this.bottom.equals(rhs.bottom))))&&((this.right == rhs.right)||((this.right!= null)&&this.right.equals(rhs.right))))&&((this.message == rhs.message)||((this.message!= null)&&this.message.equals(rhs.message))))&&((this.properties == rhs.properties)||((this.properties!= null)&&this.properties.equals(rhs.properties))));
    }

}
