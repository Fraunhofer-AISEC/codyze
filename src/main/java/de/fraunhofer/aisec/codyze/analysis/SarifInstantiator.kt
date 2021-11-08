package de.fraunhofer.aisec.codyze.analysis

import de.fraunhofer.aisec.codyze.analysis.FindingDescription.Companion.instance
import de.fraunhofer.aisec.codyze.analysis.generated.*
import de.fraunhofer.aisec.mark.markDsl.Action
import java.net.URI
import java.net.URISyntaxException
import java.util.*

/**
 * This class was created to bundle operations regarding the Sarif Template instead of spreading them all over the code
 * e.g. The URI and name of the generated class differ between each SARIF version.
 */
class SarifInstantiator internal constructor() {
    /**
     * class members definitely need to be reviewed/changed after parsing a new SARIF template
     */
    private val sarif = Sarif210Rtm4()

    //TODO: get schema from json file (first line)
    private val schema = "http://json-schema.org/draft-04/schema#"

    //TODO: automate getting the name/version
    private val driverName = "codyze"
    private val version = "2.0.0-alpha4"
    private val download = "https://github.com/Fraunhofer-AISEC/codyze"
    private fun addRun(r: Run) {
        val runs = sarif.runs
        runs.add(r)
        sarif.runs = runs
    }

    // TODO: (Kotlin) specify default parameters

    /**
     * appends the given parameters as a new run to the end of the Sarif Object
     *
     *
     */
    fun pushRun(findings: Set<Finding>) {
        // tries to parse the downloadURI
        val downloadURI: URI? = try {
            URI(download)
        } catch (e: URISyntaxException) {
            null
        }

        /*
        imports the rules as parsed from FindingDescription.kt
        assumes that important fields like shortDescription DO NOT return null, otherwise they will be empty (not null)
         */
        val possibleFindings = instance.getItems()
        val rules: Set<ReportingDescriptor> =
                possibleFindings?.map { (id, item) ->
                    generateReportingDescriptor(id, setOf(),
                            generateMultiformatMessageString(item.shortDescription?.text ?: "", null),
                            generateMultiformatMessageString(item.fullDescription?.text ?: "", null))
                }?.toSet() ?: setOf()

        // IMPORTANT TODO: revise and change over time, this is just an initial draft
        // annotation: not satisfied with limited information passed by "Finding" -> need to get to the source of it

        // driver set and no extensions by default (not changeable as of now)
        val driver = generateToolComponent(driverName, version, downloadURI, "Fraunhofer AISEC", rules)
        val tool = generateTool(driver, setOf())
        // changes the given set of findings into a sarif compliant list of results
        val results = LinkedList<Result>()
        for ((messageIdCounter, finding) in findings.withIndex()) {
            // tries to determine kind and level (only two kinds at the moment since it is implemented as a bool)
            val kind = if (finding.isProblem) Result.Kind.FAIL else Result.Kind.PASS
            val level = when (finding.action) {
                Action.FAIL -> Result.Level.ERROR
                Action.WARN -> Result.Level.WARNING
                Action.INFO -> Result.Level.NOTE
                else        -> Result.Level.NONE
            }
            // the message has the pass or fail description and a unique id composed
            val id = finding.identifier + "Message" + messageIdCounter
            val messageText = when (kind) {
                Result.Kind.PASS -> instance.getDescriptionPass(finding.identifier) ?: ""
                else             -> instance.getDescriptionFull(finding.identifier) ?: ""
            }
            // this far no markdown is supported
            val message = generateMessage(messageText, null, id, listOf())
            // the locations can be taken from the corresponding parameter
            val locations = LinkedList<Location>()
            for ((locationIdCounter, location) in finding.locations.withIndex()) {
                val reg = generateRegion(location.region.startLine, location.region.endLine,
                        location.region.startColumn, location.region.endColumn)
                val aLoc = generateArtifactLocation(location.artifactLocation.toString(), null, -1, null)
                val pLoc = generatePhysicalLocation(aLoc, reg, null)
                // simple location object without an own message or any annotations/relationships
                locations.add(generateLocation(locationIdCounter, pLoc, null, setOf(), setOf()))
            }
            // combine all of the parameters into a Result and add it to the result List
            results.add(generateResult(finding.identifier, kind, level, message, locations, null, setOf(), setOf(), setOf()))
        }

        // generates the run with the results (no artifacts and graphs yet) and adds it to the List of runs
        val run = generateRun(tool, setOf(), setOf(), results)
        sarif.runs.add(run)
    }

