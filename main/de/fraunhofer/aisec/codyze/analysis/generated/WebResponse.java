
package de.fraunhofer.aisec.codyze.analysis.generated;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Describes the response to an HTTP request.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "index",
    "protocol",
    "version",
    "statusCode",
    "reasonPhrase",
    "headers",
    "body",
    "noResponseReceived",
    "properties"
})
@Generated("jsonschema2pojo")
public class WebResponse {

    /**
     * The index within the run.webResponses array of the response object associated with this result.
     * 
     */
    @JsonProperty("index")
    @JsonPropertyDescription("The index within the run.webResponses array of the response object associated with this result.")
    private Integer index = -1;
    /**
     * The response protocol. Example: 'http'.
     * 
     */
    @JsonProperty("protocol")
    @JsonPropertyDescription("The response protocol. Example: 'http'.")
    private String protocol;
    /**
     * The response version. Example: '1.1'.
     * 
     */
    @JsonProperty("version")
    @JsonPropertyDescription("The response version. Example: '1.1'.")
    private String version;
    /**
     * The response status code. Example: 451.
     * 
     */
    @JsonProperty("statusCode")
    @JsonPropertyDescription("The response status code. Example: 451.")
    private Integer statusCode;
    /**
     * The response reason. Example: 'Not found'.
     * 
     */
    @JsonProperty("reasonPhrase")
    @JsonPropertyDescription("The response reason. Example: 'Not found'.")
    private String reasonPhrase;
    /**
     * The response headers.
     * 
     */
    @JsonProperty("headers")
    @JsonPropertyDescription("The response headers.")
    private Headers__1 headers;
    /**
     * Represents the contents of an artifact.
     * 
     */
    @JsonProperty("body")
    @JsonPropertyDescription("Represents the contents of an artifact.")
    private ArtifactContent body;
    /**
     * Specifies whether a response was received from the server.
     * 
     */
    @JsonProperty("noResponseReceived")
    @JsonPropertyDescription("Specifies whether a response was received from the server.")
    private Boolean noResponseReceived = false;
    /**
     * Key/value pairs that provide additional information about the object.
     * 
     */
    @JsonProperty("properties")
    @JsonPropertyDescription("Key/value pairs that provide additional information about the object.")
    private PropertyBag properties;

    /**
     * The index within the run.webResponses array of the response object associated with this result.
     * 
     */
    @JsonProperty("index")
    public Integer getIndex() {
        return index;
    }

    /**
     * The index within the run.webResponses array of the response object associated with this result.
     * 
     */
    @JsonProperty("index")
    public void setIndex(Integer index) {
        this.index = index;
    }

    /**
     * The response protocol. Example: 'http'.
     * 
     */
    @JsonProperty("protocol")
    public String getProtocol() {
        return protocol;
    }

    /**
     * The response protocol. Example: 'http'.
     * 
     */
    @JsonProperty("protocol")
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * The response version. Example: '1.1'.
     * 
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /**
     * The response version. Example: '1.1'.
     * 
     */
    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * The response status code. Example: 451.
     * 
     */
    @JsonProperty("statusCode")
    public Integer getStatusCode() {
        return statusCode;
    }

    /**
     * The response status code. Example: 451.
     * 
     */
    @JsonProperty("statusCode")
    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * The response reason. Example: 'Not found'.
     * 
     */
    @JsonProperty("reasonPhrase")
    public String getReasonPhrase() {
        return reasonPhrase;
    }

    /**
     * The response reason. Example: 'Not found'.
     * 
     */
    @JsonProperty("reasonPhrase")
    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    /**
     * The response headers.
     * 
     */
    @JsonProperty("headers")
    public Headers__1 getHeaders() {
        return headers;
    }

    /**
     * The response headers.
     * 
     */
    @JsonProperty("headers")
    public void setHeaders(Headers__1 headers) {
        this.headers = headers;
    }

