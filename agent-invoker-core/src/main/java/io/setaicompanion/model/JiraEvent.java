package io.setaicompanion.model;

import java.time.Instant;

public record JiraEvent(
    String eventId,
    Instant timestamp,
    String issueKey,
    String fieldName,
    String oldValue,
    String newValue,
    String changedBy
) implements ApplicationEvent {}
