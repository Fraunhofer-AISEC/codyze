
package de.fraunhofer.aisec.codyze.analysis.generated;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;


/**
 * Metadata that describes a specific report produced by the tool, as part of the analysis it provides or its runtime reporting.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "deprecatedIds",
    "guid",
    "deprecatedGuids",
    "name",
    "deprecatedNames",
    "shortDescription",
    "fullDescription",
    "messageStrings",
    "defaultConfiguration",
    "helpUri",
    "help",
    "relationships",
    "properties"
})
@Generated("jsonschema2pojo")
public class ReportingDescriptor {

    /**
     * A stable, opaque identifier for the report.
     * (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("A stable, opaque identifier for the report.")
    private String id;
    /**
     * An array of stable, opaque identifiers by which this report was known in some previous version of the analysis tool.
     * 
     */
    @JsonProperty("deprecatedIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of stable, opaque identifiers by which this report was known in some previous version of the analysis tool.")
    private Set<String> deprecatedIds = new LinkedHashSet<String>();
    /**
     * A unique identifier for the reporting descriptor in the form of a GUID.
     * 
     */
    @JsonProperty("guid")
    @JsonPropertyDescription("A unique identifier for the reporting descriptor in the form of a GUID.")
    private String guid;
    /**
     * An array of unique identifies in the form of a GUID by which this report was known in some previous version of the analysis tool.
     * 
     */
    @JsonProperty("deprecatedGuids")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of unique identifies in the form of a GUID by which this report was known in some previous version of the analysis tool.")
    private Set<String> deprecatedGuids = new LinkedHashSet<String>();
    /**
     * A report identifier that is understandable to an end user.
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("A report identifier that is understandable to an end user.")
    private String name;
    /**
     * An array of readable identifiers by which this report was known in some previous version of the analysis tool.
     * 
     */
    @JsonProperty("deprecatedNames")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of readable identifiers by which this report was known in some previous version of the analysis tool.")
    private Set<String> deprecatedNames = new LinkedHashSet<String>();
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
     * A set of name/value pairs with arbitrary names. Each value is a multiformatMessageString object, which holds message strings in plain text and (optionally) Markdown format. The strings can include placeholders, which can be used to construct a message in combination with an arbitrary number of additional string arguments.
     * 
     */
    @JsonProperty("messageStrings")
    @JsonPropertyDescription("A set of name/value pairs with arbitrary names. Each value is a multiformatMessageString object, which holds message strings in plain text and (optionally) Markdown format. The strings can include placeholders, which can be used to construct a message in combination with an arbitrary number of additional string arguments.")
    private MessageStrings messageStrings;
    /**
     * Information about a rule or notification that can be configured at runtime.
     * 
     */
    @JsonProperty("defaultConfiguration")
    @JsonPropertyDescription("Information about a rule or notification that can be configured at runtime.")
    private ReportingConfiguration defaultConfiguration;
    /**
     * A URI where the primary documentation for the report can be found.
     * 
     */
    @JsonProperty("helpUri")
    @JsonPropertyDescription("A URI where the primary documentation for the report can be found.")
    private URI helpUri;
    /**
     * A message string or message format string rendered in multiple formats.
     * 
     */
    @JsonProperty("help")
    @JsonPropertyDescription("A message string or message format string rendered in multiple formats.")
    private MultiformatMessageString help;
    /**
     * An array of objects that describe relationships between this reporting descriptor and others.
     * 
     */
    @JsonProperty("relationships")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of objects that describe relationships between this reporting descriptor and others.")
    private Set<ReportingDescriptorRelationship> relationships = new LinkedHashSet<ReportingDescriptorRelationship>();
    /**
     * Key/value pairs that provide additional information about the object.
     * 
     */
    @JsonProperty("properties")
    @JsonPropertyDescription("Key/value pairs that provide additional information about the object.")
    private PropertyBag properties;

    /**
     * A stable, opaque identifier for the report.
     * (Required)
     * 
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * A stable, opaque identifier for the report.
     * (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * An array of stable, opaque identifiers by which this report was known in some previous version of the analysis tool.
     * 
     */
    @JsonProperty("deprecatedIds")
    public Set<String> getDeprecatedIds() {
        return deprecatedIds;
    }

    /**
     * An array of stable, opaque identifiers by which this report was known in some previous version of the analysis tool.
     * 
     */
    @JsonProperty("deprecatedIds")
    public void setDeprecatedIds(Set<String> deprecatedIds) {
        this.deprecatedIds = deprecatedIds;
    }

    /**
     * A unique identifier for the reporting descriptor in the form of a GUID.
     * 
     */
    @JsonProperty("guid")
    public String getGuid() {
        return guid;
    }

    /**
     * A unique identifier for the reporting descriptor in the form of a GUID.
     * 
     */
    @JsonProperty("guid")
    public void setGuid(String guid) {
        this.guid = guid;
    }

    /**
     * An array of unique identifies in the form of a GUID by which this report was known in some previous version of the analysis tool.
     * 
     */
    @JsonProperty("deprecatedGuids")
    public Set<String> getDeprecatedGuids() {
        return deprecatedGuids;
    }

    /**
     * An array of unique identifies in the form of a GUID by which this report was known in some previous version of the analysis tool.
     * 
     */
    @JsonProperty("deprecatedGuids")
    public void setDeprecatedGuids(Set<String> deprecatedGuids) {
        this.deprecatedGuids = deprecatedGuids;
    }

    /**
     * A report identifier that is understandable to an end user.
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * A report identifier that is understandable to an end user.
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * An array of readable identifiers by which this report was known in some previous version of the analysis tool.
     * 
     */
    @JsonProperty("deprecatedNames")
    public Set<String> getDeprecatedNames() {
        return deprecatedNames;
    }

    /**
     * An array of readable identifiers by which this report was known in some previous version of the analysis tool.
     * 
     */
    @JsonProperty("deprecatedNames")
    public void setDeprecatedNames(Set<String> deprecatedNames) {
        this.deprecatedNames = deprecatedNames;
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
     * A set of name/value pairs with arbitrary names. Each value is a multiformatMessageString object, which holds message strings in plain text and (optionally) Markdown format. The strings can include placeholders, which can be used to construct a message in combination with an arbitrary number of additional string arguments.
     * 
     */
    @JsonProperty("messageStrings")
    public MessageStrings getMessageStrings() {
        return messageStrings;
    }

    /**
     * A set of name/value pairs with arbitrary names. Each value is a multiformatMessageString object, which holds message strings in plain text and (optionally) Markdown format. The strings can include placeholders, which can be used to construct a message in combination with an arbitrary number of additional string arguments.
     * 
     */
    @JsonProperty("messageStrings")
    public void setMessageStrings(MessageStrings messageStrings) {
        this.messageStrings = messageStrings;
    }

    /**
     * Information about a rule or notification that can be configured at runtime.
     * 
     */
    @JsonProperty("defaultConfiguration")
    public ReportingConfiguration getDefaultConfiguration() {
        return defaultConfiguration;
    }

    /**
     * Information about a rule or notification that can be configured at runtime.
     * 
     */
    @JsonProperty("defaultConfiguration")
    public void setDefaultConfiguration(ReportingConfiguration defaultConfiguration) {
        this.defaultConfiguration = defaultConfiguration;
    }

    /**
     * A URI where the primary documentation for the report can be found.
     * 
     */
    @JsonProperty("helpUri")
    public URI getHelpUri() {
        return helpUri;
    }

    /**
     * A URI where the primary documentation for the report can be found.
     * 
     */
    @JsonProperty("helpUri")
    public void setHelpUri(URI helpUri) {
        this.helpUri = helpUri;
    }

    /**
     * A message string or message format string rendered in multiple formats.
     * 
     */
    @JsonProperty("help")
    public MultiformatMessageString getHelp() {
        return help;
    }

    /**
     * A message string or message format string rendered in multiple formats.
     * 
     */
    @JsonProperty("help")
    public void setHelp(MultiformatMessageString help) {
        this.help = help;
    }

    /**
     * An array of objects that describe relationships between this reporting descriptor and others.
     * 
     */
    @JsonProperty("relationships")
    public Set<ReportingDescriptorRelationship> getRelationships() {
        return relationships;
    }

    /**
     * An array of objects that describe relationships between this reporting descriptor and others.
     * 
     */
    @JsonProperty("relationships")
    public void setRelationships(Set<ReportingDescriptorRelationship> relationships) {
        this.relationships = relationships;
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
        sb.append(ReportingDescriptor.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null)?"<null>":this.id));
        sb.append(',');
        sb.append("deprecatedIds");
        sb.append('=');
        sb.append(((this.deprecatedIds == null)?"<null>":this.deprecatedIds));
        sb.append(',');
        sb.append("guid");
        sb.append('=');
        sb.append(((this.guid == null)?"<null>":this.guid));
        sb.append(',');
        sb.append("deprecatedGuids");
        sb.append('=');
        sb.append(((this.deprecatedGuids == null)?"<null>":this.deprecatedGuids));
        sb.append(',');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null)?"<null>":this.name));
        sb.append(',');
        sb.append("deprecatedNames");
        sb.append('=');
        sb.append(((this.deprecatedNames == null)?"<null>":this.deprecatedNames));
        sb.append(',');
        sb.append("shortDescription");
        sb.append('=');
        sb.append(((this.shortDescription == null)?"<null>":this.shortDescription));
        sb.append(',');
        sb.append("fullDescription");
        sb.append('=');
        sb.append(((this.fullDescription == null)?"<null>":this.fullDescription));
        sb.append(',');
        sb.append("messageStrings");
        sb.append('=');
        sb.append(((this.messageStrings == null)?"<null>":this.messageStrings));
        sb.append(',');
        sb.append("defaultConfiguration");
        sb.append('=');
        sb.append(((this.defaultConfiguration == null)?"<null>":this.defaultConfiguration));
        sb.append(',');
        sb.append("helpUri");
        sb.append('=');
        sb.append(((this.helpUri == null)?"<null>":this.helpUri));
        sb.append(',');
        sb.append("help");
        sb.append('=');
        sb.append(((this.help == null)?"<null>":this.help));
        sb.append(',');
        sb.append("relationships");
        sb.append('=');
        sb.append(((this.relationships == null)?"<null>":this.relationships));
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
        result = ((result* 31)+((this.deprecatedIds == null)? 0 :this.deprecatedIds.hashCode()));
        result = ((result* 31)+((this.deprecatedGuids == null)? 0 :this.deprecatedGuids.hashCode()));
        result = ((result* 31)+((this.shortDescription == null)? 0 :this.shortDescription.hashCode()));
        result = ((result* 31)+((this.fullDescription == null)? 0 :this.fullDescription.hashCode()));
        result = ((result* 31)+((this.helpUri == null)? 0 :this.helpUri.hashCode()));
        result = ((result* 31)+((this.defaultConfiguration == null)? 0 :this.defaultConfiguration.hashCode()));
        result = ((result* 31)+((this.help == null)? 0 :this.help.hashCode()));
        result = ((result* 31)+((this.relationships == null)? 0 :this.relationships.hashCode()));
        result = ((result* 31)+((this.messageStrings == null)? 0 :this.messageStrings.hashCode()));
        result = ((result* 31)+((this.name == null)? 0 :this.name.hashCode()));
        result = ((result* 31)+((this.guid == null)? 0 :this.guid.hashCode()));
        result = ((result* 31)+((this.deprecatedNames == null)? 0 :this.deprecatedNames.hashCode()));
        result = ((result* 31)+((this.id == null)? 0 :this.id.hashCode()));
        result = ((result* 31)+((this.properties == null)? 0 :this.properties.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReportingDescriptor) == false) {
            return false;
        }
        ReportingDescriptor rhs = ((ReportingDescriptor) other);
        return (((((((((((((((this.deprecatedIds == rhs.deprecatedIds)||((this.deprecatedIds!= null)&&this.deprecatedIds.equals(rhs.deprecatedIds)))&&((this.deprecatedGuids == rhs.deprecatedGuids)||((this.deprecatedGuids!= null)&&this.deprecatedGuids.equals(rhs.deprecatedGuids))))&&((this.shortDescription == rhs.shortDescription)||((this.shortDescription!= null)&&this.shortDescription.equals(rhs.shortDescription))))&&((this.fullDescription == rhs.fullDescription)||((this.fullDescription!= null)&&this.fullDescription.equals(rhs.fullDescription))))&&((this.helpUri == rhs.helpUri)||((this.helpUri!= null)&&this.helpUri.equals(rhs.helpUri))))&&((this.defaultConfiguration == rhs.defaultConfiguration)||((this.defaultConfiguration!= null)&&this.defaultConfiguration.equals(rhs.defaultConfiguration))))&&((this.help == rhs.help)||((this.help!= null)&&this.help.equals(rhs.help))))&&((this.relationships == rhs.relationships)||((this.relationships!= null)&&this.relationships.equals(rhs.relationships))))&&((this.messageStrings == rhs.messageStrings)||((this.messageStrings!= null)&&this.messageStrings.equals(rhs.messageStrings))))&&((this.name == rhs.name)||((this.name!= null)&&this.name.equals(rhs.name))))&&((this.guid == rhs.guid)||((this.guid!= null)&&this.guid.equals(rhs.guid))))&&((this.deprecatedNames == rhs.deprecatedNames)||((this.deprecatedNames!= null)&&this.deprecatedNames.equals(rhs.deprecatedNames))))&&((this.id == rhs.id)||((this.id!= null)&&this.id.equals(rhs.id))))&&((this.properties == rhs.properties)||((this.properties!= null)&&this.properties.equals(rhs.properties))));
    }

}
