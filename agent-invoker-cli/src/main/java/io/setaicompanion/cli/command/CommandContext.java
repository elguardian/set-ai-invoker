package io.setaicompanion.cli.command;

import io.setaicompanion.AgentInvokerService;
import io.setaicompanion.agent.AgentService;
import io.setaicompanion.collector.EventCollector;
import io.setaicompanion.store.ConfigStore;
import io.setaicompanion.marshaller.MarshallerProvider;
import io.setaicompanion.store.StateStore;
import io.setaicompanion.store.StoreProvider;
import io.setaicompanion.cli.TerminalOutput;
import org.jline.terminal.Terminal;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

public class CommandContext {

    // ── Immutable providers ────────────────────────────────────────────────────

    public final List<StoreProvider>      storeProviders;
    public final List<MarshallerProvider> marshallerProviders;
    public final List<EventCollector>     collectors;
    public final List<AgentService>       agents;
    public final TerminalOutput           out;
    public final Terminal                 terminal;

    // ── Mutable session state ──────────────────────────────────────────────────

    public URI    configUri;
    public URI    stateUri;
    public String storeImpl;
    public String marshallerImpl;
    public String agentName;

    public CommandContext(AgentInvokerService service,
                          TerminalOutput out,
                          Terminal terminal,
                          URI configUri,
                          URI stateUri,
                          String storeImpl,
                          String marshallerImpl,
                          String agentName) {
        this.storeProviders      = service.storeProviders();
        this.marshallerProviders = service.marshallerProviders();
        this.collectors          = service.collectors();
        this.agents              = service.agents();
        this.out                 = out;
        this.terminal            = terminal;
        this.configUri           = configUri;
        this.stateUri            = stateUri;
        this.storeImpl           = storeImpl;
        this.marshallerImpl      = marshallerImpl;
        this.agentName           = agentName;
    }

    // ── Resolution helpers ─────────────────────────────────────────────────────

    public MarshallerProvider resolveMarshaller() {
        if (marshallerProviders.isEmpty()) {
            out.error("No MarshallerProvider implementation found on classpath.");
            return null;
        }
        if (marshallerImpl == null) {
            if (marshallerProviders.size() > 1) {
                String names = marshallerProviders.stream().map(MarshallerProvider::name)
                    .collect(Collectors.joining(", "));
                out.error("Multiple MarshallerProvider implementations available: " + names
                    + "\n  Specify one with --marshaller <name>");
                return null;
            }
            return marshallerProviders.get(0);
        }
        MarshallerProvider found = marshallerProviders.stream()
            .filter(m -> m.name().equals(marshallerImpl)).findFirst().orElse(null);
        if (found == null) {
            out.error("No MarshallerProvider named '" + marshallerImpl + "'. Available: "
                + marshallerProviders.stream().map(MarshallerProvider::name).toList());
        }
        return found;
    }

    public StoreProvider resolveStoreProvider() {
        if (storeProviders.isEmpty()) {
            out.error("No StoreProvider implementation found on classpath.");
            return null;
        }
        if (storeImpl == null) {
            if (storeProviders.size() > 1) {
                String names = storeProviders.stream().map(StoreProvider::name)
                    .collect(Collectors.joining(", "));
                out.error("Multiple StoreProvider implementations available: " + names
                    + "\n  Specify one with --store-impl <name>");
                return null;
            }
            return storeProviders.get(0);
        }
        StoreProvider found = storeProviders.stream()
            .filter(p -> p.name().equals(storeImpl)).findFirst().orElse(null);
        if (found == null) {
            out.error("No StoreProvider named '" + storeImpl + "'. Available: "
                + storeProviders.stream().map(StoreProvider::name).toList());
        }
        return found;
    }

    public StateStore loadState() throws Exception {
        MarshallerProvider mp = resolveMarshaller();
        StoreProvider      sp = resolveStoreProvider();
        if (mp == null || sp == null) return null;
        StateStore store = sp.getStateStore();
        store.init(mp.getStateMarshaller());
        store.load(stateUri);
        return store;
    }

    public ConfigStore loadConfig() throws Exception {
        MarshallerProvider mp = resolveMarshaller();
        StoreProvider      sp = resolveStoreProvider();
        if (mp == null || sp == null) return null;
        ConfigStore store = sp.getConfigStore();
        store.init(mp.getConfigMarshaller());
        store.load(configUri);
        return store;
    }

    public EventCollector findCollector(String type) {
        return collectors.stream()
            .filter(c -> c.getType().equals(type))
            .findFirst()
            .orElseGet(() -> {
                out.error("No EventCollector found for type '" + type + "'.");
                return null;
            });
    }

    public AgentService findAgent() {
        return agents.stream()
            .filter(a -> a.getName().equalsIgnoreCase(agentName))
            .findFirst()
            .orElseGet(() -> {
                out.error("No AgentService found with name '" + agentName + "'. Available: "
                    + agents.stream().map(AgentService::getName).toList());
                return null;
            });
    }

    public void saveConfig(ConfigStore cfg) {
        try { cfg.save(); } catch (Exception e) {
            out.error("Could not save config: " + e.getMessage());
        }
    }

    public void saveState(StateStore state) {
        try { state.save(); } catch (Exception e) {
            out.error("Could not save state: " + e.getMessage());
        }
    }
}
