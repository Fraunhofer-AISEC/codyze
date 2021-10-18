package de.fraunhofer.aisec.codyze.analysis;

import de.fraunhofer.aisec.codyze.analysis.generated.*;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

//TODO: determine where a List of findings is converted (possibly into a List of Result-objects)

/**
 * This class was created to bundle operations regarding the Sarif Template instead of spreading them all over the code
 * e.g. The URI and name of the generated class differ between each SARIF version.
 */
public class SarifInstantiator {

    /**
     * class members definitely need to be reviewed/changed after parsing a new SARIF template
     */
    private final Sarif210Rtm4 sarif = new Sarif210Rtm4();
    private final String schema = "https://schemastore.azurewebsites.net/schemas/json/sarif-2.1.0-rtm.4.json";
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
     * @return          the resulting run
     */
    private Run generateRun(Tool tool) {
        Run run = new Run();
        run.setTool(tool);

        //TODO: finish the run according to the specification
        //      (may eventually provide the full list of parameters from the specification

        return run;
    }

    /**
     *
     * @param driver        the tool's primary executable
     * @param extensions    possibly used extensions (null OR empty set if none were used)
     * @return              the resulting tool object
     */
    private Tool generateTool(ToolComponent driver, @Nullable Set<ToolComponent> extensions) {
        Tool tool = new Tool();
        tool.setDriver(driver);
        tool.setExtensions(extensions);
        return tool;
    }

    private ToolComponent generateToolComponent(String name, String version, URI downloadURI,
                                                String organization) {
        ToolComponent toolC = new ToolComponent();
        toolC.setName(name);
        toolC.setVersion(version);
        toolC.setDownloadUri(downloadURI);
        toolC.setOrganization(organization);

        //TODO: finish the toolComponent according to the specification
        //      (may eventually provide the full list of parameters from the specification

        return toolC;
    }

    /**
     * getter with intentionally generic return type (prevents rewriting with every new sarif version)
     *
     * @return the sarif object this class manages
     */
    public Object getSarif() {return sarif;}
}
