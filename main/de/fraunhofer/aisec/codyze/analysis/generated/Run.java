
package de.fraunhofer.aisec.codyze.analysis.generated;

import java.util.ArrayList;
import java.util.Arrays;
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
 * Describes a single run of an analysis tool, and contains the reported output of that run.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "tool",
    "invocations",
    "conversion",
    "language",
    "versionControlProvenance",
    "originalUriBaseIds",
    "artifacts",
    "logicalLocations",
    "graphs",
    "results",
    "automationDetails",
    "runAggregates",
    "baselineGuid",
    "redactionTokens",
    "defaultEncoding",
    "defaultSourceLanguage",
    "newlineSequences",
    "columnKind",
    "externalPropertyFileReferences",
    "threadFlowLocations",
    "taxonomies",
    "addresses",
    "translations",
    "policies",
    "webRequests",
    "webResponses",
    "specialLocations",
    "properties"
})
@Generated("jsonschema2pojo")
public class Run {

    /**
     * The analysis tool that was run.
     * (Required)
     * 
     */
    @JsonProperty("tool")
    @JsonPropertyDescription("The analysis tool that was run.")
    private Tool tool;
    /**
     * Describes the invocation of the analysis tool.
     * 
     */
    @JsonProperty("invocations")
    @JsonPropertyDescription("Describes the invocation of the analysis tool.")
    private List<Invocation> invocations = new ArrayList<Invocation>();
    /**
     * Describes how a converter transformed the output of a static analysis tool from the analysis tool's native output format into the SARIF format.
     * 
     */
    @JsonProperty("conversion")
    @JsonPropertyDescription("Describes how a converter transformed the output of a static analysis tool from the analysis tool's native output format into the SARIF format.")
    private Conversion conversion;
    /**
     * The language of the messages emitted into the log file during this run (expressed as an ISO 639-1 two-letter lowercase culture code) and an optional region (expressed as an ISO 3166-1 two-letter uppercase subculture code associated with a country or region). The casing is recommended but not required (in order for this data to conform to RFC5646).
     * 
     */
    @JsonProperty("language")
    @JsonPropertyDescription("The language of the messages emitted into the log file during this run (expressed as an ISO 639-1 two-letter lowercase culture code) and an optional region (expressed as an ISO 3166-1 two-letter uppercase subculture code associated with a country or region). The casing is recommended but not required (in order for this data to conform to RFC5646).")
    private String language = "en-US";
    /**
     * Specifies the revision in version control of the artifacts that were scanned.
     * 
     */
    @JsonProperty("versionControlProvenance")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("Specifies the revision in version control of the artifacts that were scanned.")
    private Set<VersionControlDetails> versionControlProvenance = new LinkedHashSet<VersionControlDetails>();
    /**
     * The artifact location specified by each uriBaseId symbol on the machine where the tool originally ran.
     * 
     */
    @JsonProperty("originalUriBaseIds")
    @JsonPropertyDescription("The artifact location specified by each uriBaseId symbol on the machine where the tool originally ran.")
    private OriginalUriBaseIds originalUriBaseIds;
    /**
     * An array of artifact objects relevant to the run.
     * 
     */
    @JsonProperty("artifacts")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of artifact objects relevant to the run.")
    private Set<Artifact> artifacts = new LinkedHashSet<Artifact>();
    /**
     * An array of logical locations such as namespaces, types or functions.
     * 
     */
    @JsonProperty("logicalLocations")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of logical locations such as namespaces, types or functions.")
    private Set<LogicalLocation> logicalLocations = new LinkedHashSet<LogicalLocation>();
    /**
     * An array of zero or more unique graph objects associated with the run.
     * 
     */
    @JsonProperty("graphs")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of zero or more unique graph objects associated with the run.")
    private Set<Graph> graphs = new LinkedHashSet<Graph>();
    /**
     * The set of results contained in an SARIF log. The results array can be omitted when a run is solely exporting rules metadata. It must be present (but may be empty) if a log file represents an actual scan.
     * 
     */
    @JsonProperty("results")
    @JsonPropertyDescription("The set of results contained in an SARIF log. The results array can be omitted when a run is solely exporting rules metadata. It must be present (but may be empty) if a log file represents an actual scan.")
    private List<Result> results = new ArrayList<Result>();
    /**
     * Information that describes a run's identity and role within an engineering system process.
     * 
     */
    @JsonProperty("automationDetails")
    @JsonPropertyDescription("Information that describes a run's identity and role within an engineering system process.")
    private RunAutomationDetails automationDetails;
    /**
     * Automation details that describe the aggregate of runs to which this run belongs.
     * 
     */
    @JsonProperty("runAggregates")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("Automation details that describe the aggregate of runs to which this run belongs.")
    private Set<RunAutomationDetails> runAggregates = new LinkedHashSet<RunAutomationDetails>();
    /**
     * The 'guid' property of a previous SARIF 'run' that comprises the baseline that was used to compute result 'baselineState' properties for the run.
     * 
     */
    @JsonProperty("baselineGuid")
    @JsonPropertyDescription("The 'guid' property of a previous SARIF 'run' that comprises the baseline that was used to compute result 'baselineState' properties for the run.")
    private String baselineGuid;
    /**
     * An array of strings used to replace sensitive information in a redaction-aware property.
     * 
     */
    @JsonProperty("redactionTokens")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of strings used to replace sensitive information in a redaction-aware property.")
    private Set<String> redactionTokens = new LinkedHashSet<String>();
    /**
     * Specifies the default encoding for any artifact object that refers to a text file.
     * 
     */
    @JsonProperty("defaultEncoding")
    @JsonPropertyDescription("Specifies the default encoding for any artifact object that refers to a text file.")
    private String defaultEncoding;
    /**
     * Specifies the default source language for any artifact object that refers to a text file that contains source code.
     * 
     */
    @JsonProperty("defaultSourceLanguage")
    @JsonPropertyDescription("Specifies the default source language for any artifact object that refers to a text file that contains source code.")
    private String defaultSourceLanguage;
    /**
     * An ordered list of character sequences that were treated as line breaks when computing region information for the run.
     * 
     */
    @JsonProperty("newlineSequences")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An ordered list of character sequences that were treated as line breaks when computing region information for the run.")
    private Set<String> newlineSequences = new LinkedHashSet<String>(Arrays.asList("\r\n", "\n"));
    /**
     * Specifies the unit in which the tool measures columns.
     * 
     */
    @JsonProperty("columnKind")
    @JsonPropertyDescription("Specifies the unit in which the tool measures columns.")
    private Run.ColumnKind columnKind;
    /**
     * References to external property files that should be inlined with the content of a root log file.
     * 
     */
    @JsonProperty("externalPropertyFileReferences")
    @JsonPropertyDescription("References to external property files that should be inlined with the content of a root log file.")
    private ExternalPropertyFileReferences externalPropertyFileReferences;
    /**
     * An array of threadFlowLocation objects cached at run level.
     * 
     */
    @JsonProperty("threadFlowLocations")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of threadFlowLocation objects cached at run level.")
    private Set<ThreadFlowLocation> threadFlowLocations = new LinkedHashSet<ThreadFlowLocation>();
    /**
     * An array of toolComponent objects relevant to a taxonomy in which results are categorized.
     * 
     */
    @JsonProperty("taxonomies")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of toolComponent objects relevant to a taxonomy in which results are categorized.")
    private Set<ToolComponent> taxonomies = new LinkedHashSet<ToolComponent>();
    /**
     * Addresses associated with this run instance, if any.
     * 
     */
    @JsonProperty("addresses")
    @JsonPropertyDescription("Addresses associated with this run instance, if any.")
    private List<Address> addresses = new ArrayList<Address>();
    /**
     * The set of available translations of the localized data provided by the tool.
     * 
     */
    @JsonProperty("translations")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("The set of available translations of the localized data provided by the tool.")
    private Set<ToolComponent> translations = new LinkedHashSet<ToolComponent>();
    /**
     * Contains configurations that may potentially override both reportingDescriptor.defaultConfiguration (the tool's default severities) and invocation.configurationOverrides (severities established at run-time from the command line).
     * 
     */
    @JsonProperty("policies")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("Contains configurations that may potentially override both reportingDescriptor.defaultConfiguration (the tool's default severities) and invocation.configurationOverrides (severities established at run-time from the command line).")
    private Set<ToolComponent> policies = new LinkedHashSet<ToolComponent>();
    /**
     * An array of request objects cached at run level.
     * 
     */
    @JsonProperty("webRequests")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of request objects cached at run level.")
    private Set<WebRequest> webRequests = new LinkedHashSet<WebRequest>();
    /**
     * An array of response objects cached at run level.
     * 
     */
    @JsonProperty("webResponses")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of response objects cached at run level.")
    private Set<WebResponse> webResponses = new LinkedHashSet<WebResponse>();
    /**
     * Defines locations of special significance to SARIF consumers.
     * 
     */
    @JsonProperty("specialLocations")
    @JsonPropertyDescription("Defines locations of special significance to SARIF consumers.")
    private SpecialLocations specialLocations;
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
     * Describes the invocation of the analysis tool.
     * 
     */
    @JsonProperty("invocations")
    public List<Invocation> getInvocations() {
        return invocations;
    }

