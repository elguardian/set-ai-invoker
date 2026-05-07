package io.setaicompanion.collector;

/**
 * A single filter predicate with a key, comparison operator, and string value.
 * Operators: {@code =}, {@code <}, {@code >}, {@code <=}, {@code >=}.
 */
public record Filter(String key, String operator, String value) {}
