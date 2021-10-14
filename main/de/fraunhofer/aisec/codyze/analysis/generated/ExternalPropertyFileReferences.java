
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
 * References to external property files that should be inlined with the content of a root log file.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "conversion",
    "graphs",
    "externalizedProperties",
    "artifacts",
    "invocations",
    "logicalLocations",
    "threadFlowLocations",
    "results",
    "taxonomies",
    "addresses",
    "driver",
    "extensions",
    "policies",
    "translations",
    "webRequests",
    "webResponses",
    "properties"
})
@Generated("jsonschema2pojo")
public class ExternalPropertyFileReferences {

    /**
     * Contains information that enables a SARIF consumer to locate the external property file that contains the value of an externalized property associated with the run.
     * 
     */
    @JsonProperty("conversion")
    @JsonPropertyDescription("Contains information that enables a SARIF consumer to locate the external property file that contains the value of an externalized property associated with the run.")
    private ExternalPropertyFileReference conversion;
    /**
     * An array of external property files containing a run.graphs object to be merged with the root log file.
     * 
     */
    @JsonProperty("graphs")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of external property files containing a run.graphs object to be merged with the root log file.")
    private Set<ExternalPropertyFileReference> graphs = new LinkedHashSet<ExternalPropertyFileReference>();
    /**
     * Contains information that enables a SARIF consumer to locate the external property file that contains the value of an externalized property associated with the run.
     * 
     */
    @JsonProperty("externalizedProperties")
    @JsonPropertyDescription("Contains information that enables a SARIF consumer to locate the external property file that contains the value of an externalized property associated with the run.")
    private ExternalPropertyFileReference externalizedProperties;
    /**
     * An array of external property files containing run.artifacts arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("artifacts")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of external property files containing run.artifacts arrays to be merged with the root log file.")
    private Set<ExternalPropertyFileReference> artifacts = new LinkedHashSet<ExternalPropertyFileReference>();
    /**
     * An array of external property files containing run.invocations arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("invocations")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of external property files containing run.invocations arrays to be merged with the root log file.")
    private Set<ExternalPropertyFileReference> invocations = new LinkedHashSet<ExternalPropertyFileReference>();
    /**
     * An array of external property files containing run.logicalLocations arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("logicalLocations")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of external property files containing run.logicalLocations arrays to be merged with the root log file.")
    private Set<ExternalPropertyFileReference> logicalLocations = new LinkedHashSet<ExternalPropertyFileReference>();
    /**
     * An array of external property files containing run.threadFlowLocations arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("threadFlowLocations")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of external property files containing run.threadFlowLocations arrays to be merged with the root log file.")
    private Set<ExternalPropertyFileReference> threadFlowLocations = new LinkedHashSet<ExternalPropertyFileReference>();
    /**
     * An array of external property files containing run.results arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("results")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of external property files containing run.results arrays to be merged with the root log file.")
    private Set<ExternalPropertyFileReference> results = new LinkedHashSet<ExternalPropertyFileReference>();
    /**
     * An array of external property files containing run.taxonomies arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("taxonomies")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of external property files containing run.taxonomies arrays to be merged with the root log file.")
    private Set<ExternalPropertyFileReference> taxonomies = new LinkedHashSet<ExternalPropertyFileReference>();
    /**
     * An array of external property files containing run.addresses arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("addresses")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of external property files containing run.addresses arrays to be merged with the root log file.")
    private Set<ExternalPropertyFileReference> addresses = new LinkedHashSet<ExternalPropertyFileReference>();
    /**
     * Contains information that enables a SARIF consumer to locate the external property file that contains the value of an externalized property associated with the run.
     * 
     */
    @JsonProperty("driver")
    @JsonPropertyDescription("Contains information that enables a SARIF consumer to locate the external property file that contains the value of an externalized property associated with the run.")
    private ExternalPropertyFileReference driver;
    /**
     * An array of external property files containing run.extensions arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("extensions")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of external property files containing run.extensions arrays to be merged with the root log file.")
    private Set<ExternalPropertyFileReference> extensions = new LinkedHashSet<ExternalPropertyFileReference>();
    /**
     * An array of external property files containing run.policies arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("policies")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of external property files containing run.policies arrays to be merged with the root log file.")
    private Set<ExternalPropertyFileReference> policies = new LinkedHashSet<ExternalPropertyFileReference>();
    /**
     * An array of external property files containing run.translations arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("translations")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of external property files containing run.translations arrays to be merged with the root log file.")
    private Set<ExternalPropertyFileReference> translations = new LinkedHashSet<ExternalPropertyFileReference>();
    /**
     * An array of external property files containing run.requests arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("webRequests")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of external property files containing run.requests arrays to be merged with the root log file.")
    private Set<ExternalPropertyFileReference> webRequests = new LinkedHashSet<ExternalPropertyFileReference>();
    /**
     * An array of external property files containing run.responses arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("webResponses")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of external property files containing run.responses arrays to be merged with the root log file.")
    private Set<ExternalPropertyFileReference> webResponses = new LinkedHashSet<ExternalPropertyFileReference>();
    /**
     * Key/value pairs that provide additional information about the object.
     * 
     */
    @JsonProperty("properties")
    @JsonPropertyDescription("Key/value pairs that provide additional information about the object.")
    private PropertyBag properties;

