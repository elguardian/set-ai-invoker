package org.jboss.set.agent.invoker.model;

import java.time.Instant;

public record PullRequestEvent(
    String  eventId,
    Instant timestamp,
    String  eventType,
    String  owner,
    String  repo,
    int     prNumber,
    String  url,
    String  action
) implements ApplicationEvent {}
