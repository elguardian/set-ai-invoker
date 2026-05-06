package io.setaicompanion.model;

import java.util.Map;

/**
 * One entry in the configuration file describing a monitored event source.
 * <p>
 * {@code event-user}, {@code event-api-token}, and {@code event-password} accept
 * either a literal value or an environment-variable reference in the form {@code ${VAR_NAME}}.
 * <p>
 * {@code event-filter} is an opaque JSON object whose structure is defined by each
 * collector implementation. The CLI populates it via {@code config filter <type> <url>}.
 */
public record EventSourceConfig(
    String eventType,
    String eventUrl,
    String eventUser,
    String eventApiToken,
    String eventPassword,
    Map<String, Object> eventFilter
) {
    public EventSourceConfig {
        eventFilter = eventFilter == null ? Map.of() : Map.copyOf(eventFilter);
    }

    /** Returns the resolved value of {@code event-api-token}, expanding {@code ${VAR}} references. */
    public String resolvedApiToken() { return resolve(eventApiToken); }

    /** Returns the resolved value of {@code event-user}. */
    public String resolvedUser() { return resolve(eventUser); }

    /** Returns the resolved value of {@code event-password}. */
    public String resolvedPassword() { return resolve(eventPassword); }

    private static String resolve(String value) {
        if (value == null) return null;
        if (value.startsWith("${") && value.endsWith("}")) {
            return System.getenv(value.substring(2, value.length() - 1));
        }
        return value;
    }
}
