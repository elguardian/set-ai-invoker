package io.setaicompanion.model;

import java.time.Instant;

public sealed interface ApplicationEvent permits PullRequestEvent, JiraEvent {
    String eventId();
    Instant timestamp();
}
