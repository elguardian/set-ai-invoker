/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.set.agent.invoker.json;

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
import org.jboss.set.agent.invoker.collector.FilterParser;
import org.jboss.set.agent.invoker.collector.Filters;
import org.jboss.set.agent.invoker.marshaller.ConfigMarshaller;
import org.jboss.set.agent.invoker.model.ConfigData;

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
    public byte[] marshalConfig(ConfigData data) throws Exception {
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsBytes(data);
    }

    @Override
    public ConfigData unmarshalConfig(byte[] bytes) throws Exception {
        return MAPPER.readValue(bytes, ConfigData.class);
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
