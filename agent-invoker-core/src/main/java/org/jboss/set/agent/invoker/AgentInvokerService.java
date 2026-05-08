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

package org.jboss.set.agent.invoker;

import org.jboss.set.agent.invoker.agent.AgentService;
import org.jboss.set.agent.invoker.collector.EventCollector;
import org.jboss.set.agent.invoker.marshaller.MarshallerProvider;
import org.jboss.set.agent.invoker.store.StoreProvider;

import java.util.List;
import java.util.ServiceLoader;

public class AgentInvokerService {

    private final List<StoreProvider>      storeProviders;
    private final List<MarshallerProvider> marshallerProviders;
    private final List<EventCollector>     collectors;
    private final List<AgentService>       agents;

    private AgentInvokerService() {
        this.storeProviders      = ServiceLoader.load(StoreProvider.class).stream()
            .map(ServiceLoader.Provider::get).toList();
        this.marshallerProviders = ServiceLoader.load(MarshallerProvider.class).stream()
            .map(ServiceLoader.Provider::get).toList();
        this.collectors          = ServiceLoader.load(EventCollector.class).stream()
            .map(ServiceLoader.Provider::get).toList();
        this.agents              = ServiceLoader.load(AgentService.class).stream()
            .map(ServiceLoader.Provider::get).toList();
    }

    public static AgentInvokerService load() {
        return new AgentInvokerService();
    }

    public List<StoreProvider>      storeProviders()      { return storeProviders; }
    public List<MarshallerProvider> marshallerProviders() { return marshallerProviders; }
    public List<EventCollector>     collectors()          { return collectors; }
    public List<AgentService>       agents()              { return agents; }
}
