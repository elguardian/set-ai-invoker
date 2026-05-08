package org.jboss.set.agent.invoker.model;

/**
 * One entry in the state file: tracks the last checkpoint for a single event source.
 * <p>
 * GitHub checkpoint: JSON string {@code {"etag":"...","seen":{"prNum":"updatedAt",...}}}
 * Jira checkpoint: epoch-milliseconds as a decimal string
 */
public record StateEntry(
    String eventType,
    String eventUrl,
    String eventCheckpoint
) {}
