
package de.fraunhofer.aisec.codyze.analysis.generated;

import java.net.URI;
import java.util.Date;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Specifies the information necessary to retrieve a desired revision from a version control system.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "repositoryUri",
    "revisionId",
    "branch",
    "revisionTag",
    "asOfTimeUtc",
    "mappedTo",
    "properties"
})
@Generated("jsonschema2pojo")
public class VersionControlDetails {

    /**
     * The absolute URI of the repository.
     * (Required)
     * 
     */
    @JsonProperty("repositoryUri")
    @JsonPropertyDescription("The absolute URI of the repository.")
    private URI repositoryUri;
    /**
     * A string that uniquely and permanently identifies the revision within the repository.
     * 
     */
    @JsonProperty("revisionId")
    @JsonPropertyDescription("A string that uniquely and permanently identifies the revision within the repository.")
    private String revisionId;
    /**
     * The name of a branch containing the revision.
     * 
     */
    @JsonProperty("branch")
    @JsonPropertyDescription("The name of a branch containing the revision.")
    private String branch;
    /**
     * A tag that has been applied to the revision.
     * 
     */
    @JsonProperty("revisionTag")
    @JsonPropertyDescription("A tag that has been applied to the revision.")
    private String revisionTag;
    /**
     * A Coordinated Universal Time (UTC) date and time that can be used to synchronize an enlistment to the state of the repository at that time.
     * 
     */
    @JsonProperty("asOfTimeUtc")
    @JsonPropertyDescription("A Coordinated Universal Time (UTC) date and time that can be used to synchronize an enlistment to the state of the repository at that time.")
    private Date asOfTimeUtc;
    /**
     * Specifies the location of an artifact.
     * 
     */
    @JsonProperty("mappedTo")
    @JsonPropertyDescription("Specifies the location of an artifact.")
    private ArtifactLocation mappedTo;
    /**
     * Key/value pairs that provide additional information about the object.
     * 
     */
    @JsonProperty("properties")
    @JsonPropertyDescription("Key/value pairs that provide additional information about the object.")
    private PropertyBag properties;

    /**
     * The absolute URI of the repository.
     * (Required)
     * 
     */
    @JsonProperty("repositoryUri")
    public URI getRepositoryUri() {
        return repositoryUri;
    }

    /**
     * The absolute URI of the repository.
     * (Required)
     * 
     */
    @JsonProperty("repositoryUri")
    public void setRepositoryUri(URI repositoryUri) {
        this.repositoryUri = repositoryUri;
    }

    /**
     * A string that uniquely and permanently identifies the revision within the repository.
     * 
     */
    @JsonProperty("revisionId")
    public String getRevisionId() {
        return revisionId;
    }

    /**
     * A string that uniquely and permanently identifies the revision within the repository.
     * 
     */
    @JsonProperty("revisionId")
    public void setRevisionId(String revisionId) {
        this.revisionId = revisionId;
    }

    /**
     * The name of a branch containing the revision.
     * 
     */
    @JsonProperty("branch")
    public String getBranch() {
        return branch;
    }

    /**
     * The name of a branch containing the revision.
     * 
     */
    @JsonProperty("branch")
    public void setBranch(String branch) {
        this.branch = branch;
    }

    /**
     * A tag that has been applied to the revision.
     * 
     */
    @JsonProperty("revisionTag")
    public String getRevisionTag() {
        return revisionTag;
    }

    /**
     * A tag that has been applied to the revision.
     * 
     */
    @JsonProperty("revisionTag")
    public void setRevisionTag(String revisionTag) {
        this.revisionTag = revisionTag;
    }

    /**
     * A Coordinated Universal Time (UTC) date and time that can be used to synchronize an enlistment to the state of the repository at that time.
     * 
     */
    @JsonProperty("asOfTimeUtc")
    public Date getAsOfTimeUtc() {
        return asOfTimeUtc;
    }

    /**
     * A Coordinated Universal Time (UTC) date and time that can be used to synchronize an enlistment to the state of the repository at that time.
     * 
     */
    @JsonProperty("asOfTimeUtc")
    public void setAsOfTimeUtc(Date asOfTimeUtc) {
        this.asOfTimeUtc = asOfTimeUtc;
    }

    /**
     * Specifies the location of an artifact.
     * 
     */
    @JsonProperty("mappedTo")
    public ArtifactLocation getMappedTo() {
        return mappedTo;
    }

    /**
     * Specifies the location of an artifact.
     * 
     */
    @JsonProperty("mappedTo")
    public void setMappedTo(ArtifactLocation mappedTo) {
        this.mappedTo = mappedTo;
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
        sb.append(VersionControlDetails.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("repositoryUri");
        sb.append('=');
        sb.append(((this.repositoryUri == null)?"<null>":this.repositoryUri));
        sb.append(',');
        sb.append("revisionId");
        sb.append('=');
        sb.append(((this.revisionId == null)?"<null>":this.revisionId));
        sb.append(',');
        sb.append("branch");
        sb.append('=');
        sb.append(((this.branch == null)?"<null>":this.branch));
        sb.append(',');
        sb.append("revisionTag");
        sb.append('=');
        sb.append(((this.revisionTag == null)?"<null>":this.revisionTag));
        sb.append(',');
        sb.append("asOfTimeUtc");
        sb.append('=');
        sb.append(((this.asOfTimeUtc == null)?"<null>":this.asOfTimeUtc));
        sb.append(',');
        sb.append("mappedTo");
        sb.append('=');
        sb.append(((this.mappedTo == null)?"<null>":this.mappedTo));
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
        result = ((result* 31)+((this.revisionId == null)? 0 :this.revisionId.hashCode()));
        result = ((result* 31)+((this.repositoryUri == null)? 0 :this.repositoryUri.hashCode()));
        result = ((result* 31)+((this.mappedTo == null)? 0 :this.mappedTo.hashCode()));
        result = ((result* 31)+((this.branch == null)? 0 :this.branch.hashCode()));
        result = ((result* 31)+((this.asOfTimeUtc == null)? 0 :this.asOfTimeUtc.hashCode()));
        result = ((result* 31)+((this.properties == null)? 0 :this.properties.hashCode()));
        result = ((result* 31)+((this.revisionTag == null)? 0 :this.revisionTag.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof VersionControlDetails) == false) {
            return false;
        }
        VersionControlDetails rhs = ((VersionControlDetails) other);
        return ((((((((this.revisionId == rhs.revisionId)||((this.revisionId!= null)&&this.revisionId.equals(rhs.revisionId)))&&((this.repositoryUri == rhs.repositoryUri)||((this.repositoryUri!= null)&&this.repositoryUri.equals(rhs.repositoryUri))))&&((this.mappedTo == rhs.mappedTo)||((this.mappedTo!= null)&&this.mappedTo.equals(rhs.mappedTo))))&&((this.branch == rhs.branch)||((this.branch!= null)&&this.branch.equals(rhs.branch))))&&((this.asOfTimeUtc == rhs.asOfTimeUtc)||((this.asOfTimeUtc!= null)&&this.asOfTimeUtc.equals(rhs.asOfTimeUtc))))&&((this.properties == rhs.properties)||((this.properties!= null)&&this.properties.equals(rhs.properties))))&&((this.revisionTag == rhs.revisionTag)||((this.revisionTag!= null)&&this.revisionTag.equals(rhs.revisionTag))));
    }

}
