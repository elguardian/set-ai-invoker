package io.setaicompanion.collector;

import java.util.List;
import java.util.Map;

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
     * Returns a human-readable description of the filter tokens this collector accepts
     * in {@link #parseFilter(List)}.  Shown by the CLI {@code config filter} command.
     * Default: no filter options.
     */
    default String filterHelp() {
        return "(no filter options for type '" + getType() + "')";
    }

    /**
     * Parses CLI filter tokens (e.g. {@code ["project=FOO", "project=BAR"]}) into
     * the opaque filter map stored in {@link CollectorConfig#filter()}.
     * The result is persisted in the config store under {@code event-filter}.
     * Default: returns an empty map (no filtering).
     */
    default Map<String, Object> parseFilter(List<String> tokens) {
        return Map.of();
    }
}
