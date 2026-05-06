package io.setaicompanion.model;

import java.time.Instant;

public sealed interface ApplicationEvent permits PullRequestEvent, JiraFieldChangeEvent {
    String eventId();
    Instant timestamp();
}
