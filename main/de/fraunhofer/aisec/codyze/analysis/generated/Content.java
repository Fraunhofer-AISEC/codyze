
package de.fraunhofer.aisec.codyze.analysis.generated;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

@Generated("jsonschema2pojo")
public enum Content {

    LOCALIZED_DATA("localizedData"),
    NON_LOCALIZED_DATA("nonLocalizedData");
    private final String value;
    private final static Map<String, Content> CONSTANTS = new HashMap<String, Content>();

    static {
        for (Content c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private Content(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @JsonValue
    public String value() {
        return this.value;
    }

    @JsonCreator
    public static Content fromValue(String value) {
        Content constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