    /**
     * Contains information that enables a SARIF consumer to locate the external property file that contains the value of an externalized property associated with the run.
     * 
     */
    @JsonProperty("conversion")
    public ExternalPropertyFileReference getConversion() {
        return conversion;
    }

    /**
     * Contains information that enables a SARIF consumer to locate the external property file that contains the value of an externalized property associated with the run.
     * 
     */
    @JsonProperty("conversion")
    public void setConversion(ExternalPropertyFileReference conversion) {
        this.conversion = conversion;
    }

    /**
     * An array of external property files containing a run.graphs object to be merged with the root log file.
     * 
     */
    @JsonProperty("graphs")
    public Set<ExternalPropertyFileReference> getGraphs() {
        return graphs;
    }

    /**
     * An array of external property files containing a run.graphs object to be merged with the root log file.
     * 
     */
    @JsonProperty("graphs")
    public void setGraphs(Set<ExternalPropertyFileReference> graphs) {
        this.graphs = graphs;
    }

    /**
     * Contains information that enables a SARIF consumer to locate the external property file that contains the value of an externalized property associated with the run.
     * 
     */
    @JsonProperty("externalizedProperties")
    public ExternalPropertyFileReference getExternalizedProperties() {
        return externalizedProperties;
    }

    /**
     * Contains information that enables a SARIF consumer to locate the external property file that contains the value of an externalized property associated with the run.
     * 
     */
    @JsonProperty("externalizedProperties")
    public void setExternalizedProperties(ExternalPropertyFileReference externalizedProperties) {
        this.externalizedProperties = externalizedProperties;
    }

    /**
     * An array of external property files containing run.artifacts arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("artifacts")
    public Set<ExternalPropertyFileReference> getArtifacts() {
        return artifacts;
    }

    /**
     * An array of external property files containing run.artifacts arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("artifacts")
    public void setArtifacts(Set<ExternalPropertyFileReference> artifacts) {
        this.artifacts = artifacts;
    }

    /**
     * An array of external property files containing run.invocations arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("invocations")
    public Set<ExternalPropertyFileReference> getInvocations() {
        return invocations;
    }

    /**
     * An array of external property files containing run.invocations arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("invocations")
    public void setInvocations(Set<ExternalPropertyFileReference> invocations) {
        this.invocations = invocations;
    }

    /**
     * An array of external property files containing run.logicalLocations arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("logicalLocations")
    public Set<ExternalPropertyFileReference> getLogicalLocations() {
        return logicalLocations;
    }

    /**
     * An array of external property files containing run.logicalLocations arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("logicalLocations")
    public void setLogicalLocations(Set<ExternalPropertyFileReference> logicalLocations) {
        this.logicalLocations = logicalLocations;
    }

    /**
     * An array of external property files containing run.threadFlowLocations arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("threadFlowLocations")
    public Set<ExternalPropertyFileReference> getThreadFlowLocations() {
        return threadFlowLocations;
    }

    /**
     * An array of external property files containing run.threadFlowLocations arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("threadFlowLocations")
    public void setThreadFlowLocations(Set<ExternalPropertyFileReference> threadFlowLocations) {
        this.threadFlowLocations = threadFlowLocations;
    }

    /**
     * An array of external property files containing run.results arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("results")
    public Set<ExternalPropertyFileReference> getResults() {
        return results;
    }

    /**
     * An array of external property files containing run.results arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("results")
    public void setResults(Set<ExternalPropertyFileReference> results) {
        this.results = results;
    }

    /**
     * An array of external property files containing run.taxonomies arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("taxonomies")
    public Set<ExternalPropertyFileReference> getTaxonomies() {
        return taxonomies;
    }

    /**
     * An array of external property files containing run.taxonomies arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("taxonomies")
    public void setTaxonomies(Set<ExternalPropertyFileReference> taxonomies) {
        this.taxonomies = taxonomies;
    }

    /**
     * An array of external property files containing run.addresses arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("addresses")
    public Set<ExternalPropertyFileReference> getAddresses() {
        return addresses;
    }

    /**
     * An array of external property files containing run.addresses arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("addresses")
    public void setAddresses(Set<ExternalPropertyFileReference> addresses) {
        this.addresses = addresses;
    }

    /**
     * Contains information that enables a SARIF consumer to locate the external property file that contains the value of an externalized property associated with the run.
     * 
     */
    @JsonProperty("driver")
    public ExternalPropertyFileReference getDriver() {
        return driver;
    }

