package org.jboss.set.agent.invoker.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.jboss.set.agent.invoker.marshaller.StateMarshaller;
import org.jboss.set.agent.invoker.model.StateEntry;

import java.util.List;

public class JsonStateMarshaller implements StateMarshaller {

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public String name() {
        return "json";
    }

    @Override
    public byte[] marshalState(List<StateEntry> entries) throws Exception {
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsBytes(entries);
    }

    @Override
    public List<StateEntry> unmarshalState(byte[] bytes) throws Exception {
        return MAPPER.readValue(bytes, new TypeReference<>() {});
    }
}