    /**
     * Describes the invocation of the analysis tool.
     * 
     */
    @JsonProperty("invocations")
    public void setInvocations(List<Invocation> invocations) {
        this.invocations = invocations;
    }

    /**
     * Describes how a converter transformed the output of a static analysis tool from the analysis tool's native output format into the SARIF format.
     * 
     */
    @JsonProperty("conversion")
    public Conversion getConversion() {
        return conversion;
    }

    /**
     * Describes how a converter transformed the output of a static analysis tool from the analysis tool's native output format into the SARIF format.
     * 
     */
    @JsonProperty("conversion")
    public void setConversion(Conversion conversion) {
        this.conversion = conversion;
    }

    /**
     * The language of the messages emitted into the log file during this run (expressed as an ISO 639-1 two-letter lowercase culture code) and an optional region (expressed as an ISO 3166-1 two-letter uppercase subculture code associated with a country or region). The casing is recommended but not required (in order for this data to conform to RFC5646).
     * 
     */
    @JsonProperty("language")
    public String getLanguage() {
        return language;
    }

    /**
     * The language of the messages emitted into the log file during this run (expressed as an ISO 639-1 two-letter lowercase culture code) and an optional region (expressed as an ISO 3166-1 two-letter uppercase subculture code associated with a country or region). The casing is recommended but not required (in order for this data to conform to RFC5646).
     * 
     */
    @JsonProperty("language")
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Specifies the revision in version control of the artifacts that were scanned.
     * 
     */
    @JsonProperty("versionControlProvenance")
    public Set<VersionControlDetails> getVersionControlProvenance() {
        return versionControlProvenance;
    }

