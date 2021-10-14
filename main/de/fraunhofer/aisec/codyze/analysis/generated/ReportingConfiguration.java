
package de.fraunhofer.aisec.codyze.analysis.generated;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * Information about a rule or notification that can be configured at runtime.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "enabled",
    "level",
    "rank",
    "parameters",
    "properties"
})
@Generated("jsonschema2pojo")
public class ReportingConfiguration {

    /**
     * Specifies whether the report may be produced during the scan.
     * 
     */
    @JsonProperty("enabled")
    @JsonPropertyDescription("Specifies whether the report may be produced during the scan.")
    private Boolean enabled = true;
    /**
     * Specifies the failure level for the report.
     * 
     */
    @JsonProperty("level")
    @JsonPropertyDescription("Specifies the failure level for the report.")
    private ReportingConfiguration.Level level = ReportingConfiguration.Level.fromValue("warning");
    /**
     * Specifies the relative priority of the report. Used for analysis output only.
     * 
     */
    @JsonProperty("rank")
    @JsonPropertyDescription("Specifies the relative priority of the report. Used for analysis output only.")
    private Double rank = -1.0D;
    /**
     * Key/value pairs that provide additional information about the object.
     * 
     */
    @JsonProperty("parameters")
    @JsonPropertyDescription("Key/value pairs that provide additional information about the object.")
    private PropertyBag parameters;
    /**
     * Key/value pairs that provide additional information about the object.
     * 
     */
    @JsonProperty("properties")
    @JsonPropertyDescription("Key/value pairs that provide additional information about the object.")
    private PropertyBag properties;

    /**
     * Specifies whether the report may be produced during the scan.
     * 
     */
    @JsonProperty("enabled")
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Specifies whether the report may be produced during the scan.
     * 
     */
    @JsonProperty("enabled")
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Specifies the failure level for the report.
     * 
     */
    @JsonProperty("level")
    public ReportingConfiguration.Level getLevel() {
        return level;
    }

    /**
     * Specifies the failure level for the report.
     * 
     */
    @JsonProperty("level")
    public void setLevel(ReportingConfiguration.Level level) {
        this.level = level;
    }

    /**
     * Specifies the relative priority of the report. Used for analysis output only.
     * 
     */
    @JsonProperty("rank")
    public Double getRank() {
        return rank;
    }

    /**
     * Specifies the relative priority of the report. Used for analysis output only.
     * 
     */
    @JsonProperty("rank")
    public void setRank(Double rank) {
        this.rank = rank;
    }

    /**
     * Key/value pairs that provide additional information about the object.
     * 
     */
    @JsonProperty("parameters")
    public PropertyBag getParameters() {
        return parameters;
    }

    /**
     * Key/value pairs that provide additional information about the object.
     * 
     */
    @JsonProperty("parameters")
    public void setParameters(PropertyBag parameters) {
        this.parameters = parameters;
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
        sb.append(ReportingConfiguration.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("enabled");
        sb.append('=');
        sb.append(((this.enabled == null)?"<null>":this.enabled));
        sb.append(',');
        sb.append("level");
        sb.append('=');
        sb.append(((this.level == null)?"<null>":this.level));
        sb.append(',');
        sb.append("rank");
        sb.append('=');
        sb.append(((this.rank == null)?"<null>":this.rank));
        sb.append(',');
        sb.append("parameters");
        sb.append('=');
        sb.append(((this.parameters == null)?"<null>":this.parameters));
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
        result = ((result* 31)+((this.rank == null)? 0 :this.rank.hashCode()));
        result = ((result* 31)+((this.level == null)? 0 :this.level.hashCode()));
        result = ((result* 31)+((this.parameters == null)? 0 :this.parameters.hashCode()));
        result = ((result* 31)+((this.enabled == null)? 0 :this.enabled.hashCode()));
        result = ((result* 31)+((this.properties == null)? 0 :this.properties.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReportingConfiguration) == false) {
            return false;
        }
        ReportingConfiguration rhs = ((ReportingConfiguration) other);
        return ((((((this.rank == rhs.rank)||((this.rank!= null)&&this.rank.equals(rhs.rank)))&&((this.level == rhs.level)||((this.level!= null)&&this.level.equals(rhs.level))))&&((this.parameters == rhs.parameters)||((this.parameters!= null)&&this.parameters.equals(rhs.parameters))))&&((this.enabled == rhs.enabled)||((this.enabled!= null)&&this.enabled.equals(rhs.enabled))))&&((this.properties == rhs.properties)||((this.properties!= null)&&this.properties.equals(rhs.properties))));
    }


    /**
     * Specifies the failure level for the report.
     * 
     */
    @Generated("jsonschema2pojo")
    public enum Level {

        NONE("none"),
        NOTE("note"),
        WARNING("warning"),
        ERROR("error");
        private final String value;
        private final static Map<String, ReportingConfiguration.Level> CONSTANTS = new HashMap<String, ReportingConfiguration.Level>();

        static {
            for (ReportingConfiguration.Level c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private Level(String value) {
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
        public static ReportingConfiguration.Level fromValue(String value) {
            ReportingConfiguration.Level constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
