
package de.fraunhofer.aisec.codyze;

public class ConfigurationFile {
	private CodyzeConfiguration codyze;
	private CpgConfiguration cpg;

	public CodyzeConfiguration getCodyzeConfig() {
		return codyze;
	}

	public void setCodyze(CodyzeConfiguration codyze) {
		this.codyze = codyze;
	}

	public CpgConfiguration getCpgConfig() {
		return cpg;
	}

	public void setCpg(CpgConfiguration cpg) {
		this.cpg = cpg;
	}
}