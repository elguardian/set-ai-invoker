package io.setaicompanion.model;

import java.time.Instant;

public sealed interface ApplicationEvent permits PullRequestEvent, JiraIssueEvent {
    String eventId();
    Instant timestamp();
}
