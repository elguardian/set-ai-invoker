package io.setaicompanion.agent;

import io.setaicompanion.model.ApplicationEvent;

import java.util.function.Consumer;

/**
 * Unattended AI agent that analyses an application event and streams the response
 * line by line. Implementations delegate to an external CLI tool via ProcessBuilder.
 */
public interface AgentService {

    /** Unique name identifying this agent, e.g. {@code "claude"} or {@code "ibm-bob"}. */
    String getName();

    /**
     * Processes an event, streaming each output line to {@code outputLine} as it arrives.
     * Returns the full accumulated analysis once the agent process completes.
     */
    AgentResponse process(ApplicationEvent event, Consumer<String> outputLine);
}
