package org.jboss.set.agent.invoker.agent;

import java.time.Instant;

public record AgentResponse(
    String agentName,
    String eventId,
    String analysis,
    Instant processedAt
) {}
