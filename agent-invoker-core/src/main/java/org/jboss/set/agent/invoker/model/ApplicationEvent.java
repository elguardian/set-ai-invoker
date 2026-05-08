package org.jboss.set.agent.invoker.model;

import java.time.Instant;

public sealed interface ApplicationEvent permits PullRequestEvent, JiraIssueEvent {
    String eventId();
    Instant timestamp();
}
