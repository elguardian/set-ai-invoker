package org.jboss.set.agent.invoker.collector;

/**
 * Resolved credentials and parameters for a single collector invocation.
 *
 * @param url      base URL of the source (GitHub repo API URL or Jira base URL)
 * @param user     optional username for basic-auth sources
 * @param apiToken bearer / personal-access token
 * @param password optional password for basic-auth sources
 * @param filter   collector-specific filter predicates (see each collector's
 *                 {@link EventCollector#filterHelp()} for supported keys)
 */
public record CollectorConfig(
    String  url,
    String  user,
    String  apiToken,
    String  password,
    Filters filter
) {
    public CollectorConfig {
        filter = filter == null ? Filters.empty() : filter;
    }
}