    fun popRun(): Run {
        return sarif.runs.removeLast()
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
    private fun generateRun(tool: Tool, artifacts: Set<Artifact>, graphs: Set<Graph>, results:
    List<Result>?): Run {
        val run = Run()
        run.columnKind = Run.ColumnKind.UTF_16_CODE_UNITS // TODO: in reality it if UTF-8, checkhow this affects the result
        run.redactionTokens = setOf("[REDACTED]")
        run.tool = tool
        run.artifacts = artifacts
        run.graphs = graphs
        run.results = results
        return run
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
    private fun generateResult(ruleId: String?, kind: Result.Kind,
                               level: Result.Level, message: Message,
                               locations: List<Location>, analysisTarget: ArtifactLocation?,
                                // List<CodeFlow> codeFlows, GraphTraversal graphTraversal, Stack stacks
                               relatedLocations: Set<Location>, attachments: Set<Attachment>,
                               fixes: Set<Fix>): Result {
        val result = Result()
        result.ruleId = ruleId
        result.kind = kind
        result.level = level
        result.message = message
        result.locations = locations
        result.analysisTarget = analysisTarget
        result.relatedLocations = relatedLocations
        result.attachments = attachments
        result.fixes = fixes
        return result
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
    private fun generateAttachment(description: Message, location: ArtifactLocation, regions: Set<Region>,
                                   rectangles: Set<Rectangle>): Attachment {
        val attachment = Attachment()
        attachment.description = description
        attachment.artifactLocation = location
        attachment.regions = regions
        attachment.rectangles = rectangles
        return attachment
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
    private fun generateRectangle(message: Message, top: Double, left: Double, bottom: Double, right: Double): Rectangle {
        val rectangle = Rectangle()
        rectangle.message = message
        rectangle.top = top
        rectangle.left = left
        rectangle.bottom = bottom
        rectangle.right = right
        return rectangle
    }

    /**
     * generates a fix object composed of one or more changes
     *
     * @param description       a description for the fix
     * @param artifactChanges   one or more changes in files
     * @return                  the resulting fix
     */
    private fun generateFix(description: Message?, artifactChanges: Set<ArtifactChange>): Fix {
        val fix = Fix()
        fix.description = description
        fix.artifactChanges = artifactChanges
        return fix
    }

    /**
     * generates an artifactChange object composed of an artifact location and replacement details
     *
     * @param artifactLocation  the location of the file
     * @param replacements      the changes done in the file
     * @return                  the resulting artifactChange object
     */
    private fun generateArtifactChange(artifactLocation: ArtifactLocation, replacements: List<Replacement>): ArtifactChange {
        val artifactChange = ArtifactChange()
        artifactChange.artifactLocation = artifactLocation
        artifactChange.replacements = replacements
        return artifactChange
    }

    /**
     * generates a replacement in a file
     *
     * @param deletedRegion     the region to delete (if the length is 0, it specifies an insertion point)
     * @param insertedContent   specifies the content to insert in place of the region specified by deleteRegion
     * @return                  the resulting replacement
     */
    private fun generateReplacement(deletedRegion: Region, insertedContent: ArtifactContent?): Replacement {
        val replacement = Replacement()
        replacement.deletedRegion = deletedRegion
        replacement.insertedContent = insertedContent
        return replacement
    }

    /**
     * generates an artifactContent object
     *
     * @param text      the relevant text in UTF-8 minding any characters that JSON requires to be escaped
     * @param rendered  a rendered view of the contents
     * @return          the resulting artifactContent object
     */
    private fun generateArtifactContent(text: String, rendered: MultiformatMessageString?): ArtifactContent {
        val artifactContent = ArtifactContent()
        artifactContent.text = text
        artifactContent.rendered = rendered
        return artifactContent
    }

    /**
     * generates a multiformatMessageString grouping all available textual formats
     *
     * @param text      a plain representation of the message
     * @param markdown  the formatted message expressed in GitHub-Flavored Markdown (GFM)
     * @return          the resulting multiformatMessageString object
     */
    private fun generateMultiformatMessageString(text: String, markdown: String?): MultiformatMessageString {
        val multiformatMessageString = MultiformatMessageString()
        multiformatMessageString.text = text
        multiformatMessageString.markdown = markdown
        return multiformatMessageString
    }

    /**
     * generates a graph (intended for cpg)
     *
     * @param description   description of the resulting graph
     * @param nodes         a set containing all the graph's nodes
     * @param edges         a set containing all the graph's edges
     * @return              the resulting graph
     */
    private fun generateGraph(description: Message, nodes: Set<Node>, edges: Set<Edge>): Graph {
        val graph = Graph()
        graph.description = description
        graph.nodes = nodes
        graph.edges = edges
        return graph
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
    private fun generateNode(id: String, label: Message, location: Location, children: Set<Node>): Node {
        val node = Node()
        node.id = id
        node.label = label
        node.location = location
        node.children = children
        return node
    }

    /**
     * generates a Message object with the possibility of markdown, placeholders and embedded links
     *
     * @param text      plain text message (mandatory if markdown is present) without any formatting.
     * Preferably only one sentence long or summarized in the first sentence.
     * @param markdown  formatted text message expressed in GitHub-Flavored Markdown (GFM) WITHOUT any HTML.
     * @param id        identifier for the message, used for message string lookup
     * @param arguments List of arguments for placeholders used in either text, markdown or id parameters
     * @return          the resulting message object
     */
    private fun generateMessage(text: String, markdown: String?, id: String, arguments: List<String>): Message {
        val message = Message()
        message.text = text
        message.markdown = markdown
        message.id = id
        message.arguments = arguments
        return message
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
    private fun generateLocation(id: Int, physicalLocation: PhysicalLocation, message: Message?,
                                 annotations: Set<Region>, relationships:
                                 Set<LocationRelationship>): Location {
        val location = Location()
        location.id = id
        location.physicalLocation = physicalLocation
        location.message = message
        location.annotations = annotations
        location.relationships = relationships
        return location
    }

    /**
     * generates a physical location with variable precision
     *
     * @param artifactLocation  the location of the file
     * @param region            the region within the file (if applicable)
     * @param contextRegion     a superset of the region giving additional context (only when a region is specified)
     * @return                  the resulting physical location
     */
    private fun generatePhysicalLocation(artifactLocation: ArtifactLocation, region: Region?,
                                         contextRegion: Region?): PhysicalLocation {
        val physicalLocation = PhysicalLocation()
        physicalLocation.artifactLocation = artifactLocation
        physicalLocation.region = region
        physicalLocation.contextRegion = contextRegion
        return physicalLocation
    }

    /**
     * generates an artifact location to specify a file location. Either uri or index SHALL be
    present (or both)
     *
     * @param uri           the URI specifying the location, relative to the root
     * @param uriBaseId     the URI of the root directory (absent if uri is an absolute path)
     * @param index         the index within the artifacts array of the run which describes this artifact (-1 if not set)
     * @param description   a description for this artifact
     * @return              the resulting artifact location
     */
    private fun generateArtifactLocation(uri: String?, uriBaseId: String?, index: Int,
                                         description: Message?): ArtifactLocation {
        val artifactLocation = ArtifactLocation()
        artifactLocation.uri = uri
        artifactLocation.uriBaseId = uriBaseId
        artifactLocation.index = index
        artifactLocation.description = description
        return artifactLocation
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
    private fun generateRegion(startLine: Int, endLine: Int, startColumn: Int, endColumn: Int): Region {
        val region = Region()
        region.startLine = startLine
        region.endLine = endLine
        region.startColumn = startColumn
        region.endColumn = endColumn
        return region
    }

    /**
     * generates a relationship between two locations
     *
     * @param target        the id which identifies the target among all location objects in the result (equal to target id)
     * @param kinds         the kind of relationship (one or more from: "includes", "isIncludedBy", "relevant")
     * @param description   an additional description for the relationship
     * @return              the resulting relationship
     */
    private fun generateLocationRelationship(target: Int, kinds: Set<String>, description: Message?): LocationRelationship {
        val locationRelationship = LocationRelationship()
        locationRelationship.target = target
        locationRelationship.kinds = kinds
        locationRelationship.description = description
        return locationRelationship
    }

    /**
     * generates a Tool object from a driver and extensions
     *
     * @param driver        the tool's primary executable
     * @param extensions    possibly used extensions (empty set if none were used)
     * @return              the resulting tool
     */
    private fun generateTool(driver: ToolComponent, extensions: Set<ToolComponent>?): Tool {
        val tool = Tool()
        tool.driver = driver
        tool.extensions = extensions
        return tool
    }

    /**
     * generates a ToolComponent specified by name, version, organization and the downloadURI
     *
     * @param name          the name of the component
     * @param version       the version of the component
     * @param downloadURI   the URI of the component's download location
     * @param organization  the organization behind the component
     * @param rules         set of rules supported by the component
     * @return              the resulting tool component
     */
    private fun generateToolComponent(name: String, version: String, downloadURI: URI?,
                                      organization: String, rules: Set<ReportingDescriptor>): ToolComponent {
        val toolC = ToolComponent()
        toolC.name = name
        toolC.version = version
        toolC.downloadUri = downloadURI
        toolC.organization = organization
        toolC.rules = rules
        return toolC
    }

    /**
     * generates a reportingDescriptor containing information about a reporting item
     *
     * @param id                the id, in case of a rule it must be stable (doesn't change) and should be opaque (not user-readable)
     * @param deprecatedIds     ids by which this reporting item was known in previous versions
     * @param shortDescription  a concise description of the reporting item (should be a single sentence)
     * @param fullDescription   a comprehensive description of the reporting item
     * @return
     */
    private fun generateReportingDescriptor(id: String, deprecatedIds: Set<String>,
                                            shortDescription: MultiformatMessageString,
                                            fullDescription: MultiformatMessageString): ReportingDescriptor {
        val reportingDescriptor = ReportingDescriptor()
        reportingDescriptor.id = id
        reportingDescriptor.deprecatedIds = deprecatedIds
        reportingDescriptor.shortDescription = shortDescription
        reportingDescriptor.fullDescription = fullDescription
        return reportingDescriptor
    }

    /**
     * getter with intentionally generic return type (prevents rewriting with every new sarif version)
     *
     * @return the sarif object this class manages
     */
    fun getSarif(): Any {
        return sarif
    }

    init {
        val schemaURI: URI? = try {
            URI(schema)
        } catch (e: URISyntaxException) {
            null
        }
        sarif.`$schema` = schemaURI
    }
}