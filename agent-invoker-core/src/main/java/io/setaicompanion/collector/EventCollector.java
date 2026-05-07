package io.setaicompanion.collector;

import java.util.List;

/**
 * Single-shot event collector. Fetches new events from a source since the last
 * recorded checkpoint (ETag or timestamp) and returns the next checkpoint value.
 * The caller is responsible for persisting the checkpoint after a successful run.
 */
public interface EventCollector {

    /** Unique type identifier, e.g. {@code "github"} or {@code "jira"}. */
    String getType();

    /**
     * Collects new events using the supplied configuration.
     *
     * @param config     source URL, credentials and optional filter
     * @param checkpoint last persisted checkpoint string, or {@code null} for a first run
     * @return collected events and the next checkpoint value to persist
     */
    EventsCollected collect(CollectorConfig config, String checkpoint) throws Exception;

    // ── Filter contract ───────────────────────────────────────────────────────

    /**
     * Returns the filter key names this collector accepts.
     * Used by {@link FilterParser} for validation and by the CLI for help text.
     * Default: no filters.
     */
    default List<String> getFilterKeysSupported() {
        return List.of();
    }

    /**
     * Returns a human-readable description of the filter keys this collector accepts.
     * Shown by the CLI {@code config filter} command.
     */
    default String filterHelp() {
        List<String> keys = getFilterKeysSupported();
        return keys.isEmpty()
            ? "(no filter options for type '" + getType() + "')"
            : "Supported filter keys: " + String.join(", ", keys)
                + " — usage: key=value, key>=value, etc.";
    }
}
