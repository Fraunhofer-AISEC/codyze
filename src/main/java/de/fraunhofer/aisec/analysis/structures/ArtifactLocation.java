
package de.fraunhofer.aisec.analysis.structures;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.net.URI;

/**
 * A subset of the SARIF artifactLocation object.
 *
 * https://github.com/oasis-tcs/sarif-spec/blob/master/Documents/CommitteeSpecifications/2.1.0/sarif-v2.1.0-committee-specification.pdf §3.4
 *
 */
public class ArtifactLocation {

	/**
	 * URI of the artifact. Absolute, if uriBaseId is null. Otherwise relative to uriBaseId (RFC3986), or relative to archive file, if artifact is located in an archive
	 * (zip, tar).
	 */
	@NonNull
	private URI uri;

	/**
	 * URI of top-level artifact (e.g., project root).
	 */
	@Nullable
	private URI uriBaseId;

	public ArtifactLocation(@NonNull URI uri, @Nullable URI uriBaseId) {
		this.uri = uri;
		this.uriBaseId = uriBaseId;
	}

	@NonNull
	public URI getUri() {
		return uri;
	}

	@Nullable
	public URI getUriBaseId() {
		return uriBaseId;
	}
}
