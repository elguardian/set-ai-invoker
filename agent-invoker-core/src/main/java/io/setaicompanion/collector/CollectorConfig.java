package io.setaicompanion.collector;

import java.util.Map;

/**
 * Resolved credentials and parameters for a single collector invocation.
 *
 * @param url    base URL of the source (GitHub repo URL or Jira base URL)
 * @param user   optional username for basic-auth sources
 * @param apiToken bearer / personal-access token
 * @param password optional password for basic-auth sources
 * @param filter opaque map of collector-specific filter options (see each collector's
 *               {@link EventCollector#filterHelp()} for supported keys)
 */
public record CollectorConfig(
    String url,
    String user,
    String apiToken,
    String password,
    Map<String, Object> filter
) {
    public CollectorConfig {
        filter = filter == null ? Map.of() : Map.copyOf(filter);
    }
}
