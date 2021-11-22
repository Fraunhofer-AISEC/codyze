
package de.fraunhofer.aisec.codyze;

public class ConfigurationFile {
	private CodyzeConfigurationFile codyze;
	private CpgConfigurationFile cpg;

	public CodyzeConfigurationFile getCodyzeConfig() {
		return codyze;
	}

	public void setCodyze(CodyzeConfigurationFile codyze) {
		this.codyze = codyze;
	}

	public CpgConfigurationFile getCpgConfig() {
		return cpg;
	}

	public void setCpg(CpgConfigurationFile cpg) {
		this.cpg = cpg;
	}
}