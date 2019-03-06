package de.fraunhofer.aisec.crymlin.server;

/** The configuration for the {@link AnalysisServer} holds all values used by the server. */
public class ServerConfiguration {

  /** Should the server launch an interactive CLI console? */
  public final boolean launchConsole;

  private ServerConfiguration(boolean launchConsole) {
    this.launchConsole = launchConsole;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private boolean launchConsole = true;

    public Builder launchConsole(boolean launchConsole) {
      this.launchConsole = launchConsole;
      return this;
    }

    public ServerConfiguration build() {
      return new ServerConfiguration(launchConsole);
    }
  }
}
