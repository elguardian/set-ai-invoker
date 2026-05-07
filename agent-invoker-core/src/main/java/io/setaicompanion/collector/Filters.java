package io.setaicompanion.collector;

import java.util.Collections;
import java.util.List;

/**
 * An ordered collection of {@link Filter} predicates attached to an event source.
 * <p>
 * Values are extracted via the builder API:
 * <pre>
 *   // first match for key "project" (= operator)
 *   String p = filters.newStringValue("project").build().get();
 *
 *   // all values for key "project" where operator is =
 *   List&lt;String&gt; ps = filters.newStringValue("project").multiple().build().get();
 * </pre>
 */
public final class Filters {

    private static final Filters EMPTY = new Filters(List.of());

    private final List<Filter> filters;

    private Filters(List<Filter> filters) {
        this.filters = Collections.unmodifiableList(filters);
    }

    public static Filters empty() {
        return EMPTY;
    }

    public static Filters of(List<Filter> filters) {
        if (filters == null || filters.isEmpty()) return EMPTY;
        return new Filters(List.copyOf(filters));
    }

    public List<Filter> all() {
        return filters;
    }

    public boolean isEmpty() {
        return filters.isEmpty();
    }

    // ── Builder API ───────────────────────────────────────────────────────────

    public StringValueBuilder newStringValue(String key) {
        return new StringValueBuilder(key);
    }

    public final class StringValueBuilder {

        private final String key;

        StringValueBuilder(String key) {
            this.key = key;
        }

        /** Returns the value of the first {@code =} filter matching this key. */
        public FilterValue<String> build() {
            return filters.stream()
                .filter(f -> f.key().equals(key) && "=".equals(f.operator()))
                .findFirst()
                .map(f -> new FilterValue<>(f.value()))
                .orElse(new FilterValue<>(null));
        }

        /** Collects all {@code =} filter values for this key into a list. */
        public MultipleBuilder multiple() {
            return new MultipleBuilder(key);
        }
    }

    public final class MultipleBuilder {

        private final String key;

        MultipleBuilder(String key) {
            this.key = key;
        }

        /** Returns all {@code =} filter values for this key. Multiple requires operator {@code =}. */
        public FilterValue<List<String>> build() {
            List<String> values = filters.stream()
                .filter(f -> f.key().equals(key) && "=".equals(f.operator()))
                .map(Filter::value)
                .toList();
            return new FilterValue<>(values);
        }
    }

    @Override
    public String toString() {
        return filters.toString();
    }
}
