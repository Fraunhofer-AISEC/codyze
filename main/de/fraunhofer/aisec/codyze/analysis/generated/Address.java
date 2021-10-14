
package de.fraunhofer.aisec.codyze.analysis.generated;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * A physical or virtual address, or a range of addresses, in an 'addressable region' (memory or a binary file).
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "absoluteAddress",
    "relativeAddress",
    "length",
    "kind",
    "name",
    "fullyQualifiedName",
    "offsetFromParent",
    "index",
    "parentIndex",
    "properties"
})
@Generated("jsonschema2pojo")
public class Address {

    /**
     * The address expressed as a byte offset from the start of the addressable region.
     * 
     */
    @JsonProperty("absoluteAddress")
    @JsonPropertyDescription("The address expressed as a byte offset from the start of the addressable region.")
    private Integer absoluteAddress = -1;
    /**
     * The address expressed as a byte offset from the absolute address of the top-most parent object.
     * 
     */
    @JsonProperty("relativeAddress")
    @JsonPropertyDescription("The address expressed as a byte offset from the absolute address of the top-most parent object.")
    private Integer relativeAddress;
    /**
     * The number of bytes in this range of addresses.
     * 
     */
    @JsonProperty("length")
    @JsonPropertyDescription("The number of bytes in this range of addresses.")
    private Integer length;
    /**
     * An open-ended string that identifies the address kind. 'data', 'function', 'header','instruction', 'module', 'page', 'section', 'segment', 'stack', 'stackFrame', 'table' are well-known values.
     * 
     */
    @JsonProperty("kind")
    @JsonPropertyDescription("An open-ended string that identifies the address kind. 'data', 'function', 'header','instruction', 'module', 'page', 'section', 'segment', 'stack', 'stackFrame', 'table' are well-known values.")
    private String kind;
    /**
     * A name that is associated with the address, e.g., '.text'.
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("A name that is associated with the address, e.g., '.text'.")
    private String name;
    /**
     * A human-readable fully qualified name that is associated with the address.
     * 
     */
    @JsonProperty("fullyQualifiedName")
    @JsonPropertyDescription("A human-readable fully qualified name that is associated with the address.")
    private String fullyQualifiedName;
    /**
     * The byte offset of this address from the absolute or relative address of the parent object.
     * 
     */
    @JsonProperty("offsetFromParent")
    @JsonPropertyDescription("The byte offset of this address from the absolute or relative address of the parent object.")
    private Integer offsetFromParent;
    /**
     * The index within run.addresses of the cached object for this address.
     * 
     */
    @JsonProperty("index")
    @JsonPropertyDescription("The index within run.addresses of the cached object for this address.")
    private Integer index = -1;
    /**
     * The index within run.addresses of the parent object.
     * 
     */
    @JsonProperty("parentIndex")
    @JsonPropertyDescription("The index within run.addresses of the parent object.")
    private Integer parentIndex = -1;
    /**
     * Key/value pairs that provide additional information about the object.
     * 
     */
    @JsonProperty("properties")
    @JsonPropertyDescription("Key/value pairs that provide additional information about the object.")
    private PropertyBag properties;

    /**
     * The address expressed as a byte offset from the start of the addressable region.
     * 
     */
    @JsonProperty("absoluteAddress")
    public Integer getAbsoluteAddress() {
        return absoluteAddress;
    }

    /**
     * The address expressed as a byte offset from the start of the addressable region.
     * 
     */
    @JsonProperty("absoluteAddress")
    public void setAbsoluteAddress(Integer absoluteAddress) {
        this.absoluteAddress = absoluteAddress;
    }

    /**
     * The address expressed as a byte offset from the absolute address of the top-most parent object.
     * 
     */
    @JsonProperty("relativeAddress")
    public Integer getRelativeAddress() {
        return relativeAddress;
    }

    /**
     * The address expressed as a byte offset from the absolute address of the top-most parent object.
     * 
     */
    @JsonProperty("relativeAddress")
    public void setRelativeAddress(Integer relativeAddress) {
        this.relativeAddress = relativeAddress;
    }

    /**
     * The number of bytes in this range of addresses.
     * 
     */
    @JsonProperty("length")
    public Integer getLength() {
        return length;
    }

    /**
     * The number of bytes in this range of addresses.
     * 
     */
    @JsonProperty("length")
    public void setLength(Integer length) {
        this.length = length;
    }

    /**
     * An open-ended string that identifies the address kind. 'data', 'function', 'header','instruction', 'module', 'page', 'section', 'segment', 'stack', 'stackFrame', 'table' are well-known values.
     * 
     */
    @JsonProperty("kind")
    public String getKind() {
        return kind;
    }

    /**
     * An open-ended string that identifies the address kind. 'data', 'function', 'header','instruction', 'module', 'page', 'section', 'segment', 'stack', 'stackFrame', 'table' are well-known values.
     * 
     */
    @JsonProperty("kind")
    public void setKind(String kind) {
        this.kind = kind;
    }

