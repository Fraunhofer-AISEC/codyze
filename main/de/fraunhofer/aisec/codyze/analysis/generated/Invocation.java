
package de.fraunhofer.aisec.codyze.analysis.generated;

import java.util.ArrayList;
import java.util.Date;
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
 * The runtime environment of the analysis tool run.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "commandLine",
    "arguments",
    "responseFiles",
    "startTimeUtc",
    "endTimeUtc",
    "exitCode",
    "ruleConfigurationOverrides",
    "notificationConfigurationOverrides",
    "toolExecutionNotifications",
    "toolConfigurationNotifications",
    "exitCodeDescription",
    "exitSignalName",
    "exitSignalNumber",
    "processStartFailureMessage",
    "executionSuccessful",
    "machine",
    "account",
    "processId",
    "executableLocation",
    "workingDirectory",
    "environmentVariables",
    "stdin",
    "stdout",
    "stderr",
    "stdoutStderr",
    "properties"
})
@Generated("jsonschema2pojo")
public class Invocation {

    /**
     * The command line used to invoke the tool.
     * 
     */
    @JsonProperty("commandLine")
    @JsonPropertyDescription("The command line used to invoke the tool.")
    private String commandLine;
    /**
     * An array of strings, containing in order the command line arguments passed to the tool from the operating system.
     * 
     */
    @JsonProperty("arguments")
    @JsonPropertyDescription("An array of strings, containing in order the command line arguments passed to the tool from the operating system.")
    private List<String> arguments = new ArrayList<String>();
    /**
     * The locations of any response files specified on the tool's command line.
     * 
     */
    @JsonProperty("responseFiles")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("The locations of any response files specified on the tool's command line.")
    private Set<ArtifactLocation> responseFiles = new LinkedHashSet<ArtifactLocation>();
    /**
     * The Coordinated Universal Time (UTC) date and time at which the run started. See "Date/time properties" in the SARIF spec for the required format.
     * 
     */
    @JsonProperty("startTimeUtc")
    @JsonPropertyDescription("The Coordinated Universal Time (UTC) date and time at which the run started. See \"Date/time properties\" in the SARIF spec for the required format.")
    private Date startTimeUtc;
    /**
     * The Coordinated Universal Time (UTC) date and time at which the run ended. See "Date/time properties" in the SARIF spec for the required format.
     * 
     */
    @JsonProperty("endTimeUtc")
    @JsonPropertyDescription("The Coordinated Universal Time (UTC) date and time at which the run ended. See \"Date/time properties\" in the SARIF spec for the required format.")
    private Date endTimeUtc;
    /**
     * The process exit code.
     * 
     */
    @JsonProperty("exitCode")
    @JsonPropertyDescription("The process exit code.")
    private Integer exitCode;
    /**
     * An array of configurationOverride objects that describe rules related runtime overrides.
     * 
     */
    @JsonProperty("ruleConfigurationOverrides")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of configurationOverride objects that describe rules related runtime overrides.")
    private Set<ConfigurationOverride> ruleConfigurationOverrides = new LinkedHashSet<ConfigurationOverride>();
    /**
     * An array of configurationOverride objects that describe notifications related runtime overrides.
     * 
     */
    @JsonProperty("notificationConfigurationOverrides")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("An array of configurationOverride objects that describe notifications related runtime overrides.")
    private Set<ConfigurationOverride> notificationConfigurationOverrides = new LinkedHashSet<ConfigurationOverride>();
    /**
     * A list of runtime conditions detected by the tool during the analysis.
     * 
     */
    @JsonProperty("toolExecutionNotifications")
    @JsonPropertyDescription("A list of runtime conditions detected by the tool during the analysis.")
    private List<Notification> toolExecutionNotifications = new ArrayList<Notification>();
    /**
     * A list of conditions detected by the tool that are relevant to the tool's configuration.
     * 
     */
    @JsonProperty("toolConfigurationNotifications")
    @JsonPropertyDescription("A list of conditions detected by the tool that are relevant to the tool's configuration.")
    private List<Notification> toolConfigurationNotifications = new ArrayList<Notification>();
    /**
     * The reason for the process exit.
     * 
     */
    @JsonProperty("exitCodeDescription")
    @JsonPropertyDescription("The reason for the process exit.")
    private String exitCodeDescription;
    /**
     * The name of the signal that caused the process to exit.
     * 
     */
    @JsonProperty("exitSignalName")
    @JsonPropertyDescription("The name of the signal that caused the process to exit.")
    private String exitSignalName;
    /**
     * The numeric value of the signal that caused the process to exit.
     * 
     */
    @JsonProperty("exitSignalNumber")
    @JsonPropertyDescription("The numeric value of the signal that caused the process to exit.")
    private Integer exitSignalNumber;
    /**
     * The reason given by the operating system that the process failed to start.
     * 
     */
    @JsonProperty("processStartFailureMessage")
    @JsonPropertyDescription("The reason given by the operating system that the process failed to start.")
    private String processStartFailureMessage;
    /**
     * Specifies whether the tool's execution completed successfully.
     * (Required)
     * 
     */
    @JsonProperty("executionSuccessful")
    @JsonPropertyDescription("Specifies whether the tool's execution completed successfully.")
    private Boolean executionSuccessful;
    /**
     * The machine that hosted the analysis tool run.
     * 
     */
    @JsonProperty("machine")
    @JsonPropertyDescription("The machine that hosted the analysis tool run.")
    private String machine;
    /**
     * The account that ran the analysis tool.
     * 
     */
    @JsonProperty("account")
    @JsonPropertyDescription("The account that ran the analysis tool.")
    private String account;
    /**
     * The process id for the analysis tool run.
     * 
     */
    @JsonProperty("processId")
    @JsonPropertyDescription("The process id for the analysis tool run.")
    private Integer processId;
    /**
     * Specifies the location of an artifact.
     * 
     */
    @JsonProperty("executableLocation")
    @JsonPropertyDescription("Specifies the location of an artifact.")
    private ArtifactLocation executableLocation;
    /**
     * Specifies the location of an artifact.
     * 
     */
    @JsonProperty("workingDirectory")
    @JsonPropertyDescription("Specifies the location of an artifact.")
    private ArtifactLocation workingDirectory;
    /**
     * The environment variables associated with the analysis tool process, expressed as key/value pairs.
     * 
     */
    @JsonProperty("environmentVariables")
    @JsonPropertyDescription("The environment variables associated with the analysis tool process, expressed as key/value pairs.")
    private EnvironmentVariables environmentVariables;
    /**
     * Specifies the location of an artifact.
     * 
     */
    @JsonProperty("stdin")
    @JsonPropertyDescription("Specifies the location of an artifact.")
    private ArtifactLocation stdin;
    /**
     * Specifies the location of an artifact.
     * 
     */
    @JsonProperty("stdout")
    @JsonPropertyDescription("Specifies the location of an artifact.")
    private ArtifactLocation stdout;
    /**
     * Specifies the location of an artifact.
     * 
     */
    @JsonProperty("stderr")
    @JsonPropertyDescription("Specifies the location of an artifact.")
    private ArtifactLocation stderr;
    /**
     * Specifies the location of an artifact.
     * 
     */
    @JsonProperty("stdoutStderr")
    @JsonPropertyDescription("Specifies the location of an artifact.")
    private ArtifactLocation stdoutStderr;
    /**
     * Key/value pairs that provide additional information about the object.
     * 
     */
    @JsonProperty("properties")
    @JsonPropertyDescription("Key/value pairs that provide additional information about the object.")
    private PropertyBag properties;

