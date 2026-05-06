package io.setaicompanion.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.setaicompanion.marshaller.ConfigMarshaller;
import io.setaicompanion.model.EventSourceConfig;

import java.util.List;

public class JsonConfigMarshaller implements ConfigMarshaller {

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public String name() {
        return "json";
    }

    @Override
    public byte[] marshalConfig(List<EventSourceConfig> entries) throws Exception {
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsBytes(entries);
    }

    @Override
    public List<EventSourceConfig> unmarshalConfig(byte[] bytes) throws Exception {
        return MAPPER.readValue(bytes, new TypeReference<>() {});
    }
}
