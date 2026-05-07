package io.setaicompanion.model;

import java.time.Instant;

public record JiraIssueEvent(
    String  eventId,
    Instant timestamp,
    String  issueKey,
    String  summary,
    String  status,
    String  assignee,
    String  priority,
    String  reporter
) implements ApplicationEvent {}
