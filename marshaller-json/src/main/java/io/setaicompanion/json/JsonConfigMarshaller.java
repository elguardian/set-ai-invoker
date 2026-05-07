package io.setaicompanion.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.setaicompanion.collector.FilterParser;
import io.setaicompanion.collector.Filters;
import io.setaicompanion.marshaller.ConfigMarshaller;
import io.setaicompanion.model.EventSourceConfig;

import java.io.IOException;
import java.util.List;

public class JsonConfigMarshaller implements ConfigMarshaller {

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(filtersModule());

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

    // ── Filters serialization ─────────────────────────────────────────────────

    private static SimpleModule filtersModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Filters.class, new FiltersSerializer());
        module.addDeserializer(Filters.class, new FiltersDeserializer());
        return module;
    }

    private static final class FiltersSerializer extends StdSerializer<Filters> {

        FiltersSerializer() { super(Filters.class); }

        @Override
        public void serialize(Filters filters, JsonGenerator gen, SerializerProvider provider)
                throws IOException {
            gen.writeStartArray();
            for (var f : filters.all()) {
                gen.writeString(f.key() + f.operator().symbol() + f.value());
            }
            gen.writeEndArray();
        }
    }

    private static final class FiltersDeserializer extends StdDeserializer<Filters> {

        FiltersDeserializer() { super(Filters.class); }

        @Override
        public Filters deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            List<String> tokens = p.readValueAs(new TypeReference<>() {});
            return FilterParser.parseTokens(tokens);
        }
    }
}
