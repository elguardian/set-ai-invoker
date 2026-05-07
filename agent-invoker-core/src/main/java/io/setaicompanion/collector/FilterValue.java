package io.setaicompanion.collector;

/** The typed result produced by a {@link Filters} builder. */
public final class FilterValue<T> {

    private final T value;

    FilterValue(T value) {
        this.value = value;
    }

    /** Returns the resolved value, or {@code null} / empty list when no matching filter exists. */
    public T get() {
        return value;
    }

    public boolean isPresent() {
        return value != null;
    }
}