    /**
     * Specifies the revision in version control of the artifacts that were scanned.
     * 
     */
    @JsonProperty("versionControlProvenance")
    public void setVersionControlProvenance(Set<VersionControlDetails> versionControlProvenance) {
        this.versionControlProvenance = versionControlProvenance;
    }

    /**
     * The artifact location specified by each uriBaseId symbol on the machine where the tool originally ran.
     * 
     */
    @JsonProperty("originalUriBaseIds")
    public OriginalUriBaseIds getOriginalUriBaseIds() {
        return originalUriBaseIds;
    }

    /**
     * The artifact location specified by each uriBaseId symbol on the machine where the tool originally ran.
     * 
     */
    @JsonProperty("originalUriBaseIds")
    public void setOriginalUriBaseIds(OriginalUriBaseIds originalUriBaseIds) {
        this.originalUriBaseIds = originalUriBaseIds;
    }

    /**
     * An array of artifact objects relevant to the run.
     * 
     */
    @JsonProperty("artifacts")
    public Set<Artifact> getArtifacts() {
        return artifacts;
    }

    /**
     * An array of artifact objects relevant to the run.
     * 
     */
    @JsonProperty("artifacts")
    public void setArtifacts(Set<Artifact> artifacts) {
        this.artifacts = artifacts;
    }

    /**
     * An array of logical locations such as namespaces, types or functions.
     * 
     */
    @JsonProperty("logicalLocations")
    public Set<LogicalLocation> getLogicalLocations() {
        return logicalLocations;
    }

