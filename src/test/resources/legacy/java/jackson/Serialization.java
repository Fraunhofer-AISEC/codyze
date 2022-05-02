import com.fasterxml.jackson.databind.ObjectMapper;

public class Serialization {

    void doSerialize() {
        // It seems, that var is not yet properly resolved

        // var mapper = new ObjectMapper();
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
    }

}