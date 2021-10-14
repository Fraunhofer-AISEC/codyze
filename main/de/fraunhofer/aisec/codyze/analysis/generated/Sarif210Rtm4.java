
package de.fraunhofer.aisec.codyze.analysis.generated;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;


/**
 * Static Analysis Results Format (SARIF) Version 2.1.0-rtm.4 JSON Schema
 * <p>
 * Static Analysis Results Format (SARIF) Version 2.1.0-rtm.4 JSON Schema: a standard format for the output of static analysis tools.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "$schema",
    "version",
    "runs",
    "inlineExternalProperties",
    "properties"
})
@Generated("jsonschema2pojo")
public class Sarif210Rtm4 {

    /**
     * The URI of the JSON schema corresponding to the version.
     * 
     */
    @JsonProperty("$schema")
    @JsonPropertyDescription("The URI of the JSON schema corresponding to the version.")
    private URI $schema;
    /**
     * The SARIF format version of this log file.
     * (Required)
     * 
     */
    @JsonProperty("version")
    @JsonPropertyDescription("The SARIF format version of this log file.")
    private Sarif210Rtm4 .Version version;
    /**
     * The set of runs contained in this log file.
     * (Required)
     * 
     */
    @JsonProperty("runs")
    @JsonPropertyDescription("The set of runs contained in this log file.")
    private List<Run> runs = new ArrayList<Run>();
    /**
     * References to external property files that share data between runs.
     * 
     */
    @JsonProperty("inlineExternalProperties")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("References to external property files that share data between runs.")
    private Set<ExternalProperties> inlineExternalProperties = new LinkedHashSet<ExternalProperties>();
    /**
     * Key/value pairs that provide additional information about the object.
     * 
     */
    @JsonProperty("properties")
    @JsonPropertyDescription("Key/value pairs that provide additional information about the object.")
    private PropertyBag properties;

    /**
     * The URI of the JSON schema corresponding to the version.
     * 
     */
    @JsonProperty("$schema")
    public URI get$schema() {
        return $schema;
    }

    /**
     * The URI of the JSON schema corresponding to the version.
     * 
     */
    @JsonProperty("$schema")
    public void set$schema(URI $schema) {
        this.$schema = $schema;
    }

    /**
     * The SARIF format version of this log file.
     * (Required)
     * 
     */
    @JsonProperty("version")
    public Sarif210Rtm4 .Version getVersion() {
        return version;
    }

    /**
     * The SARIF format version of this log file.
     * (Required)
     * 
     */
    @JsonProperty("version")
    public void setVersion(Sarif210Rtm4 .Version version) {
        this.version = version;
    }

    /**
     * The set of runs contained in this log file.
     * (Required)
     * 
     */
    @JsonProperty("runs")
    public List<Run> getRuns() {
        return runs;
    }

    /**
     * The set of runs contained in this log file.
     * (Required)
     * 
     */
    @JsonProperty("runs")
    public void setRuns(List<Run> runs) {
        this.runs = runs;
    }

    /**
     * References to external property files that share data between runs.
     * 
     */
    @JsonProperty("inlineExternalProperties")
    public Set<ExternalProperties> getInlineExternalProperties() {
        return inlineExternalProperties;
    }

    /**
     * References to external property files that share data between runs.
     * 
     */
    @JsonProperty("inlineExternalProperties")
    public void setInlineExternalProperties(Set<ExternalProperties> inlineExternalProperties) {
        this.inlineExternalProperties = inlineExternalProperties;
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
        sb.append(Sarif210Rtm4 .class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("$schema");
        sb.append('=');
        sb.append(((this.$schema == null)?"<null>":this.$schema));
        sb.append(',');
        sb.append("version");
        sb.append('=');
        sb.append(((this.version == null)?"<null>":this.version));
        sb.append(',');
        sb.append("runs");
        sb.append('=');
        sb.append(((this.runs == null)?"<null>":this.runs));
        sb.append(',');
        sb.append("inlineExternalProperties");
        sb.append('=');
        sb.append(((this.inlineExternalProperties == null)?"<null>":this.inlineExternalProperties));
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
        result = ((result* 31)+((this.inlineExternalProperties == null)? 0 :this.inlineExternalProperties.hashCode()));
        result = ((result* 31)+((this.$schema == null)? 0 :this.$schema.hashCode()));
        result = ((result* 31)+((this.version == null)? 0 :this.version.hashCode()));
        result = ((result* 31)+((this.runs == null)? 0 :this.runs.hashCode()));
        result = ((result* 31)+((this.properties == null)? 0 :this.properties.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Sarif210Rtm4) == false) {
            return false;
        }
        Sarif210Rtm4 rhs = ((Sarif210Rtm4) other);
        return ((((((this.inlineExternalProperties == rhs.inlineExternalProperties)||((this.inlineExternalProperties!= null)&&this.inlineExternalProperties.equals(rhs.inlineExternalProperties)))&&((this.$schema == rhs.$schema)||((this.$schema!= null)&&this.$schema.equals(rhs.$schema))))&&((this.version == rhs.version)||((this.version!= null)&&this.version.equals(rhs.version))))&&((this.runs == rhs.runs)||((this.runs!= null)&&this.runs.equals(rhs.runs))))&&((this.properties == rhs.properties)||((this.properties!= null)&&this.properties.equals(rhs.properties))));
    }


    /**
     * The SARIF format version of this log file.
     * 
     */
    @Generated("jsonschema2pojo")
    public enum Version {

        _2_1_0("2.1.0");
        private final String value;
        private final static Map<String, Sarif210Rtm4 .Version> CONSTANTS = new HashMap<String, Sarif210Rtm4 .Version>();

        static {
            for (Sarif210Rtm4 .Version c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private Version(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static Sarif210Rtm4 .Version fromValue(String value) {
            Sarif210Rtm4 .Version constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
