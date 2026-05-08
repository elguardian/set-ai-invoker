package org.jboss.set.agent.invoker.collector;

import org.jboss.set.agent.invoker.model.ApplicationEvent;

import java.util.List;

/**
 * Result of a single collector run.
 *
 * @param nextCheckpoint opaque string to persist as the new checkpoint, or {@code null} if
 *                       the checkpoint has not changed and should not be updated
 * @param events         ordered list of new application events, oldest first
 */
public record EventsCollected(
    String nextCheckpoint,
    List<ApplicationEvent> events
) {}
