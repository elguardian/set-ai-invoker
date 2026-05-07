package io.setaicompanion.collector;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * An ordered collection of {@link Filter} predicates attached to an event source.
 * <p>
 * Values are extracted via typed builder methods:
 * <pre>
 *   // first = match, returned as String
 *   String p = filters.newStringValue("project").build().get();
 *
 *   // all = matches, returned as List&lt;String&gt;
 *   List&lt;String&gt; ps = filters.newStringValue("project").multiple().build().get();
 *
 *   // typed variants
 *   Integer count = filters.newIntegerValue("count").build().get();
 *   Long    ts    = filters.newLongValue("since").build().get();
 *   LocalDate d   = filters.newDateValue("after").build().get();
 * </pre>
 * Supported types for {@code multiple()}: String, Integer, Long, LocalDate.
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

    // ── Typed builder factory methods ─────────────────────────────────────────

    public ValueBuilder<String> newStringValue(String key) {
        return new ValueBuilder<>(key, s -> s);
    }

    public ValueBuilder<Integer> newIntegerValue(String key) {
        return new ValueBuilder<>(key, Integer::parseInt);
    }

    public ValueBuilder<Long> newLongValue(String key) {
        return new ValueBuilder<>(key, Long::parseLong);
    }

    /** Parses ISO-8601 date strings ({@code yyyy-MM-dd}). */
    public ValueBuilder<LocalDate> newDateValue(String key) {
        return new ValueBuilder<>(key, LocalDate::parse);
    }

    // ── Generic builder ───────────────────────────────────────────────────────

    public final class ValueBuilder<T> {

        private final String           key;
        private final Function<String, T> converter;

        ValueBuilder(String key, Function<String, T> converter) {
            this.key       = key;
            this.converter = converter;
        }

        /** Returns the converted value of the first filter matching this key (any operator). */
        public FilterValue<T> build() {
            return filters.stream()
                .filter(f -> f.key().equals(key))
                .findFirst()
                .map(f -> new FilterValue<>(converter.apply(f.value()), f.operator()))
                .orElse(new FilterValue<>(null, "="));
        }

        /** Collects all {@code =} filter values for this key as a typed list. */
        public MultipleValueBuilder<T> multiple() {
            return new MultipleValueBuilder<>(key, converter);
        }
    }

    public final class MultipleValueBuilder<T> {

        private final String              key;
        private final Function<String, T> converter;

        MultipleValueBuilder(String key, Function<String, T> converter) {
            this.key       = key;
            this.converter = converter;
        }

        /** Returns all {@code =} filter values for this key as a typed list. */
        public FilterValue<List<T>> build() {
            List<T> values = filters.stream()
                .filter(f -> f.key().equals(key) && "=".equals(f.operator()))
                .map(f -> converter.apply(f.value()))
                .toList();
            return new FilterValue<>(values, "in");
        }
    }

    @Override
    public String toString() {
        return filters.toString();
    }
}
