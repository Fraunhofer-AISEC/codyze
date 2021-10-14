
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
 * Describes how a converter transformed the output of a static analysis tool from the analysis tool's native output format into the SARIF format.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "tool",
    "invocation",
    "analysisToolLogFiles",
    "properties"
})
@Generated("jsonschema2pojo")
public class Conversion {

    /**
     * The analysis tool that was run.
     * (Required)
     * 
     */
    @JsonProperty("tool")
    @JsonPropertyDescription("The analysis tool that was run.")
    private Tool tool;
    /**
     * The runtime environment of the analysis tool run.
     * 
     */
    @JsonProperty("invocation")
    @JsonPropertyDescription("The runtime environment of the analysis tool run.")
    private Invocation invocation;
    /**
     * The locations of the analysis tool's per-run log files.
     * 
     */
    @JsonProperty("analysisToolLogFiles")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("The locations of the analysis tool's per-run log files.")
    private Set<ArtifactLocation> analysisToolLogFiles = new LinkedHashSet<ArtifactLocation>();
    /**
     * Key/value pairs that provide additional information about the object.
     * 
     */
    @JsonProperty("properties")
    @JsonPropertyDescription("Key/value pairs that provide additional information about the object.")
    private PropertyBag properties;

    /**
     * The analysis tool that was run.
     * (Required)
     * 
     */
    @JsonProperty("tool")
    public Tool getTool() {
        return tool;
    }

    /**
     * The analysis tool that was run.
     * (Required)
     * 
     */
    @JsonProperty("tool")
    public void setTool(Tool tool) {
        this.tool = tool;
    }

    /**
     * The runtime environment of the analysis tool run.
     * 
     */
    @JsonProperty("invocation")
    public Invocation getInvocation() {
        return invocation;
    }

    /**
     * The runtime environment of the analysis tool run.
     * 
     */
    @JsonProperty("invocation")
    public void setInvocation(Invocation invocation) {
        this.invocation = invocation;
    }

    /**
     * The locations of the analysis tool's per-run log files.
     * 
     */
    @JsonProperty("analysisToolLogFiles")
    public Set<ArtifactLocation> getAnalysisToolLogFiles() {
        return analysisToolLogFiles;
    }

    /**
     * The locations of the analysis tool's per-run log files.
     * 
     */
    @JsonProperty("analysisToolLogFiles")
    public void setAnalysisToolLogFiles(Set<ArtifactLocation> analysisToolLogFiles) {
        this.analysisToolLogFiles = analysisToolLogFiles;
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
        sb.append(Conversion.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("tool");
        sb.append('=');
        sb.append(((this.tool == null)?"<null>":this.tool));
        sb.append(',');
        sb.append("invocation");
        sb.append('=');
        sb.append(((this.invocation == null)?"<null>":this.invocation));
        sb.append(',');
        sb.append("analysisToolLogFiles");
        sb.append('=');
        sb.append(((this.analysisToolLogFiles == null)?"<null>":this.analysisToolLogFiles));
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
        result = ((result* 31)+((this.invocation == null)? 0 :this.invocation.hashCode()));
        result = ((result* 31)+((this.analysisToolLogFiles == null)? 0 :this.analysisToolLogFiles.hashCode()));
        result = ((result* 31)+((this.tool == null)? 0 :this.tool.hashCode()));
        result = ((result* 31)+((this.properties == null)? 0 :this.properties.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Conversion) == false) {
            return false;
        }
        Conversion rhs = ((Conversion) other);
        return (((((this.invocation == rhs.invocation)||((this.invocation!= null)&&this.invocation.equals(rhs.invocation)))&&((this.analysisToolLogFiles == rhs.analysisToolLogFiles)||((this.analysisToolLogFiles!= null)&&this.analysisToolLogFiles.equals(rhs.analysisToolLogFiles))))&&((this.tool == rhs.tool)||((this.tool!= null)&&this.tool.equals(rhs.tool))))&&((this.properties == rhs.properties)||((this.properties!= null)&&this.properties.equals(rhs.properties))));
    }

}
