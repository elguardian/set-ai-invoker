package io.setaicompanion.collector;

/** The typed result produced by a {@link Filters} builder. */
public final class FilterValue<T> {

    private final T               value;
    private final FilterOperator  operator;

    FilterValue(T value, FilterOperator operator) {
        this.value    = value;
        this.operator = operator;
    }

    /** Returns the resolved value, or {@code null} / empty list when no matching filter exists. */
    public T get() {
        return value;
    }

    /**
     * Returns the operator of the matched filter, or {@code null} when no filter was found.
     * Always {@link FilterOperator#IN} for results produced by {@code multiple().build()}.
     */
    public FilterOperator operator() {
        return operator;
    }

    public boolean isPresent() {
        return value != null;
    }
}
