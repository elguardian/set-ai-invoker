package io.setaicompanion.model;

import java.time.Instant;

public record PullRequestEvent(
    String eventId,
    Instant timestamp,
    String owner,
    String repo,
    int prNumber,
    String title,
    String author,
    String url,
    String description
) implements ApplicationEvent {}
