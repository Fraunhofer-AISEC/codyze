
package de.fraunhofer.aisec.codyze.analysis.generated;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;


/**
 * A component, such as a plug-in or the driver, of the analysis tool that was run.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "guid",
    "name",
    "organization",
    "product",
    "productSuite",
    "shortDescription",
    "fullDescription",
    "fullName",
    "version",
    "semanticVersion",
    "dottedQuadFileVersion",
    "releaseDateUtc",
    "downloadUri",
    "informationUri",
    "globalMessageStrings",
    "notifications",
    "rules",
    "taxa",
    "locations",
    "language",
    "contents",
    "isComprehensive",
    "localizedDataSemanticVersion",
    "minimumRequiredLocalizedDataSemanticVersion",
    "associatedComponent",
    "translationMetadata",
    "supportedTaxonomies",
    "properties"
})
@Generated("jsonschema2pojo")
public class ToolComponent {

    /**
     * A unique identifier for the tool component in the form of a GUID.
     * 
     */
    @JsonProperty("guid")
    @JsonPropertyDescription("A unique identifier for the tool component in the form of a GUID.")
    private String guid;
    /**
     * The name of the tool component.
     * (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("The name of the tool component.")
    private String name;
    /**
     * The organization or company that produced the tool component.
     * 
     */
    @JsonProperty("organization")
    @JsonPropertyDescription("The organization or company that produced the tool component.")
    private String organization;
    /**
     * A product suite to which the tool component belongs.
     * 
     */
    @JsonProperty("product")
    @JsonPropertyDescription("A product suite to which the tool component belongs.")
    private String product;
    /**
     * A localizable string containing the name of the suite of products to which the tool component belongs.
     * 
     */
    @JsonProperty("productSuite")
    @JsonPropertyDescription("A localizable string containing the name of the suite of products to which the tool component belongs.")
    private String productSuite;
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
     * The name of the tool component along with its version and any other useful identifying information, such as its locale.
     * 
     */
    @JsonProperty("fullName")
    @JsonPropertyDescription("The name of the tool component along with its version and any other useful identifying information, such as its locale.")
    private String fullName;
    /**
     * The tool component version, in whatever format the component natively provides.
     * 
     */
    @JsonProperty("version")
    @JsonPropertyDescription("The tool component version, in whatever format the component natively provides.")
    private String version;
    /**
     * The tool component version in the format specified by Semantic Versioning 2.0.
     * 
     */
    @JsonProperty("semanticVersion")
    @JsonPropertyDescription("The tool component version in the format specified by Semantic Versioning 2.0.")
    private String semanticVersion;
    /**
     * The binary version of the tool component's primary executable file expressed as four non-negative integers separated by a period (for operating systems that express file versions in this way).
     * 
     */
    @JsonProperty("dottedQuadFileVersion")
    @JsonPropertyDescription("The binary version of the tool component's primary executable file expressed as four non-negative integers separated by a period (for operating systems that express file versions in this way).")
    private String dottedQuadFileVersion;
    /**
     * A string specifying the UTC date (and optionally, the time) of the component's release.
     * 
     */
    @JsonProperty("releaseDateUtc")
    @JsonPropertyDescription("A string specifying the UTC date (and optionally, the time) of the component's release.")
    private String releaseDateUtc;
    /**
     * The absolute URI from which the tool component can be downloaded.
     * 
     */
    @JsonProperty("downloadUri")
    @JsonPropertyDescription("The absolute URI from which the tool component can be downloaded.")
    private URI downloadUri;
    /**
     * The absolute URI at which information about this version of the tool component can be found.
     * 
     */
    @JsonProperty("informationUri")
    @JsonPropertyDescription("The absolute URI at which information about this version of the tool component can be found.")
    private URI informationUri;
    /**
     * A dictionary, each of whose keys is a resource identifier and each of whose values is a multiformatMessageString object, which holds message strings in plain text and (optionally) Markdown format. The strings can include placeholders, which can be used to construct a message in combination with an arbitrary number of additional string arguments.
     * 
     */
    @JsonProperty("globalMessageStrings")
    @JsonPropertyDescription("A dictionary, each of whose keys is a resource identifier and each of whose values is a multiformatMessageString object, which holds message strings in plain text and (optionally) Markdown format. The strings can include placeholders, which can be used to construct a message in combination with an arbitrary number of additional string arguments.")
    private GlobalMessageStrings globalMessageStrings;
    /**
     * An array of reportingDescriptor objects relevant to the notifications related to the configuration and runtime execution of the tool component.
     * 
     */
    @JsonProperty("notifications")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of reportingDescriptor objects relevant to the notifications related to the configuration and runtime execution of the tool component.")
    private Set<ReportingDescriptor> notifications = new LinkedHashSet<ReportingDescriptor>();
    /**
     * An array of reportingDescriptor objects relevant to the analysis performed by the tool component.
     * 
     */
    @JsonProperty("rules")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of reportingDescriptor objects relevant to the analysis performed by the tool component.")
    private Set<ReportingDescriptor> rules = new LinkedHashSet<ReportingDescriptor>();
    /**
     * An array of reportingDescriptor objects relevant to the definitions of both standalone and tool-defined taxonomies.
     * 
     */
    @JsonProperty("taxa")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of reportingDescriptor objects relevant to the definitions of both standalone and tool-defined taxonomies.")
    private Set<ReportingDescriptor> taxa = new LinkedHashSet<ReportingDescriptor>();
    /**
     * An array of the artifactLocation objects associated with the tool component.
     * 
     */
    @JsonProperty("locations")
    @JsonPropertyDescription("An array of the artifactLocation objects associated with the tool component.")
    private List<ArtifactLocation> locations = new ArrayList<ArtifactLocation>();
    /**
     * The language of the messages emitted into the log file during this run (expressed as an ISO 639-1 two-letter lowercase language code) and an optional region (expressed as an ISO 3166-1 two-letter uppercase subculture code associated with a country or region). The casing is recommended but not required (in order for this data to conform to RFC5646).
     * 
     */
    @JsonProperty("language")
    @JsonPropertyDescription("The language of the messages emitted into the log file during this run (expressed as an ISO 639-1 two-letter lowercase language code) and an optional region (expressed as an ISO 3166-1 two-letter uppercase subculture code associated with a country or region). The casing is recommended but not required (in order for this data to conform to RFC5646).")
    private String language = "en-US";
    /**
     * The kinds of data contained in this object.
     * 
     */
    @JsonProperty("contents")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("The kinds of data contained in this object.")
    private Set<Content> contents = new LinkedHashSet<Content>(Arrays.asList(Content.fromValue("localizedData"), Content.fromValue("nonLocalizedData")));
    /**
     * Specifies whether this object contains a complete definition of the localizable and/or non-localizable data for this component, as opposed to including only data that is relevant to the results persisted to this log file.
     * 
     */
    @JsonProperty("isComprehensive")
    @JsonPropertyDescription("Specifies whether this object contains a complete definition of the localizable and/or non-localizable data for this component, as opposed to including only data that is relevant to the results persisted to this log file.")
    private Boolean isComprehensive = false;
    /**
     * The semantic version of the localized strings defined in this component; maintained by components that provide translations.
     * 
     */
    @JsonProperty("localizedDataSemanticVersion")
    @JsonPropertyDescription("The semantic version of the localized strings defined in this component; maintained by components that provide translations.")
    private String localizedDataSemanticVersion;
    /**
     * The minimum value of localizedDataSemanticVersion required in translations consumed by this component; used by components that consume translations.
     * 
     */
    @JsonProperty("minimumRequiredLocalizedDataSemanticVersion")
    @JsonPropertyDescription("The minimum value of localizedDataSemanticVersion required in translations consumed by this component; used by components that consume translations.")
    private String minimumRequiredLocalizedDataSemanticVersion;
    /**
     * Identifies a particular toolComponent object, either the driver or an extension.
     * 
     */
    @JsonProperty("associatedComponent")
    @JsonPropertyDescription("Identifies a particular toolComponent object, either the driver or an extension.")
    private ToolComponentReference associatedComponent;
    /**
     * Provides additional metadata related to translation.
     * 
     */
    @JsonProperty("translationMetadata")
    @JsonPropertyDescription("Provides additional metadata related to translation.")
    private TranslationMetadata translationMetadata;
    /**
     * An array of toolComponentReference objects to declare the taxonomies supported by the tool component.
     * 
     */
    @JsonProperty("supportedTaxonomies")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of toolComponentReference objects to declare the taxonomies supported by the tool component.")
    private Set<ToolComponentReference> supportedTaxonomies = new LinkedHashSet<ToolComponentReference>();
    /**
     * Key/value pairs that provide additional information about the object.
     * 
     */
    @JsonProperty("properties")
    @JsonPropertyDescription("Key/value pairs that provide additional information about the object.")
    private PropertyBag properties;

    /**
     * A unique identifier for the tool component in the form of a GUID.
     * 
     */
    @JsonProperty("guid")
    public String getGuid() {
        return guid;
    }

    /**
     * A unique identifier for the tool component in the form of a GUID.
     * 
     */
    @JsonProperty("guid")
    public void setGuid(String guid) {
        this.guid = guid;
    }

    /**
     * The name of the tool component.
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * The name of the tool component.
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The organization or company that produced the tool component.
     * 
     */
    @JsonProperty("organization")
    public String getOrganization() {
        return organization;
    }

    /**
     * The organization or company that produced the tool component.
     * 
     */
    @JsonProperty("organization")
    public void setOrganization(String organization) {
        this.organization = organization;
    }

    /**
     * A product suite to which the tool component belongs.
     * 
     */
    @JsonProperty("product")
    public String getProduct() {
        return product;
    }

    /**
     * A product suite to which the tool component belongs.
     * 
     */
    @JsonProperty("product")
    public void setProduct(String product) {
        this.product = product;
    }

    /**
     * A localizable string containing the name of the suite of products to which the tool component belongs.
     * 
     */
    @JsonProperty("productSuite")
    public String getProductSuite() {
        return productSuite;
    }

    /**
     * A localizable string containing the name of the suite of products to which the tool component belongs.
     * 
     */
    @JsonProperty("productSuite")
    public void setProductSuite(String productSuite) {
        this.productSuite = productSuite;
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
     * The name of the tool component along with its version and any other useful identifying information, such as its locale.
     * 
     */
    @JsonProperty("fullName")
    public String getFullName() {
        return fullName;
    }

    /**
     * The name of the tool component along with its version and any other useful identifying information, such as its locale.
     * 
     */
    @JsonProperty("fullName")
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * The tool component version, in whatever format the component natively provides.
     * 
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /**
     * The tool component version, in whatever format the component natively provides.
     * 
     */
    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * The tool component version in the format specified by Semantic Versioning 2.0.
     * 
     */
    @JsonProperty("semanticVersion")
    public String getSemanticVersion() {
        return semanticVersion;
    }

    /**
     * The tool component version in the format specified by Semantic Versioning 2.0.
     * 
     */
    @JsonProperty("semanticVersion")
    public void setSemanticVersion(String semanticVersion) {
        this.semanticVersion = semanticVersion;
    }

    /**
     * The binary version of the tool component's primary executable file expressed as four non-negative integers separated by a period (for operating systems that express file versions in this way).
     * 
     */
    @JsonProperty("dottedQuadFileVersion")
    public String getDottedQuadFileVersion() {
        return dottedQuadFileVersion;
    }

    /**
     * The binary version of the tool component's primary executable file expressed as four non-negative integers separated by a period (for operating systems that express file versions in this way).
     * 
     */
    @JsonProperty("dottedQuadFileVersion")
    public void setDottedQuadFileVersion(String dottedQuadFileVersion) {
        this.dottedQuadFileVersion = dottedQuadFileVersion;
    }

    /**
     * A string specifying the UTC date (and optionally, the time) of the component's release.
     * 
     */
    @JsonProperty("releaseDateUtc")
    public String getReleaseDateUtc() {
        return releaseDateUtc;
    }

    /**
     * A string specifying the UTC date (and optionally, the time) of the component's release.
     * 
     */
    @JsonProperty("releaseDateUtc")
    public void setReleaseDateUtc(String releaseDateUtc) {
        this.releaseDateUtc = releaseDateUtc;
    }

    /**
     * The absolute URI from which the tool component can be downloaded.
     * 
     */
    @JsonProperty("downloadUri")
    public URI getDownloadUri() {
        return downloadUri;
    }

    /**
     * The absolute URI from which the tool component can be downloaded.
     * 
     */
    @JsonProperty("downloadUri")
    public void setDownloadUri(URI downloadUri) {
        this.downloadUri = downloadUri;
    }

    /**
     * The absolute URI at which information about this version of the tool component can be found.
     * 
     */
    @JsonProperty("informationUri")
    public URI getInformationUri() {
        return informationUri;
    }

    /**
     * The absolute URI at which information about this version of the tool component can be found.
     * 
     */
    @JsonProperty("informationUri")
    public void setInformationUri(URI informationUri) {
        this.informationUri = informationUri;
    }

    /**
     * A dictionary, each of whose keys is a resource identifier and each of whose values is a multiformatMessageString object, which holds message strings in plain text and (optionally) Markdown format. The strings can include placeholders, which can be used to construct a message in combination with an arbitrary number of additional string arguments.
     * 
     */
    @JsonProperty("globalMessageStrings")
    public GlobalMessageStrings getGlobalMessageStrings() {
        return globalMessageStrings;
    }

    /**
     * A dictionary, each of whose keys is a resource identifier and each of whose values is a multiformatMessageString object, which holds message strings in plain text and (optionally) Markdown format. The strings can include placeholders, which can be used to construct a message in combination with an arbitrary number of additional string arguments.
     * 
     */
    @JsonProperty("globalMessageStrings")
    public void setGlobalMessageStrings(GlobalMessageStrings globalMessageStrings) {
        this.globalMessageStrings = globalMessageStrings;
    }

    /**
     * An array of reportingDescriptor objects relevant to the notifications related to the configuration and runtime execution of the tool component.
     * 
     */
    @JsonProperty("notifications")
    public Set<ReportingDescriptor> getNotifications() {
        return notifications;
    }

    /**
     * An array of reportingDescriptor objects relevant to the notifications related to the configuration and runtime execution of the tool component.
     * 
     */
    @JsonProperty("notifications")
    public void setNotifications(Set<ReportingDescriptor> notifications) {
        this.notifications = notifications;
    }

    /**
     * An array of reportingDescriptor objects relevant to the analysis performed by the tool component.
     * 
     */
    @JsonProperty("rules")
    public Set<ReportingDescriptor> getRules() {
        return rules;
    }

    /**
     * An array of reportingDescriptor objects relevant to the analysis performed by the tool component.
     * 
     */
    @JsonProperty("rules")
    public void setRules(Set<ReportingDescriptor> rules) {
        this.rules = rules;
    }

    /**
     * An array of reportingDescriptor objects relevant to the definitions of both standalone and tool-defined taxonomies.
     * 
     */
    @JsonProperty("taxa")
    public Set<ReportingDescriptor> getTaxa() {
        return taxa;
    }

    /**
     * An array of reportingDescriptor objects relevant to the definitions of both standalone and tool-defined taxonomies.
     * 
     */
    @JsonProperty("taxa")
    public void setTaxa(Set<ReportingDescriptor> taxa) {
        this.taxa = taxa;
    }

    /**
     * An array of the artifactLocation objects associated with the tool component.
     * 
     */
    @JsonProperty("locations")
    public List<ArtifactLocation> getLocations() {
        return locations;
    }

    /**
     * An array of the artifactLocation objects associated with the tool component.
     * 
     */
    @JsonProperty("locations")
    public void setLocations(List<ArtifactLocation> locations) {
        this.locations = locations;
    }

    /**
     * The language of the messages emitted into the log file during this run (expressed as an ISO 639-1 two-letter lowercase language code) and an optional region (expressed as an ISO 3166-1 two-letter uppercase subculture code associated with a country or region). The casing is recommended but not required (in order for this data to conform to RFC5646).
     * 
     */
    @JsonProperty("language")
    public String getLanguage() {
        return language;
    }

    /**
     * The language of the messages emitted into the log file during this run (expressed as an ISO 639-1 two-letter lowercase language code) and an optional region (expressed as an ISO 3166-1 two-letter uppercase subculture code associated with a country or region). The casing is recommended but not required (in order for this data to conform to RFC5646).
     * 
     */
    @JsonProperty("language")
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * The kinds of data contained in this object.
     * 
     */
    @JsonProperty("contents")
    public Set<Content> getContents() {
        return contents;
    }

    /**
     * The kinds of data contained in this object.
     * 
     */
    @JsonProperty("contents")
    public void setContents(Set<Content> contents) {
        this.contents = contents;
    }

    /**
     * Specifies whether this object contains a complete definition of the localizable and/or non-localizable data for this component, as opposed to including only data that is relevant to the results persisted to this log file.
     * 
     */
    @JsonProperty("isComprehensive")
    public Boolean getIsComprehensive() {
        return isComprehensive;
    }

    /**
     * Specifies whether this object contains a complete definition of the localizable and/or non-localizable data for this component, as opposed to including only data that is relevant to the results persisted to this log file.
     * 
     */
    @JsonProperty("isComprehensive")
    public void setIsComprehensive(Boolean isComprehensive) {
        this.isComprehensive = isComprehensive;
    }

    /**
     * The semantic version of the localized strings defined in this component; maintained by components that provide translations.
     * 
     */
    @JsonProperty("localizedDataSemanticVersion")
    public String getLocalizedDataSemanticVersion() {
        return localizedDataSemanticVersion;
    }

    /**
     * The semantic version of the localized strings defined in this component; maintained by components that provide translations.
     * 
     */
    @JsonProperty("localizedDataSemanticVersion")
    public void setLocalizedDataSemanticVersion(String localizedDataSemanticVersion) {
        this.localizedDataSemanticVersion = localizedDataSemanticVersion;
    }

    /**
     * The minimum value of localizedDataSemanticVersion required in translations consumed by this component; used by components that consume translations.
     * 
     */
    @JsonProperty("minimumRequiredLocalizedDataSemanticVersion")
    public String getMinimumRequiredLocalizedDataSemanticVersion() {
        return minimumRequiredLocalizedDataSemanticVersion;
    }

    /**
     * The minimum value of localizedDataSemanticVersion required in translations consumed by this component; used by components that consume translations.
     * 
     */
    @JsonProperty("minimumRequiredLocalizedDataSemanticVersion")
    public void setMinimumRequiredLocalizedDataSemanticVersion(String minimumRequiredLocalizedDataSemanticVersion) {
        this.minimumRequiredLocalizedDataSemanticVersion = minimumRequiredLocalizedDataSemanticVersion;
    }

    /**
     * Identifies a particular toolComponent object, either the driver or an extension.
     * 
     */
    @JsonProperty("associatedComponent")
    public ToolComponentReference getAssociatedComponent() {
        return associatedComponent;
    }

    /**
     * Identifies a particular toolComponent object, either the driver or an extension.
     * 
     */
    @JsonProperty("associatedComponent")
    public void setAssociatedComponent(ToolComponentReference associatedComponent) {
        this.associatedComponent = associatedComponent;
    }

    /**
     * Provides additional metadata related to translation.
     * 
     */
    @JsonProperty("translationMetadata")
    public TranslationMetadata getTranslationMetadata() {
        return translationMetadata;
    }

    /**
     * Provides additional metadata related to translation.
     * 
     */
    @JsonProperty("translationMetadata")
    public void setTranslationMetadata(TranslationMetadata translationMetadata) {
        this.translationMetadata = translationMetadata;
    }

    /**
     * An array of toolComponentReference objects to declare the taxonomies supported by the tool component.
     * 
     */
    @JsonProperty("supportedTaxonomies")
    public Set<ToolComponentReference> getSupportedTaxonomies() {
        return supportedTaxonomies;
    }

    /**
     * An array of toolComponentReference objects to declare the taxonomies supported by the tool component.
     * 
     */
    @JsonProperty("supportedTaxonomies")
    public void setSupportedTaxonomies(Set<ToolComponentReference> supportedTaxonomies) {
        this.supportedTaxonomies = supportedTaxonomies;
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
        sb.append(ToolComponent.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("guid");
        sb.append('=');
        sb.append(((this.guid == null)?"<null>":this.guid));
        sb.append(',');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null)?"<null>":this.name));
        sb.append(',');
        sb.append("organization");
        sb.append('=');
        sb.append(((this.organization == null)?"<null>":this.organization));
        sb.append(',');
        sb.append("product");
        sb.append('=');
        sb.append(((this.product == null)?"<null>":this.product));
        sb.append(',');
        sb.append("productSuite");
        sb.append('=');
        sb.append(((this.productSuite == null)?"<null>":this.productSuite));
        sb.append(',');
        sb.append("shortDescription");
        sb.append('=');
        sb.append(((this.shortDescription == null)?"<null>":this.shortDescription));
        sb.append(',');
        sb.append("fullDescription");
        sb.append('=');
        sb.append(((this.fullDescription == null)?"<null>":this.fullDescription));
        sb.append(',');
        sb.append("fullName");
        sb.append('=');
        sb.append(((this.fullName == null)?"<null>":this.fullName));
        sb.append(',');
        sb.append("version");
        sb.append('=');
        sb.append(((this.version == null)?"<null>":this.version));
        sb.append(',');
        sb.append("semanticVersion");
        sb.append('=');
        sb.append(((this.semanticVersion == null)?"<null>":this.semanticVersion));
        sb.append(',');
        sb.append("dottedQuadFileVersion");
        sb.append('=');
        sb.append(((this.dottedQuadFileVersion == null)?"<null>":this.dottedQuadFileVersion));
        sb.append(',');
        sb.append("releaseDateUtc");
        sb.append('=');
        sb.append(((this.releaseDateUtc == null)?"<null>":this.releaseDateUtc));
        sb.append(',');
        sb.append("downloadUri");
        sb.append('=');
        sb.append(((this.downloadUri == null)?"<null>":this.downloadUri));
        sb.append(',');
        sb.append("informationUri");
        sb.append('=');
        sb.append(((this.informationUri == null)?"<null>":this.informationUri));
        sb.append(',');
        sb.append("globalMessageStrings");
        sb.append('=');
        sb.append(((this.globalMessageStrings == null)?"<null>":this.globalMessageStrings));
        sb.append(',');
        sb.append("notifications");
        sb.append('=');
        sb.append(((this.notifications == null)?"<null>":this.notifications));
        sb.append(',');
        sb.append("rules");
        sb.append('=');
        sb.append(((this.rules == null)?"<null>":this.rules));
        sb.append(',');
        sb.append("taxa");
        sb.append('=');
        sb.append(((this.taxa == null)?"<null>":this.taxa));
        sb.append(',');
        sb.append("locations");
        sb.append('=');
        sb.append(((this.locations == null)?"<null>":this.locations));
        sb.append(',');
        sb.append("language");
        sb.append('=');
        sb.append(((this.language == null)?"<null>":this.language));
        sb.append(',');
        sb.append("contents");
        sb.append('=');
        sb.append(((this.contents == null)?"<null>":this.contents));
        sb.append(',');
        sb.append("isComprehensive");
        sb.append('=');
        sb.append(((this.isComprehensive == null)?"<null>":this.isComprehensive));
        sb.append(',');
        sb.append("localizedDataSemanticVersion");
        sb.append('=');
        sb.append(((this.localizedDataSemanticVersion == null)?"<null>":this.localizedDataSemanticVersion));
        sb.append(',');
        sb.append("minimumRequiredLocalizedDataSemanticVersion");
        sb.append('=');
        sb.append(((this.minimumRequiredLocalizedDataSemanticVersion == null)?"<null>":this.minimumRequiredLocalizedDataSemanticVersion));
        sb.append(',');
        sb.append("associatedComponent");
        sb.append('=');
        sb.append(((this.associatedComponent == null)?"<null>":this.associatedComponent));
        sb.append(',');
        sb.append("translationMetadata");
        sb.append('=');
        sb.append(((this.translationMetadata == null)?"<null>":this.translationMetadata));
        sb.append(',');
        sb.append("supportedTaxonomies");
        sb.append('=');
        sb.append(((this.supportedTaxonomies == null)?"<null>":this.supportedTaxonomies));
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
        result = ((result* 31)+((this.releaseDateUtc == null)? 0 :this.releaseDateUtc.hashCode()));
        result = ((result* 31)+((this.rules == null)? 0 :this.rules.hashCode()));
        result = ((result* 31)+((this.language == null)? 0 :this.language.hashCode()));
        result = ((result* 31)+((this.downloadUri == null)? 0 :this.downloadUri.hashCode()));
        result = ((result* 31)+((this.supportedTaxonomies == null)? 0 :this.supportedTaxonomies.hashCode()));
        result = ((result* 31)+((this.fullDescription == null)? 0 :this.fullDescription.hashCode()));
        result = ((result* 31)+((this.informationUri == null)? 0 :this.informationUri.hashCode()));
        result = ((result* 31)+((this.associatedComponent == null)? 0 :this.associatedComponent.hashCode()));
        result = ((result* 31)+((this.translationMetadata == null)? 0 :this.translationMetadata.hashCode()));
        result = ((result* 31)+((this.productSuite == null)? 0 :this.productSuite.hashCode()));
        result = ((result* 31)+((this.taxa == null)? 0 :this.taxa.hashCode()));
        result = ((result* 31)+((this.product == null)? 0 :this.product.hashCode()));
        result = ((result* 31)+((this.isComprehensive == null)? 0 :this.isComprehensive.hashCode()));
        result = ((result* 31)+((this.minimumRequiredLocalizedDataSemanticVersion == null)? 0 :this.minimumRequiredLocalizedDataSemanticVersion.hashCode()));
        result = ((result* 31)+((this.fullName == null)? 0 :this.fullName.hashCode()));
        result = ((result* 31)+((this.shortDescription == null)? 0 :this.shortDescription.hashCode()));
        result = ((result* 31)+((this.version == null)? 0 :this.version.hashCode()));
        result = ((result* 31)+((this.globalMessageStrings == null)? 0 :this.globalMessageStrings.hashCode()));
        result = ((result* 31)+((this.localizedDataSemanticVersion == null)? 0 :this.localizedDataSemanticVersion.hashCode()));
        result = ((result* 31)+((this.dottedQuadFileVersion == null)? 0 :this.dottedQuadFileVersion.hashCode()));
        result = ((result* 31)+((this.contents == null)? 0 :this.contents.hashCode()));
        result = ((result* 31)+((this.organization == null)? 0 :this.organization.hashCode()));
        result = ((result* 31)+((this.name == null)? 0 :this.name.hashCode()));
        result = ((result* 31)+((this.semanticVersion == null)? 0 :this.semanticVersion.hashCode()));
        result = ((result* 31)+((this.guid == null)? 0 :this.guid.hashCode()));
        result = ((result* 31)+((this.locations == null)? 0 :this.locations.hashCode()));
        result = ((result* 31)+((this.notifications == null)? 0 :this.notifications.hashCode()));
        result = ((result* 31)+((this.properties == null)? 0 :this.properties.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ToolComponent) == false) {
            return false;
        }
        ToolComponent rhs = ((ToolComponent) other);
        return (((((((((((((((((((((((((((((this.releaseDateUtc == rhs.releaseDateUtc)||((this.releaseDateUtc!= null)&&this.releaseDateUtc.equals(rhs.releaseDateUtc)))&&((this.rules == rhs.rules)||((this.rules!= null)&&this.rules.equals(rhs.rules))))&&((this.language == rhs.language)||((this.language!= null)&&this.language.equals(rhs.language))))&&((this.downloadUri == rhs.downloadUri)||((this.downloadUri!= null)&&this.downloadUri.equals(rhs.downloadUri))))&&((this.supportedTaxonomies == rhs.supportedTaxonomies)||((this.supportedTaxonomies!= null)&&this.supportedTaxonomies.equals(rhs.supportedTaxonomies))))&&((this.fullDescription == rhs.fullDescription)||((this.fullDescription!= null)&&this.fullDescription.equals(rhs.fullDescription))))&&((this.informationUri == rhs.informationUri)||((this.informationUri!= null)&&this.informationUri.equals(rhs.informationUri))))&&((this.associatedComponent == rhs.associatedComponent)||((this.associatedComponent!= null)&&this.associatedComponent.equals(rhs.associatedComponent))))&&((this.translationMetadata == rhs.translationMetadata)||((this.translationMetadata!= null)&&this.translationMetadata.equals(rhs.translationMetadata))))&&((this.productSuite == rhs.productSuite)||((this.productSuite!= null)&&this.productSuite.equals(rhs.productSuite))))&&((this.taxa == rhs.taxa)||((this.taxa!= null)&&this.taxa.equals(rhs.taxa))))&&((this.product == rhs.product)||((this.product!= null)&&this.product.equals(rhs.product))))&&((this.isComprehensive == rhs.isComprehensive)||((this.isComprehensive!= null)&&this.isComprehensive.equals(rhs.isComprehensive))))&&((this.minimumRequiredLocalizedDataSemanticVersion == rhs.minimumRequiredLocalizedDataSemanticVersion)||((this.minimumRequiredLocalizedDataSemanticVersion!= null)&&this.minimumRequiredLocalizedDataSemanticVersion.equals(rhs.minimumRequiredLocalizedDataSemanticVersion))))&&((this.fullName == rhs.fullName)||((this.fullName!= null)&&this.fullName.equals(rhs.fullName))))&&((this.shortDescription == rhs.shortDescription)||((this.shortDescription!= null)&&this.shortDescription.equals(rhs.shortDescription))))&&((this.version == rhs.version)||((this.version!= null)&&this.version.equals(rhs.version))))&&((this.globalMessageStrings == rhs.globalMessageStrings)||((this.globalMessageStrings!= null)&&this.globalMessageStrings.equals(rhs.globalMessageStrings))))&&((this.localizedDataSemanticVersion == rhs.localizedDataSemanticVersion)||((this.localizedDataSemanticVersion!= null)&&this.localizedDataSemanticVersion.equals(rhs.localizedDataSemanticVersion))))&&((this.dottedQuadFileVersion == rhs.dottedQuadFileVersion)||((this.dottedQuadFileVersion!= null)&&this.dottedQuadFileVersion.equals(rhs.dottedQuadFileVersion))))&&((this.contents == rhs.contents)||((this.contents!= null)&&this.contents.equals(rhs.contents))))&&((this.organization == rhs.organization)||((this.organization!= null)&&this.organization.equals(rhs.organization))))&&((this.name == rhs.name)||((this.name!= null)&&this.name.equals(rhs.name))))&&((this.semanticVersion == rhs.semanticVersion)||((this.semanticVersion!= null)&&this.semanticVersion.equals(rhs.semanticVersion))))&&((this.guid == rhs.guid)||((this.guid!= null)&&this.guid.equals(rhs.guid))))&&((this.locations == rhs.locations)||((this.locations!= null)&&this.locations.equals(rhs.locations))))&&((this.notifications == rhs.notifications)||((this.notifications!= null)&&this.notifications.equals(rhs.notifications))))&&((this.properties == rhs.properties)||((this.properties!= null)&&this.properties.equals(rhs.properties))));
    }

}
