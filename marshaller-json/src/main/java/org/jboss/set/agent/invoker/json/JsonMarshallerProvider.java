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

import org.jboss.set.agent.invoker.marshaller.ConfigMarshaller;
import org.jboss.set.agent.invoker.marshaller.MarshallerProvider;
import org.jboss.set.agent.invoker.marshaller.StateMarshaller;

public class JsonMarshallerProvider implements MarshallerProvider {

    private final JsonStateMarshaller  stateMarshaller  = new JsonStateMarshaller();
    private final JsonConfigMarshaller configMarshaller = new JsonConfigMarshaller();

    @Override
    public String name() {
        return "json";
    }

    @Override
    public StateMarshaller getStateMarshaller() {
        return stateMarshaller;
    }

    @Override
    public ConfigMarshaller getConfigMarshaller() {
        return configMarshaller;
    }
}
