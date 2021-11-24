
package de.fraunhofer.aisec.codyze;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class LanguageDeseralizer extends StdDeserializer<CpgConfigurationFile> {

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
	public CpgConfigurationFile deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JacksonException {

		JsonToken token = jp.getCurrentToken();
		Object o = jp.getCurrentValue();
//		ObjectCodec oc = jp.getCodec();
//		JsonNode node = oc.readTree(jp);

		return null;
	}
}
