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
