
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
 * A result produced by an analysis tool.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "ruleId",
    "ruleIndex",
    "rule",
    "kind",
    "level",
    "message",
    "analysisTarget",
    "locations",
    "guid",
    "correlationGuid",
    "occurrenceCount",
    "partialFingerprints",
    "fingerprints",
    "stacks",
    "codeFlows",
    "graphs",
    "graphTraversals",
    "relatedLocations",
    "suppressions",
    "baselineState",
    "rank",
    "attachments",
    "hostedViewerUri",
    "workItemUris",
    "provenance",
    "fixes",
    "taxa",
    "webRequest",
    "webResponse",
    "properties"
})
@Generated("jsonschema2pojo")
public class Result {

    /**
     * The stable, unique identifier of the rule, if any, to which this notification is relevant. This member can be used to retrieve rule metadata from the rules dictionary, if it exists.
     * 
     */
    @JsonProperty("ruleId")
    @JsonPropertyDescription("The stable, unique identifier of the rule, if any, to which this notification is relevant. This member can be used to retrieve rule metadata from the rules dictionary, if it exists.")
    private String ruleId;
    /**
     * The index within the tool component rules array of the rule object associated with this result.
     * 
     */
    @JsonProperty("ruleIndex")
    @JsonPropertyDescription("The index within the tool component rules array of the rule object associated with this result.")
    private Integer ruleIndex = -1;
    /**
     * Information about how to locate a relevant reporting descriptor.
     * 
     */
    @JsonProperty("rule")
    @JsonPropertyDescription("Information about how to locate a relevant reporting descriptor.")
    private ReportingDescriptorReference rule;
    /**
     * A value that categorizes results by evaluation state.
     * 
     */
    @JsonProperty("kind")
    @JsonPropertyDescription("A value that categorizes results by evaluation state.")
    private Result.Kind kind = Result.Kind.fromValue("fail");
    /**
     * A value specifying the severity level of the result.
     * 
     */
    @JsonProperty("level")
    @JsonPropertyDescription("A value specifying the severity level of the result.")
    private Result.Level level = Result.Level.fromValue("warning");
    /**
     * Encapsulates a message intended to be read by the end user.
     * (Required)
     * 
     */
    @JsonProperty("message")
    @JsonPropertyDescription("Encapsulates a message intended to be read by the end user.")
    private Message message;
    /**
     * Specifies the location of an artifact.
     * 
     */
    @JsonProperty("analysisTarget")
    @JsonPropertyDescription("Specifies the location of an artifact.")
    private ArtifactLocation analysisTarget;
    /**
     * The set of locations where the result was detected. Specify only one location unless the problem indicated by the result can only be corrected by making a change at every specified location.
     * 
     */
    @JsonProperty("locations")
    @JsonPropertyDescription("The set of locations where the result was detected. Specify only one location unless the problem indicated by the result can only be corrected by making a change at every specified location.")
    private List<Location> locations = new ArrayList<Location>();
    /**
     * A stable, unique identifier for the result in the form of a GUID.
     * 
     */
    @JsonProperty("guid")
    @JsonPropertyDescription("A stable, unique identifier for the result in the form of a GUID.")
    private String guid;
    /**
     * A stable, unique identifier for the equivalence class of logically identical results to which this result belongs, in the form of a GUID.
     * 
     */
    @JsonProperty("correlationGuid")
    @JsonPropertyDescription("A stable, unique identifier for the equivalence class of logically identical results to which this result belongs, in the form of a GUID.")
    private String correlationGuid;
    /**
     * A positive integer specifying the number of times this logically unique result was observed in this run.
     * 
     */
    @JsonProperty("occurrenceCount")
    @JsonPropertyDescription("A positive integer specifying the number of times this logically unique result was observed in this run.")
    private Integer occurrenceCount;
    /**
     * A set of strings that contribute to the stable, unique identity of the result.
     * 
     */
    @JsonProperty("partialFingerprints")
    @JsonPropertyDescription("A set of strings that contribute to the stable, unique identity of the result.")
    private PartialFingerprints partialFingerprints;
    /**
     * A set of strings each of which individually defines a stable, unique identity for the result.
     * 
     */
    @JsonProperty("fingerprints")
    @JsonPropertyDescription("A set of strings each of which individually defines a stable, unique identity for the result.")
    private Fingerprints fingerprints;
    /**
     * An array of 'stack' objects relevant to the result.
     * 
     */
    @JsonProperty("stacks")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of 'stack' objects relevant to the result.")
    private Set<Stack> stacks = new LinkedHashSet<Stack>();
    /**
     * An array of 'codeFlow' objects relevant to the result.
     * 
     */
    @JsonProperty("codeFlows")
    @JsonPropertyDescription("An array of 'codeFlow' objects relevant to the result.")
    private List<CodeFlow> codeFlows = new ArrayList<CodeFlow>();
    /**
     * An array of zero or more unique graph objects associated with the result.
     * 
     */
    @JsonProperty("graphs")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of zero or more unique graph objects associated with the result.")
    private Set<Graph> graphs = new LinkedHashSet<Graph>();
    /**
     * An array of one or more unique 'graphTraversal' objects.
     * 
     */
    @JsonProperty("graphTraversals")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of one or more unique 'graphTraversal' objects.")
    private Set<GraphTraversal> graphTraversals = new LinkedHashSet<GraphTraversal>();
    /**
     * A set of locations relevant to this result.
     * 
     */
    @JsonProperty("relatedLocations")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("A set of locations relevant to this result.")
    private Set<Location> relatedLocations = new LinkedHashSet<Location>();
    /**
     * A set of suppressions relevant to this result.
     * 
     */
    @JsonProperty("suppressions")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("A set of suppressions relevant to this result.")
    private Set<Suppression> suppressions = new LinkedHashSet<Suppression>();
    /**
     * The state of a result relative to a baseline of a previous run.
     * 
     */
    @JsonProperty("baselineState")
    @JsonPropertyDescription("The state of a result relative to a baseline of a previous run.")
    private Result.BaselineState baselineState;
    /**
     * A number representing the priority or importance of the result.
     * 
     */
    @JsonProperty("rank")
    @JsonPropertyDescription("A number representing the priority or importance of the result.")
    private Double rank = -1.0D;
    /**
     * A set of artifacts relevant to the result.
     * 
     */
    @JsonProperty("attachments")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("A set of artifacts relevant to the result.")
    private Set<Attachment> attachments = new LinkedHashSet<Attachment>();
    /**
     * An absolute URI at which the result can be viewed.
     * 
     */
    @JsonProperty("hostedViewerUri")
    @JsonPropertyDescription("An absolute URI at which the result can be viewed.")
    private URI hostedViewerUri;
    /**
     * The URIs of the work items associated with this result.
     * 
     */
    @JsonProperty("workItemUris")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("The URIs of the work items associated with this result.")
    private Set<URI> workItemUris = new LinkedHashSet<URI>();
    /**
     * Contains information about how and when a result was detected.
     * 
     */
    @JsonProperty("provenance")
    @JsonPropertyDescription("Contains information about how and when a result was detected.")
    private ResultProvenance provenance;
    /**
     * An array of 'fix' objects, each of which represents a proposed fix to the problem indicated by the result.
     * 
     */
    @JsonProperty("fixes")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of 'fix' objects, each of which represents a proposed fix to the problem indicated by the result.")
    private Set<Fix> fixes = new LinkedHashSet<Fix>();
    /**
     * An array of references to taxonomy reporting descriptors that are applicable to the result.
     * 
     */
    @JsonProperty("taxa")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of references to taxonomy reporting descriptors that are applicable to the result.")
    private Set<ReportingDescriptorReference> taxa = new LinkedHashSet<ReportingDescriptorReference>();
    /**
     * Describes an HTTP request.
     * 
     */
    @JsonProperty("webRequest")
    @JsonPropertyDescription("Describes an HTTP request.")
    private WebRequest webRequest;
    /**
     * Describes the response to an HTTP request.
     * 
     */
    @JsonProperty("webResponse")
    @JsonPropertyDescription("Describes the response to an HTTP request.")
    private WebResponse webResponse;
    /**
     * Key/value pairs that provide additional information about the object.
     * 
     */
    @JsonProperty("properties")
    @JsonPropertyDescription("Key/value pairs that provide additional information about the object.")
    private PropertyBag properties;

