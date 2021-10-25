package de.fraunhofer.aisec.codyze.analysis;

import de.fraunhofer.aisec.codyze.analysis.generated.*;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

/**
 * This class was created to bundle operations regarding the Sarif Template instead of spreading them all over the code
 * e.g. The URI and name of the generated class differ between each SARIF version.
 */
public class SarifInstantiator {

    /**
     * class members definitely need to be reviewed/changed after parsing a new SARIF template
     */
    private final Sarif210Rtm4 sarif = new Sarif210Rtm4();
    //TODO: get schema from json file (first line)
    private final String schema = "http://json-schema.org/draft-04/schema#";
    //TODO: automate getting the name/version
    private final String driverName = "codyze";
    private final String version = "2.0.0-alpha4";
    private final String download = "https://github.com/Fraunhofer-AISEC/codyze";

    SarifInstantiator() {
        URI schemaURI;
        try {schemaURI = new URI(schema);} catch (URISyntaxException e) {schemaURI = null;}

        sarif.set$schema(schemaURI);

        // TODO: determine how much will be done in the constructor and what in the method calls

        URI downloadURI;
        try {downloadURI = new URI(download);} catch (URISyntaxException e) {downloadURI = null;}

        ToolComponent driver = generateToolComponent(driverName, version, downloadURI, "Fraunhofer AISEC");
        Tool tool = generateTool(driver, Set.of());
    }

    private void addRun(Run r) {
        List<Run> runs = sarif.getRuns();
        runs.add(r);
        sarif.setRuns(runs);
    }

    /**
     * generates a single run in the SARIF schema
     *
     * @param tool      the tool used for this run
     * @param artifacts the artifacts (files) analyzed OR at least those with relevant results
     * @param graphs    a Set containing the created CPG (Subject to removal)
     * @param results   the results of the analysis performed in this run (null ONLY if tool failed to start analysis)
     * @return          the resulting run
     */
    private Run generateRun(Tool tool, Set<Artifact> artifacts, Set<Graph> graphs, @Nullable List<Result> results) {
        Run run = new Run();

        run.setColumnKind(Run.ColumnKind.UNICODE_CODE_POINTS);  // TODO: check whether column is defined as utf16 or unicode character
        run.setRedactionTokens(Set.of("[REDACTED]"));

        run.setTool(tool);
        run.setArtifacts(artifacts);
        run.setGraphs(graphs);
        run.setResults(results);

        return run;
    }

    /**
     * generates a result object
     *
     * @param ruleId            the identifier of the rule that was evaluated
     * @param kind              the kind of the result (e.g. PASS, INFORMATIONAL, FAIL, ...)
     * @param level             the severity level of the result (NONE if kind is not FAIL)
     * @param message           a description of the result, shall include the following:
     *                              - analysis target and problem location
     *                              - condition that led to the problem being reported
     *                              - potential risks associated when not fixing the problem
     *                              - full range of possible responses the end user could take
     * @param locations         specifies location(s) where the result occurred.
     *                          Only more than one if condition can only be corrected by making a change at every location.
     *                          Explicitly NOT for distinct occurrences of the same result.
     * @param analysisTarget    the analysis target, only applicable analysis target and result file differ
     * @param relatedLocations  locations related to understanding the problem
     * @param attachments       artifacts relevant to the detection of the result
     * @param fixes             possible fixes for the problem
     * @return                  the resulting result object
     */
    // TODO: rules here and in the ToolComponent
    private Result generateResult(@Nullable String ruleId, Result.Kind kind, Result.Level level, Message message,
                                  List<Location> locations, @Nullable ArtifactLocation analysisTarget,
                                  // List<CodeFlow> codeFlows, GraphTraversal graphTraversal, Stack stacks
                                  Set<Location> relatedLocations, Set<Attachment> attachments, Set<Fix> fixes) {
        Result result = new Result();

        result.setRuleId(ruleId);
        result.setKind(kind);
        result.setLevel(level);
        result.setMessage(message);
        result.setLocations(locations);
        result.setAnalysisTarget(analysisTarget);
        result.setRelatedLocations(relatedLocations);
        result.setAttachments(attachments);
        result.setFixes(fixes);

        return result;
    }

    /**
     * generates an attachment object that is relevant to a result
     *
     * @param description   a message describing the role played by the attachment
     * @param location      the location of the attachment
     * @param regions       regions of interest within the attachment (should contain a message each)
     * @param rectangles    rectangles specifying an area of interest ONLY if the attachment is an image
     * @return              the resulting attachment
     */
    private Attachment generateAttachment(Message description, ArtifactLocation location, Set<Region> regions,
                                          Set<Rectangle> rectangles) {
        Attachment attachment = new Attachment();

        attachment.setDescription(description);
        attachment.setArtifactLocation(location);
        attachment.setRegions(regions);
        attachment.setRectangles(rectangles);

        return attachment;
    }