    /**
     * The command line used to invoke the tool.
     * 
     */
    @JsonProperty("commandLine")
    public String getCommandLine() {
        return commandLine;
    }

    /**
     * The command line used to invoke the tool.
     * 
     */
    @JsonProperty("commandLine")
    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }

    /**
     * An array of strings, containing in order the command line arguments passed to the tool from the operating system.
     * 
     */
    @JsonProperty("arguments")
    public List<String> getArguments() {
        return arguments;
    }

    /**
     * An array of strings, containing in order the command line arguments passed to the tool from the operating system.
     * 
     */
    @JsonProperty("arguments")
    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    /**
     * The locations of any response files specified on the tool's command line.
     * 
     */
    @JsonProperty("responseFiles")
    public Set<ArtifactLocation> getResponseFiles() {
        return responseFiles;
    }

    /**
     * The locations of any response files specified on the tool's command line.
     * 
     */
    @JsonProperty("responseFiles")
    public void setResponseFiles(Set<ArtifactLocation> responseFiles) {
        this.responseFiles = responseFiles;
    }

    /**
     * The Coordinated Universal Time (UTC) date and time at which the run started. See "Date/time properties" in the SARIF spec for the required format.
     * 
     */
    @JsonProperty("startTimeUtc")
    public Date getStartTimeUtc() {
        return startTimeUtc;
    }

    /**
     * The Coordinated Universal Time (UTC) date and time at which the run started. See "Date/time properties" in the SARIF spec for the required format.
     * 
     */
    @JsonProperty("startTimeUtc")
    public void setStartTimeUtc(Date startTimeUtc) {
        this.startTimeUtc = startTimeUtc;
    }

    /**
     * The Coordinated Universal Time (UTC) date and time at which the run ended. See "Date/time properties" in the SARIF spec for the required format.
     * 
     */
    @JsonProperty("endTimeUtc")
    public Date getEndTimeUtc() {
        return endTimeUtc;
    }

    /**
     * The Coordinated Universal Time (UTC) date and time at which the run ended. See "Date/time properties" in the SARIF spec for the required format.
     * 
     */
    @JsonProperty("endTimeUtc")
    public void setEndTimeUtc(Date endTimeUtc) {
        this.endTimeUtc = endTimeUtc;
    }

    /**
     * The process exit code.
     * 
     */
    @JsonProperty("exitCode")
    public Integer getExitCode() {
        return exitCode;
    }

    /**
     * The process exit code.
     * 
     */
    @JsonProperty("exitCode")
    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

    /**
     * An array of configurationOverride objects that describe rules related runtime overrides.
     * 
     */
    @JsonProperty("ruleConfigurationOverrides")
    public Set<ConfigurationOverride> getRuleConfigurationOverrides() {
        return ruleConfigurationOverrides;
    }

    /**
     * An array of configurationOverride objects that describe rules related runtime overrides.
     * 
     */
    @JsonProperty("ruleConfigurationOverrides")
    public void setRuleConfigurationOverrides(Set<ConfigurationOverride> ruleConfigurationOverrides) {
        this.ruleConfigurationOverrides = ruleConfigurationOverrides;
    }

    /**
     * An array of configurationOverride objects that describe notifications related runtime overrides.
     * 
     */
    @JsonProperty("notificationConfigurationOverrides")
    public Set<ConfigurationOverride> getNotificationConfigurationOverrides() {
        return notificationConfigurationOverrides;
    }

    /**
     * An array of configurationOverride objects that describe notifications related runtime overrides.
     * 
     */
    @JsonProperty("notificationConfigurationOverrides")
    public void setNotificationConfigurationOverrides(Set<ConfigurationOverride> notificationConfigurationOverrides) {
        this.notificationConfigurationOverrides = notificationConfigurationOverrides;
    }

    /**
     * A list of runtime conditions detected by the tool during the analysis.
     * 
     */
    @JsonProperty("toolExecutionNotifications")
    public List<Notification> getToolExecutionNotifications() {
        return toolExecutionNotifications;
    }

    /**
     * A list of runtime conditions detected by the tool during the analysis.
     * 
     */
    @JsonProperty("toolExecutionNotifications")
    public void setToolExecutionNotifications(List<Notification> toolExecutionNotifications) {
        this.toolExecutionNotifications = toolExecutionNotifications;
    }

    /**
     * A list of conditions detected by the tool that are relevant to the tool's configuration.
     * 
     */
    @JsonProperty("toolConfigurationNotifications")
    public List<Notification> getToolConfigurationNotifications() {
        return toolConfigurationNotifications;
    }

    /**
     * A list of conditions detected by the tool that are relevant to the tool's configuration.
     * 
     */
    @JsonProperty("toolConfigurationNotifications")
    public void setToolConfigurationNotifications(List<Notification> toolConfigurationNotifications) {
        this.toolConfigurationNotifications = toolConfigurationNotifications;
    }

    /**
     * The reason for the process exit.
     * 
     */
    @JsonProperty("exitCodeDescription")
    public String getExitCodeDescription() {
        return exitCodeDescription;
    }

    /**
     * The reason for the process exit.
     * 
     */
    @JsonProperty("exitCodeDescription")
    public void setExitCodeDescription(String exitCodeDescription) {
        this.exitCodeDescription = exitCodeDescription;
    }

    /**
     * The name of the signal that caused the process to exit.
     * 
     */
    @JsonProperty("exitSignalName")
    public String getExitSignalName() {
        return exitSignalName;
    }

    /**
     * The name of the signal that caused the process to exit.
     * 
     */
    @JsonProperty("exitSignalName")
    public void setExitSignalName(String exitSignalName) {
        this.exitSignalName = exitSignalName;
    }

    /**
     * The numeric value of the signal that caused the process to exit.
     * 
     */
    @JsonProperty("exitSignalNumber")
    public Integer getExitSignalNumber() {
        return exitSignalNumber;
    }

    /**
     * The numeric value of the signal that caused the process to exit.
     * 
     */
    @JsonProperty("exitSignalNumber")
    public void setExitSignalNumber(Integer exitSignalNumber) {
        this.exitSignalNumber = exitSignalNumber;
    }

    /**
     * The reason given by the operating system that the process failed to start.
     * 
     */
    @JsonProperty("processStartFailureMessage")
    public String getProcessStartFailureMessage() {
        return processStartFailureMessage;
    }

    /**
     * The reason given by the operating system that the process failed to start.
     * 
     */
    @JsonProperty("processStartFailureMessage")
    public void setProcessStartFailureMessage(String processStartFailureMessage) {
        this.processStartFailureMessage = processStartFailureMessage;
    }

    /**
     * Specifies whether the tool's execution completed successfully.
     * (Required)
     * 
     */
    @JsonProperty("executionSuccessful")
    public Boolean getExecutionSuccessful() {
        return executionSuccessful;
    }

    /**
     * Specifies whether the tool's execution completed successfully.
     * (Required)
     * 
     */
    @JsonProperty("executionSuccessful")
    public void setExecutionSuccessful(Boolean executionSuccessful) {
        this.executionSuccessful = executionSuccessful;
    }

    /**
     * The machine that hosted the analysis tool run.
     * 
     */
    @JsonProperty("machine")
    public String getMachine() {
        return machine;
    }

    /**
     * The machine that hosted the analysis tool run.
     * 
     */
    @JsonProperty("machine")
    public void setMachine(String machine) {
        this.machine = machine;
    }

    /**
     * The account that ran the analysis tool.
     * 
     */
    @JsonProperty("account")
    public String getAccount() {
        return account;
    }

    /**
     * The account that ran the analysis tool.
     * 
     */
    @JsonProperty("account")
    public void setAccount(String account) {
        this.account = account;
    }

    /**
     * The process id for the analysis tool run.
     * 
     */
    @JsonProperty("processId")
    public Integer getProcessId() {
        return processId;
    }

    /**
     * The process id for the analysis tool run.
     * 
     */
    @JsonProperty("processId")
    public void setProcessId(Integer processId) {
        this.processId = processId;
    }

    /**
     * Specifies the location of an artifact.
     * 
     */
    @JsonProperty("executableLocation")
    public ArtifactLocation getExecutableLocation() {
        return executableLocation;
    }

    /**
     * Specifies the location of an artifact.
     * 
     */
    @JsonProperty("executableLocation")
    public void setExecutableLocation(ArtifactLocation executableLocation) {
        this.executableLocation = executableLocation;
    }

    /**
     * Specifies the location of an artifact.
     * 
     */
    @JsonProperty("workingDirectory")
    public ArtifactLocation getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * Specifies the location of an artifact.
     * 
     */
    @JsonProperty("workingDirectory")
    public void setWorkingDirectory(ArtifactLocation workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * The environment variables associated with the analysis tool process, expressed as key/value pairs.
     * 
     */
    @JsonProperty("environmentVariables")
    public EnvironmentVariables getEnvironmentVariables() {
        return environmentVariables;
    }

    /**
     * The environment variables associated with the analysis tool process, expressed as key/value pairs.
     * 
     */
    @JsonProperty("environmentVariables")
    public void setEnvironmentVariables(EnvironmentVariables environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    /**
     * Specifies the location of an artifact.
     * 
     */
    @JsonProperty("stdin")
    public ArtifactLocation getStdin() {
        return stdin;
    }

    /**
     * Specifies the location of an artifact.
     * 
     */
    @JsonProperty("stdin")
    public void setStdin(ArtifactLocation stdin) {
        this.stdin = stdin;
    }

    /**
     * Specifies the location of an artifact.
     * 
     */
    @JsonProperty("stdout")
    public ArtifactLocation getStdout() {
        return stdout;
    }

    /**
     * Specifies the location of an artifact.
     * 
     */
    @JsonProperty("stdout")
    public void setStdout(ArtifactLocation stdout) {
        this.stdout = stdout;
    }

    /**
     * Specifies the location of an artifact.
     * 
     */
    @JsonProperty("stderr")
    public ArtifactLocation getStderr() {
        return stderr;
    }

    /**
     * Specifies the location of an artifact.
     * 
     */
    @JsonProperty("stderr")
    public void setStderr(ArtifactLocation stderr) {
        this.stderr = stderr;
    }

    /**
     * Specifies the location of an artifact.
     * 
     */
    @JsonProperty("stdoutStderr")
    public ArtifactLocation getStdoutStderr() {
        return stdoutStderr;
    }

    /**
     * Specifies the location of an artifact.
     * 
     */
    @JsonProperty("stdoutStderr")
    public void setStdoutStderr(ArtifactLocation stdoutStderr) {
        this.stdoutStderr = stdoutStderr;
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
        sb.append(Invocation.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("commandLine");
        sb.append('=');
        sb.append(((this.commandLine == null)?"<null>":this.commandLine));
        sb.append(',');
        sb.append("arguments");
        sb.append('=');
        sb.append(((this.arguments == null)?"<null>":this.arguments));
        sb.append(',');
        sb.append("responseFiles");
        sb.append('=');
        sb.append(((this.responseFiles == null)?"<null>":this.responseFiles));
        sb.append(',');
        sb.append("startTimeUtc");
        sb.append('=');
        sb.append(((this.startTimeUtc == null)?"<null>":this.startTimeUtc));
        sb.append(',');
        sb.append("endTimeUtc");
        sb.append('=');
        sb.append(((this.endTimeUtc == null)?"<null>":this.endTimeUtc));
        sb.append(',');
        sb.append("exitCode");
        sb.append('=');
        sb.append(((this.exitCode == null)?"<null>":this.exitCode));
        sb.append(',');
        sb.append("ruleConfigurationOverrides");
        sb.append('=');
        sb.append(((this.ruleConfigurationOverrides == null)?"<null>":this.ruleConfigurationOverrides));
        sb.append(',');
        sb.append("notificationConfigurationOverrides");
        sb.append('=');
        sb.append(((this.notificationConfigurationOverrides == null)?"<null>":this.notificationConfigurationOverrides));
        sb.append(',');
        sb.append("toolExecutionNotifications");
        sb.append('=');
        sb.append(((this.toolExecutionNotifications == null)?"<null>":this.toolExecutionNotifications));
        sb.append(',');
        sb.append("toolConfigurationNotifications");
        sb.append('=');
        sb.append(((this.toolConfigurationNotifications == null)?"<null>":this.toolConfigurationNotifications));
        sb.append(',');
        sb.append("exitCodeDescription");
        sb.append('=');
        sb.append(((this.exitCodeDescription == null)?"<null>":this.exitCodeDescription));
        sb.append(',');
        sb.append("exitSignalName");
        sb.append('=');
        sb.append(((this.exitSignalName == null)?"<null>":this.exitSignalName));
        sb.append(',');
        sb.append("exitSignalNumber");
        sb.append('=');
        sb.append(((this.exitSignalNumber == null)?"<null>":this.exitSignalNumber));
        sb.append(',');
        sb.append("processStartFailureMessage");
        sb.append('=');
        sb.append(((this.processStartFailureMessage == null)?"<null>":this.processStartFailureMessage));
        sb.append(',');
        sb.append("executionSuccessful");
        sb.append('=');
        sb.append(((this.executionSuccessful == null)?"<null>":this.executionSuccessful));
        sb.append(',');
        sb.append("machine");
        sb.append('=');
        sb.append(((this.machine == null)?"<null>":this.machine));
        sb.append(',');
        sb.append("account");
        sb.append('=');
        sb.append(((this.account == null)?"<null>":this.account));
        sb.append(',');
        sb.append("processId");
        sb.append('=');
        sb.append(((this.processId == null)?"<null>":this.processId));
        sb.append(',');
        sb.append("executableLocation");
        sb.append('=');
        sb.append(((this.executableLocation == null)?"<null>":this.executableLocation));
        sb.append(',');
        sb.append("workingDirectory");
        sb.append('=');
        sb.append(((this.workingDirectory == null)?"<null>":this.workingDirectory));
        sb.append(',');
        sb.append("environmentVariables");
        sb.append('=');
        sb.append(((this.environmentVariables == null)?"<null>":this.environmentVariables));
        sb.append(',');
        sb.append("stdin");
        sb.append('=');
        sb.append(((this.stdin == null)?"<null>":this.stdin));
        sb.append(',');
        sb.append("stdout");
        sb.append('=');
        sb.append(((this.stdout == null)?"<null>":this.stdout));
        sb.append(',');
        sb.append("stderr");
        sb.append('=');
        sb.append(((this.stderr == null)?"<null>":this.stderr));
        sb.append(',');
        sb.append("stdoutStderr");
        sb.append('=');
        sb.append(((this.stdoutStderr == null)?"<null>":this.stdoutStderr));
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
        result = ((result* 31)+((this.endTimeUtc == null)? 0 :this.endTimeUtc.hashCode()));
        result = ((result* 31)+((this.stdin == null)? 0 :this.stdin.hashCode()));
        result = ((result* 31)+((this.stdout == null)? 0 :this.stdout.hashCode()));
        result = ((result* 31)+((this.workingDirectory == null)? 0 :this.workingDirectory.hashCode()));
        result = ((result* 31)+((this.exitSignalNumber == null)? 0 :this.exitSignalNumber.hashCode()));
        result = ((result* 31)+((this.exitCodeDescription == null)? 0 :this.exitCodeDescription.hashCode()));
        result = ((result* 31)+((this.executableLocation == null)? 0 :this.executableLocation.hashCode()));
        result = ((result* 31)+((this.processId == null)? 0 :this.processId.hashCode()));
        result = ((result* 31)+((this.exitCode == null)? 0 :this.exitCode.hashCode()));
        result = ((result* 31)+((this.toolConfigurationNotifications == null)? 0 :this.toolConfigurationNotifications.hashCode()));
        result = ((result* 31)+((this.notificationConfigurationOverrides == null)? 0 :this.notificationConfigurationOverrides.hashCode()));
        result = ((result* 31)+((this.processStartFailureMessage == null)? 0 :this.processStartFailureMessage.hashCode()));
        result = ((result* 31)+((this.stderr == null)? 0 :this.stderr.hashCode()));
        result = ((result* 31)+((this.ruleConfigurationOverrides == null)? 0 :this.ruleConfigurationOverrides.hashCode()));
        result = ((result* 31)+((this.toolExecutionNotifications == null)? 0 :this.toolExecutionNotifications.hashCode()));
        result = ((result* 31)+((this.machine == null)? 0 :this.machine.hashCode()));
        result = ((result* 31)+((this.environmentVariables == null)? 0 :this.environmentVariables.hashCode()));
        result = ((result* 31)+((this.stdoutStderr == null)? 0 :this.stdoutStderr.hashCode()));
        result = ((result* 31)+((this.arguments == null)? 0 :this.arguments.hashCode()));
        result = ((result* 31)+((this.responseFiles == null)? 0 :this.responseFiles.hashCode()));
        result = ((result* 31)+((this.commandLine == null)? 0 :this.commandLine.hashCode()));
        result = ((result* 31)+((this.executionSuccessful == null)? 0 :this.executionSuccessful.hashCode()));
        result = ((result* 31)+((this.startTimeUtc == null)? 0 :this.startTimeUtc.hashCode()));
        result = ((result* 31)+((this.account == null)? 0 :this.account.hashCode()));
        result = ((result* 31)+((this.properties == null)? 0 :this.properties.hashCode()));
        result = ((result* 31)+((this.exitSignalName == null)? 0 :this.exitSignalName.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Invocation) == false) {
            return false;
        }
        Invocation rhs = ((Invocation) other);
        return (((((((((((((((((((((((((((this.endTimeUtc == rhs.endTimeUtc)||((this.endTimeUtc!= null)&&this.endTimeUtc.equals(rhs.endTimeUtc)))&&((this.stdin == rhs.stdin)||((this.stdin!= null)&&this.stdin.equals(rhs.stdin))))&&((this.stdout == rhs.stdout)||((this.stdout!= null)&&this.stdout.equals(rhs.stdout))))&&((this.workingDirectory == rhs.workingDirectory)||((this.workingDirectory!= null)&&this.workingDirectory.equals(rhs.workingDirectory))))&&((this.exitSignalNumber == rhs.exitSignalNumber)||((this.exitSignalNumber!= null)&&this.exitSignalNumber.equals(rhs.exitSignalNumber))))&&((this.exitCodeDescription == rhs.exitCodeDescription)||((this.exitCodeDescription!= null)&&this.exitCodeDescription.equals(rhs.exitCodeDescription))))&&((this.executableLocation == rhs.executableLocation)||((this.executableLocation!= null)&&this.executableLocation.equals(rhs.executableLocation))))&&((this.processId == rhs.processId)||((this.processId!= null)&&this.processId.equals(rhs.processId))))&&((this.exitCode == rhs.exitCode)||((this.exitCode!= null)&&this.exitCode.equals(rhs.exitCode))))&&((this.toolConfigurationNotifications == rhs.toolConfigurationNotifications)||((this.toolConfigurationNotifications!= null)&&this.toolConfigurationNotifications.equals(rhs.toolConfigurationNotifications))))&&((this.notificationConfigurationOverrides == rhs.notificationConfigurationOverrides)||((this.notificationConfigurationOverrides!= null)&&this.notificationConfigurationOverrides.equals(rhs.notificationConfigurationOverrides))))&&((this.processStartFailureMessage == rhs.processStartFailureMessage)||((this.processStartFailureMessage!= null)&&this.processStartFailureMessage.equals(rhs.processStartFailureMessage))))&&((this.stderr == rhs.stderr)||((this.stderr!= null)&&this.stderr.equals(rhs.stderr))))&&((this.ruleConfigurationOverrides == rhs.ruleConfigurationOverrides)||((this.ruleConfigurationOverrides!= null)&&this.ruleConfigurationOverrides.equals(rhs.ruleConfigurationOverrides))))&&((this.toolExecutionNotifications == rhs.toolExecutionNotifications)||((this.toolExecutionNotifications!= null)&&this.toolExecutionNotifications.equals(rhs.toolExecutionNotifications))))&&((this.machine == rhs.machine)||((this.machine!= null)&&this.machine.equals(rhs.machine))))&&((this.environmentVariables == rhs.environmentVariables)||((this.environmentVariables!= null)&&this.environmentVariables.equals(rhs.environmentVariables))))&&((this.stdoutStderr == rhs.stdoutStderr)||((this.stdoutStderr!= null)&&this.stdoutStderr.equals(rhs.stdoutStderr))))&&((this.arguments == rhs.arguments)||((this.arguments!= null)&&this.arguments.equals(rhs.arguments))))&&((this.responseFiles == rhs.responseFiles)||((this.responseFiles!= null)&&this.responseFiles.equals(rhs.responseFiles))))&&((this.commandLine == rhs.commandLine)||((this.commandLine!= null)&&this.commandLine.equals(rhs.commandLine))))&&((this.executionSuccessful == rhs.executionSuccessful)||((this.executionSuccessful!= null)&&this.executionSuccessful.equals(rhs.executionSuccessful))))&&((this.startTimeUtc == rhs.startTimeUtc)||((this.startTimeUtc!= null)&&this.startTimeUtc.equals(rhs.startTimeUtc))))&&((this.account == rhs.account)||((this.account!= null)&&this.account.equals(rhs.account))))&&((this.properties == rhs.properties)||((this.properties!= null)&&this.properties.equals(rhs.properties))))&&((this.exitSignalName == rhs.exitSignalName)||((this.exitSignalName!= null)&&this.exitSignalName.equals(rhs.exitSignalName))));
    }

}