    /**
     * Contains information that enables a SARIF consumer to locate the external property file that contains the value of an externalized property associated with the run.
     * 
     */
    @JsonProperty("driver")
    public void setDriver(ExternalPropertyFileReference driver) {
        this.driver = driver;
    }

    /**
     * An array of external property files containing run.extensions arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("extensions")
    public Set<ExternalPropertyFileReference> getExtensions() {
        return extensions;
    }

    /**
     * An array of external property files containing run.extensions arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("extensions")
    public void setExtensions(Set<ExternalPropertyFileReference> extensions) {
        this.extensions = extensions;
    }

    /**
     * An array of external property files containing run.policies arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("policies")
    public Set<ExternalPropertyFileReference> getPolicies() {
        return policies;
    }

    /**
     * An array of external property files containing run.policies arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("policies")
    public void setPolicies(Set<ExternalPropertyFileReference> policies) {
        this.policies = policies;
    }

    /**
     * An array of external property files containing run.translations arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("translations")
    public Set<ExternalPropertyFileReference> getTranslations() {
        return translations;
    }

    /**
     * An array of external property files containing run.translations arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("translations")
    public void setTranslations(Set<ExternalPropertyFileReference> translations) {
        this.translations = translations;
    }

    /**
     * An array of external property files containing run.requests arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("webRequests")
    public Set<ExternalPropertyFileReference> getWebRequests() {
        return webRequests;
    }

    /**
     * An array of external property files containing run.requests arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("webRequests")
    public void setWebRequests(Set<ExternalPropertyFileReference> webRequests) {
        this.webRequests = webRequests;
    }

    /**
     * An array of external property files containing run.responses arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("webResponses")
    public Set<ExternalPropertyFileReference> getWebResponses() {
        return webResponses;
    }

    /**
     * An array of external property files containing run.responses arrays to be merged with the root log file.
     * 
     */
    @JsonProperty("webResponses")
    public void setWebResponses(Set<ExternalPropertyFileReference> webResponses) {
        this.webResponses = webResponses;
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
        sb.append(ExternalPropertyFileReferences.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("conversion");
        sb.append('=');
        sb.append(((this.conversion == null)?"<null>":this.conversion));
        sb.append(',');
        sb.append("graphs");
        sb.append('=');
        sb.append(((this.graphs == null)?"<null>":this.graphs));
        sb.append(',');
        sb.append("externalizedProperties");
        sb.append('=');
        sb.append(((this.externalizedProperties == null)?"<null>":this.externalizedProperties));
        sb.append(',');
        sb.append("artifacts");
        sb.append('=');
        sb.append(((this.artifacts == null)?"<null>":this.artifacts));
        sb.append(',');
        sb.append("invocations");
        sb.append('=');
        sb.append(((this.invocations == null)?"<null>":this.invocations));
        sb.append(',');
        sb.append("logicalLocations");
        sb.append('=');
        sb.append(((this.logicalLocations == null)?"<null>":this.logicalLocations));
        sb.append(',');
        sb.append("threadFlowLocations");
        sb.append('=');
        sb.append(((this.threadFlowLocations == null)?"<null>":this.threadFlowLocations));
        sb.append(',');
        sb.append("results");
        sb.append('=');
        sb.append(((this.results == null)?"<null>":this.results));
        sb.append(',');
        sb.append("taxonomies");
        sb.append('=');
        sb.append(((this.taxonomies == null)?"<null>":this.taxonomies));
        sb.append(',');
        sb.append("addresses");
        sb.append('=');
        sb.append(((this.addresses == null)?"<null>":this.addresses));
        sb.append(',');
        sb.append("driver");
        sb.append('=');
        sb.append(((this.driver == null)?"<null>":this.driver));
        sb.append(',');
        sb.append("extensions");
        sb.append('=');
        sb.append(((this.extensions == null)?"<null>":this.extensions));
        sb.append(',');
        sb.append("policies");
        sb.append('=');
        sb.append(((this.policies == null)?"<null>":this.policies));
        sb.append(',');
        sb.append("translations");
        sb.append('=');
        sb.append(((this.translations == null)?"<null>":this.translations));
        sb.append(',');
        sb.append("webRequests");
        sb.append('=');
        sb.append(((this.webRequests == null)?"<null>":this.webRequests));
        sb.append(',');
        sb.append("webResponses");
        sb.append('=');
        sb.append(((this.webResponses == null)?"<null>":this.webResponses));
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
        result = ((result* 31)+((this.externalizedProperties == null)? 0 :this.externalizedProperties.hashCode()));
        result = ((result* 31)+((this.invocations == null)? 0 :this.invocations.hashCode()));
        result = ((result* 31)+((this.graphs == null)? 0 :this.graphs.hashCode()));
        result = ((result* 31)+((this.extensions == null)? 0 :this.extensions.hashCode()));
        result = ((result* 31)+((this.driver == null)? 0 :this.driver.hashCode()));
        result = ((result* 31)+((this.taxonomies == null)? 0 :this.taxonomies.hashCode()));
        result = ((result* 31)+((this.translations == null)? 0 :this.translations.hashCode()));
        result = ((result* 31)+((this.webResponses == null)? 0 :this.webResponses.hashCode()));
        result = ((result* 31)+((this.webRequests == null)? 0 :this.webRequests.hashCode()));
        result = ((result* 31)+((this.results == null)? 0 :this.results.hashCode()));
        result = ((result* 31)+((this.threadFlowLocations == null)? 0 :this.threadFlowLocations.hashCode()));
        result = ((result* 31)+((this.properties == null)? 0 :this.properties.hashCode()));
        result = ((result* 31)+((this.conversion == null)? 0 :this.conversion.hashCode()));
        result = ((result* 31)+((this.artifacts == null)? 0 :this.artifacts.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExternalPropertyFileReferences) == false) {
            return false;
        }
        ExternalPropertyFileReferences rhs = ((ExternalPropertyFileReferences) other);
        return ((((((((((((((((((this.addresses == rhs.addresses)||((this.addresses!= null)&&this.addresses.equals(rhs.addresses)))&&((this.logicalLocations == rhs.logicalLocations)||((this.logicalLocations!= null)&&this.logicalLocations.equals(rhs.logicalLocations))))&&((this.policies == rhs.policies)||((this.policies!= null)&&this.policies.equals(rhs.policies))))&&((this.externalizedProperties == rhs.externalizedProperties)||((this.externalizedProperties!= null)&&this.externalizedProperties.equals(rhs.externalizedProperties))))&&((this.invocations == rhs.invocations)||((this.invocations!= null)&&this.invocations.equals(rhs.invocations))))&&((this.graphs == rhs.graphs)||((this.graphs!= null)&&this.graphs.equals(rhs.graphs))))&&((this.extensions == rhs.extensions)||((this.extensions!= null)&&this.extensions.equals(rhs.extensions))))&&((this.driver == rhs.driver)||((this.driver!= null)&&this.driver.equals(rhs.driver))))&&((this.taxonomies == rhs.taxonomies)||((this.taxonomies!= null)&&this.taxonomies.equals(rhs.taxonomies))))&&((this.translations == rhs.translations)||((this.translations!= null)&&this.translations.equals(rhs.translations))))&&((this.webResponses == rhs.webResponses)||((this.webResponses!= null)&&this.webResponses.equals(rhs.webResponses))))&&((this.webRequests == rhs.webRequests)||((this.webRequests!= null)&&this.webRequests.equals(rhs.webRequests))))&&((this.results == rhs.results)||((this.results!= null)&&this.results.equals(rhs.results))))&&((this.threadFlowLocations == rhs.threadFlowLocations)||((this.threadFlowLocations!= null)&&this.threadFlowLocations.equals(rhs.threadFlowLocations))))&&((this.properties == rhs.properties)||((this.properties!= null)&&this.properties.equals(rhs.properties))))&&((this.conversion == rhs.conversion)||((this.conversion!= null)&&this.conversion.equals(rhs.conversion))))&&((this.artifacts == rhs.artifacts)||((this.artifacts!= null)&&this.artifacts.equals(rhs.artifacts))));
    }

}
