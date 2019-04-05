package de.fraunhofer.aisec.crymlin.server;

/** The configuration for the {@link AnalysisServer} holds all values used by the server. */
public class ServerConfiguration {

  /** Should the server launch an interactive CLI console? */
  public final boolean launchConsole;

  /** Should the server launch an LSP server? */
  public final boolean launchLsp;

  /** Neo4J URI. */
  public String dbUri;

  /** Neo4J username. */
  public String dbUser;

  /** Neo4J password. */
  public String dbPassword;

  /** Directory or file with MARK entities/rules. */
  public String markModelFiles;

  private ServerConfiguration(
      boolean launchConsole,
      boolean launchLsp,
      String dbUri,
      String dbUser,
      String dbPassword,
      String markModelFiles) {
    this.launchConsole = launchConsole;
    this.launchLsp = launchLsp;
    this.dbUri = dbUri;
    this.dbUser = dbUser;
    this.dbPassword = dbPassword;
    this.markModelFiles = markModelFiles;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private boolean launchConsole = true;
    private boolean launchLsp = true;
    private String dbUri = "bolt://localhost";
    private String dbUser = "neo4j";
    private String dbPassword = "password";
    private String markModelFiles = ""; // Path of a file or directory

    public Builder launchConsole(boolean launchConsole) {
      this.launchConsole = launchConsole;
      return this;
    }

    public Builder launchLsp(boolean launchLsp) {
      this.launchLsp = launchLsp;
      return this;
    }

    public Builder dbUri(String dbUri) {
      this.dbUri = dbUri;
      return this;
    }

    public Builder dbUser(String dbUser) {
      this.dbUser = dbUser;
      return this;
    }

    public Builder dbPassword(String dbPassword) {
      this.dbPassword = dbPassword;
      return this;
    }

    public Builder markFiles(String markModelFiles) {
      this.markModelFiles = markModelFiles;
      return this;
    }

    public ServerConfiguration build() {
      return new ServerConfiguration(
          launchConsole, launchLsp, dbUri, dbUser, dbPassword, markModelFiles);
    }
  }
}