    /**
     * The stable, unique identifier of the rule, if any, to which this notification is relevant. This member can be used to retrieve rule metadata from the rules dictionary, if it exists.
     * 
     */
    @JsonProperty("ruleId")
    public String getRuleId() {
        return ruleId;
    }

    /**
     * The stable, unique identifier of the rule, if any, to which this notification is relevant. This member can be used to retrieve rule metadata from the rules dictionary, if it exists.
     * 
     */
    @JsonProperty("ruleId")
    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    /**
     * The index within the tool component rules array of the rule object associated with this result.
     * 
     */
    @JsonProperty("ruleIndex")
    public Integer getRuleIndex() {
        return ruleIndex;
    }

    /**
     * The index within the tool component rules array of the rule object associated with this result.
     * 
     */
    @JsonProperty("ruleIndex")
    public void setRuleIndex(Integer ruleIndex) {
        this.ruleIndex = ruleIndex;
    }

    /**
     * Information about how to locate a relevant reporting descriptor.
     * 
     */
    @JsonProperty("rule")
    public ReportingDescriptorReference getRule() {
        return rule;
    }

    /**
     * Information about how to locate a relevant reporting descriptor.
     * 
     */
    @JsonProperty("rule")
    public void setRule(ReportingDescriptorReference rule) {
        this.rule = rule;
    }

