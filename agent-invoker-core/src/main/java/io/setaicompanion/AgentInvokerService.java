package io.setaicompanion;

import io.setaicompanion.agent.AgentService;
import io.setaicompanion.collector.EventCollector;
import io.setaicompanion.marshaller.MarshallerProvider;
import io.setaicompanion.store.StoreProvider;

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
