
package de.fraunhofer.aisec.codyze.analysis;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Human-readable description of a finding (i.e. of an analysis result).
 */
public class FindingDescription {
	private static final Logger log = LoggerFactory.getLogger(FindingDescription.class);

	private static FindingDescription instance = null;
	private DocumentContext json;

	private FindingDescription() {
		/* do not instantiate */
	}

	/**
	 * Singleton.
	 *
	 * Currently, there can be only one set of descriptions. This may change in the future when multiple MARK sources are supported.
	 *
	 * @return
	 */
	public static FindingDescription getInstance() {
		if (instance == null) {
			instance = new FindingDescription();
		}
		return instance;
	}

	@Nullable
	public String getDescriptionFull(@NonNull final String onFailId) {
		try {
			return json.read("$['" + onFailId + "']['fullDescription']['text']");
		}
		catch (PathNotFoundException | NullPointerException e) {
			return null;
		}
	}

	@Nullable
	public String getDescriptionShort(@NonNull final String onFailId) {
		try {
			return json.read("$['" + onFailId + "']['shortDescription']['text']");
		}
		catch (PathNotFoundException | NullPointerException e) {
			return null;
		}
	}

	@Nullable
	public String getHelpUri(@NonNull final String onFailId) {
		try {
			return json.read("$['" + onFailId + "']['helpUri']");
		}
		catch (PathNotFoundException | NullPointerException e) {
			return null;
		}
	}

	@Nullable
	public List<String> getFixes(@NonNull final String onFailId) {
		try {
			return json.read("$['" + onFailId + "']['fixes'][*]['description']['text']");
		}
		catch (PathNotFoundException | NullPointerException e) {
			return null;
		}
	}

	public void init(@NonNull File descriptionFile) {
		log.info("Parsing MARK description file from {}", descriptionFile.getAbsolutePath());
		try {
			Configuration conf = Configuration.defaultConfiguration();
			conf.addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);
			conf.addOptions(Option.SUPPRESS_EXCEPTIONS); // Return null instead of exception

			json = JsonPath.using(conf).parse(descriptionFile);

			log.info("Loaded {} descriptions", ((Map) json.read("$")).size());
		}
		catch (IOException e) {
			log.warn("Failed to load findingDescription.json, explanations will be empty", e);
		}
	}
}