    /**
     * A value that categorizes results by evaluation state.
     * 
     */
    @JsonProperty("kind")
    public Result.Kind getKind() {
        return kind;
    }

    /**
     * A value that categorizes results by evaluation state.
     * 
     */
    @JsonProperty("kind")
    public void setKind(Result.Kind kind) {
        this.kind = kind;
    }

    /**
     * A value specifying the severity level of the result.
     * 
     */
    @JsonProperty("level")
    public Result.Level getLevel() {
        return level;
    }

    /**
     * A value specifying the severity level of the result.
     * 
     */
    @JsonProperty("level")
    public void setLevel(Result.Level level) {
        this.level = level;
    }

    /**
     * Encapsulates a message intended to be read by the end user.
     * (Required)
     * 
     */
    @JsonProperty("message")
    public Message getMessage() {
        return message;
    }

    /**
     * Encapsulates a message intended to be read by the end user.
     * (Required)
     * 
     */
    @JsonProperty("message")
    public void setMessage(Message message) {
        this.message = message;
    }

    /**
     * Specifies the location of an artifact.
     * 
     */
    @JsonProperty("analysisTarget")
    public ArtifactLocation getAnalysisTarget() {
        return analysisTarget;
    }

    /**
     * Specifies the location of an artifact.
     * 
     */
    @JsonProperty("analysisTarget")
    public void setAnalysisTarget(ArtifactLocation analysisTarget) {
        this.analysisTarget = analysisTarget;
    }

    /**
     * The set of locations where the result was detected. Specify only one location unless the problem indicated by the result can only be corrected by making a change at every specified location.
     * 
     */
    @JsonProperty("locations")
    public List<Location> getLocations() {
        return locations;
    }

    /**
     * The set of locations where the result was detected. Specify only one location unless the problem indicated by the result can only be corrected by making a change at every specified location.
     * 
     */
    @JsonProperty("locations")
    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    /**
     * A stable, unique identifier for the result in the form of a GUID.
     * 
     */
    @JsonProperty("guid")
    public String getGuid() {
        return guid;
    }

    /**
     * A stable, unique identifier for the result in the form of a GUID.
     * 
     */
    @JsonProperty("guid")
    public void setGuid(String guid) {
        this.guid = guid;
    }

    /**
     * A stable, unique identifier for the equivalence class of logically identical results to which this result belongs, in the form of a GUID.
     * 
     */
    @JsonProperty("correlationGuid")
    public String getCorrelationGuid() {
        return correlationGuid;
    }

    /**
     * A stable, unique identifier for the equivalence class of logically identical results to which this result belongs, in the form of a GUID.
     * 
     */
    @JsonProperty("correlationGuid")
    public void setCorrelationGuid(String correlationGuid) {
        this.correlationGuid = correlationGuid;
    }

