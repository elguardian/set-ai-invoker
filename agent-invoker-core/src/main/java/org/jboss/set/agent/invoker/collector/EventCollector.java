/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.set.agent.invoker.collector;

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
