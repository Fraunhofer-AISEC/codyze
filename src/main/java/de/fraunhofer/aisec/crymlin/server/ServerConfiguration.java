package de.fraunhofer.aisec.crymlin.server;

/** The configuration for the {@link AnalysisServer} holds all values used by the server. */
public class ServerConfiguration {

  /** Should the server launch an interactive CLI console? */
  public final boolean launchConsole;

  /** Should the server launch an LSP server? */
  public final boolean launchLsp;

  private ServerConfiguration(boolean launchConsole, boolean launchLsp) {
    this.launchConsole = launchConsole;
    this.launchLsp = launchLsp;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private boolean launchConsole = true;
    private boolean launchLsp = true;

    public Builder launchConsole(boolean launchConsole) {
      this.launchConsole = launchConsole;
      return this;
    }

    public Builder launchLsp(boolean launchLsp) {
      this.launchLsp = launchLsp;
      return this;
    }

    public ServerConfiguration build() {
      return new ServerConfiguration(launchConsole, launchLsp);
    }
  }
}