    /**
     * A positive integer specifying the number of times this logically unique result was observed in this run.
     * 
     */
    @JsonProperty("occurrenceCount")
    public Integer getOccurrenceCount() {
        return occurrenceCount;
    }

    /**
     * A positive integer specifying the number of times this logically unique result was observed in this run.
     * 
     */
    @JsonProperty("occurrenceCount")
    public void setOccurrenceCount(Integer occurrenceCount) {
        this.occurrenceCount = occurrenceCount;
    }

    /**
     * A set of strings that contribute to the stable, unique identity of the result.
     * 
     */
    @JsonProperty("partialFingerprints")
    public PartialFingerprints getPartialFingerprints() {
        return partialFingerprints;
    }

    /**
     * A set of strings that contribute to the stable, unique identity of the result.
     * 
     */
    @JsonProperty("partialFingerprints")
    public void setPartialFingerprints(PartialFingerprints partialFingerprints) {
        this.partialFingerprints = partialFingerprints;
    }

    /**
     * A set of strings each of which individually defines a stable, unique identity for the result.
     * 
     */
    @JsonProperty("fingerprints")
    public Fingerprints getFingerprints() {
        return fingerprints;
    }

    /**
     * A set of strings each of which individually defines a stable, unique identity for the result.
     * 
     */
    @JsonProperty("fingerprints")
    public void setFingerprints(Fingerprints fingerprints) {
        this.fingerprints = fingerprints;
    }

    /**
     * An array of 'stack' objects relevant to the result.
     * 
     */
    @JsonProperty("stacks")
    public Set<Stack> getStacks() {
        return stacks;
    }

    /**
     * An array of 'stack' objects relevant to the result.
     * 
     */
    @JsonProperty("stacks")
    public void setStacks(Set<Stack> stacks) {
        this.stacks = stacks;
    }

    /**
     * An array of 'codeFlow' objects relevant to the result.
     * 
     */
    @JsonProperty("codeFlows")
    public List<CodeFlow> getCodeFlows() {
        return codeFlows;
    }

    /**
     * An array of 'codeFlow' objects relevant to the result.
     * 
     */
    @JsonProperty("codeFlows")
    public void setCodeFlows(List<CodeFlow> codeFlows) {
        this.codeFlows = codeFlows;
    }

    /**
     * An array of zero or more unique graph objects associated with the result.
     * 
     */
    @JsonProperty("graphs")
    public Set<Graph> getGraphs() {
        return graphs;
    }

    /**
     * An array of zero or more unique graph objects associated with the result.
     * 
     */
    @JsonProperty("graphs")
    public void setGraphs(Set<Graph> graphs) {
        this.graphs = graphs;
    }

    /**
     * An array of one or more unique 'graphTraversal' objects.
     * 
     */
    @JsonProperty("graphTraversals")
    public Set<GraphTraversal> getGraphTraversals() {
        return graphTraversals;
    }

    /**
     * An array of one or more unique 'graphTraversal' objects.
     * 
     */
    @JsonProperty("graphTraversals")
    public void setGraphTraversals(Set<GraphTraversal> graphTraversals) {
        this.graphTraversals = graphTraversals;
    }

    /**
     * A set of locations relevant to this result.
     * 
     */
    @JsonProperty("relatedLocations")
    public Set<Location> getRelatedLocations() {
        return relatedLocations;
    }

    /**
     * A set of locations relevant to this result.
     * 
     */
    @JsonProperty("relatedLocations")
    public void setRelatedLocations(Set<Location> relatedLocations) {
        this.relatedLocations = relatedLocations;
    }

    /**
     * A set of suppressions relevant to this result.
     * 
     */
    @JsonProperty("suppressions")
    public Set<Suppression> getSuppressions() {
        return suppressions;
    }

    /**
     * A set of suppressions relevant to this result.
     * 
     */
    @JsonProperty("suppressions")
    public void setSuppressions(Set<Suppression> suppressions) {
        this.suppressions = suppressions;
    }

    /**
     * The state of a result relative to a baseline of a previous run.
     * 
     */
    @JsonProperty("baselineState")
    public Result.BaselineState getBaselineState() {
        return baselineState;
    }