    /**
     * A name that is associated with the address, e.g., '.text'.
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * A name that is associated with the address, e.g., '.text'.
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * A human-readable fully qualified name that is associated with the address.
     * 
     */
    @JsonProperty("fullyQualifiedName")
    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    /**
     * A human-readable fully qualified name that is associated with the address.
     * 
     */
    @JsonProperty("fullyQualifiedName")
    public void setFullyQualifiedName(String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
    }

    /**
     * The byte offset of this address from the absolute or relative address of the parent object.
     * 
     */
    @JsonProperty("offsetFromParent")
    public Integer getOffsetFromParent() {
        return offsetFromParent;
    }

    /**
     * The byte offset of this address from the absolute or relative address of the parent object.
     * 
     */
    @JsonProperty("offsetFromParent")
    public void setOffsetFromParent(Integer offsetFromParent) {
        this.offsetFromParent = offsetFromParent;
    }

    /**
     * The index within run.addresses of the cached object for this address.
     * 
     */
    @JsonProperty("index")
    public Integer getIndex() {
        return index;
    }

    /**
     * The index within run.addresses of the cached object for this address.
     * 
     */
    @JsonProperty("index")
    public void setIndex(Integer index) {
        this.index = index;
    }

    /**
     * The index within run.addresses of the parent object.
     * 
     */
    @JsonProperty("parentIndex")
    public Integer getParentIndex() {
        return parentIndex;
    }

    /**
     * The index within run.addresses of the parent object.
     * 
     */
    @JsonProperty("parentIndex")
    public void setParentIndex(Integer parentIndex) {
        this.parentIndex = parentIndex;
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
        sb.append(Address.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("absoluteAddress");
        sb.append('=');
        sb.append(((this.absoluteAddress == null)?"<null>":this.absoluteAddress));
        sb.append(',');
        sb.append("relativeAddress");
        sb.append('=');
        sb.append(((this.relativeAddress == null)?"<null>":this.relativeAddress));
        sb.append(',');
        sb.append("length");
        sb.append('=');
        sb.append(((this.length == null)?"<null>":this.length));
        sb.append(',');
        sb.append("kind");
        sb.append('=');
        sb.append(((this.kind == null)?"<null>":this.kind));
        sb.append(',');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null)?"<null>":this.name));
        sb.append(',');
        sb.append("fullyQualifiedName");
        sb.append('=');
        sb.append(((this.fullyQualifiedName == null)?"<null>":this.fullyQualifiedName));
        sb.append(',');
        sb.append("offsetFromParent");
        sb.append('=');
        sb.append(((this.offsetFromParent == null)?"<null>":this.offsetFromParent));
        sb.append(',');
        sb.append("index");
        sb.append('=');
        sb.append(((this.index == null)?"<null>":this.index));
        sb.append(',');
        sb.append("parentIndex");
        sb.append('=');
        sb.append(((this.parentIndex == null)?"<null>":this.parentIndex));
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
        result = ((result* 31)+((this.offsetFromParent == null)? 0 :this.offsetFromParent.hashCode()));
        result = ((result* 31)+((this.parentIndex == null)? 0 :this.parentIndex.hashCode()));
        result = ((result* 31)+((this.relativeAddress == null)? 0 :this.relativeAddress.hashCode()));
        result = ((result* 31)+((this.kind == null)? 0 :this.kind.hashCode()));
        result = ((result* 31)+((this.length == null)? 0 :this.length.hashCode()));
        result = ((result* 31)+((this.name == null)? 0 :this.name.hashCode()));
        result = ((result* 31)+((this.index == null)? 0 :this.index.hashCode()));
        result = ((result* 31)+((this.fullyQualifiedName == null)? 0 :this.fullyQualifiedName.hashCode()));
        result = ((result* 31)+((this.properties == null)? 0 :this.properties.hashCode()));
        result = ((result* 31)+((this.absoluteAddress == null)? 0 :this.absoluteAddress.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Address) == false) {
            return false;
        }
        Address rhs = ((Address) other);
        return (((((((((((this.offsetFromParent == rhs.offsetFromParent)||((this.offsetFromParent!= null)&&this.offsetFromParent.equals(rhs.offsetFromParent)))&&((this.parentIndex == rhs.parentIndex)||((this.parentIndex!= null)&&this.parentIndex.equals(rhs.parentIndex))))&&((this.relativeAddress == rhs.relativeAddress)||((this.relativeAddress!= null)&&this.relativeAddress.equals(rhs.relativeAddress))))&&((this.kind == rhs.kind)||((this.kind!= null)&&this.kind.equals(rhs.kind))))&&((this.length == rhs.length)||((this.length!= null)&&this.length.equals(rhs.length))))&&((this.name == rhs.name)||((this.name!= null)&&this.name.equals(rhs.name))))&&((this.index == rhs.index)||((this.index!= null)&&this.index.equals(rhs.index))))&&((this.fullyQualifiedName == rhs.fullyQualifiedName)||((this.fullyQualifiedName!= null)&&this.fullyQualifiedName.equals(rhs.fullyQualifiedName))))&&((this.properties == rhs.properties)||((this.properties!= null)&&this.properties.equals(rhs.properties))))&&((this.absoluteAddress == rhs.absoluteAddress)||((this.absoluteAddress!= null)&&this.absoluteAddress.equals(rhs.absoluteAddress))));
    }

}