    /**
     * Represents the contents of an artifact.
     * 
     */
    @JsonProperty("body")
    public ArtifactContent getBody() {
        return body;
    }

    /**
     * Represents the contents of an artifact.
     * 
     */
    @JsonProperty("body")
    public void setBody(ArtifactContent body) {
        this.body = body;
    }

    /**
     * Specifies whether a response was received from the server.
     * 
     */
    @JsonProperty("noResponseReceived")
    public Boolean getNoResponseReceived() {
        return noResponseReceived;
    }

    /**
     * Specifies whether a response was received from the server.
     * 
     */
    @JsonProperty("noResponseReceived")
    public void setNoResponseReceived(Boolean noResponseReceived) {
        this.noResponseReceived = noResponseReceived;
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
        sb.append(WebResponse.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("index");
        sb.append('=');
        sb.append(((this.index == null)?"<null>":this.index));
        sb.append(',');
        sb.append("protocol");
        sb.append('=');
        sb.append(((this.protocol == null)?"<null>":this.protocol));
        sb.append(',');
        sb.append("version");
        sb.append('=');
        sb.append(((this.version == null)?"<null>":this.version));
        sb.append(',');
        sb.append("statusCode");
        sb.append('=');
        sb.append(((this.statusCode == null)?"<null>":this.statusCode));
        sb.append(',');
        sb.append("reasonPhrase");
        sb.append('=');
        sb.append(((this.reasonPhrase == null)?"<null>":this.reasonPhrase));
        sb.append(',');
        sb.append("headers");
        sb.append('=');
        sb.append(((this.headers == null)?"<null>":this.headers));
        sb.append(',');
        sb.append("body");
        sb.append('=');
        sb.append(((this.body == null)?"<null>":this.body));
        sb.append(',');
        sb.append("noResponseReceived");
        sb.append('=');
        sb.append(((this.noResponseReceived == null)?"<null>":this.noResponseReceived));
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
        result = ((result* 31)+((this.headers == null)? 0 :this.headers.hashCode()));
        result = ((result* 31)+((this.protocol == null)? 0 :this.protocol.hashCode()));
        result = ((result* 31)+((this.reasonPhrase == null)? 0 :this.reasonPhrase.hashCode()));
        result = ((result* 31)+((this.noResponseReceived == null)? 0 :this.noResponseReceived.hashCode()));
        result = ((result* 31)+((this.index == null)? 0 :this.index.hashCode()));
        result = ((result* 31)+((this.body == null)? 0 :this.body.hashCode()));
        result = ((result* 31)+((this.version == null)? 0 :this.version.hashCode()));
        result = ((result* 31)+((this.properties == null)? 0 :this.properties.hashCode()));
        result = ((result* 31)+((this.statusCode == null)? 0 :this.statusCode.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WebResponse) == false) {
            return false;
        }
        WebResponse rhs = ((WebResponse) other);
        return ((((((((((this.headers == rhs.headers)||((this.headers!= null)&&this.headers.equals(rhs.headers)))&&((this.protocol == rhs.protocol)||((this.protocol!= null)&&this.protocol.equals(rhs.protocol))))&&((this.reasonPhrase == rhs.reasonPhrase)||((this.reasonPhrase!= null)&&this.reasonPhrase.equals(rhs.reasonPhrase))))&&((this.noResponseReceived == rhs.noResponseReceived)||((this.noResponseReceived!= null)&&this.noResponseReceived.equals(rhs.noResponseReceived))))&&((this.index == rhs.index)||((this.index!= null)&&this.index.equals(rhs.index))))&&((this.body == rhs.body)||((this.body!= null)&&this.body.equals(rhs.body))))&&((this.version == rhs.version)||((this.version!= null)&&this.version.equals(rhs.version))))&&((this.properties == rhs.properties)||((this.properties!= null)&&this.properties.equals(rhs.properties))))&&((this.statusCode == rhs.statusCode)||((this.statusCode!= null)&&this.statusCode.equals(rhs.statusCode))));
    }

}
