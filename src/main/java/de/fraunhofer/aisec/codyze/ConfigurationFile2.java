
package de.fraunhofer.aisec.codyze;

public class ConfigurationFile2 {
	private CodyzeConfigurationFile2 codyze;
	private CpgConfigurationFile2 cpg;

	public CodyzeConfigurationFile2 getCodyzeConfig() {
		return codyze;
	}

	public void setCodyze(CodyzeConfigurationFile2 codyze) {
		this.codyze = codyze;
	}

	public CpgConfigurationFile2 getCpgConfig() {
		return cpg;
	}

	public void setCpg(CpgConfigurationFile2 cpg) {
		this.cpg = cpg;
	}
}