    /**
     * The state of a result relative to a baseline of a previous run.
     * 
     */
    @JsonProperty("baselineState")
    public void setBaselineState(Result.BaselineState baselineState) {
        this.baselineState = baselineState;
    }

    /**
     * A number representing the priority or importance of the result.
     * 
     */
    @JsonProperty("rank")
    public Double getRank() {
        return rank;
    }

    /**
     * A number representing the priority or importance of the result.
     * 
     */
    @JsonProperty("rank")
    public void setRank(Double rank) {
        this.rank = rank;
    }

    /**
     * A set of artifacts relevant to the result.
     * 
     */
    @JsonProperty("attachments")
    public Set<Attachment> getAttachments() {
        return attachments;
    }

    /**
     * A set of artifacts relevant to the result.
     * 
     */
    @JsonProperty("attachments")
    public void setAttachments(Set<Attachment> attachments) {
        this.attachments = attachments;
    }

    /**
     * An absolute URI at which the result can be viewed.
     * 
     */
    @JsonProperty("hostedViewerUri")
    public URI getHostedViewerUri() {
        return hostedViewerUri;
    }

    /**
     * An absolute URI at which the result can be viewed.
     * 
     */
    @JsonProperty("hostedViewerUri")
    public void setHostedViewerUri(URI hostedViewerUri) {
        this.hostedViewerUri = hostedViewerUri;
    }

    /**
     * The URIs of the work items associated with this result.
     * 
     */
    @JsonProperty("workItemUris")
    public Set<URI> getWorkItemUris() {
        return workItemUris;
    }

    /**
     * The URIs of the work items associated with this result.
     * 
     */
    @JsonProperty("workItemUris")
    public void setWorkItemUris(Set<URI> workItemUris) {
        this.workItemUris = workItemUris;
    }

    /**
     * Contains information about how and when a result was detected.
     * 
     */
    @JsonProperty("provenance")
    public ResultProvenance getProvenance() {
        return provenance;
    }

    /**
     * Contains information about how and when a result was detected.
     * 
     */
    @JsonProperty("provenance")
    public void setProvenance(ResultProvenance provenance) {
        this.provenance = provenance;
    }

    /**
     * An array of 'fix' objects, each of which represents a proposed fix to the problem indicated by the result.
     * 
     */
    @JsonProperty("fixes")
    public Set<Fix> getFixes() {
        return fixes;
    }

    /**
     * An array of 'fix' objects, each of which represents a proposed fix to the problem indicated by the result.
     * 
     */
    @JsonProperty("fixes")
    public void setFixes(Set<Fix> fixes) {
        this.fixes = fixes;
    }

    /**
     * An array of references to taxonomy reporting descriptors that are applicable to the result.
     * 
     */
    @JsonProperty("taxa")
    public Set<ReportingDescriptorReference> getTaxa() {
        return taxa;
    }

    /**
     * An array of references to taxonomy reporting descriptors that are applicable to the result.
     * 
     */
    @JsonProperty("taxa")
    public void setTaxa(Set<ReportingDescriptorReference> taxa) {
        this.taxa = taxa;
    }

    /**
     * Describes an HTTP request.
     * 
     */
    @JsonProperty("webRequest")
    public WebRequest getWebRequest() {
        return webRequest;
    }

    /**
     * Describes an HTTP request.
     * 
     */
    @JsonProperty("webRequest")
    public void setWebRequest(WebRequest webRequest) {
        this.webRequest = webRequest;
    }

    /**
     * Describes the response to an HTTP request.
     * 
     */
    @JsonProperty("webResponse")
    public WebResponse getWebResponse() {
        return webResponse;
    }

