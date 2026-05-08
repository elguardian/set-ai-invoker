package org.jboss.set.agent.invoker.model;

import org.jboss.set.agent.invoker.collector.Filters;

/**
 * One entry in the configuration file describing a monitored event source.
 * <p>
 * {@code event-user}, {@code event-api-token}, and {@code event-password} accept
 * either a literal value or an environment-variable reference in the form {@code ${VAR_NAME}}.
 * <p>
 * {@code event-filter} holds collector-specific filter predicates set via
 * {@code config filter <type> <url>}.
 */
public record EventSourceConfig(
    String  eventType,
    String  eventUrl,
    String  eventUser,
    String  eventApiToken,
    String  eventPassword,
    Filters eventFilter
) {
    public EventSourceConfig {
        eventFilter = eventFilter == null ? Filters.empty() : eventFilter;
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
            String property = value.substring(2, value.length() - 1);
            return System.getenv(property);
        }
        return value;
    }
}