    /**
     * An array of logical locations such as namespaces, types or functions.
     * 
     */
    @JsonProperty("logicalLocations")
    public void setLogicalLocations(Set<LogicalLocation> logicalLocations) {
        this.logicalLocations = logicalLocations;
    }

    /**
     * An array of zero or more unique graph objects associated with the run.
     * 
     */
    @JsonProperty("graphs")
    public Set<Graph> getGraphs() {
        return graphs;
    }

    /**
     * An array of zero or more unique graph objects associated with the run.
     * 
     */
    @JsonProperty("graphs")
    public void setGraphs(Set<Graph> graphs) {
        this.graphs = graphs;
    }

    /**
     * The set of results contained in an SARIF log. The results array can be omitted when a run is solely exporting rules metadata. It must be present (but may be empty) if a log file represents an actual scan.
     * 
     */
    @JsonProperty("results")
    public List<Result> getResults() {
        return results;
    }

    /**
     * The set of results contained in an SARIF log. The results array can be omitted when a run is solely exporting rules metadata. It must be present (but may be empty) if a log file represents an actual scan.
     * 
     */
    @JsonProperty("results")
    public void setResults(List<Result> results) {
        this.results = results;
    }

    /**
     * Information that describes a run's identity and role within an engineering system process.
     * 
     */
    @JsonProperty("automationDetails")
    public RunAutomationDetails getAutomationDetails() {
        return automationDetails;
    }

    /**
     * Information that describes a run's identity and role within an engineering system process.
     * 
     */
    @JsonProperty("automationDetails")
    public void setAutomationDetails(RunAutomationDetails automationDetails) {
        this.automationDetails = automationDetails;
    }

    /**
     * Automation details that describe the aggregate of runs to which this run belongs.
     * 
     */
    @JsonProperty("runAggregates")
    public Set<RunAutomationDetails> getRunAggregates() {
        return runAggregates;
    }

    /**
     * Automation details that describe the aggregate of runs to which this run belongs.
     * 
     */
    @JsonProperty("runAggregates")
    public void setRunAggregates(Set<RunAutomationDetails> runAggregates) {
        this.runAggregates = runAggregates;
    }

    /**
     * The 'guid' property of a previous SARIF 'run' that comprises the baseline that was used to compute result 'baselineState' properties for the run.
     * 
     */
    @JsonProperty("baselineGuid")
    public String getBaselineGuid() {
        return baselineGuid;
    }

    /**
     * The 'guid' property of a previous SARIF 'run' that comprises the baseline that was used to compute result 'baselineState' properties for the run.
     * 
     */
    @JsonProperty("baselineGuid")
    public void setBaselineGuid(String baselineGuid) {
        this.baselineGuid = baselineGuid;
    }

    /**
     * An array of strings used to replace sensitive information in a redaction-aware property.
     * 
     */
    @JsonProperty("redactionTokens")
    public Set<String> getRedactionTokens() {
        return redactionTokens;
    }

    /**
     * An array of strings used to replace sensitive information in a redaction-aware property.
     * 
     */
    @JsonProperty("redactionTokens")
    public void setRedactionTokens(Set<String> redactionTokens) {
        this.redactionTokens = redactionTokens;
    }

