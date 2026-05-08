package org.jboss.set.agent.invoker.collector;

/**
 * A single filter predicate with a key, comparison operator, and string value.
 */
public record Filter(String key, FilterOperator operator, String value) {}