    /**
     * generates a rectangle for highlighting purposes within an image file
     *
     * @param message   a message relevant to this area of the image
     * @param top       the Y coordinate of the top edge of the rectangle, measured in the image's natural units.
     * @param left      the X coordinate of the left edge of the rectangle, measured in the image's natural units.
     * @param bottom    the Y coordinate of the bottom edge of the rectangle, measured in the image's natural units.
     * @param right     the X coordinate of the right edge of the rectangle, measured in the image's natural units.
     * @return          the resulting rectangle object
     */
    private Rectangle rectangle(Message message, Double top, Double left, Double bottom, Double right) {
        Rectangle rectangle = new Rectangle();

        rectangle.setMessage(message);
        rectangle.setTop(top);
        rectangle.setLeft(left);
        rectangle.setBottom(bottom);
        rectangle.setRight(right);

        return rectangle;
    }

    /**
     * generates a fix object composed of one or more changes
     *
     * @param description       a description for the fix
     * @param artifactChanges   one or more changes in files
     * @return                  the resulting fix
     */
    private Fix generateFix(@Nullable Message description,Set<ArtifactChange> artifactChanges) {
        Fix fix = new Fix();

        fix.setDescription(description);
        fix.setArtifactChanges(artifactChanges);

        return fix;
    }

    /**
     * generates an artifactChange object composed of an artifact location and replacement details
     *
     * @param artifactLocation  the location of the file
     * @param replacements      the changes done in the file
     * @return                  the resulting artifactChange object
     */
    private ArtifactChange generateArtifactChange(ArtifactLocation artifactLocation, List<Replacement> replacements) {
        ArtifactChange artifactChange = new ArtifactChange();

        artifactChange.setArtifactLocation(artifactLocation);
        artifactChange.setReplacements(replacements);

        return artifactChange;
    }

    /**
     * generates a replacement in a file
     *
     * @param deletedRegion     the region to delete (if the length is 0, it specifies an insertion point)
     * @param insertedContent   specifies the content to insert in place of the region specified by deleteRegion
     * @return                  the resulting replacement
     */
    private Replacement generateReplacement(Region deletedRegion, @Nullable ArtifactContent insertedContent) {
        Replacement replacement = new Replacement();

        replacement.setDeletedRegion(deletedRegion);
        replacement.setInsertedContent(insertedContent);

        return replacement;
    }

    /**
     * generates an artifactContent object
     *
     * @param text      the relevant text in UTF-8 minding any characters that JSON requires to be escaped
     * @param rendered  a rendered view of the contents
     * @return          the resulting artifactContent object
     */
    private ArtifactContent generateArtifactContent(String text, @Nullable MultiformatMessageString rendered) {
        ArtifactContent artifactContent = new ArtifactContent();

        artifactContent.setText(text);
        artifactContent.setRendered(rendered);

        return artifactContent;
    }

    /**
     * generates a multiformatMessageString grouping all available textual formats
     *
     * @param text      a plain representation of the message
     * @param markdown  the formatted message expressed in GitHub-Flavored Markdown [GFM]
     * @return          the resulting multiformatMessageString object
     */
    private MultiformatMessageString generateMultiformatMessageString(String text, @Nullable String markdown) {
        MultiformatMessageString multiformatMessageString = new MultiformatMessageString();

        multiformatMessageString.setText(text);
        multiformatMessageString.setMarkdown(markdown);

        return multiformatMessageString;
    }

    /**
     * generates a graph (intended for cpg)
     *
     * @param description   description of the resulting graph
     * @param nodes         a set containing all the graph's nodes
     * @param edges         a set containing all the graph's edges
     * @return              the resulting graph
     */
    private Graph generateGraph(Message description, Set<Node> nodes, Set<Edge> edges) {
        Graph graph = new Graph();

        graph.setDescription(description);
        graph.setNodes(nodes);
        graph.setEdges(edges);

        return graph;
    }

    /**
     * generates a single node to be used in a graph
     *
     * @param id        an id that UNIQUELY identifies the node within the graph
     * @param label     a short description of the node
     * @param location  specifies the code location associated with the node
     * @param children  a (possibly empty) set of child nodes, forming a nested graph
     * @return          the resulting node
     */
    private Node generateNode(String id, Message label, Location location, Set<Node> children) {
        Node node = new Node();

        node.setId(id);
        node.setLabel(label);
        node.setLocation(location);
        node.setChildren(children);

        return node;
    }

    /**
     * generates a Message object with the possibility of markdown, placeholders and embedded links
     *
     * @param text      plain text message (mandatory if markdown is present) without any formatting.
     *                  Preferably only one sentence long or summarized in the first sentence.
     * @param markdown  formatted text message expressed in GitHub-Flavored Markdown (GFM) WITHOUT any HTML.
     * @param id        identifier for the message, used for message string lookup
     * @param arguments List of arguments for placeholders used in either text, markdown or id parameters
     * @return          the resulting message object
     */
    private Message generateMessage(String text, @Nullable String markdown, String id, List<String> arguments) {
        Message message = new Message();

        message.setText(text);
        message.setMarkdown(markdown);
        message.setId(id);
        message.setArguments(arguments);

        return message;
    }