    /**
     * Specifies the default encoding for any artifact object that refers to a text file.
     * 
     */
    @JsonProperty("defaultEncoding")
    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    /**
     * Specifies the default encoding for any artifact object that refers to a text file.
     * 
     */
    @JsonProperty("defaultEncoding")
    public void setDefaultEncoding(String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    /**
     * Specifies the default source language for any artifact object that refers to a text file that contains source code.
     * 
     */
    @JsonProperty("defaultSourceLanguage")
    public String getDefaultSourceLanguage() {
        return defaultSourceLanguage;
    }

    /**
     * Specifies the default source language for any artifact object that refers to a text file that contains source code.
     * 
     */
    @JsonProperty("defaultSourceLanguage")
    public void setDefaultSourceLanguage(String defaultSourceLanguage) {
        this.defaultSourceLanguage = defaultSourceLanguage;
    }

    /**
     * An ordered list of character sequences that were treated as line breaks when computing region information for the run.
     * 
     */
    @JsonProperty("newlineSequences")
    public Set<String> getNewlineSequences() {
        return newlineSequences;
    }

    /**
     * An ordered list of character sequences that were treated as line breaks when computing region information for the run.
     * 
     */
    @JsonProperty("newlineSequences")
    public void setNewlineSequences(Set<String> newlineSequences) {
        this.newlineSequences = newlineSequences;
    }

    /**
     * Specifies the unit in which the tool measures columns.
     * 
     */
    @JsonProperty("columnKind")
    public Run.ColumnKind getColumnKind() {
        return columnKind;
    }

    /**
     * Specifies the unit in which the tool measures columns.
     * 
     */
    @JsonProperty("columnKind")
    public void setColumnKind(Run.ColumnKind columnKind) {
        this.columnKind = columnKind;
    }

    /**
     * References to external property files that should be inlined with the content of a root log file.
     * 
     */
    @JsonProperty("externalPropertyFileReferences")
    public ExternalPropertyFileReferences getExternalPropertyFileReferences() {
        return externalPropertyFileReferences;
    }

    /**
     * References to external property files that should be inlined with the content of a root log file.
     * 
     */
    @JsonProperty("externalPropertyFileReferences")
    public void setExternalPropertyFileReferences(ExternalPropertyFileReferences externalPropertyFileReferences) {
        this.externalPropertyFileReferences = externalPropertyFileReferences;
    }

    /**
     * An array of threadFlowLocation objects cached at run level.
     * 
     */
    @JsonProperty("threadFlowLocations")
    public Set<ThreadFlowLocation> getThreadFlowLocations() {
        return threadFlowLocations;
    }

    /**
     * An array of threadFlowLocation objects cached at run level.
     * 
     */
    @JsonProperty("threadFlowLocations")
    public void setThreadFlowLocations(Set<ThreadFlowLocation> threadFlowLocations) {
        this.threadFlowLocations = threadFlowLocations;
    }

    /**
     * An array of toolComponent objects relevant to a taxonomy in which results are categorized.
     * 
     */
    @JsonProperty("taxonomies")
    public Set<ToolComponent> getTaxonomies() {
        return taxonomies;
    }

    /**
     * An array of toolComponent objects relevant to a taxonomy in which results are categorized.
     * 
     */
    @JsonProperty("taxonomies")
    public void setTaxonomies(Set<ToolComponent> taxonomies) {
        this.taxonomies = taxonomies;
    }

    /**
     * Addresses associated with this run instance, if any.
     * 
     */
    @JsonProperty("addresses")
    public List<Address> getAddresses() {
        return addresses;
    }

    /**
     * Addresses associated with this run instance, if any.
     * 
     */
    @JsonProperty("addresses")
    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    /**
     * The set of available translations of the localized data provided by the tool.
     * 
     */
    @JsonProperty("translations")
    public Set<ToolComponent> getTranslations() {
        return translations;
    }

    /**
     * The set of available translations of the localized data provided by the tool.
     * 
     */
    @JsonProperty("translations")
    public void setTranslations(Set<ToolComponent> translations) {
        this.translations = translations;
    }

    /**
     * Contains configurations that may potentially override both reportingDescriptor.defaultConfiguration (the tool's default severities) and invocation.configurationOverrides (severities established at run-time from the command line).
     * 
     */
    @JsonProperty("policies")
    public Set<ToolComponent> getPolicies() {
        return policies;
    }

    /**
     * Contains configurations that may potentially override both reportingDescriptor.defaultConfiguration (the tool's default severities) and invocation.configurationOverrides (severities established at run-time from the command line).
     * 
     */
    @JsonProperty("policies")
    public void setPolicies(Set<ToolComponent> policies) {
        this.policies = policies;
    }

    /**
     * An array of request objects cached at run level.
     * 
     */
    @JsonProperty("webRequests")
    public Set<WebRequest> getWebRequests() {
        return webRequests;
    }

    /**
     * An array of request objects cached at run level.
     * 
     */
    @JsonProperty("webRequests")
    public void setWebRequests(Set<WebRequest> webRequests) {
        this.webRequests = webRequests;
    }

    /**
     * An array of response objects cached at run level.
     * 
     */
    @JsonProperty("webResponses")
    public Set<WebResponse> getWebResponses() {
        return webResponses;
    }

    /**
     * An array of response objects cached at run level.
     * 
     */
    @JsonProperty("webResponses")
    public void setWebResponses(Set<WebResponse> webResponses) {
        this.webResponses = webResponses;
    }

    /**
     * Defines locations of special significance to SARIF consumers.
     * 
     */
    @JsonProperty("specialLocations")
    public SpecialLocations getSpecialLocations() {
        return specialLocations;
    }

    /**
     * Defines locations of special significance to SARIF consumers.
     * 
     */
    @JsonProperty("specialLocations")
    public void setSpecialLocations(SpecialLocations specialLocations) {
        this.specialLocations = specialLocations;
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
        sb.append(Run.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("tool");
        sb.append('=');
        sb.append(((this.tool == null)?"<null>":this.tool));
        sb.append(',');
        sb.append("invocations");
        sb.append('=');
        sb.append(((this.invocations == null)?"<null>":this.invocations));
        sb.append(',');
        sb.append("conversion");
        sb.append('=');
        sb.append(((this.conversion == null)?"<null>":this.conversion));
        sb.append(',');
        sb.append("language");
        sb.append('=');
        sb.append(((this.language == null)?"<null>":this.language));
        sb.append(',');
        sb.append("versionControlProvenance");
        sb.append('=');
        sb.append(((this.versionControlProvenance == null)?"<null>":this.versionControlProvenance));
        sb.append(',');
        sb.append("originalUriBaseIds");
        sb.append('=');
        sb.append(((this.originalUriBaseIds == null)?"<null>":this.originalUriBaseIds));
        sb.append(',');
        sb.append("artifacts");
        sb.append('=');
        sb.append(((this.artifacts == null)?"<null>":this.artifacts));
        sb.append(',');
        sb.append("logicalLocations");
        sb.append('=');
        sb.append(((this.logicalLocations == null)?"<null>":this.logicalLocations));
        sb.append(',');
        sb.append("graphs");
        sb.append('=');
        sb.append(((this.graphs == null)?"<null>":this.graphs));
        sb.append(',');
        sb.append("results");
        sb.append('=');
        sb.append(((this.results == null)?"<null>":this.results));
        sb.append(',');
        sb.append("automationDetails");
        sb.append('=');
        sb.append(((this.automationDetails == null)?"<null>":this.automationDetails));
        sb.append(',');
        sb.append("runAggregates");
        sb.append('=');
        sb.append(((this.runAggregates == null)?"<null>":this.runAggregates));
        sb.append(',');
        sb.append("baselineGuid");
        sb.append('=');
        sb.append(((this.baselineGuid == null)?"<null>":this.baselineGuid));
        sb.append(',');
        sb.append("redactionTokens");
        sb.append('=');
        sb.append(((this.redactionTokens == null)?"<null>":this.redactionTokens));
        sb.append(',');
        sb.append("defaultEncoding");
        sb.append('=');
        sb.append(((this.defaultEncoding == null)?"<null>":this.defaultEncoding));
        sb.append(',');
        sb.append("defaultSourceLanguage");
        sb.append('=');
        sb.append(((this.defaultSourceLanguage == null)?"<null>":this.defaultSourceLanguage));
        sb.append(',');
        sb.append("newlineSequences");
        sb.append('=');
        sb.append(((this.newlineSequences == null)?"<null>":this.newlineSequences));
        sb.append(',');
        sb.append("columnKind");
        sb.append('=');
        sb.append(((this.columnKind == null)?"<null>":this.columnKind));
        sb.append(',');
        sb.append("externalPropertyFileReferences");
        sb.append('=');
        sb.append(((this.externalPropertyFileReferences == null)?"<null>":this.externalPropertyFileReferences));
        sb.append(',');
        sb.append("threadFlowLocations");
        sb.append('=');
        sb.append(((this.threadFlowLocations == null)?"<null>":this.threadFlowLocations));
        sb.append(',');
        sb.append("taxonomies");
        sb.append('=');
        sb.append(((this.taxonomies == null)?"<null>":this.taxonomies));
        sb.append(',');
        sb.append("addresses");
        sb.append('=');
        sb.append(((this.addresses == null)?"<null>":this.addresses));
        sb.append(',');
        sb.append("translations");
        sb.append('=');
        sb.append(((this.translations == null)?"<null>":this.translations));
        sb.append(',');
        sb.append("policies");
        sb.append('=');
        sb.append(((this.policies == null)?"<null>":this.policies));
        sb.append(',');
        sb.append("webRequests");
        sb.append('=');
        sb.append(((this.webRequests == null)?"<null>":this.webRequests));
        sb.append(',');
        sb.append("webResponses");
        sb.append('=');
        sb.append(((this.webResponses == null)?"<null>":this.webResponses));
        sb.append(',');
        sb.append("specialLocations");
        sb.append('=');
        sb.append(((this.specialLocations == null)?"<null>":this.specialLocations));
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
        result = ((result* 31)+((this.addresses == null)? 0 :this.addresses.hashCode()));
        result = ((result* 31)+((this.logicalLocations == null)? 0 :this.logicalLocations.hashCode()));
        result = ((result* 31)+((this.policies == null)? 0 :this.policies.hashCode()));
        result = ((result* 31)+((this.language == null)? 0 :this.language.hashCode()));
        result = ((result* 31)+((this.invocations == null)? 0 :this.invocations.hashCode()));
        result = ((result* 31)+((this.graphs == null)? 0 :this.graphs.hashCode()));
        result = ((result* 31)+((this.baselineGuid == null)? 0 :this.baselineGuid.hashCode()));
        result = ((result* 31)+((this.translations == null)? 0 :this.translations.hashCode()));
        result = ((result* 31)+((this.newlineSequences == null)? 0 :this.newlineSequences.hashCode()));
        result = ((result* 31)+((this.webResponses == null)? 0 :this.webResponses.hashCode()));
        result = ((result* 31)+((this.externalPropertyFileReferences == null)? 0 :this.externalPropertyFileReferences.hashCode()));
        result = ((result* 31)+((this.defaultSourceLanguage == null)? 0 :this.defaultSourceLanguage.hashCode()));
        result = ((result* 31)+((this.webRequests == null)? 0 :this.webRequests.hashCode()));
        result = ((result* 31)+((this.results == null)? 0 :this.results.hashCode()));
        result = ((result* 31)+((this.automationDetails == null)? 0 :this.automationDetails.hashCode()));
        result = ((result* 31)+((this.conversion == null)? 0 :this.conversion.hashCode()));
        result = ((result* 31)+((this.artifacts == null)? 0 :this.artifacts.hashCode()));
        result = ((result* 31)+((this.originalUriBaseIds == null)? 0 :this.originalUriBaseIds.hashCode()));
        result = ((result* 31)+((this.specialLocations == null)? 0 :this.specialLocations.hashCode()));
        result = ((result* 31)+((this.defaultEncoding == null)? 0 :this.defaultEncoding.hashCode()));
        result = ((result* 31)+((this.tool == null)? 0 :this.tool.hashCode()));
        result = ((result* 31)+((this.versionControlProvenance == null)? 0 :this.versionControlProvenance.hashCode()));
        result = ((result* 31)+((this.runAggregates == null)? 0 :this.runAggregates.hashCode()));
        result = ((result* 31)+((this.redactionTokens == null)? 0 :this.redactionTokens.hashCode()));
        result = ((result* 31)+((this.taxonomies == null)? 0 :this.taxonomies.hashCode()));
        result = ((result* 31)+((this.columnKind == null)? 0 :this.columnKind.hashCode()));
        result = ((result* 31)+((this.threadFlowLocations == null)? 0 :this.threadFlowLocations.hashCode()));
        result = ((result* 31)+((this.properties == null)? 0 :this.properties.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Run) == false) {
            return false;
        }
        Run rhs = ((Run) other);
        return (((((((((((((((((((((((((((((this.addresses == rhs.addresses)||((this.addresses!= null)&&this.addresses.equals(rhs.addresses)))&&((this.logicalLocations == rhs.logicalLocations)||((this.logicalLocations!= null)&&this.logicalLocations.equals(rhs.logicalLocations))))&&((this.policies == rhs.policies)||((this.policies!= null)&&this.policies.equals(rhs.policies))))&&((this.language == rhs.language)||((this.language!= null)&&this.language.equals(rhs.language))))&&((this.invocations == rhs.invocations)||((this.invocations!= null)&&this.invocations.equals(rhs.invocations))))&&((this.graphs == rhs.graphs)||((this.graphs!= null)&&this.graphs.equals(rhs.graphs))))&&((this.baselineGuid == rhs.baselineGuid)||((this.baselineGuid!= null)&&this.baselineGuid.equals(rhs.baselineGuid))))&&((this.translations == rhs.translations)||((this.translations!= null)&&this.translations.equals(rhs.translations))))&&((this.newlineSequences == rhs.newlineSequences)||((this.newlineSequences!= null)&&this.newlineSequences.equals(rhs.newlineSequences))))&&((this.webResponses == rhs.webResponses)||((this.webResponses!= null)&&this.webResponses.equals(rhs.webResponses))))&&((this.externalPropertyFileReferences == rhs.externalPropertyFileReferences)||((this.externalPropertyFileReferences!= null)&&this.externalPropertyFileReferences.equals(rhs.externalPropertyFileReferences))))&&((this.defaultSourceLanguage == rhs.defaultSourceLanguage)||((this.defaultSourceLanguage!= null)&&this.defaultSourceLanguage.equals(rhs.defaultSourceLanguage))))&&((this.webRequests == rhs.webRequests)||((this.webRequests!= null)&&this.webRequests.equals(rhs.webRequests))))&&((this.results == rhs.results)||((this.results!= null)&&this.results.equals(rhs.results))))&&((this.automationDetails == rhs.automationDetails)||((this.automationDetails!= null)&&this.automationDetails.equals(rhs.automationDetails))))&&((this.conversion == rhs.conversion)||((this.conversion!= null)&&this.conversion.equals(rhs.conversion))))&&((this.artifacts == rhs.artifacts)||((this.artifacts!= null)&&this.artifacts.equals(rhs.artifacts))))&&((this.originalUriBaseIds == rhs.originalUriBaseIds)||((this.originalUriBaseIds!= null)&&this.originalUriBaseIds.equals(rhs.originalUriBaseIds))))&&((this.specialLocations == rhs.specialLocations)||((this.specialLocations!= null)&&this.specialLocations.equals(rhs.specialLocations))))&&((this.defaultEncoding == rhs.defaultEncoding)||((this.defaultEncoding!= null)&&this.defaultEncoding.equals(rhs.defaultEncoding))))&&((this.tool == rhs.tool)||((this.tool!= null)&&this.tool.equals(rhs.tool))))&&((this.versionControlProvenance == rhs.versionControlProvenance)||((this.versionControlProvenance!= null)&&this.versionControlProvenance.equals(rhs.versionControlProvenance))))&&((this.runAggregates == rhs.runAggregates)||((this.runAggregates!= null)&&this.runAggregates.equals(rhs.runAggregates))))&&((this.redactionTokens == rhs.redactionTokens)||((this.redactionTokens!= null)&&this.redactionTokens.equals(rhs.redactionTokens))))&&((this.taxonomies == rhs.taxonomies)||((this.taxonomies!= null)&&this.taxonomies.equals(rhs.taxonomies))))&&((this.columnKind == rhs.columnKind)||((this.columnKind!= null)&&this.columnKind.equals(rhs.columnKind))))&&((this.threadFlowLocations == rhs.threadFlowLocations)||((this.threadFlowLocations!= null)&&this.threadFlowLocations.equals(rhs.threadFlowLocations))))&&((this.properties == rhs.properties)||((this.properties!= null)&&this.properties.equals(rhs.properties))));
    }


    /**
     * Specifies the unit in which the tool measures columns.
     * 
     */
    @Generated("jsonschema2pojo")
    public enum ColumnKind {

        UTF_16_CODE_UNITS("utf16CodeUnits"),
        UNICODE_CODE_POINTS("unicodeCodePoints");
        private final String value;
        private final static Map<String, Run.ColumnKind> CONSTANTS = new HashMap<String, Run.ColumnKind>();

        static {
            for (Run.ColumnKind c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private ColumnKind(String value) {
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
        public static Run.ColumnKind fromValue(String value) {
            Run.ColumnKind constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
