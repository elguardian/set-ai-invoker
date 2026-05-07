package io.setaicompanion.model;

import java.util.List;

/**
 * Root structure of the configuration file.
 * Holds the global agent configuration and the list of monitored event sources.
 */
public record ConfigData(AgentConfig agent, List<EventSourceConfig> sources) {

    public ConfigData {
        sources = sources == null ? List.of() : List.copyOf(sources);
    }

    public static ConfigData empty() {
        return new ConfigData(null, List.of());
    }
}