    /**
     * Describes the response to an HTTP request.
     * 
     */
    @JsonProperty("webResponse")
    public void setWebResponse(WebResponse webResponse) {
        this.webResponse = webResponse;
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
        sb.append(Result.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("ruleId");
        sb.append('=');
        sb.append(((this.ruleId == null)?"<null>":this.ruleId));
        sb.append(',');
        sb.append("ruleIndex");
        sb.append('=');
        sb.append(((this.ruleIndex == null)?"<null>":this.ruleIndex));
        sb.append(',');
        sb.append("rule");
        sb.append('=');
        sb.append(((this.rule == null)?"<null>":this.rule));
        sb.append(',');
        sb.append("kind");
        sb.append('=');
        sb.append(((this.kind == null)?"<null>":this.kind));
        sb.append(',');
        sb.append("level");
        sb.append('=');
        sb.append(((this.level == null)?"<null>":this.level));
        sb.append(',');
        sb.append("message");
        sb.append('=');
        sb.append(((this.message == null)?"<null>":this.message));
        sb.append(',');
        sb.append("analysisTarget");
        sb.append('=');
        sb.append(((this.analysisTarget == null)?"<null>":this.analysisTarget));
        sb.append(',');
        sb.append("locations");
        sb.append('=');
        sb.append(((this.locations == null)?"<null>":this.locations));
        sb.append(',');
        sb.append("guid");
        sb.append('=');
        sb.append(((this.guid == null)?"<null>":this.guid));
        sb.append(',');
        sb.append("correlationGuid");
        sb.append('=');
        sb.append(((this.correlationGuid == null)?"<null>":this.correlationGuid));
        sb.append(',');
        sb.append("occurrenceCount");
        sb.append('=');
        sb.append(((this.occurrenceCount == null)?"<null>":this.occurrenceCount));
        sb.append(',');
        sb.append("partialFingerprints");
        sb.append('=');
        sb.append(((this.partialFingerprints == null)?"<null>":this.partialFingerprints));
        sb.append(',');
        sb.append("fingerprints");
        sb.append('=');
        sb.append(((this.fingerprints == null)?"<null>":this.fingerprints));
        sb.append(',');
        sb.append("stacks");
        sb.append('=');
        sb.append(((this.stacks == null)?"<null>":this.stacks));
        sb.append(',');
        sb.append("codeFlows");
        sb.append('=');
        sb.append(((this.codeFlows == null)?"<null>":this.codeFlows));
        sb.append(',');
        sb.append("graphs");
        sb.append('=');
        sb.append(((this.graphs == null)?"<null>":this.graphs));
        sb.append(',');
        sb.append("graphTraversals");
        sb.append('=');
        sb.append(((this.graphTraversals == null)?"<null>":this.graphTraversals));
        sb.append(',');
        sb.append("relatedLocations");
        sb.append('=');
        sb.append(((this.relatedLocations == null)?"<null>":this.relatedLocations));
        sb.append(',');
        sb.append("suppressions");
        sb.append('=');
        sb.append(((this.suppressions == null)?"<null>":this.suppressions));
        sb.append(',');
        sb.append("baselineState");
        sb.append('=');
        sb.append(((this.baselineState == null)?"<null>":this.baselineState));
        sb.append(',');
        sb.append("rank");
        sb.append('=');
        sb.append(((this.rank == null)?"<null>":this.rank));
        sb.append(',');
        sb.append("attachments");
        sb.append('=');
        sb.append(((this.attachments == null)?"<null>":this.attachments));
        sb.append(',');
        sb.append("hostedViewerUri");
        sb.append('=');
        sb.append(((this.hostedViewerUri == null)?"<null>":this.hostedViewerUri));
        sb.append(',');
        sb.append("workItemUris");
        sb.append('=');
        sb.append(((this.workItemUris == null)?"<null>":this.workItemUris));
        sb.append(',');
        sb.append("provenance");
        sb.append('=');
        sb.append(((this.provenance == null)?"<null>":this.provenance));
        sb.append(',');
        sb.append("fixes");
        sb.append('=');
        sb.append(((this.fixes == null)?"<null>":this.fixes));
        sb.append(',');
        sb.append("taxa");
        sb.append('=');
        sb.append(((this.taxa == null)?"<null>":this.taxa));
        sb.append(',');
        sb.append("webRequest");
        sb.append('=');
        sb.append(((this.webRequest == null)?"<null>":this.webRequest));
        sb.append(',');
        sb.append("webResponse");
        sb.append('=');
        sb.append(((this.webResponse == null)?"<null>":this.webResponse));
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
        result = ((result* 31)+((this.attachments == null)? 0 :this.attachments.hashCode()));
        result = ((result* 31)+((this.correlationGuid == null)? 0 :this.correlationGuid.hashCode()));
        result = ((result* 31)+((this.webRequest == null)? 0 :this.webRequest.hashCode()));
        result = ((result* 31)+((this.graphTraversals == null)? 0 :this.graphTraversals.hashCode()));
        result = ((result* 31)+((this.rule == null)? 0 :this.rule.hashCode()));
        result = ((result* 31)+((this.analysisTarget == null)? 0 :this.analysisTarget.hashCode()));
        result = ((result* 31)+((this.fixes == null)? 0 :this.fixes.hashCode()));
        result = ((result* 31)+((this.relatedLocations == null)? 0 :this.relatedLocations.hashCode()));
        result = ((result* 31)+((this.graphs == null)? 0 :this.graphs.hashCode()));
        result = ((result* 31)+((this.provenance == null)? 0 :this.provenance.hashCode()));
        result = ((result* 31)+((this.rank == null)? 0 :this.rank.hashCode()));
        result = ((result* 31)+((this.ruleId == null)? 0 :this.ruleId.hashCode()));
        result = ((result* 31)+((this.taxa == null)? 0 :this.taxa.hashCode()));
        result = ((result* 31)+((this.ruleIndex == null)? 0 :this.ruleIndex.hashCode()));
        result = ((result* 31)+((this.suppressions == null)? 0 :this.suppressions.hashCode()));
        result = ((result* 31)+((this.level == null)? 0 :this.level.hashCode()));
        result = ((result* 31)+((this.hostedViewerUri == null)? 0 :this.hostedViewerUri.hashCode()));
        result = ((result* 31)+((this.kind == null)? 0 :this.kind.hashCode()));
        result = ((result* 31)+((this.stacks == null)? 0 :this.stacks.hashCode()));
        result = ((result* 31)+((this.occurrenceCount == null)? 0 :this.occurrenceCount.hashCode()));
        result = ((result* 31)+((this.message == null)? 0 :this.message.hashCode()));
        result = ((result* 31)+((this.fingerprints == null)? 0 :this.fingerprints.hashCode()));
        result = ((result* 31)+((this.codeFlows == null)? 0 :this.codeFlows.hashCode()));
        result = ((result* 31)+((this.guid == null)? 0 :this.guid.hashCode()));
        result = ((result* 31)+((this.partialFingerprints == null)? 0 :this.partialFingerprints.hashCode()));
        result = ((result* 31)+((this.webResponse == null)? 0 :this.webResponse.hashCode()));
        result = ((result* 31)+((this.locations == null)? 0 :this.locations.hashCode()));
        result = ((result* 31)+((this.baselineState == null)? 0 :this.baselineState.hashCode()));
        result = ((result* 31)+((this.workItemUris == null)? 0 :this.workItemUris.hashCode()));
        result = ((result* 31)+((this.properties == null)? 0 :this.properties.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Result) == false) {
            return false;
        }
        Result rhs = ((Result) other);
        return (((((((((((((((((((((((((((((((this.attachments == rhs.attachments)||((this.attachments!= null)&&this.attachments.equals(rhs.attachments)))&&((this.correlationGuid == rhs.correlationGuid)||((this.correlationGuid!= null)&&this.correlationGuid.equals(rhs.correlationGuid))))&&((this.webRequest == rhs.webRequest)||((this.webRequest!= null)&&this.webRequest.equals(rhs.webRequest))))&&((this.graphTraversals == rhs.graphTraversals)||((this.graphTraversals!= null)&&this.graphTraversals.equals(rhs.graphTraversals))))&&((this.rule == rhs.rule)||((this.rule!= null)&&this.rule.equals(rhs.rule))))&&((this.analysisTarget == rhs.analysisTarget)||((this.analysisTarget!= null)&&this.analysisTarget.equals(rhs.analysisTarget))))&&((this.fixes == rhs.fixes)||((this.fixes!= null)&&this.fixes.equals(rhs.fixes))))&&((this.relatedLocations == rhs.relatedLocations)||((this.relatedLocations!= null)&&this.relatedLocations.equals(rhs.relatedLocations))))&&((this.graphs == rhs.graphs)||((this.graphs!= null)&&this.graphs.equals(rhs.graphs))))&&((this.provenance == rhs.provenance)||((this.provenance!= null)&&this.provenance.equals(rhs.provenance))))&&((this.rank == rhs.rank)||((this.rank!= null)&&this.rank.equals(rhs.rank))))&&((this.ruleId == rhs.ruleId)||((this.ruleId!= null)&&this.ruleId.equals(rhs.ruleId))))&&((this.taxa == rhs.taxa)||((this.taxa!= null)&&this.taxa.equals(rhs.taxa))))&&((this.ruleIndex == rhs.ruleIndex)||((this.ruleIndex!= null)&&this.ruleIndex.equals(rhs.ruleIndex))))&&((this.suppressions == rhs.suppressions)||((this.suppressions!= null)&&this.suppressions.equals(rhs.suppressions))))&&((this.level == rhs.level)||((this.level!= null)&&this.level.equals(rhs.level))))&&((this.hostedViewerUri == rhs.hostedViewerUri)||((this.hostedViewerUri!= null)&&this.hostedViewerUri.equals(rhs.hostedViewerUri))))&&((this.kind == rhs.kind)||((this.kind!= null)&&this.kind.equals(rhs.kind))))&&((this.stacks == rhs.stacks)||((this.stacks!= null)&&this.stacks.equals(rhs.stacks))))&&((this.occurrenceCount == rhs.occurrenceCount)||((this.occurrenceCount!= null)&&this.occurrenceCount.equals(rhs.occurrenceCount))))&&((this.message == rhs.message)||((this.message!= null)&&this.message.equals(rhs.message))))&&((this.fingerprints == rhs.fingerprints)||((this.fingerprints!= null)&&this.fingerprints.equals(rhs.fingerprints))))&&((this.codeFlows == rhs.codeFlows)||((this.codeFlows!= null)&&this.codeFlows.equals(rhs.codeFlows))))&&((this.guid == rhs.guid)||((this.guid!= null)&&this.guid.equals(rhs.guid))))&&((this.partialFingerprints == rhs.partialFingerprints)||((this.partialFingerprints!= null)&&this.partialFingerprints.equals(rhs.partialFingerprints))))&&((this.webResponse == rhs.webResponse)||((this.webResponse!= null)&&this.webResponse.equals(rhs.webResponse))))&&((this.locations == rhs.locations)||((this.locations!= null)&&this.locations.equals(rhs.locations))))&&((this.baselineState == rhs.baselineState)||((this.baselineState!= null)&&this.baselineState.equals(rhs.baselineState))))&&((this.workItemUris == rhs.workItemUris)||((this.workItemUris!= null)&&this.workItemUris.equals(rhs.workItemUris))))&&((this.properties == rhs.properties)||((this.properties!= null)&&this.properties.equals(rhs.properties))));
    }


    /**
     * The state of a result relative to a baseline of a previous run.
     * 
     */
    @Generated("jsonschema2pojo")
    public enum BaselineState {

        NEW("new"),
        UNCHANGED("unchanged"),
        UPDATED("updated"),
        ABSENT("absent");
        private final String value;
        private final static Map<String, Result.BaselineState> CONSTANTS = new HashMap<String, Result.BaselineState>();

        static {
            for (Result.BaselineState c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private BaselineState(String value) {
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
        public static Result.BaselineState fromValue(String value) {
            Result.BaselineState constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * A value that categorizes results by evaluation state.
     * 
     */
    @Generated("jsonschema2pojo")
    public enum Kind {

        NOT_APPLICABLE("notApplicable"),
        PASS("pass"),
        FAIL("fail"),
        REVIEW("review"),
        OPEN("open"),
        INFORMATIONAL("informational");
        private final String value;
        private final static Map<String, Result.Kind> CONSTANTS = new HashMap<String, Result.Kind>();

        static {
            for (Result.Kind c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private Kind(String value) {
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
        public static Result.Kind fromValue(String value) {
            Result.Kind constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * A value specifying the severity level of the result.
     * 
     */
    @Generated("jsonschema2pojo")
    public enum Level {

        NONE("none"),
        NOTE("note"),
        WARNING("warning"),
        ERROR("error");
        private final String value;
        private final static Map<String, Result.Level> CONSTANTS = new HashMap<String, Result.Level>();

        static {
            for (Result.Level c: values()) {
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
        public static Result.Level fromValue(String value) {
            Result.Level constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
