
package de.fraunhofer.aisec.analysis.structures;

import org.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class FindingDescription {
	private static final Logger log = LoggerFactory.getLogger(FindingDescription.class);

	private JSONObject source = null;
	private static FindingDescription instance = null;

	private FindingDescription() {
		/* do not instantiate */
	}

	public static FindingDescription getInstance() {
		if (instance == null) {
			instance = new FindingDescription();
		}
		return instance;
	}

	public String getDescriptionDetailed(String constant) {
		if (source == null) {
			return constant;
		}

		if (source.has(constant)) {
			JSONObject jsonObject = source.getJSONObject(constant);
			if (jsonObject.has("detailed")) {
				return jsonObject.getString("detailed");
			}
		}

		return constant;
	}

	public String getDescriptionBrief(String constant) {
		if (source == null) {
			return constant;
		}

		if (source.has(constant)) {
			JSONObject jsonObject = source.getJSONObject(constant);
			if (jsonObject.has("brief")) {
				return jsonObject.getString("brief");
			}
		}

		return constant;
	}

	public void init(File descriptionFile) {
		log.info("Parsing MARK description file from {}", descriptionFile.getAbsolutePath());
		try (
				InputStream inputStream = new FileInputStream(descriptionFile);
				BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));) {

			StringBuilder sb = new StringBuilder();
			String readLine;

			while ((readLine = in.readLine()) != null) {
				sb.append(readLine);
				sb.append("\n");
			}
			source = new JSONObject(sb.toString());
			log.info("Loaded {} descriptions", source.length());
		}
		catch (IOException e) {
			log.warn("Failed to load findingDescription.json, explanations will be empty", e);
		}
	}
}
