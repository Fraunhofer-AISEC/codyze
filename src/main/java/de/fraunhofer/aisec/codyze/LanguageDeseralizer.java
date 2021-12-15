
package de.fraunhofer.aisec.codyze;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.EnumSet;

/**
 * Custom deserializer for languages to turn them directly into enums
 */
public class LanguageDeseralizer extends StdDeserializer<EnumSet<Language>> {
	private static final Logger log = LoggerFactory.getLogger(LanguageDeseralizer.class);

	public LanguageDeseralizer() {
		super((JavaType) null);
	}

	protected LanguageDeseralizer(Class<?> vc) {
		super(vc);
	}

	protected LanguageDeseralizer(JavaType valueType) {
		super(valueType);
	}

	protected LanguageDeseralizer(StdDeserializer<?> src) {
		super(src);
	}

	@Override
	public EnumSet<Language> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {

		EnumSet<Language> result = EnumSet.noneOf(Language.class);
		JsonToken current = jp.getCurrentToken();
		if (current.equals(JsonToken.START_ARRAY)) {
			current = jp.nextToken();
			while (!current.equals(JsonToken.END_ARRAY)) {
				if (current.equals(JsonToken.VALUE_STRING)) {
					String s = jp.getValueAsString();
					try {
						result.add(Language.valueOf(s.toUpperCase()));
					}
					catch (IllegalArgumentException e) {
						log.warn("Could not parse configuration file correctly because " +
								"{} is not a supported programming language.",
							s);
						log.warn("Continue with parsing rest of configuration file.");
					}
				}
				current = jp.nextToken();
			}
		}

		return result;
	}
}