    /**
     * generates a Location object with possible annotations and relationships (currently only supports physical locations)
     *
     * @param id                the (non-negative) identifier unique within the result object this belongs to (-1 if not set)
     * @param physicalLocation  the physical location identifying the file of the location
     * @param message           a message relevant to the location
     * @param annotations       regions within the file relevant to the location (each one should contain a message)
     * @param relationships     relationships to other location objects
     * @return                  the resulting location
     */
    private Location generateLocation(Integer id, PhysicalLocation physicalLocation, @Nullable Message message,
                                      Set<Region> annotations, Set<LocationRelationship> relationships) {
        Location location = new Location();

        location.setId(id);
        location.setPhysicalLocation(physicalLocation);
        location.setMessage(message);
        location.setAnnotations(annotations);
        location.setRelationships(relationships);

        return location;
    }

    /**
     * generates a physical location with variable precision
     *
     * @param artifactLocation  the location of the file
     * @param region            the region within the file (if applicable)
     * @param contextRegion     a superset of the region giving additional context (only when a region is specified)
     * @return                  the resulting physical location
     */
    private PhysicalLocation generatePhysicalLocation(ArtifactLocation artifactLocation, @Nullable Region region,
                                                      @Nullable Region contextRegion) {
        PhysicalLocation physicalLocation = new PhysicalLocation();

        physicalLocation.setArtifactLocation(artifactLocation);
        physicalLocation.setRegion(region);
        physicalLocation.setContextRegion(contextRegion);

        return physicalLocation;
    }

    /**
     * generates an artifact location to specify a file location. Either uri or index SHALL be present (or both)
     *
     * @param uri           the URI specifying the location, relative to the root
     * @param uriBaseId     the URI of the root directory (absent if uri is an absolute path)
     * @param index         the index within the artifacts array of the run which describes this artifact (-1 if not set)
     * @param description   a description for this artifact
     * @return              the resulting artifact location
     */
    private ArtifactLocation generateArtifactLocation(@Nullable String uri, @Nullable String uriBaseId, Integer index,
                                                      @Nullable Message description) {
        ArtifactLocation artifactLocation = new ArtifactLocation();

        artifactLocation.setUri(uri);
        artifactLocation.setUriBaseId(uriBaseId);
        artifactLocation.setIndex(index);
        artifactLocation.setDescription(description);

        return artifactLocation;
    }

    /**
     * generates a text region defined by line and column number (both starting at 1)
     *
     * @param startLine     the line number where the region starts
     * @param endLine       the line number where the region ends
     * @param startColumn   the column number where the region starts within the startLine
     * @param endColumn     the column number where the region ends (excluding the character specified by this)
     * @return              the resulting region
     */
    private Region generateRegion(Integer startLine, Integer endLine, Integer startColumn, Integer endColumn) {
        Region region = new Region();

        region.setStartLine(startLine);
        region.setEndLine(endLine);
        region.setStartColumn(startColumn);
        region.setEndColumn(endColumn);

        return region;
    }

    /**
     * generates a relationship between two locations
     *
     * @param target        the id which identifies the target among all location objects in the result (equal to target id)
     * @param kinds         the kind of relationship (one or more from: "includes", "isIncludedBy", "relevant")
     * @param description   an additional description for the relationship
     * @return              the resulting relationship
     */
    private LocationRelationship generateLocationRelationship(Integer target, Set<String> kinds, @Nullable Message description) {
        LocationRelationship locationRelationship = new LocationRelationship();

        locationRelationship.setTarget(target);
        locationRelationship.setKinds(kinds);
        locationRelationship.setDescription(description);

        return locationRelationship;
    }

    /**
     * generates a Tool object from a driver and extensions
     *
     * @param driver        the tool's primary executable
     * @param extensions    possibly used extensions (empty set if none were used)
     * @return              the resulting tool
     */
    private Tool generateTool(ToolComponent driver, @Nullable Set<ToolComponent> extensions) {
        Tool tool = new Tool();

        tool.setDriver(driver);
        tool.setExtensions(extensions);

        return tool;
    }

    /**
     * generates a ToolComponent specified by name, version, organization and the downloadURI
     *
     * @param name          the name of the component
     * @param version       the version of the component
     * @param downloadURI   the URI of the component's download location
     * @param organization  the organization behind the component
     * @return              the resulting tool component
     */
    private ToolComponent generateToolComponent(String name, String version, URI downloadURI,
                                                String organization) {
        ToolComponent toolC = new ToolComponent();

        toolC.setName(name);
        toolC.setVersion(version);
        toolC.setDownloadUri(downloadURI);
        toolC.setOrganization(organization);

        return toolC;
    }

    /**
     * getter with intentionally generic return type (prevents rewriting with every new sarif version)
     *
     * @return the sarif object this class manages
     */
    public Object getSarif() {return sarif;}
}
